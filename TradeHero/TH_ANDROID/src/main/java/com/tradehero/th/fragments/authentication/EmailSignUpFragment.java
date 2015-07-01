package com.tradehero.th.fragments.authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import com.tradehero.common.fragment.ActivityResultDTO;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.activities.ActivityHelper;
import com.tradehero.th.activities.AuthenticationActivity;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.auth.AuthData;
import com.tradehero.th.auth.AuthDataUtil;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.settings.ProfileInfoView;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.TimberOnErrorAction;
import com.tradehero.th.rx.ToastAndLogOnErrorAction;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.rx.view.DismissDialogAction0;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.appsflyer.AppsFlyerConstants;
import com.tradehero.th.utils.metrics.appsflyer.THAppsFlyer;
import com.tradehero.th.utils.metrics.events.MethodEvent;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import dagger.Lazy;
import javax.inject.Inject;
import rx.Observable;
import rx.Observer;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import timber.log.Timber;

public class EmailSignUpFragment extends Fragment
{
    private static final String BUNDLE_KEY_DEEP_LINK = EmailSignUpFragment.class.getName() + ".deepLink";

    @Inject Analytics analytics;
    @Inject Lazy<DashboardNavigator> navigator;
    @Inject UserServiceWrapper userServiceWrapper;

    @Bind(R.id.profile_info) ProfileInfoView profileView;
    @Bind(R.id.authentication_sign_up_email) EditText emailEditText;
    @Bind(R.id.authentication_sign_up_button) View signUpButton;
    @Bind(R.id.social_network_button_list) SocialNetworkButtonListLinear socialNetworkButtonList;

    private SubscriptionList onStopSubscriptions;
    @Nullable private ActivityResultDTO receivedActivityResult;
    @Nullable Observer<SocialNetworkEnum> socialNetworkEnumObserver;
    @Nullable Uri deepLink;

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.authentication_back_button) void handleBackButtonClicked()
    {
        navigator.get().popFragment();
    }

    public static void putDeepLink(@NonNull Bundle args, @NonNull Uri deepLink)
    {
        args.putString(BUNDLE_KEY_DEEP_LINK, deepLink.toString());
    }

    @Nullable private static Uri getDeepLink(@NonNull Bundle args)
    {
        String link = args.getString(BUNDLE_KEY_DEEP_LINK);
        return link == null ? null : Uri.parse(link);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        HierarchyInjector.inject(this);
        analytics.tagScreen(AnalyticsConstants.Register_Form);
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.RegisterFormScreen));
        analytics.addEvent(new MethodEvent(AnalyticsConstants.SignUp_Tap, AnalyticsConstants.Email));
        deepLink = getDeepLink(getArguments());
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.socialNetworkEnumObserver = ((AuthenticationActivity) activity).getSelectedSocialNetworkObserver();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.authentication_email_sign_up, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        signUpButton.setEnabled(false);

        DeviceUtil.showKeyboardDelayed(emailEditText);

        ActivityResultDTO copy = receivedActivityResult;
        if (copy != null)
        {
            profileView.onActivityResult(
                    copy.activity,
                    copy.requestCode,
                    copy.resultCode,
                    copy.data);
            receivedActivityResult = null;
        }

        try
        {
            view.setBackgroundResource(R.drawable.login_bg_4);
        } catch (Throwable e)
        {
            Timber.e(e, "Failed to set guide background");
            view.setBackgroundColor(getResources().getColor(R.color.authentication_guide_bg_color));
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (profileView != null)
        {
            profileView.onActivityResult(getActivity(), requestCode, resultCode, data);
        }
        else
        {
            receivedActivityResult = new ActivityResultDTO(getActivity(), requestCode, resultCode, data);
        }
    }

    @Override public void onStart()
    {
        super.onStart();
        onStopSubscriptions = new SubscriptionList();
        onStopSubscriptions.add(AppObservable.bindFragment(this, profileView.getFieldsValidObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Boolean>()
                        {
                            @Override public void call(Boolean areFieldsValid)
                            {
                                signUpButton.setEnabled(areFieldsValid);
                            }
                        },
                        new TimberOnErrorAction("Failed to listen to valid fields")));
        onStopSubscriptions.add(AppObservable.bindFragment(this, getSignUpObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new EmptyAction1<Pair<AuthData, UserProfileDTO>>(),
                        new TimberOnErrorAction("Failed to listen to sign-up observable")));
        onStopSubscriptions.add(socialNetworkButtonList.getSocialNetworkEnumObservable()
                .subscribe(
                        new Action1<SocialNetworkEnum>()
                        {
                            @Override public void call(SocialNetworkEnum socialNetworkEnum)
                            {
                                if (socialNetworkEnumObserver != null)
                                {
                                    socialNetworkEnumObserver.onNext(socialNetworkEnum);
                                }
                            }
                        },
                        new ToastAndLogOnErrorAction("Failed to listent to social network button")));
    }

    @Override public void onStop()
    {
        onStopSubscriptions.unsubscribe();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override public void onDetach()
    {
        this.socialNetworkEnumObserver = null;
        super.onDetach();
    }

    protected Observable<Pair<AuthData, UserProfileDTO>> getSignUpObservable()
    {
        return ViewObservable.clicks(signUpButton, false)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<OnClickEvent, Observable<? extends UserFormDTO>>()
                {
                    @Override public Observable<? extends UserFormDTO> call(OnClickEvent view1)
                    {
                        return profileView.obtainUserFormDTO();
                    }
                })
                .flatMap(new Func1<UserFormDTO, Observable<? extends Pair<AuthData, UserProfileDTO>>>()
                {
                    @Override public Observable<? extends Pair<AuthData, UserProfileDTO>> call(UserFormDTO userFormDTO)
                    {
                        ProgressDialog progressDialog = ProgressDialog.show(EmailSignUpFragment.this.getActivity(),
                                EmailSignUpFragment.this.getString(R.string.alert_dialog_please_wait),
                                EmailSignUpFragment.this.getString(R.string.authentication_connecting_tradehero_only), true);

                        final AuthData authData = new AuthData(userFormDTO.email, userFormDTO.password);
                        return userServiceWrapper.signUpWithEmailRx(authData, userFormDTO)
                                .map(new Func1<UserProfileDTO, Pair<AuthData, UserProfileDTO>>()
                                {
                                    @Override public Pair<AuthData, UserProfileDTO> call(UserProfileDTO userProfileDTO)
                                    {
                                        return Pair.create(authData, userProfileDTO);
                                    }
                                })
                                .doOnUnsubscribe(new DismissDialogAction0(progressDialog));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Pair<AuthData, UserProfileDTO>>()
                {
                    @Override public void call(Pair<AuthData, UserProfileDTO> pair)
                    {
                        THAppsFlyer.sendTrackingWithEvent(getActivity(), AppsFlyerConstants.REGISTRATION_EMAIL);
                        AuthDataUtil.saveAccountAndResult(getActivity(), pair.first, pair.second.email);
                        ActivityHelper.launchDashboard(
                                getActivity(),
                                deepLink);
                    }
                })
                .doOnError(new ToastOnErrorAction())
                .retry();
    }
}




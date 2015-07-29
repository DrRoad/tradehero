package com.tradehero.th.fragments.authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.BuildConfig;
import com.tradehero.th.R;
import com.tradehero.th.activities.ActivityHelper;
import com.tradehero.th.activities.AuthenticationActivity;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.users.LoginSignUpFormDTO;
import com.tradehero.th.api.users.UserLoginDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.password.ForgotPasswordDTO;
import com.tradehero.th.api.users.password.ForgotPasswordFormDTO;
import com.tradehero.th.auth.AuthData;
import com.tradehero.th.auth.AuthDataUtil;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.SessionServiceWrapper;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.TimberOnErrorAction1;
import com.tradehero.th.rx.TimberAndToastOnErrorAction1;
import com.tradehero.th.rx.ToastOnErrorAction1;
import com.tradehero.th.rx.dialog.OnDialogClickEvent;
import com.tradehero.th.rx.view.DismissDialogAction0;
import com.tradehero.th.rx.view.DismissDialogAction1;
import com.tradehero.th.utils.AlertDialogRxUtil;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.appsflyer.AppsFlyerConstants;
import com.tradehero.th.utils.metrics.appsflyer.THAppsFlyer;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.widget.validation.TextValidator;
import com.tradehero.th.widget.validation.ValidatedText;
import com.tradehero.th.widget.validation.ValidatedView;
import com.tradehero.th.widget.validation.ValidationMessage;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;
import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EmailSignInFragment extends Fragment
{
    private static final String BUNDLE_KEY_DEEP_LINK = EmailSignInFragment.class.getName() + ".deepLink";

    @Inject UserServiceWrapper userServiceWrapper;
    @Inject Analytics analytics;
    @Inject Lazy<DashboardNavigator> navigator;
    @Inject Provider<LoginSignUpFormDTO.Builder2> loginSignUpFormDTOProvider;
    @Inject SessionServiceWrapper sessionServiceWrapper;

    @Bind(R.id.authentication_sign_in_email) ValidatedText email;
    TextValidator emailValidator;
    @Bind(R.id.et_pwd_login) ValidatedText password;
    TextValidator passwordValidator;
    @Bind(R.id.btn_login) View loginButton;
    @Bind(R.id.social_network_button_list) SocialNetworkButtonListLinear socialNetworkButtonList;
    SubscriptionList onStopSubscriptions;

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
        analytics.tagScreen(AnalyticsConstants.Login_Form);
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.LoginFormScreen));
        deepLink = getDeepLink(getArguments());
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        socialNetworkEnumObserver = ((AuthenticationActivity) activity).getSelectedSocialNetworkObserver();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.authentication_email_sign_in, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EmailSignInUtils.populateDefaults(email, password);
        loginButton.setEnabled(BuildConfig.DEBUG);

        emailValidator = email.getValidator();
        email.addTextChangedListener(emailValidator);
        passwordValidator = password.getValidator();
        password.addTextChangedListener(passwordValidator);

        DeviceUtil.showKeyboardDelayed(email);

        try
        {
            view.setBackgroundResource(R.drawable.login_bg_4);
        } catch (Throwable e)
        {
            Timber.e(e, "Failed to set guide background");
            view.setBackgroundColor(getResources().getColor(R.color.authentication_guide_bg_color));
        }
    }

    @NonNull protected Observable<Pair<AuthData, UserProfileDTO>> handleClick(@NonNull OnClickEvent event)
    {
        DeviceUtil.dismissKeyboard(event.view());
        AuthData authData = new AuthData(email.getText().toString(), password.getText().toString());
        LoginSignUpFormDTO signUpFormDTO = loginSignUpFormDTOProvider.get()
                .authData(authData)
                .build();
        return signInProper(signUpFormDTO);
    }

    @NonNull protected Observable<Pair<AuthData, UserProfileDTO>> signInProper(LoginSignUpFormDTO loginSignUpFormDTO)
    {
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getString(R.string.alert_dialog_please_wait),
                getString(R.string.authentication_connecting_tradehero_only), true);
        final AuthData authData = loginSignUpFormDTO.authData;
        Observable<UserProfileDTO> userLoginDTOObservable = sessionServiceWrapper.signupAndLoginRx(
                authData.getTHToken(), loginSignUpFormDTO)
                .map(new Func1<UserLoginDTO, UserProfileDTO>()
                {
                    @Override public UserProfileDTO call(UserLoginDTO userLoginDTO)
                    {
                        return userLoginDTO.profileDTO;
                    }
                });

        return Observable.zip(Observable.just(authData), userLoginDTOObservable,
                new Func2<AuthData, UserProfileDTO, Pair<AuthData, UserProfileDTO>>()
                {
                    @Override public Pair<AuthData, UserProfileDTO> call(AuthData t1, UserProfileDTO t2)
                    {
                        return Pair.create(t1, t2);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<Pair<AuthData, UserProfileDTO>>()
                {
                    @Override public void call(Pair<AuthData, UserProfileDTO> pair)
                    {
                        THAppsFlyer.sendTrackingWithEvent(getActivity(), AppsFlyerConstants.REGISTRATION_EMAIL);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Pair<AuthData, UserProfileDTO>>()
                {
                    @Override public void call(Pair<AuthData, UserProfileDTO> pair)
                    {
                        AuthDataUtil.saveAccountAndResult(getActivity(), pair.first, pair.second.email);
                        ActivityHelper.launchDashboard(
                                getActivity(),
                                deepLink);
                    }
                })
                .doOnError(new ToastOnErrorAction1())
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog));
    }

    @Override public void onStart()
    {
        super.onStart();
        onStopSubscriptions = new SubscriptionList();
        onStopSubscriptions.add(AppObservable.bindSupportFragment(this, emailValidator.getValidationMessageObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createValidatorObserver(email)));
        onStopSubscriptions.add(AppObservable.bindSupportFragment(this, passwordValidator.getValidationMessageObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createValidatorObserver(password)));
        onStopSubscriptions.add(AppObservable.bindSupportFragment(this, getFieldsValidationObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Boolean>()
                        {
                            @Override public void call(Boolean areFieldsValid)
                            {
                                loginButton.setEnabled(areFieldsValid);
                            }
                        },
                        new TimberOnErrorAction1("Error in validation")));
        onStopSubscriptions.add(AppObservable.bindSupportFragment(
                this,
                ViewObservable.clicks(loginButton, false)
                        .flatMap(new Func1<OnClickEvent, Observable<? extends Pair<AuthData, UserProfileDTO>>>()
                        {
                            @Override public Observable<? extends Pair<AuthData, UserProfileDTO>> call(OnClickEvent event)
                            {
                                return EmailSignInFragment.this.handleClick(event);
                            }
                        })
                        .retry())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new EmptyAction1<Pair<AuthData, UserProfileDTO>>(),
                        new EmptyAction1<Throwable>()));
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
                        new TimberAndToastOnErrorAction1("Failed to listent to social network button")));
    }

    @Override public void onStop()
    {
        onStopSubscriptions.unsubscribe();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        email.removeTextChangedListener(emailValidator);
        password.removeTextChangedListener(passwordValidator);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override public void onDetach()
    {
        socialNetworkEnumObserver = null;
        super.onDetach();
    }

    @NonNull protected Observer<ValidationMessage> createValidatorObserver(@NonNull final ValidatedText validatedText)
    {
        return new Observer<ValidationMessage>()
        {
            @Override public void onNext(ValidationMessage validationMessage)
            {
                validatedText.setStatus(validationMessage.getValidStatus());
                String message = validationMessage.getMessage();
                if (message != null && !TextUtils.isEmpty(message))
                {
                    THToast.show(validationMessage.getMessage());
                }
            }

            @Override public void onCompleted()
            {
            }

            @Override public void onError(Throwable e)
            {
                Timber.e(e, "Failed to listen to validation message");
            }
        };
    }

    @NonNull protected Observable<Boolean> getFieldsValidationObservable()
    {
        return Observable.combineLatest(
                emailValidator.getValidationMessageObservable().doOnNext(new Action1<ValidationMessage>()
                {
                    @Override public void call(ValidationMessage validationMessage)
                    {
                        Timber.d("");
                    }
                }),
                passwordValidator.getValidationMessageObservable().doOnNext(new Action1<ValidationMessage>()
                {
                    @Override public void call(ValidationMessage validationMessage)
                    {
                        Timber.d("");
                    }
                }),
                new Func2<ValidationMessage, ValidationMessage, Boolean>()
                {
                    @Override public Boolean call(ValidationMessage validationMessage, ValidationMessage validationMessage2)
                    {
                        return validationMessage.getValidStatus().equals(ValidatedView.Status.VALID)
                                && validationMessage2.getValidStatus().equals(ValidatedView.Status.VALID);
                    }
                }
        );
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.authentication_sign_in_forgot_password) void showForgotPasswordUI()
    {
        final View forgotDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.forgot_password_dialog, null);
        final ValidatedText validatedEmail = ((ValidatedText) forgotDialogView.findViewById(R.id.authentication_forgot_password_validated_email));
        validatedEmail.setText(email.getText().toString());
        onStopSubscriptions.add(AppObservable.bindSupportFragment(
                this,
                AlertDialogRxUtil.buildDefault(getActivity())
                        .setTitle(R.string.authentication_ask_for_email)
                        .setPositiveButton(R.string.ok)
                        .setNegativeButton(R.string.authentication_cancel)
                        .setView(forgotDialogView)
                        .build()
                        .flatMap(new Func1<OnDialogClickEvent, Observable<OnDialogClickEvent>>()
                        {
                            @Override public Observable<OnDialogClickEvent> call(OnDialogClickEvent onDialogClickEvent)
                            {
                                if (onDialogClickEvent.isPositive())
                                {
                                    return validateForgottenEmail(validatedEmail);
                                }
                                else
                                {
                                    return Observable.empty();
                                }
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new EmptyAction1<OnDialogClickEvent>(),
                        new TimberOnErrorAction1("Failed to ask for forgotten password")));
    }

    @NonNull protected Observable<OnDialogClickEvent> validateForgottenEmail(@NonNull ValidatedText validatedEmail)
    {
        final String email1 = validatedEmail.getText().toString();
        TextValidator textValidator = validatedEmail.getValidator();
        textValidator.setText(email1);
        return textValidator.getValidationMessageObservable()
                .flatMap(new Func1<ValidationMessage, Observable<OnDialogClickEvent>>()
                {
                    @Override public Observable<OnDialogClickEvent> call(ValidationMessage validationMessage)
                    {
                        if (validationMessage.getValidStatus().equals(ValidatedView.Status.VALID))
                        {
                            return effectForgotPassword(email1);
                        }
                        else
                        {
                            return AlertDialogRxUtil.buildDefault(getActivity())
                                    .setTitle(R.string.forgot_email_incorrect_input_email)
                                    .setNegativeButton(R.string.ok)
                                    .build();
                        }
                    }
                });
    }

    @NonNull protected Observable<OnDialogClickEvent> effectForgotPassword(@NonNull String email1)
    {
        ForgotPasswordFormDTO forgotPasswordFormDTO = new ForgotPasswordFormDTO();
        forgotPasswordFormDTO.userEmail = email1;

        final ProgressDialog mProgressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.alert_dialog_please_wait),
                getString(R.string.authentication_connecting_tradehero_only),
                true);

        return userServiceWrapper.forgotPasswordRx(forgotPasswordFormDTO)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEach(new DismissDialogAction1<Notification<? super ForgotPasswordDTO>>(
                        mProgressDialog))
                .flatMap(new Func1<ForgotPasswordDTO, Observable<OnDialogClickEvent>>()
                {
                    @Override public Observable<OnDialogClickEvent> call(ForgotPasswordDTO forgotPasswordDTO)
                    {
                        return AlertDialogRxUtil.buildDefault(getActivity())
                                .setTitle(R.string.authentication_thank_you_message_email)
                                .setNegativeButton(R.string.ok)
                                .build();
                    }
                });
    }
}

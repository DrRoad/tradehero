package com.tradehero.th.fragments.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.etiennelawlor.quickreturn.library.views.NotifyingScrollView;
import com.tradehero.common.utils.OnlineStateReceiver;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.auth.AuthData;
import com.tradehero.th.fragments.authentication.AuthDataAccountAction;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.MakePairFunc2;
import com.tradehero.th.rx.ToastAction;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.widget.ValidationListener;
import com.tradehero.th.widget.ValidationMessage;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.functions.Actions;

public class SettingsProfileFragment extends DashboardFragment implements ValidationListener
{
    @InjectView(R.id.authentication_sign_up_button) protected Button updateButton;
    @InjectView(R.id.sign_up_form_wrapper) protected NotifyingScrollView scrollView;
    @InjectView(R.id.profile_info) protected ProfileInfoView profileView;
    @InjectView(R.id.authentication_sign_up_referral_code) protected EditText referralCodeEditText;

    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserProfileCacheRx> userProfileCache;
    @Inject Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject Provider<AuthDataAccountAction> authDataActionProvider;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings_profile, container, false);
        ButterKnife.inject(this, view);
        updateButton.setText(R.string.update);
        referralCodeEditText.setVisibility(View.GONE);
        scrollView.setOnScrollChangedListener(dashboardBottomTabScrollViewScrollListener.get());
        setHasOptionsMenu(true);
        return view;
    }

    @Override public void onStart()
    {
        super.onStart();
        populateCurrentUser();
    }

    @Override public void onDestroyView()
    {
        profileView = null;
        scrollView.setOnScrollChangedListener(null);
        updateButton = null;
        referralCodeEditText = null;
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public boolean areFieldsValid()
    {
        return profileView.areFieldsValid();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (profileView != null)
        {
            profileView.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void populateCurrentUser()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get().get(currentUserId.toUserBaseKey()))
                .subscribe(
                        pair -> profileView.populate(pair.second),
                        e -> THToast.show(new THException(e))));
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.authentication_sign_up_button)
    protected void updateProfile(View view)
    {
        DeviceUtil.dismissKeyboard(view);

        if (!OnlineStateReceiver.isOnline(getActivity()))
        {
            THToast.show(R.string.network_error);
        }
        else if (!areFieldsValid())
        {
            THToast.show(R.string.validation_please_correct);
        }
        else
        {
            ProgressDialog progressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.alert_dialog_please_wait),
                    getString(R.string.authentication_connecting_tradehero_only),
                    true);
            progressDialog.setCancelable(true);

            onStopSubscriptions.add(AppObservable.bindFragment(
                    this,
                    profileView.obtainUserFormDTO()
                            .flatMap(userFormDTO -> {
                                final AuthData authData = new AuthData(userFormDTO.email, userFormDTO.password);
                                Observable<UserProfileDTO> userProfileDTOObservable = userServiceWrapper.get().updateProfileRx(currentUserId
                                        .toUserBaseKey(), userFormDTO);
                                return Observable.zip(
                                        Observable.just(authData),
                                        userProfileDTOObservable,
                                        new MakePairFunc2<>());
                            }))
                    .doOnNext(pair -> {
                        THToast.show(R.string.settings_update_profile_successful);
                        authDataActionProvider.get().call(pair);
                        navigator.get().popFragment();
                    })
                    .doOnError(new ToastAction<>(getString(R.string.error_update_your_user_profile)))
                    .finallyDo(progressDialog::dismiss)
                    .subscribe(Actions.empty(), Actions.empty()));
        }
    }

    public String getPath(Uri uri)
    {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override public void notifyValidation(ValidationMessage message)
    {
        if (message != null && !message.getStatus() && message.getMessage() != null)
        {
            THToast.show(message.getMessage());
        }
    }
}




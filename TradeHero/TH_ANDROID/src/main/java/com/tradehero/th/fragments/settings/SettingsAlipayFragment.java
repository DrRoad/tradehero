package com.tradehero.th.fragments.settings;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.payment.UpdateAlipayAccountDTO;
import com.tradehero.th.api.users.payment.UpdateAlipayAccountFormDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.rx.view.DismissDialogAction0;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.widget.ServerValidatedEmailText;
import javax.inject.Inject;
import rx.android.app.AppObservable;
import rx.functions.Action1;

public class SettingsAlipayFragment extends DashboardFragment
{
    @InjectView(R.id.settings_alipay_email_text) protected ServerValidatedEmailText alipayAccountText;
    @InjectView(R.id.settings_alipay_id_text) protected ServerValidatedEmailText alipayAccountIDText;
    @InjectView(R.id.settings_alipay_realname_text) protected ServerValidatedEmailText alipayAccountRealNameText;

    @Inject UserServiceWrapper userServiceWrapper;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject CurrentUserId currentUserId;
    @Inject Analytics analytics;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_settings_alipay, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        // HACK: force this email to focus instead of the TabHost stealing focus..
        alipayAccountText.setOnTouchListener(new FocusableOnTouchListener());
        alipayAccountIDText.setOnTouchListener(new FocusableOnTouchListener());
        alipayAccountRealNameText.setOnTouchListener(new FocusableOnTouchListener());
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchUserProfile();
    }

    @Override public void onResume()
    {
        super.onResume();

        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Settings_Alipay));
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        setActionBarTitle(R.string.settings_alipay_header);
        super.onCreateOptionsMenu(menu, inflater);
    }
    //</editor-fold>

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    protected void fetchUserProfile()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<UserBaseKey, UserProfileDTO>()))
                .subscribe(
                        new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO profile)
                            {
                                SettingsAlipayFragment.this.onUserProfileReceived(profile);
                            }
                        },
                        new Action1<Throwable>()
                        {
                            @Override public void call(Throwable error)
                            {
                                SettingsAlipayFragment.this.onUserProfileError(error);
                            }
                        }));
    }

    protected void onUserProfileReceived(@NonNull UserProfileDTO profileDTO)
    {
        alipayAccountText.setText(profileDTO.alipayAccount);
        alipayAccountIDText.setText(profileDTO.alipayIdentityNumber);
        alipayAccountRealNameText.setText(profileDTO.alipayRealName);
    }

    @SuppressWarnings("UnusedParameters")
    protected void onUserProfileError(@NonNull Throwable e)
    {
        THToast.show(getString(R.string.error_fetch_your_user_profile));
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.settings_alipay_update_button)
    public void onSubmitClicked(@SuppressWarnings("UnusedParameters") View view)
    {
        final ProgressDialog progressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.alert_dialog_please_wait),
                getString(R.string.authentication_connecting_tradehero_only),
                true);
        UpdateAlipayAccountFormDTO accountDTO = new UpdateAlipayAccountFormDTO();
        accountDTO.newAlipayAccount = alipayAccountText.getText().toString();
        accountDTO.userIdentityNumber = alipayAccountIDText.getText().toString();
        accountDTO.userRealName = alipayAccountRealNameText.getText().toString();
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userServiceWrapper.updateAlipayAccountRx(
                        currentUserId.toUserBaseKey(), accountDTO))
                .finallyDo(new DismissDialogAction0(progressDialog))
                .subscribe(
                        new Action1<UpdateAlipayAccountDTO>()
                        {
                            @Override public void call(UpdateAlipayAccountDTO accountDTO1)
                            {
                                SettingsAlipayFragment.this.onAlipayUpdateReceived(accountDTO1);
                            }
                        },
                        new ToastOnErrorAction()));
    }

    protected void onAlipayUpdateReceived(@NonNull UpdateAlipayAccountDTO args)
    {
        if (!isDetached())
        {
            THToast.show(getString(R.string.settings_alipay_successful_update));
            navigator.get().popFragment();
        }
    }
}
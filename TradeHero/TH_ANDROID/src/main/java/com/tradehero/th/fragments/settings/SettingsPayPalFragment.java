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
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.payment.UpdatePayPalEmailDTO;
import com.tradehero.th.api.users.payment.UpdatePayPalEmailFormDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.widget.ServerValidatedEmailText;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import timber.log.Timber;

public class SettingsPayPalFragment extends DashboardFragment
{
    @InjectView(R.id.settings_paypal_email_text) protected ServerValidatedEmailText paypalEmailText;
    @InjectView(R.id.settings_paypal_update_button) protected Button submitButton;
    private ProgressDialog progressDialog;

    @Nullable private Subscription userProfileSubscription;
    @Nullable private Subscription updatePayPalEmailSubscription;

    @Inject UserServiceWrapper userServiceWrapper;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject CurrentUserId currentUserId;
    @Inject Analytics analytics;
    @Inject ProgressDialogUtil progressDialogUtil;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_settings_paypal, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        // HACK: force this email to focus instead of the TabHost stealing focus..
        paypalEmailText.setOnTouchListener(new FocusableOnTouchListener());
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchUserProfile();
    }

    @Override public void onResume()
    {
        super.onResume();
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Settings_PayPal));
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        setActionBarTitle(R.string.settings_paypal_header);
        super.onCreateOptionsMenu(menu, inflater);
    }
    //</editor-fold>

    @Override public void onStop()
    {
        super.onStop();
        unsubscribe(userProfileSubscription);
        userProfileSubscription = null;
        unsubscribe(updatePayPalEmailSubscription);
        updatePayPalEmailSubscription = null;
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    protected void fetchUserProfile()
    {
        unsubscribe(userProfileSubscription);
        userProfileSubscription = AndroidObservable.bindFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey())
                        .map(pair -> pair.second))
                .subscribe(
                        this::onUserProfileReceived,
                        this::onUserProfileError);
    }

    protected void onUserProfileReceived(@NonNull UserProfileDTO profile)
    {
        paypalEmailText.setText(profile.paypalEmailAddress);
    }

    protected void onUserProfileError(@NonNull Throwable e)
    {
        if (!isDetached())
        {
            THToast.show(getString(R.string.error_fetch_your_user_profile));
            Timber.e("Error fetching the user profile", e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.settings_paypal_update_button)
    public void onSubmitClicked(@SuppressWarnings("UnusedParameters") View view)
    {
        progressDialog = progressDialogUtil.show(
                getActivity(),
                R.string.alert_dialog_please_wait,
                R.string.authentication_connecting_tradehero_only);
        UpdatePayPalEmailFormDTO emailDTO = new UpdatePayPalEmailFormDTO();
        emailDTO.newPayPalEmailAddress = paypalEmailText.getText().toString();
        unsubscribe(updatePayPalEmailSubscription);
        updatePayPalEmailSubscription = AndroidObservable.bindFragment(
                this,
                userServiceWrapper.updatePayPalEmailRx(currentUserId.toUserBaseKey(), emailDTO))
                .subscribe(
                        this::onPayPalUpdated,
                        this::onPayPalUpdateError);
    }

    protected void onPayPalUpdated(@SuppressWarnings("UnusedParameters") @NonNull UpdatePayPalEmailDTO args)
    {
        if (!isDetached())
        {
            THToast.show(getString(R.string.settings_paypal_successful_update));
            progressDialog.hide();
            navigator.get().popFragment();
        }
    }

    protected void onPayPalUpdateError(@NonNull Throwable e)
    {
        if (!isDetached())
        {
            THToast.show(new THException(e));
            progressDialog.hide();
        }
    }
}
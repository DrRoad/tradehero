package com.tradehero.th.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.special.residemenu.ResideMenu;
import com.tradehero.common.billing.BillingPurchaseRestorer;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.persistence.prefs.BooleanPreference;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.notification.NotificationDTO;
import com.tradehero.th.api.notification.NotificationKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserLoginDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.base.Navigator;
import com.tradehero.th.billing.THBillingInteractor;
import com.tradehero.th.billing.request.BaseTHUIBillingRequest;
import com.tradehero.th.billing.request.THUIBillingRequest;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.DashboardTabHost;
import com.tradehero.th.fragments.dashboard.RootFragmentType;
import com.tradehero.th.fragments.onboarding.OnBoardDialogFragment;
import com.tradehero.th.fragments.settings.AboutFragment;
import com.tradehero.th.fragments.settings.AdminSettingsFragment;
import com.tradehero.th.fragments.settings.SettingsFragment;
import com.tradehero.th.fragments.social.friend.InviteCodeDialogFragment;
import com.tradehero.th.fragments.updatecenter.notifications.NotificationClickHandler;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.intent.THIntentFactory;
import com.tradehero.th.models.push.DeviceTokenHelper;
import com.tradehero.th.models.push.PushNotificationManager;
import com.tradehero.th.models.time.AppTiming;
import com.tradehero.th.persistence.notification.NotificationCache;
import com.tradehero.th.persistence.prefs.FirstShowInviteCodeDialog;
import com.tradehero.th.persistence.prefs.FirstShowOnBoardDialog;
import com.tradehero.th.persistence.system.SystemStatusCache;
import com.tradehero.th.persistence.timing.TimingIntervalPreference;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.ui.AppContainer;
import com.tradehero.th.ui.ViewWrapper;
import com.tradehero.th.utils.AlertDialogUtil;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.FacebookUtils;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.utils.WeiboUtils;
import com.tradehero.th.utils.metrics.Analytics;
import com.tradehero.th.utils.route.THRouter;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.Lazy;
import timber.log.Timber;

public class DashboardActivity extends SherlockFragmentActivity
        implements DashboardNavigatorActivity,
        ResideMenu.OnMenuListener
{
    private DashboardNavigator navigator;
    @Inject Set<DashboardNavigator.DashboardFragmentWatcher> dashboardFragmentWatchers;

    // It is important to have Lazy here because we set the current Activity after the injection
    // and the LogicHolder creator needs the current Activity...
    @Inject Lazy<THBillingInteractor> billingInteractor;
    @Inject Provider<BaseTHUIBillingRequest.Builder> thUiBillingRequestBuilderProvider;

    private BillingPurchaseRestorer.OnPurchaseRestorerListener purchaseRestorerFinishedListener;
    private Integer restoreRequestCode;

    @Inject Lazy<FacebookUtils> facebookUtils;
    @Inject Lazy<WeiboUtils> weiboUtils;
    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserProfileCache> userProfileCache;
    @Inject Lazy<THIntentFactory> thIntentFactory;
    @Inject CurrentActivityHolder currentActivityHolder;
    @Inject Lazy<AlertDialogUtil> alertDialogUtil;
    @Inject Lazy<ProgressDialogUtil> progressDialogUtil;
    @Inject Lazy<NotificationCache> notificationCache;
    @Inject DeviceTokenHelper deviceTokenHelper;
    @Inject @FirstShowInviteCodeDialog BooleanPreference firstShowInviteCodeDialogPreference;
    @Inject @FirstShowOnBoardDialog TimingIntervalPreference firstShowOnBoardDialogPreference;
    @Inject SystemStatusCache systemStatusCache;
    @Inject Lazy<MarketUtil> marketUtilLazy;

    @Inject AppContainer appContainer;
    @Inject ViewWrapper slideMenuContainer;
    @Inject ResideMenu resideMenu;

    @Inject THRouter thRouter;
    @Inject Lazy<PushNotificationManager> pushNotificationManager;
    @Inject Analytics analytics;

    private DTOCacheNew.HurriedListener<NotificationKey, NotificationDTO> notificationFetchListener;

    private ProgressDialog progressDialog;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        AppTiming.dashboardCreate = System.currentTimeMillis();

        // this need tobe early than super.onCreate or it will crash
        // when device scroll into landscape.
        // request the progress-bar feature for the activity
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        DaggerUtils.inject(this);

        currentActivityHolder.setCurrentActivity(this);

        if (Constants.RELEASE)
        {
            Crashlytics.setString(Constants.TH_CLIENT_TYPE,
                    String.format("%s:%d", deviceTokenHelper.getDeviceType(), Constants.TAP_STREAM_TYPE.type));
            Crashlytics.setUserIdentifier("" + currentUserId.get());
        }

        ViewGroup dashboardWrapper = appContainer.get(this);

        purchaseRestorerFinishedListener = new BillingPurchaseRestorer.OnPurchaseRestorerListener()
        {
            @Override public void onPurchaseRestored(
                    int requestCode,
                    List restoredPurchases,
                    List failedRestorePurchases,
                    List failExceptions)
            {
                if (Integer.valueOf(requestCode).equals(restoreRequestCode))
                {
                    restoreRequestCode = null;
                }
            }
        };
        launchBilling();

        detachNotificationFetchTask();
        notificationFetchListener = createNotificationFetchListener();

        // TODO better staggering of starting popups.
        suggestUpgradeIfNecessary();
        //dtoCacheUtil.initialPrefetches();//this will block first initial launch securities list,
        // and this line is no use for it will update after login in prefetchesUponLogin

        showInviteCodeDialog();

        navigator = new DashboardNavigator(this, getSupportFragmentManager(), R.id.realtabcontent);
        for (DashboardNavigator.DashboardFragmentWatcher watcher: dashboardFragmentWatchers)
        {
            navigator.addDashboardFragmentWatcher(watcher);
        }

        DashboardTabHost fragmentTabHost = (DashboardTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup();
        navigator.addDashboardFragmentWatcher(fragmentTabHost);
        fragmentTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
        {
            @Override public void onTabChanged(String tabId)
            {
                RootFragmentType selectedFragmentType = RootFragmentType.valueOf(tabId);
                navigator.goToTab(selectedFragmentType);
            }
        });

        if (savedInstanceState == null && navigator.getCurrentFragment() == null)
        {
            navigator.goToTab(RootFragmentType.getInitialTab());
        }

        if (getIntent() != null)
        {
            processNotificationDataIfPresence(getIntent().getExtras());
        }
        //TODO need check whether this is ok for urbanship,
        //TODO for baidu, PushManager.startWork can't run in Application.init() for stability, it will run in a circle. by alex
        pushNotificationManager.get().enablePush();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        return resideMenu.onInterceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    private void launchBilling()
    {
        if (restoreRequestCode != null)
        {
            billingInteractor.get().forgetRequestCode(restoreRequestCode);
        }
        restoreRequestCode = billingInteractor.get().run(createRestoreRequest());
        // TODO fetch more stuff?
    }

    protected THUIBillingRequest createRestoreRequest()
    {
        BaseTHUIBillingRequest.Builder builder = thUiBillingRequestBuilderProvider.get();
        //noinspection unchecked
        builder.restorePurchase(true)
                .startWithProgressDialog(!Constants.RELEASE)
                .popRestorePurchaseOutcome(true)
                .popRestorePurchaseOutcomeVerbose(false)
                .purchaseRestorerListener(purchaseRestorerFinishedListener);
        return builder.build();
    }

    @Override public void onBackPressed()
    {
        getNavigator().popFragment();
    }

    private void suggestUpgradeIfNecessary()
    {
        if (getIntent() != null && getIntent().getBooleanExtra(UserLoginDTO.SUGGEST_UPGRADE, false))
        {
            alertDialogUtil.get().popWithOkCancelButton(
                this, R.string.upgrade_needed, R.string.suggest_to_upgrade, R.string.update_now,
                R.string.later,
                new DialogInterface.OnClickListener()
                {
                    @Override public void onClick(DialogInterface dialog, int which)
                    {
                        THToast.show(R.string.update_guide);
                        marketUtilLazy.get().showAppOnMarket(DashboardActivity.this);
                    }
                });
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu)
    {
        UserProfileDTO currentUserProfile =
                userProfileCache.get().get(currentUserId.toUserBaseKey());
        MenuInflater menuInflater = getSupportMenuInflater();

        menuInflater.inflate(R.menu.hardware_menu, menu);

        if (currentUserProfile != null)
        {
            if (currentUserProfile.isAdmin || !Constants.RELEASE)
            {
                menuInflater.inflate(R.menu.admin_menu, menu);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        // required for fragment onOptionItemSelected to be called
        switch (item.getItemId())
        {
            case R.id.admin_settings:
                getDashboardNavigator().pushFragment(AdminSettingsFragment.class);
                return true;
            case R.id.hardware_menu_settings:
                pushFragmentIfNecessary(SettingsFragment.class);
                return true;
            case R.id.hardware_menu_about:
                pushFragmentIfNecessary(AboutFragment.class);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pushFragmentIfNecessary(Class<? extends Fragment> fragmentClass)
    {
        Fragment currentDashboardFragment = navigator.getCurrentFragment();
        if (!(fragmentClass.isInstance(currentDashboardFragment)))
        {
            getNavigator().pushFragment(fragmentClass);
        }
    }

    @Override protected void onStart()
    {
        super.onStart();
        systemStatusCache.getOrFetchAsync(currentUserId.toUserBaseKey());
    }

    @Override protected void onResume()
    {
        super.onResume();
        launchActions();
        analytics.openSession();
    }

    @Override protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        processNotificationDataIfPresence(extras);
    }

    private void processNotificationDataIfPresence(Bundle extras)
    {
        if (extras != null && extras.containsKey(NotificationKey.BUNDLE_KEY_KEY))
        {
            progressDialog = progressDialogUtil.get().show(this, "", "");

            detachNotificationFetchTask();
            NotificationKey key = new NotificationKey(extras);
            notificationCache.get().register(key, notificationFetchListener);
            notificationCache.get().getOrFetchAsync(key, false);
        }
    }

    private void detachNotificationFetchTask()
    {
        notificationCache.get().unregister(notificationFetchListener);
    }

    @Override protected void onPause()
    {
        analytics.closeSession();
        super.onPause();
    }

    @Override protected void onStop()
    {
        detachNotificationFetchTask();

        super.onStop();
    }

    @Override protected void onDestroy()
    {
        THBillingInteractor billingInteractorCopy = billingInteractor.get();
        if (billingInteractorCopy != null && restoreRequestCode != null)
        {
            billingInteractorCopy.forgetRequestCode(restoreRequestCode);
        }

        if (navigator != null)
        {
            navigator.onDestroy();
        }
        navigator = null;

        if (currentActivityHolder != null)
        {
            currentActivityHolder.unsetActivity(this);
        }
        purchaseRestorerFinishedListener = null;
        notificationFetchListener = null;

        super.onDestroy();
    }

    private void showInviteCodeDialog()
    {
        if (shouldShowOnBoard())
        {
            showOnboard();
        }
        else if (shouldShowInviteCode())
        {
            firstShowInviteCodeDialogPreference.set(false);
            InviteCodeDialogFragment.showInviteCodeDialog(getSupportFragmentManager());
        }
    }

    protected boolean shouldShowInviteCode()
    {
        UserProfileDTO userProfileDTO = userProfileCache.get().get(currentUserId.toUserBaseKey());
        return firstShowInviteCodeDialogPreference.get()
                //&& !(THUser.getTHAuthenticationProvider() instanceof EmailAuthenticationProvider)
                && (userProfileDTO == null || userProfileDTO.inviteCode == null || userProfileDTO.inviteCode.isEmpty());
    }

    protected boolean shouldShowOnBoard()
    {
        if (firstShowOnBoardDialogPreference.isItTime())
        {
            UserProfileDTO currentUserProfile =
                    userProfileCache.get().get(currentUserId.toUserBaseKey());
            if (currentUserProfile != null)
            {
                if (currentUserProfile.heroIds != null && currentUserProfile.heroIds.size() > 0)
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void showOnboard()
    {
        if (shouldShowOnBoard())
        {
            OnBoardDialogFragment.showOnBoardDialog(getSupportFragmentManager());
        }
    }

    private void launchActions()
    {
        Intent intent = getIntent();
        if (intent == null || intent.getAction() == null)
        {
            return;
        }

        if (intent.getData() != null)
        {
            String url = intent.getData().toString();
            url = url.replace("tradehero://", "");
            thRouter.open(url, this);
            return;
        }

        switch (intent.getAction())
        {
            case Intent.ACTION_VIEW:
            case Intent.ACTION_MAIN:
                if (thIntentFactory.get().isHandlableIntent(intent))
                {
                    getDashboardNavigator().goToPage(thIntentFactory.get().create(intent));
                }
                break;
        }
        Timber.d(getIntent().getAction());
    }

    //<editor-fold desc="DashboardNavigatorActivity">
    @Override public Navigator getNavigator()
    {
        return navigator;
    }

    @Override public DashboardNavigator getDashboardNavigator()
    {
        return navigator;
    }
    //</editor-fold>

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        facebookUtils.get().finishAuthentication(requestCode, resultCode, data);
        // Passing it on just in case it is expecting something
        billingInteractor.get().onActivityResult(requestCode, resultCode, data);
        weiboUtils.get().authorizeCallBack(requestCode, resultCode, data);
    }

    @Override public void openMenu()
    {
        Fragment currentFragment =
                getSupportFragmentManager().findFragmentById(R.id.realtabcontent);
        if (currentFragment != null && currentFragment instanceof ResideMenu.OnMenuListener)
        {
            ((ResideMenu.OnMenuListener) currentFragment).openMenu();
        }
    }

    @Override public void closeMenu()
    {
        Fragment currentFragment =
                getSupportFragmentManager().findFragmentById(R.id.realtabcontent);
        if (currentFragment != null && currentFragment instanceof ResideMenu.OnMenuListener)
        {
            ((ResideMenu.OnMenuListener) currentFragment).closeMenu();
        }
    }

    protected DTOCacheNew.HurriedListener<NotificationKey, NotificationDTO> createNotificationFetchListener()
    {
        return new NotificationFetchListener();
    }

    protected class NotificationFetchListener
            implements DTOCacheNew.HurriedListener<NotificationKey, NotificationDTO>
    {
        @Override public void onPreCachedDTOReceived(@NotNull NotificationKey key, @NotNull NotificationDTO value)
        {
            onDTOReceived(key, value);
        }

        @Override
        public void onDTOReceived(@NotNull NotificationKey key, @NotNull NotificationDTO value)
        {
            onFinish();

            NotificationClickHandler notificationClickHandler = new NotificationClickHandler(DashboardActivity.this, value);
            notificationClickHandler.handleNotificationItemClicked();
        }

        @Override public void onErrorThrown(@NotNull NotificationKey key, @NotNull Throwable error)
        {
            onFinish();
            THToast.show(new THException(error));
        }

        private void onFinish()
        {
            if (progressDialog != null)
            {
                progressDialog.hide();
            }
        }
    }

    @Override public void onLowMemory()
    {
        super.onLowMemory();

        // TODO remove
        // for DEBUGGING purpose only
        String currentFragmentName =
                getSupportFragmentManager().findFragmentById(R.id.realtabcontent)
                        .getClass()
                        .getName();
        Timber.e(new RuntimeException("LowMemory " + currentFragmentName), "%s",
                currentFragmentName);
        Crashlytics.setString("LowMemoryAt", new Date().toString());
    }
}

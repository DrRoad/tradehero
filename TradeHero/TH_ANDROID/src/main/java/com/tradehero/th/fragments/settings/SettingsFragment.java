package com.tradehero.th.fragments.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.squareup.okhttp.Cache;
import com.squareup.picasso.LruCache;
import com.tradehero.common.persistence.prefs.BooleanPreference;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.common.rx.DurationMeasurer;
import com.tradehero.common.rx.MinimumApparentDelayer;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.i18n.LanguageDTO;
import com.tradehero.th.api.i18n.LanguageDTOFactory;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.social.SocialNetworkFormDTO;
import com.tradehero.th.api.translation.TranslationToken;
import com.tradehero.th.api.translation.UserTranslationSettingDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.auth.AuthenticationProvider;
import com.tradehero.th.auth.SocialAuth;
import com.tradehero.th.billing.THBillingInteractorRx;
import com.tradehero.th.billing.report.PurchaseReportResult;
import com.tradehero.th.fragments.location.LocationListFragment;
import com.tradehero.th.fragments.onboarding.OnBoardNewDialogFragment;
import com.tradehero.th.fragments.social.friend.FriendsInvitationFragment;
import com.tradehero.th.fragments.translation.TranslatableLanguageListFragment;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.push.PushNotificationManager;
import com.tradehero.th.models.share.SocialShareHelper;
import com.tradehero.th.network.ServerEndpoint;
import com.tradehero.th.network.service.SessionServiceWrapper;
import com.tradehero.th.network.service.SocialServiceWrapper;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.prefs.AuthHeader;
import com.tradehero.th.persistence.prefs.ResetHelpScreens;
import com.tradehero.th.persistence.translation.TranslationTokenCacheRx;
import com.tradehero.th.persistence.translation.TranslationTokenKey;
import com.tradehero.th.persistence.translation.UserTranslationSettingPreference;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.ReplaceWith;
import com.tradehero.th.rx.TimberOnErrorAction;
import com.tradehero.th.rx.ToastAction;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.rx.dialog.OnDialogClickEvent;
import com.tradehero.th.rx.view.DismissDialogAction0;
import com.tradehero.th.utils.AlertDialogRxUtil;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.SocialAlertDialogRxUtil;
import com.tradehero.th.utils.StringUtils;
import com.tradehero.th.utils.VersionUtils;
import com.tradehero.th.utils.dagger.ForPicasso;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.MarketSegment;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Routable("settings")
public final class SettingsFragment extends BasePreferenceFragment
{
    private static final Pair<Long, TimeUnit> APPARENT_DURATION = Pair.create(500l, TimeUnit.MILLISECONDS);
    @Inject CurrentUserId currentUserId;
    @Inject @ServerEndpoint StringPreference serverEndpoint;
    @Inject Analytics analytics;
    @Inject protected THBillingInteractorRx billingInteractorRx;
    @Inject protected SessionServiceWrapper sessionServiceWrapper;
    @Inject @AuthHeader String authHeader;
    @Inject @SocialAuth Map<SocialNetworkEnum, AuthenticationProvider> authenticationProviderMap;
    @Inject @ForPicasso LruCache lruCache;
    @Inject Cache httpCache;
    @Inject @ResetHelpScreens BooleanPreference resetHelpScreen;
    @Inject protected PushNotificationManager pushNotificationManager;
    @Inject protected UserServiceWrapper userServiceWrapper;
    @Nullable protected UserTranslationSettingDTO userTranslationSettingDTO;
    @Inject protected UserTranslationSettingPreference userTranslationSettingPreference;
    @Inject protected TranslationTokenCacheRx translationTokenCache;
    @Nullable protected Subscription translationTokenCacheSubscription;
    @Nullable protected Subscription sequenceSubscription;
    @Inject protected SocialShareHelper socialShareHelper;
    @Inject protected SocialServiceWrapper socialServiceWrapper;
    @Inject protected UserProfileCacheRx userProfileCache;
    protected Subscription userProfileCacheSubscription;

    @Inject @AuthHeader protected String authToken;
    AccountManager accountManager;
    private UserProfileDTO userProfileDTO;

    @Nullable protected CheckBoxPreference socialConnectFB;
    @Nullable protected CheckBoxPreference socialConnectTW;
    @Nullable protected CheckBoxPreference socialConnectLN;
    @Nullable protected CheckBoxPreference socialConnectWB;

    @Nullable protected PreferenceCategory translationContainer;
    @Nullable protected Preference translationPreferredLang;
    @Nullable protected CheckBoxPreference translationAuto;

    @Nullable protected CheckBoxPreference emailNotification;
    @Nullable protected CheckBoxPreference pushNotification;
    @Nullable protected CheckBoxPreference pushNotificationSound;
    @Nullable protected CheckBoxPreference pushNotificationVibrate;

    private SocialNetworkEnum socialNetworkEnum;
    private CheckBoxPreference checkBoxPreference;

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        accountManager = AccountManager.get(activity);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.settings);
        localizationCustomize();

        HierarchyInjector.inject(this);

        initView();
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        view.setBackgroundColor(getResources().getColor(R.color.white));

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        if (listView != null)
        {
            listView.setPadding(
                    (int) getResources().getDimension(R.dimen.setting_padding_left),
                    (int) getResources().getDimension(R.dimen.setting_padding_top),
                    (int) getResources().getDimension(R.dimen.setting_padding_right),
                    (int) getResources().getDimension(R.dimen.setting_padding_bottom));
        }
        fetchUserProfile();
        initPreferenceClickHandlers();
        super.onViewCreated(view, savedInstanceState);
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        actionBarOwnerMixin.setActionBarTitle(R.string.settings);
    }
    //</editor-fold>

    @Override public void onStart()
    {
        super.onStart();
    }

    @Override public void onResume()
    {
        super.onResume();

        analytics.addEvent(new SimpleEvent(AnalyticsConstants.TabBar_Settings));

        if (userProfileDTO != null)
        {
            updateStatus(userProfileDTO);
        }
    }

    @Override public void onStop()
    {
        getView().findViewById(android.R.id.list).removeCallbacks(null);
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        unsubscribe(sequenceSubscription);
        unsubscribe(userProfileCacheSubscription);
        super.onDestroyView();
    }

    protected void unsubscribe(@Nullable Subscription subscription)
    {
        if (subscription != null)
        {
            subscription.unsubscribe();
        }
    }

    private void initView()
    {
        initSocialConnectView();
        initTranslationView();
        initNotificationView();
    }

    private void initSocialConnectView()
    {
        socialConnectFB = initSocialConnection(SocialNetworkEnum.FB, R.string.key_settings_sharing_facebook);
        socialConnectTW = initSocialConnection(SocialNetworkEnum.TW, R.string.key_settings_sharing_twitter);
        socialConnectLN = initSocialConnection(SocialNetworkEnum.LN, R.string.key_settings_sharing_linked_in);
        socialConnectWB = initSocialConnection(SocialNetworkEnum.WB, R.string.key_settings_sharing_weibo);
    }

    private CheckBoxPreference initSocialConnection(@NonNull SocialNetworkEnum socialNetworkEnum, @StringRes int resPreference)
    {
        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(getString(resPreference));

        if (checkBoxPreference != null)
        {
            checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    changeSocialStatus(setCurrentSocialConnect(preference), (boolean) newValue);
                    return false;
                }
            });
            showIsMainLogin(checkBoxPreference, socialNetworkEnum);
        }
        return checkBoxPreference;
    }

    @NonNull private SocialNetworkEnum setCurrentSocialConnect(@NonNull SocialNetworkEnum socialNetworkEnum)
    {
        this.socialNetworkEnum = socialNetworkEnum;
        switch (socialNetworkEnum)
        {
            case FB:
                checkBoxPreference = socialConnectFB;
                break;
            case TW:
                checkBoxPreference = socialConnectTW;
                break;
            case LN:
                checkBoxPreference = socialConnectLN;
                break;
            case WB:
                checkBoxPreference = socialConnectWB;
                break;
        }
        return socialNetworkEnum;
    }

    @Nullable public SocialNetworkEnum setCurrentSocialConnect(@NonNull Preference preference)
    {
        String key = preference.getKey();
        if (key.equals(getString(R.string.key_settings_sharing_facebook)))
        {
            return setCurrentSocialConnect(SocialNetworkEnum.FB);
        }
        else if (key.equals(getString(R.string.key_settings_sharing_twitter)))
        {
            return setCurrentSocialConnect(SocialNetworkEnum.TW);
        }
        else if (key.equals(getString(R.string.key_settings_sharing_linked_in)))
        {
            return setCurrentSocialConnect(SocialNetworkEnum.LN);
        }
        else if (key.equals(getString(R.string.key_settings_sharing_weibo)))
        {
            return setCurrentSocialConnect(SocialNetworkEnum.WB);
        }
        else if (key.equals(getString(R.string.key_settings_sharing_qq)))
        {
            return setCurrentSocialConnect(SocialNetworkEnum.QQ);
        }
        return null;
    }

    private void initTranslationView()
    {
        translationContainer = (PreferenceCategory) findPreference(getString(R.string.key_settings_translations_container));
        translationContainer.setEnabled(false);
        translationPreferredLang = findPreference(getString(R.string.key_settings_translations_preferred_language));
        translationAuto = (CheckBoxPreference) findPreference(getString(R.string.key_settings_translations_auto_translate));

        if (translationAuto != null)
        {
            translationAuto.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    handleAutoTranslateClicked((boolean) newValue);
                    return true;
                }
            });
        }

        fetchTranslationToken();
    }

    private void initNotificationView()
    {
        emailNotification = (CheckBoxPreference) findPreference(
                getString(R.string.key_settings_notifications_email));
        if (emailNotification != null)
        {
            emailNotification.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue)
                        {
                            changeStatusEmail((boolean) newValue);
                            return true;
                        }
                    });
        }

        pushNotification = (CheckBoxPreference) findPreference(
                getString(R.string.key_settings_notifications_push));
        if (pushNotification != null)
        {
            pushNotification.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue)
                        {
                            changeStatusPush((boolean) newValue);
                            return true;
                        }
                    });
        }

        pushNotificationSound = (CheckBoxPreference) findPreference(
                getString(R.string.key_settings_notifications_push_alert_sound));
        if (pushNotificationSound != null)
        {
            pushNotificationSound.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue)
                        {
                            pushNotificationManager.setSoundEnabled((boolean) newValue);
                            return true;
                        }
                    });
        }

        pushNotificationVibrate = (CheckBoxPreference) findPreference(
                getString(R.string.key_settings_notifications_push_alert_vibrate));
        if (pushNotificationVibrate != null)
        {
            pushNotificationVibrate.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue)
                        {
                            pushNotificationManager.setVibrateEnabled((boolean) newValue);
                            return true;
                        }
                    });
        }
    }

    protected boolean changeStatusEmail(boolean enable)
    {
        ProgressDialog progressDialog = ProgressDialog.show(
                getActivity(),
                getActivity().getString(R.string.settings_notifications_email_alert_title),
                getActivity().getString(R.string.settings_notifications_email_alert_message),
                true);
        onStopSubscriptions.add(userServiceWrapper.updateProfilePropertyEmailNotificationsRx(
                currentUserId.toUserBaseKey(), enable)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog))
                .subscribe(
                        new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO profile)
                            {
                                updateStatus(profile);
                            }
                        },
                        new ToastOnErrorAction()));

        return false;
    }

    protected boolean changeStatusPush(boolean enable)
    {
        ProgressDialog progressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.settings_notifications_push_alert_title),
                getString(R.string.settings_notifications_push_alert_message),
                true);
        onStopSubscriptions.add(userServiceWrapper.updateProfilePropertyPushNotificationsRx(
                currentUserId.toUserBaseKey(), enable)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog))
                .subscribe(
                        new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO profile)
                            {
                                updateStatus(profile);
                            }
                        },
                        new ToastOnErrorAction()));

        return false;
    }

    protected void updateStatus(@NonNull UserProfileDTO user)
    {
        this.userProfileDTO = user;
        if (emailNotification != null)
        {
            emailNotification.setChecked(userProfileDTO.emailNotificationsEnabled);
        }

        if (pushNotification != null)
        {
            pushNotification.setChecked(userProfileDTO.pushNotificationsEnabled);
        }

        if (pushNotificationSound != null)
        {
            pushNotificationSound.setEnabled(userProfileDTO.pushNotificationsEnabled);
        }

        if (pushNotificationVibrate != null)
        {
            pushNotificationVibrate.setEnabled(userProfileDTO.pushNotificationsEnabled);
        }

        if (userProfileDTO.pushNotificationsEnabled)
        {
            pushNotificationManager.enablePush();
        }
        else
        {
            pushNotificationManager.disablePush();
        }

        if (socialConnectFB != null)
        {
            socialConnectFB.setChecked(userProfileDTO.fbLinked);
        }

        if (socialConnectLN != null)
        {
            socialConnectLN.setChecked(userProfileDTO.liLinked);
        }

        if (socialConnectTW != null)
        {
            socialConnectTW.setChecked(userProfileDTO.twLinked);
        }

        if (socialConnectWB != null)
        {
            socialConnectWB.setChecked(userProfileDTO.wbLinked);
        }
    }

    private void localizationCustomize()
    {
        if (Constants.TAP_STREAM_TYPE.marketSegment.equals(MarketSegment.CHINA))
        {
            Preference facebookPref = getPreferenceScreen().findPreference(getString(R.string.key_settings_sharing_facebook));
            Preference twitterPref = getPreferenceScreen().findPreference(getString(R.string.key_settings_sharing_twitter));
            PreferenceGroup sharingGroupPref = (PreferenceGroup) getPreferenceScreen().findPreference(getString(R.string.key_settings_sharing_group));
            sharingGroupPref.removePreference(facebookPref);
            sharingGroupPref.removePreference(twitterPref);
        }
    }

    private void initPreferenceClickHandlers()
    {
//        allSettingViewHolders.initViews(this);

        Preference version = findPreference(getString(R.string.key_settings_misc_version_server));
        String serverPath = serverEndpoint.get().replace("http://", "").replace("https://", "");
//        PackageInfo packageInfo = null;
//        String timeStr;
//        try
//        {
//            packageInfo = getActivity().getPackageManager().getPackageInfo(
//                    getActivity().getPackageName(), 0);
//        }
//        catch (PackageManager.NameNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        if (packageInfo != null)
//        {
//            timeStr = (String) DateFormat.format(
//                    getActivity().getString(R.string.data_format_d_mmm_yyyy_kk_mm),
//                    packageInfo.lastUpdateTime);
//            timeStr = timeStr + "(" + packageInfo.lastUpdateTime + ")";
//            version.setSummary(timeStr);
//        }
        version.setTitle(VersionUtils.getVersionId(getActivity()) + " - " + serverPath);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        String key = preference.getKey();
        if (!StringUtils.isNullOrEmpty(key))
        {
            clickedPreference(key);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void clickedPreference(String key)
    {
        if (key.equals(getString(R.string.key_preference_top_banner)))
        {
            handleTopBannerClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_view_intro)))
        {
            handleViewIntro();
        }
        else if (key.equals(getString(R.string.key_settings_primary_send_love)))
        {
            handleSendLoveClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_send_feedback)))
        {
            handleSendFeedbackClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_faq)))
        {
            handleFaqClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_profile)))
        {
            handleProfileClick();
        }
        else if (key.equals(getString(R.string.key_settings_location)))
        {
            handleLocationClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_paypal)))
        {
            handlePaypalClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_alipay)))
        {
            handleAlipayClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_transaction_history)))
        {
            handleTransactionHistroyClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_restore_purchases)))
        {
            handleRestorePurchaseClick();
        }
        else if (key.equals(getString(R.string.key_settings_primary_referral_code)))
        {
            handleReferralCodeClick();
        }
        else if (key.equals(getString(R.string.key_settings_misc_sign_out)))
        {
            handleSignOutClick();
        }
        else if (key.equals(getString(R.string.key_settings_translations_preferred_language)))
        {
            handleTranslationPreferredLanguageClick();
        }
        else if (key.equals(getString(R.string.key_settings_translations_auto_translate)))
        {
            handleTranslationAutoTranslateClick();
        }
        //No Notifications , get more in initNotificationView();
        else if (key.equals(getString(R.string.key_settings_misc_reset_help_screens)))
        {
            handleResetHelpScreenClick();
        }
        else if (key.equals(getString(R.string.key_settings_misc_clear_cache)))
        {
            handleClearCacheClick();
        }
        else if (key.equals(getString(R.string.key_settings_misc_about)))
        {
            handleAboutClick();
        }
    }

    public void handleViewIntro()
    {
        //todo replace with navigatro to viewIntro
        Timber.d("handleViewIntro");
        OnBoardNewDialogFragment.showOnBoardDialog(SettingsFragment.this.getActivity().getSupportFragmentManager());
    }

    public void handleTopBannerClick()
    {
        navigator.get().pushFragment(FriendsInvitationFragment.class);
    }

    public void handleSendLoveClick()
    {
        AskForReviewDialogFragment.showReviewDialog(getActivity().getFragmentManager());
    }

    public void handleSendFeedbackClick()
    {
        startActivity(Intent.createChooser(VersionUtils.getSupportEmailIntent(getActivity(), currentUserId), ""));
    }

    public void handleFaqClick()
    {
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Settings_FAQ));
        String faqUrl = getString(R.string.th_faq_url);
        Bundle bundle = new Bundle();
        WebViewFragment.putUrl(bundle, faqUrl);
        navigator.get().pushFragment(WebViewFragment.class, bundle);
    }

    public void handleProfileClick()
    {
        navigator.get().pushFragment(SettingsProfileFragment.class);
    }

    public void handleLocationClick()
    {
        navigator.get().pushFragment(LocationListFragment.class);
    }

    public void handlePaypalClick()
    {
        navigator.get().pushFragment(SettingsPayPalFragment.class);
    }

    public void handleAlipayClick()
    {
        navigator.get().pushFragment(SettingsAlipayFragment.class);
    }

    public void handleTransactionHistroyClick()
    {
        navigator.get().pushFragment(SettingsTransactionHistoryFragment.class);
    }

    public void handleRestorePurchaseClick()
    {
        //noinspection unchecked
        onStopSubscriptions.add(billingInteractorRx.restorePurchasesAndClear()
                .subscribe(new Observer<PurchaseReportResult>()
                {
                    @Override public void onNext(PurchaseReportResult o)
                    {
                        THToast.show("restored " + o.reportedPurchase.getProductIdentifier());
                    }

                    @Override public void onCompleted()
                    {
                        THToast.show("restore completed");
                    }

                    @Override public void onError(Throwable e)
                    {
                        THToast.show("restore error " + e);
                    }
                }));
    }

    public void handleReferralCodeClick()
    {
        navigator.get().pushFragment(SettingsReferralCodeFragment.class);
    }

    public void handleSignOutClick()
    {
        Context activityContext = getActivity();
        if (activityContext != null)
        {
            AlertDialogRxUtil.buildDefault(activityContext)
                    .setTitle(R.string.settings_misc_sign_out_are_you_sure)
                    .setCancelable(true)
                    .setNegativeButton(R.string.settings_misc_sign_out_no)
                    .setPositiveButton(R.string.settings_misc_sign_out_yes)
                    .build()
                    .subscribe(
                            new Action1<OnDialogClickEvent>()
                            {
                                @Override public void call(OnDialogClickEvent event)
                                {
                                    if (event.isPositive())
                                    {
                                        effectSignOut();
                                    }
                                }
                            },
                            new EmptyAction1<Throwable>());
        }
    }

    public void handleTranslationPreferredLanguageClick()
    {
        navigator.get().pushFragment(TranslatableLanguageListFragment.class);
    }

    public void handleTranslationAutoTranslateClick()
    {
        //todo Click logic
    }

    public void handleResetHelpScreenClick()
    {
        resetHelpScreen.set(true);
        View view = getView();
        Context activityContext = getActivity();
        final ProgressDialog progressDialog;
        if (activityContext != null)
        {
            progressDialog = ProgressDialog.show(activityContext,
                    getString(R.string.settings_misc_reset_help_screen),
                    "",
                    true);
        }
        else
        {
            progressDialog = null;
        }

        if (view != null && progressDialog != null)
        {
            view.postDelayed(new Runnable()
            {
                @Override public void run()
                {
                    progressDialog.hide();
                }
            }, 500);
        }
    }

    public void handleClearCacheClick()
    {
        final ProgressDialog progressDialog;
        Context activityContext = getActivity();
        if (activityContext != null)
        {
            progressDialog = ProgressDialog.show(
                    activityContext,
                    activityContext.getString(R.string.settings_misc_cache_clearing_alert_title),
                    activityContext.getString(R.string.settings_misc_cache_clearing_alert_message),
                    true);
        }
        else
        {
            progressDialog = null;
        }

        new DurationMeasurer<>(
                new Action1<Integer>()
                {
                    @Override public void call(Integer integer)
                    {
                        flushCache();
                    }
                },
                APPARENT_DURATION.second,
                Schedulers.computation())
                .call(1)
                .flatMap(new MinimumApparentDelayer<>(1, APPARENT_DURATION))
                .doOnNext(new Action1<Integer>()
                {
                    @Override public void call(Integer ignored)
                    {
                        if (progressDialog != null)
                        {
                            progressDialog.setTitle(R.string.settings_misc_cache_cleared_alert_title);
                            progressDialog.setMessage("");
                        }
                    }
                })
                .delay(APPARENT_DURATION.first, APPARENT_DURATION.second, AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog))
                .subscribe(
                        new EmptyAction1<Integer>(),
                        new TimberOnErrorAction("Failed to clear cache"));
    }

    public void handleAboutClick()
    {
        navigator.get().pushFragment(AboutFragment.class);
    }

    protected void effectSignOut()
    {
        final ProgressDialog progressDialog;

        Activity activityContext = getActivity();
        if (activityContext != null)
        {
            progressDialog = ProgressDialog.show(
                    activityContext,
                    activityContext.getString(R.string.settings_misc_sign_out_alert_title),
                    activityContext.getString(R.string.settings_misc_sign_out_alert_message),
                    true);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(true);
        }
        else
        {
            progressDialog = null;
        }

        onStopSubscriptions.add(sessionServiceWrapper.logoutRx()
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends UserProfileDTO>>()
                {
                    @Override public Observable<? extends UserProfileDTO> call(final Throwable throwable)
                    {
                        if (progressDialog != null)
                        {
                            progressDialog.setTitle(R.string.settings_misc_sign_out_failed);
                            progressDialog.setMessage("");
                        }
                        else
                        {
                            THToast.show(new THException(throwable));
                        }
                        return Observable.just(0)
                                .delay(3000, TimeUnit.MILLISECONDS)
                                .flatMap(new ReplaceWith<Integer, Observable<UserProfileDTO>>(
                                        Observable.<UserProfileDTO>error(throwable)));
                    }
                })
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog))
                .subscribe(
                        new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO profile)
                            {
                                SettingsFragment.this.onSignedOut(profile);
                            }
                        },
                        new TimberOnErrorAction("Failed to sign out")));
    }

    protected void onSignedOut(@SuppressWarnings("UnusedParameters") UserProfileDTO userProfileDTO)
    {
        for (Map.Entry<SocialNetworkEnum, AuthenticationProvider> entry : authenticationProviderMap.entrySet())
        {
            if (authHeader.startsWith(entry.getKey().getAuthHeader()))
            {
                entry.getValue().logout();
            }
        }

        Account[] accounts = accountManager.getAccountsByType(Constants.Auth.PARAM_ACCOUNT_TYPE);
        if (accounts != null)
        {
            for (Account account : accounts)
            {
                accountManager.removeAccount(account, null, null);
            }
        }
    }

    private void flushCache()
    {
        lruCache.clear();
        try
        {
            httpCache.evictAll();
        } catch (IOException e)
        {
            Timber.e(e, "Failed to evict all in httpCache");
        }
    }

    protected void handleAutoTranslateClicked(boolean newValue)
    {
        if (userTranslationSettingDTO != null)
        {
            userTranslationSettingDTO = userTranslationSettingDTO.cloneForAuto(newValue);
            try
            {
                userTranslationSettingPreference.addOrReplaceSettingDTO(userTranslationSettingDTO);
            } catch (JsonProcessingException e)
            {
                THToast.show(R.string.translation_error_saving_preference);
                e.printStackTrace();
            }
        }
    }

    protected void fetchTranslationToken()
    {
        detachTranslationTokenCache();
        TranslationTokenKey key = new TranslationTokenKey();
        translationTokenCacheSubscription = translationTokenCache.get(key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createTranslationTokenObserver());
    }

    protected void detachTranslationTokenCache()
    {
        Subscription copy = translationTokenCacheSubscription;
        if (copy != null)
        {
            copy.unsubscribe();
        }
        translationTokenCacheSubscription = null;
    }

    protected Observer<Pair<TranslationTokenKey, TranslationToken>> createTranslationTokenObserver()
    {
        return new SettingsTranslationTokenObserver();
    }

    protected class SettingsTranslationTokenObserver implements Observer<Pair<TranslationTokenKey, TranslationToken>>
    {
        @Override public void onNext(Pair<TranslationTokenKey, TranslationToken> pair)
        {
            linkWith(pair.second);
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            Timber.e("Failed", e);
        }
    }

    protected void linkWith(@NonNull TranslationToken token)
    {
        try
        {
            linkWith(userTranslationSettingPreference.getOfSameTypeOrDefault(token));
        } catch (IOException e)
        {
            Timber.e(e, "Failed to get translation setting");
        }
    }

    protected void linkWith(@Nullable UserTranslationSettingDTO userTranslationSettingDTO)
    {
        this.userTranslationSettingDTO = userTranslationSettingDTO;
        if (userTranslationSettingDTO != null)
        {
            //noinspection ConstantConditions
            translationContainer.setEnabled(true);
            linkWith(LanguageDTOFactory.createFromCode(getResources(), userTranslationSettingDTO.languageCode));
            //noinspection ConstantConditions
            translationAuto.setChecked(userTranslationSettingDTO.autoTranslate);
            translationAuto.setSummary(userTranslationSettingDTO.getProviderStringResId());
        }
    }

    protected void linkWith(@NonNull LanguageDTO languageDTO)
    {
        String lang = getString(
                R.string.translation_preferred_language_summary,
                languageDTO.code,
                languageDTO.name,
                languageDTO.nameInOwnLang);
        Preference translationPreferredLangCopy = translationPreferredLang;
        if (translationPreferredLangCopy != null)
        {
            translationPreferredLangCopy.setSummary(lang);
        }
    }

    protected void showIsMainLogin()
    {
        CheckBoxPreference clickablePrefCopy = checkBoxPreference;
        if (clickablePrefCopy != null)
        {
            boolean mainLogin = isMainLogin(socialNetworkEnum);
            clickablePrefCopy.setEnabled(!mainLogin);
            if (mainLogin)
            {
                clickablePrefCopy.setSummary(R.string.authentication_setting_is_current_login);
            }
            else
            {
                clickablePrefCopy.setSummary(null);
            }
        }
    }

    protected void showIsMainLogin(@NonNull CheckBoxPreference cb, @NonNull SocialNetworkEnum se)
    {
        boolean mainLogin = isMainLogin(se);
        cb.setEnabled(!mainLogin);
        if (mainLogin)
        {
            cb.setSummary(R.string.authentication_setting_is_current_login);
        }
        else
        {
            cb.setSummary(null);
        }
    }

    protected boolean isMainLogin(@Nullable SocialNetworkEnum socialNetworkEnum)
    {
        return authToken != null && socialNetworkEnum != null && socialNetworkEnum.isLogin(authToken);
    }

    protected boolean changeSocialStatus(@Nullable SocialNetworkEnum socialNetworkEnum, boolean enable)
    {
        final Activity activityContext = getActivity();

        if (activityContext != null)
        {
            Observable<UserProfileDTO> sequence;
            if (enable)
            {
                sequence = linkRx(socialNetworkEnum);
            }
            else if (isMainLogin(socialNetworkEnum))
            {
                sequence = SocialAlertDialogRxUtil.popErrorUnlinkDefaultAccount(activityContext)
                        .flatMap(new Func1<OnDialogClickEvent, Observable<? extends UserProfileDTO>>()
                        {
                            @Override public Observable<? extends UserProfileDTO> call(OnDialogClickEvent pair)
                            {
                                return Observable.empty();
                            }
                        });
            }
            else
            {
                sequence = confirmUnLinkRx(socialNetworkEnum, activityContext);
            }
            unsubscribe(sequenceSubscription);
            sequenceSubscription = sequence.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<UserProfileDTO>()
                            {
                                @Override public void call(UserProfileDTO profileDTO)
                                {
                                    SettingsFragment.this.updateSocialConnectStatus(profileDTO);
                                }
                            },
                            new Action1<Throwable>()
                            {
                                @Override public void call(Throwable e)
                                {
                                    SettingsFragment.this.onChangeStatusError(activityContext, e);
                                }
                            });
        }
        return false;
    }

    @NonNull protected Observable<UserProfileDTO> linkRx(SocialNetworkEnum socialNetworkEnum)
    {
        return socialShareHelper.handleNeedToLink(socialNetworkEnum);
    }

    @NonNull protected Observable<UserProfileDTO> confirmUnLinkRx(final SocialNetworkEnum socialNetworkEnum, @NonNull final Context activityContext)
    {
        return SocialAlertDialogRxUtil.popConfirmUnlinkAccount(
                activityContext,
                socialNetworkEnum)
                .flatMap(new Func1<OnDialogClickEvent, Observable<? extends UserProfileDTO>>()
                {
                    @Override public Observable<? extends UserProfileDTO> call(OnDialogClickEvent pair)
                    {
                        if (pair.isPositive())
                        {
                            return SettingsFragment.this.effectUnlinkRx(socialNetworkEnum, activityContext);
                        }
                        return Observable.empty();
                    }
                });
    }

    @NonNull protected Observable<UserProfileDTO> effectUnlinkRx(SocialNetworkEnum socialNetworkEnum, @NonNull Context activityContext)
    {
        final ProgressDialog progressDialog = ProgressDialog.show(
                activityContext,
                activityContext.getString(socialNetworkEnum.nameResId),
                activityContext.getString(R.string.authentication_connecting_tradehero_only),
                true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);

        return socialServiceWrapper.disconnectRx(
                currentUserId.toUserBaseKey(),
                new SocialNetworkFormDTO(socialNetworkEnum))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog));
    }

    protected void updateSocialConnectStatus(@NonNull UserProfileDTO userProfileDTO)
    {
        showIsMainLogin();
        updateStatus(userProfileDTO);
    }

    protected void onChangeStatusError(@NonNull Context activityContext, @NonNull Throwable e)
    {
        if (!(e instanceof CancellationException))
        {
            SocialAlertDialogRxUtil.popErrorSocialAuth(activityContext, e)
                    .subscribe(
                            new EmptyAction1<OnDialogClickEvent>(),
                            new EmptyAction1<Throwable>());
        }
    }

    protected void fetchUserProfile()
    {
        unsubscribe(userProfileCacheSubscription);
        userProfileCacheSubscription = userProfileCache.get(currentUserId.toUserBaseKey())
                .map(new PairGetSecond<UserBaseKey, UserProfileDTO>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO profile)
                            {
                                SettingsFragment.this.updateStatus(profile);
                            }
                        },
                        new ToastAction<Throwable>(getString(R.string.error_fetch_your_user_profile)));
    }
}

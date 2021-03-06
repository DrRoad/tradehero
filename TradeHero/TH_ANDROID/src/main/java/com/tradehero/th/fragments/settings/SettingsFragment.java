package com.tradehero.th.fragments.settings;

import android.app.ActionBar;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.ServerEndpoint;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.VersionUtils;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.MarketSegment;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import javax.inject.Inject;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Routable("settings")
public final class SettingsFragment extends DashboardPreferenceFragment
{
    private static final String KEY_SOCIAL_NETWORK_TO_CONNECT = SettingsFragment.class.getName() + ".socialNetworkToConnectKey";

    @Inject CurrentUserId currentUserId;
    @Inject @ServerEndpoint StringPreference serverEndpoint;
    @Inject Analytics analytics;
    @Inject protected UnreadSettingPreferenceHolder unreadSettingPreferenceHolder;
    @Inject protected SocialConnectSettingViewHolderContainer socialConnectSettingViewHolderContainer;
    @Inject protected TopBannerSettingViewHolder topBannerSettingViewHolder;
    @Inject protected SendLoveViewHolder sendLoveViewHolder;
    @Inject protected SendFeedbackViewHolder sendFeedbackViewHolder;
    @Inject protected FaqViewHolder faqViewHolder;
    @Inject protected ProfilePreferenceViewHolder profilePreferenceViewHolder;
    @Inject protected LocationCountrySettingsViewHolder locationCountrySettingsViewHolder;
    @Inject protected PayPalSettingViewHolder payPalSettingViewHolder;
    @Inject protected AlipaySettingViewHolder alipaySettingViewHolder;
    @Inject protected TransactionHistoryViewHolder transactionHistoryViewHolder;
    @Inject protected RestorePurchaseSettingViewHolder restorePurchaseSettingViewHolder;
    @Inject protected ReferralCodeSettingViewHolder referralCodeSettingViewHolder;
    @Inject protected SignOutSettingViewHolder signOutSettingViewHolder;
    @Inject protected UserTranslationSettingsViewHolder userTranslationSettingsViewHolder;
    @Inject protected EmailNotificationSettingViewHolder emailNotificationSettingViewHolder;
    @Inject protected PushNotificationSettingViewHolder pushNotificationSettingViewHolder;
    @Inject protected ResetHelpScreensViewHolder resetHelpScreensViewHolder;
    @Inject protected ClearCacheViewHolder clearCacheViewHolder;
    @Inject protected AboutPrefViewHolder aboutPrefViewHolder;

    @NonNull private SettingViewHolderList allSettingViewHolders;
    private SocialNetworkEnum socialNetworkToConnectTo;

    public static void putSocialNetworkToConnect(@NonNull Bundle args, @NonNull SocialNetworkEnum socialNetwork)
    {
        args.putString(KEY_SOCIAL_NETWORK_TO_CONNECT, socialNetwork.name());
    }

    @Nullable public static SocialNetworkEnum getSocialNetworkToConnect(@Nullable Bundle args)
    {
        if (args == null)
        {
            return null;
        }
        String name = args.getString(KEY_SOCIAL_NETWORK_TO_CONNECT);
        if (name == null)
        {
            return null;
        }
        return SocialNetworkEnum.valueOf(name);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.settings);
        localizationCustomize();

        HierarchyInjector.inject(this);

        this.allSettingViewHolders = new SettingViewHolderList();
        allSettingViewHolders.add(topBannerSettingViewHolder);
        // Sharing
        allSettingViewHolders.add(socialConnectSettingViewHolderContainer);
        // General
        allSettingViewHolders.add(sendLoveViewHolder);
        allSettingViewHolders.add(sendFeedbackViewHolder);
        allSettingViewHolders.add(faqViewHolder);
        // Account
        allSettingViewHolders.add(profilePreferenceViewHolder);
        allSettingViewHolders.add(locationCountrySettingsViewHolder);
        allSettingViewHolders.add(payPalSettingViewHolder);
        allSettingViewHolders.add(alipaySettingViewHolder);
        allSettingViewHolders.add(transactionHistoryViewHolder);
        allSettingViewHolders.add(restorePurchaseSettingViewHolder);
        allSettingViewHolders.add(referralCodeSettingViewHolder);
        allSettingViewHolders.add(signOutSettingViewHolder);
        // Translations
        allSettingViewHolders.add(userTranslationSettingsViewHolder);
        // Notification
        allSettingViewHolders.add(emailNotificationSettingViewHolder);
        allSettingViewHolders.add(pushNotificationSettingViewHolder);
        // Misc
        allSettingViewHolders.add(resetHelpScreensViewHolder);
        allSettingViewHolders.add(clearCacheViewHolder);
        allSettingViewHolders.add(aboutPrefViewHolder);
        this.socialNetworkToConnectTo = getSocialNetworkToConnect(getArguments());
    }

    @Override public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup,
            Bundle paramBundle)
    {
        View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        view.setBackgroundColor(getResources().getColor(R.color.white));

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        if (listView != null)
        {
            listView.setPadding(
                    (int) getResources().getDimension(R.dimen.setting_padding_left),
                    (int) getResources().getDimension(R.dimen.setting_padding_top),
                    (int) getResources().getDimension(R.dimen.setting_padding_right),
                    (int) getResources().getDimension(R.dimen.setting_padding_bottom));
            listView.setOnScrollListener(dashboardBottomTabsScrollListener.get());
        }

        return view;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        initPreferenceClickHandlers();
        super.onViewCreated(view, savedInstanceState);
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(getString(R.string.settings));
    }
    //</editor-fold>

    @Override public void onStart()
    {
        super.onStart();
        moveToFirstUnread();
    }

    public void moveToFirstUnread()
    {
        if (unreadSettingPreferenceHolder.hasUnread())
        {
            ListView listView = (ListView) getView().findViewById(android.R.id.list);
            ListAdapter adapter = listView.getAdapter();
            SettingViewHolder unreadHolder = allSettingViewHolders.getFirstUnread();
            if (unreadHolder != null)
            {
                Preference toShow = unreadHolder.getPreference();
                for (int index = 0; index < adapter.getCount(); index++)
                {
                    if (/*(Preference) */adapter.getItem(index) == toShow)
                    {
                        waitAndMoveTo(listView, index);
                        break;
                    }
                }
            }
        }
    }

    private void waitAndMoveTo(@NonNull final ListView listView, final int index)
    {
        listView.post(new Runnable()
        {
            @Override public void run()
            {
                listView.smoothScrollToPosition(index);
            }
        });
    }

    @Override public void onResume()
    {
        super.onResume();

        analytics.addEvent(new SimpleEvent(AnalyticsConstants.TabBar_Settings));
        if (socialNetworkToConnectTo != null)
        {
            socialConnectSettingViewHolderContainer.changeSharing(socialNetworkToConnectTo, true);
            socialNetworkToConnectTo = null;
        }
    }

    @Override public void onStop()
    {
        getView().findViewById(android.R.id.list).removeCallbacks(null);
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        allSettingViewHolders.destroyViews();
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        aboutPrefViewHolder = null;
        clearCacheViewHolder = null;
        resetHelpScreensViewHolder = null;
        pushNotificationSettingViewHolder = null;
        emailNotificationSettingViewHolder = null;
        userTranslationSettingsViewHolder = null;
        signOutSettingViewHolder = null;
        referralCodeSettingViewHolder = null;
        restorePurchaseSettingViewHolder = null;
        transactionHistoryViewHolder = null;
        alipaySettingViewHolder = null;
        payPalSettingViewHolder = null;
        locationCountrySettingsViewHolder = null;
        profilePreferenceViewHolder = null;
        faqViewHolder = null;
        sendFeedbackViewHolder = null;
        sendLoveViewHolder = null;
        socialConnectSettingViewHolderContainer = null;
        topBannerSettingViewHolder = null;

        allSettingViewHolders.clear();
        super.onDestroy();
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
        allSettingViewHolders.initViews(this);

        Preference version = findPreference(getString(R.string.key_settings_misc_version_server));
        String serverPath = serverEndpoint.get().replace("http://", "").replace("https://", "");
        PackageInfo packageInfo = null;
        String timeStr;
        try
        {
            packageInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
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
}

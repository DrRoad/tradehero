package com.tradehero.th.fragments.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.activities.DashboardActivity;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.base.THApp;
import com.tradehero.th.fragments.ForKChartFragment;
import com.tradehero.th.fragments.ForTypographyFragment;
import com.tradehero.th.fragments.achievement.ForAchievementListTestingFragment;
import com.tradehero.th.fragments.achievement.ForQuestListTestingFragment;
import com.tradehero.th.fragments.competition.CompetitionPreseasonDialogFragment;
import com.tradehero.th.fragments.fxonboard.FxOnBoardDialogFragment;
import com.tradehero.th.fragments.level.ForXpTestingFragment;
import com.tradehero.th.fragments.onboarding.OnBoardDialogFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.models.push.PushConstants;
import com.tradehero.th.models.push.handlers.NotificationOpenedHandler;
import com.tradehero.th.network.ServerEndpoint;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.rx.dialog.OnDialogClickEvent;
import com.tradehero.th.utils.AlertDialogRxUtil;
import javax.inject.Inject;
import javax.inject.Provider;
import rx.android.app.AppObservable;
import rx.functions.Action1;

public class AdminSettingsFragment extends DashboardPreferenceFragment
{
    private static final CharSequence KEY_USER_INFO = "user_info";
    private static final CharSequence KEY_SERVER_ENDPOINT = "server_endpoint";
    private static final CharSequence KEY_SEND_FAKE_PUSH = "send_fake_push";
    private static final CharSequence KEY_DAILY_TEST_SCREEN = "show_daily_quest_test_screen";
    private static final CharSequence KEY_ACHIEVEMENT_TEST_SCREEN = "show_achievement_test_screen";
    private static final CharSequence KEY_XP_TEST_SCREEN = "show_xp_test_screen";
    private static final CharSequence KEY_TYPOGRAPHY_SCREEN = "show_typography_examples";
    private static final CharSequence KEY_PRESEASON = "show_preseason_dialog";
    private static final CharSequence KEY_KCHART = "show_kchart_examples";

    @Inject @ServerEndpoint StringPreference serverEndpointPreference;
    @Inject THApp app;
    @Inject Provider<NotificationOpenedHandler> notificationOpenedHandler;
    @Inject @ForQuestListTestingFragment Provider<Class> questListTestingFragmentClassProvider;
    @Inject @ForAchievementListTestingFragment Provider<Class> achievementListTestingFragmentClassProvider;
    @Inject @ForXpTestingFragment Provider<Class> xpTestingFragmentClassProvider;
    @Inject @ForTypographyFragment Provider<Class> typographyFragmentClassProvider;
    @Inject @ForKChartFragment Provider<Class> kChartFragmentClassProvider;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject CurrentUserId currentUserId;
    @Inject Provider<Activity> currentActivity;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        HierarchyInjector.inject(this);
        addPreferencesFromResource(R.xml.admin_settings);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        initPreferenceClickHandlers();
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnScrollListener(dashboardBottomTabsScrollListener.get());
        super.onViewCreated(view, savedInstanceState);
    }

    @Override public void onStart()
    {
        super.onStart();
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey()))
                .subscribe(
                        new Action1<Pair<UserBaseKey, UserProfileDTO>>()
                        {
                            @Override public void call(Pair<UserBaseKey, UserProfileDTO> pair)
                            {
                                Preference pref = AdminSettingsFragment.this.findPreference(KEY_USER_INFO);
                                pref.setSummary(getString(R.string.admin_setting_user_info, pair.second.displayName, pair.first.key));
                            }
                        },
                        new ToastOnErrorAction()));
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null)
        {
            actionBar.setTitle(getString(R.string.admin_setting));
        }
    }

    private void initPreferenceClickHandlers()
    {
        ListPreference serverEndpointListPreference = (ListPreference) findPreference(KEY_SERVER_ENDPOINT);
        if (serverEndpointPreference != null)
        {
            serverEndpointListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    return AdminSettingsFragment.this.onServerEndpointChanged((String) newValue);
                }
            });

            if (serverEndpointPreference != null)
            {
                int selectedIndex = serverEndpointListPreference.findIndexOfValue(serverEndpointPreference.get());
                CharSequence[] entries = serverEndpointListPreference.getEntries();
                if (entries != null && selectedIndex < entries.length)
                {
                    serverEndpointListPreference.setTitle(getString(R.string.current_endpoint) + entries[selectedIndex]);
                }
                else
                {
                    serverEndpointListPreference.setTitle(getString(R.string.select_endpoint));
                }
            }
        }

        Preference sendFakePush = findPreference(KEY_SEND_FAKE_PUSH);
        sendFakePush.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                return AdminSettingsFragment.this.askForNotificationId();
            }
        });

        Preference showReviewDialog = findPreference("show_review_dialog");
        showReviewDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                FragmentActivity activity = (FragmentActivity) currentActivity.get();
                AskForReviewDialogFragment.showReviewDialog(activity.getFragmentManager());
                return true;
            }
        });

        Preference showInviteDialog = findPreference("show_invite_dialog");
        showInviteDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                FragmentActivity activity = (FragmentActivity) currentActivity.get();
                AskForInviteDialogFragment.showInviteDialog(activity.getFragmentManager());
                return true;
            }
        });

        Preference showOnBoardDialog = findPreference("show_onBoard_dialog");
        showOnBoardDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                // FragmentActivity activityversion.properties = (FragmentActivity) currentActivityHolder.getCurrentActivity();
                OnBoardDialogFragment.showOnBoardDialog(AdminSettingsFragment.this.getActivity().getFragmentManager());
                return true;
            }
        });

        Preference showFxOnBoardDialog = findPreference("show_fx_onBoard_dialog");
        showFxOnBoardDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                FxOnBoardDialogFragment.showOnBoardDialog(AdminSettingsFragment.this.getActivity().getFragmentManager());
                return true;
            }
        });

        Preference showTestDaily = findPreference(KEY_DAILY_TEST_SCREEN);
        showTestDaily.setEnabled(questListTestingFragmentClassProvider.get() != null);
        showTestDaily.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                navigator.get().pushFragment(questListTestingFragmentClassProvider.get());
                return true;
            }
        });

        Preference showTestAchievement = findPreference(KEY_ACHIEVEMENT_TEST_SCREEN);
        showTestAchievement.setEnabled(achievementListTestingFragmentClassProvider.get() != null);
        showTestAchievement.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                navigator.get().pushFragment(achievementListTestingFragmentClassProvider.get());
                return true;
            }
        });

        Preference showXPTest = findPreference(KEY_XP_TEST_SCREEN);
        showXPTest.setEnabled(xpTestingFragmentClassProvider.get() != null);
        showXPTest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                navigator.get().pushFragment(xpTestingFragmentClassProvider.get());
                return true;
            }
        });

        Preference showTypography = findPreference(KEY_TYPOGRAPHY_SCREEN);
        showTypography.setEnabled(typographyFragmentClassProvider.get() != null);
        showTypography.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                navigator.get().pushFragment(typographyFragmentClassProvider.get());
                return true;
            }
        });

        Preference showPreseason = findPreference(KEY_PRESEASON);
        showPreseason.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {

                CompetitionPreseasonDialogFragment dialog = CompetitionPreseasonDialogFragment.newInstance(new ProviderId(24));
                dialog.show(AdminSettingsFragment.this.getActivity().getFragmentManager(), CompetitionPreseasonDialogFragment.TAG);
                return true;
            }
        });

        Preference showKChart = findPreference(KEY_KCHART);
        showKChart.setEnabled(kChartFragmentClassProvider.get() != null);
        showKChart.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override public boolean onPreferenceClick(Preference preference)
            {
                navigator.get().pushFragment(kChartFragmentClassProvider.get());
                return true;
            }
        });
    }

    private boolean askForNotificationId()
    {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.debug_ask_for_notification_id, null);
        final EditText input = (EditText) view.findViewById(R.id.pushNotification);

        AlertDialogRxUtil.build(getActivity())
                .setView(view)
                .setPositiveButton(R.string.ok)
                .build()
                .subscribe(new Action1<OnDialogClickEvent>()
                {
                    @Override public void call(OnDialogClickEvent event)
                    {
                        if (event.isPositive())
                        {
                            Editable value = input.getText();
                            int notificationId = 0;
                            try
                            {
                                notificationId = Integer.parseInt(value.toString());
                            } catch (NumberFormatException ex)
                            {
                                THToast.show("Not a number");
                            }
                            sendFakePushNotification(notificationId);
                        }
                    }
                });
        return true;
    }

    private void sendFakePushNotification(int notificationId)
    {
        Intent fakeIntent = new Intent();
        fakeIntent.putExtra(PushConstants.KEY_PUSH_ID, String.valueOf(notificationId));
        notificationOpenedHandler.get().handle(fakeIntent);
    }

    private boolean onServerEndpointChanged(String serverEndpoint)
    {
        serverEndpointPreference.set(serverEndpoint);

        app.restartActivity(DashboardActivity.class);
        return false;
    }
}

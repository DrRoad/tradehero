package com.tradehero.th.fragments.leaderboard;

import android.os.Bundle;
import com.tradehero.AbstractTestBase;
import com.tradehero.RobolectricMavenTestRunner;
import com.tradehero.th.activities.DashboardActivity;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefKey;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.social.hero.HeroAlertDialogUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(RobolectricMavenTestRunner.class)
public class LeaderboardMarkUserListFragmentTest extends AbstractTestBase
{
    private DashboardNavigator dashboardNavigator;
    private LeaderboardMarkUserListFragment leaderboardMarkUserListFragment;
    private HeroAlertDialogUtil heroAlertDialogUtil;

    @Before public void setUp()
    {
        DashboardActivity activity = Robolectric.setupActivity(DashboardActivity.class);
        dashboardNavigator = activity.getDashboardNavigator();
    }

    @After public void tearDown()
    {
        if (dashboardNavigator != null)
        {
            dashboardNavigator.popFragment();
        }
        dashboardNavigator = null;
        leaderboardMarkUserListFragment = null;
        heroAlertDialogUtil = null;
    }

    @Ignore("Apparently it crashes on start because of a problem with Loader")
    @Test public void handleFollowRequestedCallsAlertDialog()
    {
        Bundle args = new Bundle();
        LeaderboardMarkUserListFragment.putLeaderboardDefKey(args, new LeaderboardDefKey(123));
        leaderboardMarkUserListFragment = dashboardNavigator.pushFragment(LeaderboardMarkUserListFragment.class, args);
    }
}

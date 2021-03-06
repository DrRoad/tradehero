package com.tradehero.th.fragments.trade;

import android.content.Context;
import android.os.Bundle;
import com.tradehero.THRobolectricTestRunner;
import com.tradehero.TestConstants;
import com.tradehero.th.activities.DashboardActivity;
import com.tradehero.th.activities.DashboardActivityExtended;
import com.tradehero.th.fragments.DashboardNavigator;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assume.assumeTrue;

@RunWith(THRobolectricTestRunner.class)
public class TradeListFragmentTest
{
    @Inject Context context;
    private TradeListFragment tradeListFragment;
    @Inject DashboardNavigator dashboardNavigator;

    @Before
    public void setUp()
    {
        DashboardActivity activity = Robolectric.setupActivity(DashboardActivityExtended.class);
        activity.inject(this);
    }

    @After
    public void tearDown()
    {
        tradeListFragment = null;
    }

    @Test(expected = NullPointerException.class)
    public void ifNotIntelliJShouldNPEOnNullArgs()
    {
        assumeTrue(!TestConstants.IS_INTELLIJ);
        tradeListFragment = dashboardNavigator.pushFragment(TradeListFragment.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifIntelliJShouldIllegalOnNullArgs()
    {
        assumeTrue(TestConstants.IS_INTELLIJ);
        tradeListFragment = dashboardNavigator.pushFragment(TradeListFragment.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifIntelliJShouldThrowIllegalArgumentOnInvalidArgsKey()
    {
        assumeTrue(TestConstants.IS_INTELLIJ);
        Bundle args = new Bundle();
        tradeListFragment = dashboardNavigator.pushFragment(TradeListFragment.class, args);
    }
}

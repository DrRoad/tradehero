package com.tradehero.th.models.intent.trending;

import com.tradehero.RobolectricMavenTestRunner;
import com.tradehero.th.fragments.dashboard.DashboardTabType;
import com.tradehero.th.models.intent.OpenCurrentActivityHolder;
import com.tradehero.th.models.intent.THIntent;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricMavenTestRunner.class)
public class TrendingIntentTest
{
    @Before public void setUp()
    {
        THIntent.currentActivityHolder = new OpenCurrentActivityHolder(Robolectric.getShadowApplication().getApplicationContext());
    }

    @After public void tearDown()
    {
        THIntent.currentActivityHolder = null;
    }

    @Test public void constructorSetsPath()
    {
        THIntent intent = new TrendingIntent();
        Assert.assertEquals("tradehero://trending", intent.getData() + "");
    }

    @Test public void uriPathIsWellFormed()
    {
        assertEquals("tradehero://trending", new TrendingIntent().getUriPath());
    }

    @Test public void typeIsDashboard()
    {
        assertEquals(DashboardTabType.TRENDING, new TrendingIntent().getDashboardType());
    }
}

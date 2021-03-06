package com.tradehero.th.models.intent.security;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import com.tradehero.THRobolectricTestRunner;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerId;
import com.tradehero.th.fragments.trade.BuySellFragment;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(THRobolectricTestRunner.class)
public class SecurityPushBuyIntentTest
{
    private Resources resources;

    @Before public void setUp()
    {
        resources = Robolectric.getShadowApplication().getApplicationContext().getResources();
    }

    @After public void tearDown()
    {
    }

    @Test public void securityActionUriPathIsWellFormed()
    {
        SecurityIntegerId unused = new SecurityIntegerId(123);
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId useless = new SecurityId("ABB", "CDD");
        SecurityId securityId = new SecurityId("EFF", "GHH");
        assertEquals("tradehero://security/456_EFF_GHH", new SecurityPushBuyIntent(resources, unused, useless).getSecurityActionUriPath(used, securityId));
    }

    @Test public void securityActionUriIsWellFormed()
    {
        SecurityIntegerId unused = new SecurityIntegerId(123);
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId useless = new SecurityId("ABB", "CDD");
        SecurityId securityId = new SecurityId("EFF", "GHH");
        SecurityPushBuyIntent intent = new SecurityPushBuyIntent(resources, unused, useless);
        Uri uri = intent.getSecurityActionUri(used, securityId);
        List<String> pathSegments = uri.getPathSegments();

        assertEquals("tradehero", uri.getScheme());
        assertEquals("security", uri.getHost());
        assertEquals(1, pathSegments.size());
        assertEquals("456_EFF_GHH", pathSegments.get(0));
    }

    @Test public void constructorPlacesPath()
    {
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId securityId = new SecurityId("EFF", "GHH");
        SecurityPushBuyIntent intent = new SecurityPushBuyIntent(resources, used, securityId);
        Uri uri = intent.getData();

        assertEquals("tradehero://security/456_EFF_GHH", uri + "");

        List<String> pathSegments = uri.getPathSegments();
        assertEquals("tradehero", uri.getScheme());
        assertEquals("security", uri.getHost());
        assertEquals(1, pathSegments.size());
        assertEquals("456_EFF_GHH", pathSegments.get(resources.getInteger(R.integer.intent_security_push_buy_index_elements)));
    }

    @Test public void uriParserIsOk()
    {
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId securityId = new SecurityId("EFF", "GHH");
        SecurityPushBuyIntent intent = new SecurityPushBuyIntent(resources, used, securityId);
        Uri uri = intent.getData();
        assertTrue(used.equals(SecurityPushBuyIntent.getSecurityIntegerId(resources, uri)));
        assertTrue(securityId.equals(SecurityPushBuyIntent.getSecurityId(resources, uri)));
    }

    @Test public void getSecurityIdReturnsCorrect()
    {
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId securityId = new SecurityId("EFF", "GHH");
        SecurityPushBuyIntent intent = new SecurityPushBuyIntent(resources, used, securityId);

        assertTrue(used.equals(intent.getSecurityIntegerId()));
        assertTrue(securityId.equals(intent.getSecurityId()));
    }

    @Test public void actionFragmentIsCorrect()
    {
        assertEquals(BuySellFragment.class, new SecurityPushBuyIntent(resources, new SecurityIntegerId(3), new SecurityId("A", "B")).getActionFragment());
    }

    @Test public void bundleIsCorrect()
    {
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId securityId = new SecurityId("A", "B");
        SecurityPushBuyIntent intent = new SecurityPushBuyIntent(resources, used, securityId);
        Bundle bundle = intent.getBundle();
        assertEquals(1, bundle.size());
        assertEquals(2, bundle.getBundle("com.tradehero.th.fragments.trade.AbstractBuySellFragment.securityId").size());
        assertTrue(securityId.equals(new SecurityId(bundle.getBundle("com.tradehero.th.fragments.trade.AbstractBuySellFragment.securityId"))));
    }

    @Test public void populateBundleKeepsExisting()
    {
        SecurityIntegerId used = new SecurityIntegerId(456);
        SecurityId securityId = new SecurityId("A", "B");
        SecurityPushBuyIntent intent = new SecurityPushBuyIntent(resources, used, securityId);
        Bundle bundle = new Bundle();
        bundle.putString("Whoo", "bah");
        intent.populate(bundle);

        assertEquals(2, bundle.size());
        assertEquals(2, bundle.getBundle("com.tradehero.th.fragments.trade.AbstractBuySellFragment.securityId").size());
        assertTrue(securityId.equals(new SecurityId(bundle.getBundle("com.tradehero.th.fragments.trade.AbstractBuySellFragment.securityId"))));
    }
}

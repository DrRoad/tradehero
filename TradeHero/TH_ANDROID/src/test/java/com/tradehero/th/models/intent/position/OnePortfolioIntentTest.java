package com.tradehero.th.models.intent.position;

import android.net.Uri;
import android.os.Bundle;
import com.tradehero.TestConstants;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.PortfolioId;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.models.intent.OpenCurrentActivityHolder;
import com.tradehero.th.models.intent.THIntent;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConstants.MANIFEST_PATH)
public class OnePortfolioIntentTest
{
    @Before public void setUp()
    {
        THIntent.currentActivityHolder = new OpenCurrentActivityHolder(Robolectric.getShadowApplication().getApplicationContext());
    }

    @After public void tearDown()
    {
        THIntent.currentActivityHolder = null;
    }

    @Test public void portfolioActionUriPathIsWellFormed()
    {
        PortfolioId useless = new PortfolioId(234);
        PortfolioId portfolioId = new PortfolioId(567);
        assertEquals("tradehero://portfolio/buy/567", new SimpleOnePortfolioIntent(useless).getPortfolioActionUriPath(portfolioId));
    }

    @Test public void portfolioActionUriIsWellFormed()
    {
        PortfolioId useless = new PortfolioId(234);
        PortfolioId portfolioId = new PortfolioId(567);
        OnePortfolioIntent intent = new SimpleOnePortfolioIntent(useless);
        Uri uri = intent.getPortfolioActionUri(portfolioId);
        List<String> pathSegments = uri.getPathSegments();

        assertEquals("tradehero", uri.getScheme());
        assertEquals("portfolio", uri.getHost());
        assertEquals(2, pathSegments.size());
        assertEquals("buy", pathSegments.get(0));
        assertEquals(567, Integer.parseInt(pathSegments.get(1)));
    }

    @Test public void constructorPlacesPath()
    {
        PortfolioId portfolioId = new PortfolioId(567);
        OnePortfolioIntent intent = new SimpleOnePortfolioIntent(portfolioId);
        Uri uri = intent.getData();

        assertEquals("tradehero://portfolio/buy/567", uri + "");

        List<String> pathSegments = uri.getPathSegments();
        assertEquals("tradehero", uri.getScheme());
        assertEquals("portfolio", uri.getHost());
        assertEquals(2, pathSegments.size());
        assertEquals("buy", pathSegments.get(THIntent.getInteger(R.integer.intent_uri_path_index_action)));
        assertEquals(567, Integer.parseInt(pathSegments.get(THIntent.getInteger(R.integer.intent_uri_action_portfolio_path_index_id))));
    }

    @Test public void uriParserIsOk()
    {
        PortfolioId portfolioId = new PortfolioId(567);
        OnePortfolioIntent intent = new SimpleOnePortfolioIntent(portfolioId);
        Uri uri = intent.getData();
        assertTrue(portfolioId.equals(OnePortfolioIntent.getPortfolioId(uri)));
    }

    @Test public void getPortfolioIdReturnsCorrect()
    {
        PortfolioId portfolioId = new PortfolioId(567);
        OnePortfolioIntent intent = new SimpleOnePortfolioIntent(portfolioId);

        assertTrue(portfolioId.equals(intent.getPortfolioId()));
    }

    @Test public void actionFragmentIsCorrect()
    {
        assertEquals(PositionListFragment.class, new SimpleOnePortfolioIntent(new PortfolioId(567)).getActionFragment());
    }

    @Test public void bundleIsCorrect()
    {
        PortfolioId portfolioId = new PortfolioId(567);
        OnePortfolioIntent intent = new SimpleOnePortfolioIntent(portfolioId);
        Bundle bundle = intent.getBundle();
        assertEquals(1, bundle.size());
        assertTrue(false);
        // Need to change the Intent
        //assertEquals(567, bundle.getInt(PositionListFragment.BUNDLE_KEY_SHOW_PORTFOLIO_ID_BUNDLE));
    }

    @Test public void populateBundleKeepsExisting()
    {
        PortfolioId portfolioId = new PortfolioId(567);
        OnePortfolioIntent intent = new SimpleOnePortfolioIntent(portfolioId);
        Bundle bundle = new Bundle();
        bundle.putString("Whoo", "bah");
        intent.populate(bundle);

        assertEquals(2, bundle.size());
        assertTrue(false);
        // Need to change the Intent
        //assertEquals(567, bundle.getInt(PositionListFragment.BUNDLE_KEY_SHOW_PORTFOLIO_ID_BUNDLE));
    }
}

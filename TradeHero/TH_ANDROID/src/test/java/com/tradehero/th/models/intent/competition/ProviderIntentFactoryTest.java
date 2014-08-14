package com.tradehero.th.models.intent.competition;

import android.content.Intent;
import android.net.Uri;
import com.tradehero.THRobolectricTestRunner;
import com.tradehero.th.models.intent.THIntent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(THRobolectricTestRunner.class)
public class ProviderIntentFactoryTest
{
    @Before public void setUp()
    {
        THIntent.context = Robolectric.getShadowApplication().getApplicationContext();
    }

    @After public void tearDown()
    {
        THIntent.context = null;
    }

    @Test public void createsPageIntent()
    {
        ProviderIntentFactory factory = new ProviderIntentFactory();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tradehero://providers/456/pages/%252Fcompetitionpages%252Frules%253FproviderId%253D789%2526userId%253D234"));
        ProviderIntent pageIntent = factory.create(intent);

        assertTrue(pageIntent instanceof ProviderPageIntent);
        assertEquals("/competitionpages/rules?providerId=789&userId=234", ((ProviderPageIntent) pageIntent).getForwardUriPath());
    }
}

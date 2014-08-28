package com.tradehero.th.api.competition;

import com.tradehero.THRobolectricTestRunner;
import com.tradehero.th.api.users.CurrentUserId;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(THRobolectricTestRunner.class)
public class ProviderUtilTest
{
    private static final String TEST_URL = "http://www.tradehero.mobi";

    @Inject ProviderUtil providerUtil;
    @Inject CurrentUserId currentUserId;

    @After public void tearDown()
    {
        currentUserId.delete();
    }

    @Test public void shouldAppendToUrlCorrectly()
    {
        assertThat(providerUtil.appendToUrl(TEST_URL, "?test=1")).isEqualTo(TEST_URL + "?test=1");
        assertThat(providerUtil.appendToUrl(TEST_URL, "test=1")).isEqualTo(TEST_URL + "?test=1");
    }

    @Test public void shoudAppendUserIdCorrectly()
    {
        currentUserId.set(10);
        assertThat(providerUtil.appendUserId(TEST_URL, '&')).isEqualTo(TEST_URL + "?&userId=10");
        assertThat(providerUtil.appendUserId(TEST_URL, '?')).isEqualTo(TEST_URL + "?userId=10");
    }

    @Test public void shoudAppendProviderIdCorrectly()
    {
        ProviderId providerId = new ProviderId(23);
        assertThat(providerUtil.appendProviderId(TEST_URL, '&', providerId)).isEqualTo(TEST_URL + "?&providerId=23");
        assertThat(providerUtil.appendProviderId(TEST_URL, '?', providerId)).isEqualTo(TEST_URL + "?providerId=23");
    }
}
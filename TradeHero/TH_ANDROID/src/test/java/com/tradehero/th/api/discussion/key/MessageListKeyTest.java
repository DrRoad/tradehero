package com.tradehero.th.api.discussion.key;

import com.tradehero.THRobolectricTestRunner;
import com.tradehero.th.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(THRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MessageListKeyTest extends MessageListKeyTestBase
{
    @Test public void equalItself()
    {
        assertTrue(key1_1().equals(key1_1()));
        assertTrue(key1_2().equals(key1_2()));
        assertTrue(key2_1().equals(key2_1()));
        assertTrue(key2_2().equals(key2_2()));
    }

    @Test public void notEqualsIfDifferentPage()
    {
        assertFalse(key1_1().equals(key2_1()));
        assertFalse(key2_1().equals(key1_1()));

        assertFalse(key1_2().equals(key2_2()));
        assertFalse(key2_2().equals(key1_2()));
    }

    @Test public void notEqualsIfDifferentPerPage()
    {
        assertFalse(key1_1().equals(key1_2()));
        assertFalse(key1_2().equals(key1_1()));

        assertFalse(key2_1().equals(key2_2()));
        assertFalse(key2_2().equals(key2_1()));
    }
}

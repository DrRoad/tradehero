package com.ayondo.academy.api.leaderboard.key;

import com.ayondo.academyRobolectricTestRunner;
import com.ayondo.academy.BuildConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(THRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SortedPerPagedLeaderboardKeyTest extends SortedPerPagedLeaderboardKeyTestBase
{
    @Before public void setUp()
    {
    }

    @After public void tearDown()
    {
    }

    @Test public void testEqualsItself()
    {
        assertTrue(getSort7Per5Paged3Key1().equals(getSort7Per5Paged3Key1()));
        assertEquals(getSort7Per5Paged3Key1(), getSort7Per5Paged3Key1());
    }

    @Test public void testClassMakesADifference()
    {
        assertFalse(getKey1().equals(getSort7Per5Paged3Key1()));
        assertFalse(getSort7Per5Paged3Key1().equals(getKey1()));

        assertFalse(getPaged3Key1().equals(getSort7Per5Paged3Key1()));
        assertFalse(getSort7Per5Paged3Key1().equals(getPaged3Key1()));

        assertFalse(getPer5Paged3Key1().equals(getSort7Per5Paged3Key1()));
        assertFalse(getSort7Per5Paged3Key1().equals(getPer5Paged3Key1()));
    }

    @Test public void testKeyMakesADifference()
    {
        assertFalse(getSort7Per5Paged3Key1().equals(getSort7Per5Paged3Key2()));
        assertFalse(getSort7Per5Paged3Key2().equals(getSort7Per5Paged3Key1()));
    }

    @Test public void testPageMakesADifference()
    {
        assertFalse(getSort7Per5Paged3Key1().equals(getSort7Per5Paged4Key1()));
        assertFalse(getSort7Per5Paged4Key1().equals(getSort7Per5Paged3Key1()));
    }

    @Test public void testPerMakesADifference()
    {
        assertFalse(getSort7Per5Paged3Key1().equals(getSort7Per6Paged3Key1()));
        assertFalse(getSort7Per6Paged3Key1().equals(getSort7Per5Paged3Key1()));
    }

    @Test public void testSortMakesADifference()
    {
        assertFalse(getSort7Per5Paged3Key1().equals(getSort8Per5Paged3Key1()));
        assertFalse(getSort8Per5Paged3Key1().equals(getSort7Per5Paged3Key1()));
    }
}

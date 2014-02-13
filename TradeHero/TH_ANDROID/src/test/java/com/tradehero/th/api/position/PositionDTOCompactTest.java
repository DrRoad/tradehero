package com.tradehero.th.api.position;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

/**
 * Created by xavier on 2/13/14.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PositionDTOCompactTest extends BasePositionDTOCompactTest
{
    public static final String TAG = PositionDTOCompactTest.class.getSimpleName();

    @Before public void setUp()
    {
    }

    @After public void tearDown()
    {
    }

    @Test public void testCanCopyFieldsWithProperClass()
    {
        PositionDTOCompact first = new PositionDTOCompact();
        first.id = 1;
        first.averagePriceRefCcy = 2.0d;
        first.portfolioId = 3;
        first.shares = 4;
        PositionDTOCompact second = new PositionDTOCompact(first, PositionDTOCompact.class);
        assertTrue(haveSameFields(first, second));
    }
}

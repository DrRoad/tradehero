package com.tradehero.th.api.position;

import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PositionDTOTest extends BasePositionDTOTest
{
    public static final String TAG = PositionDTOTest.class.getSimpleName();

    @Before public void setUp()
    {
    }

    @After public void tearDown()
    {
    }

    @Test public void testCanCopyFieldsWithProperClass()
    {
        PositionDTO first = new PositionDTO();
        first.id = 1;
        first.averagePriceRefCcy = 2.0d;
        first.portfolioId = 3;
        first.shares = 4;
        first.userId = 5;
        first.securityId = 6;
        first.realizedPLRefCcy = 7d;
        first.unrealizedPLRefCcy = 8d;
        first.marketValueRefCcy = 9d;
        first.earliestTradeUtc = new Date(578573456);
        first.latestTradeUtc = new Date(32488945);
        first.sumInvestedAmountRefCcy = 10d;
        first.totalTransactionCostRefCcy = 11d;
        first.aggregateCount = 12;
        PositionDTO second = new PositionDTO(first, PositionDTO.class);
        assertTrue(haveSameFields(first, second));
    }
}

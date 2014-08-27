package com.tradehero.th.api.watchlist;

import com.tradehero.th.api.position.BasePositionDTOTest;
import com.tradehero.th.api.security.BaseSecurityCompactDTOTest;

abstract public class BaseWatchlistPositionDTOTest extends BasePositionDTOTest
{
    protected BaseSecurityCompactDTOTest securityCompactDTOTest;

    public void setUp()
    {
        securityCompactDTOTest = new BaseSecurityCompactDTOTest(){};
    }

    public boolean haveSameFields(WatchlistPositionDTO left, WatchlistPositionDTO right)
    {
        boolean have = super.haveSameFields(left, right);
        have &= left.watchlistPriceRefCcy == null ? right.watchlistPriceRefCcy == null : left.watchlistPriceRefCcy.equals(right.watchlistPriceRefCcy);
        if (left.securityDTO != null && right.securityDTO != null)
        {
            have &= securityCompactDTOTest.haveSameFields(left.securityDTO, right.securityDTO);
        }
        else
        {
            have &= (left.securityDTO == null && right.securityDTO == null);
        }
        return have;
    }
}

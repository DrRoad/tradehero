package com.androidth.general.api.portfolio;

import java.io.Serializable;
import java.util.Comparator;

public class OwnedPortfolioIdDisplayComparator implements Comparator<OwnedPortfolioId>, Serializable
{
    @Override public int compare(OwnedPortfolioId lhs, OwnedPortfolioId rhs)
    {
        if (lhs == null)
        {
            return rhs == null ? 0 : 1;
        }
        else if (rhs == null)
        {
            return -1;
        }
        else if (lhs.userId == null)
        {
            return rhs.userId == null ? 0 : 1;
        }
        else if (rhs.userId == null)
        {
            return -1;
        }

        int userIdComp = lhs.userId.compareTo(rhs.userId);
        if (userIdComp != 0)
        {
            return userIdComp;
        }

        if (lhs.portfolioId == null)
        {
            return rhs.portfolioId == null ? 0 : 1;
        }
        else if (rhs.portfolioId == null)
        {
            return -1;
        }

        return lhs.portfolioId.compareTo(rhs.portfolioId);
    }
}

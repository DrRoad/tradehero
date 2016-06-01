package com.ayondo.academy.billing.googleplay;

import com.tradehero.common.billing.googleplay.BaseIABProductDetailComparator;
import com.ayondo.academy.billing.ProductIdentifierDomain;

class THIABProductDetailComparator<THIABProductDetailType extends THIABProductDetail>
        extends BaseIABProductDetailComparator<THIABProductDetailType>
{
    @Override public int compare(THIABProductDetailType lhs, THIABProductDetailType rhs)
    {
        int parentCompare = super.compare(lhs, rhs);
        if (parentCompare == 0)
        {
            return parentCompare;
        }
        ProductIdentifierDomain ldom = lhs.domain;
        ProductIdentifierDomain rdom = rhs.domain;

        if (ldom == null)
        {
            return rdom == null ? 0 : 1;
        }

        if (rdom == null)
        {
            return -1;
        }

        int domainCompare = rdom.compareTo(ldom);
        if (domainCompare != 0)
        {
            return domainCompare;
        }

        return - parentCompare;
    }
}

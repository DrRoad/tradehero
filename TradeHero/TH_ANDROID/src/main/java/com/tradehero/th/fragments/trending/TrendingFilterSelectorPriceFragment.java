package com.tradehero.th.fragments.trending;

import com.tradehero.th.R;
import com.tradehero.th.api.security.TrendingPriceSecurityListType;
import com.tradehero.th.api.security.TrendingSecurityListType;

/** Created with IntelliJ IDEA. User: xavier Date: 10/18/13 Time: 10:34 AM To change this template use File | Settings | File Templates. */
public class TrendingFilterSelectorPriceFragment extends TrendingFilterSelectorFragment
{
    public static final int POSITION_IN_PAGER  = 2;

    @Override int getTitleResId()
    {
        return R.string.trending_filter_price_title;
    }

    @Override int getTitleLeftDrawableResId()
    {
        return R.drawable.ic_trending_price;
    }

    @Override int getDescriptionResId()
    {
        return R.string.trending_filter_price_description;
    }

    @Override public TrendingSecurityListType getTrendingSecurityListType(String exchangeName, Integer page, Integer perPage)
    {
        return sGetTrendingSecurityListType(exchangeName, page, perPage);
    }

    public static TrendingSecurityListType sGetTrendingSecurityListType(String exchangeName, Integer page, Integer perPage)
    {
        return new TrendingPriceSecurityListType(exchangeName, page, perPage);
    }
}

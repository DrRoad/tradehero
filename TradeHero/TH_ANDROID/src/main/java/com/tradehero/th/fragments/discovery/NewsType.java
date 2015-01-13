package com.tradehero.th.fragments.discovery;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.tradehero.th.R;
import com.tradehero.th.utils.metrics.AnalyticsConstants;

enum NewsType
{
    MotleyFool(R.string.motley_fool, R.layout.news_carousel_motleyfool, AnalyticsConstants.NewsMotleyFool),
    Region(R.string.regional_news, R.layout.news_carousel_regional, AnalyticsConstants.NewsRegional),
    Global(R.string.global_news, R.layout.news_carousel_global, AnalyticsConstants.NewsGlobal);

    @StringRes public final int titleResourceId;
    @LayoutRes public final int titleViewResourceId;
    @NonNull public final String analyticsName;

    NewsType(@StringRes int titleResourceId, @LayoutRes int titleViewResourceId, @NonNull String analyticsName)
    {
        this.titleResourceId = titleResourceId;
        this.titleViewResourceId = titleViewResourceId;
        this.analyticsName = analyticsName;
    }
}

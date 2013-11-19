package com.tradehero.th.fragments.trending;

import android.view.View;
import com.tradehero.th.R;

/** Created with IntelliJ IDEA. User: xavier Date: 10/18/13 Time: 10:34 AM To change this template use File | Settings | File Templates. */
public class TrendingFilterSelectorBasicFragment extends TrendingFilterSelectorFragment
{
    public static final int POSITION_IN_PAGER  = 0;

    @Override int getTitleResId()
    {
        return R.string.trending_filter_basic_title;
    }

    @Override int getTitleLeftDrawableResId()
    {
        return 0;
    }

    @Override int getDescriptionResId()
    {
        return R.string.trending_filter_basic_description;
    }

    @Override public void displayPreviousButton()
    {
        super.displayPreviousButton();
        if (mPrevious != null)
        {
            mPrevious.setEnabled(false);
        }
    }
}

package com.tradehero.th.fragments.security;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.fragments.discussion.stock.SecurityDiscussionFragment;
import com.tradehero.th.fragments.news.NewsHeadlineFragment;
import com.tradehero.th.models.chart.ChartTimeSpan;
import timber.log.Timber;

public class BuySellBottomStockPagerAdapter extends FragmentPagerAdapter
{
    public static final int FRAGMENT_ID_CHART = 0;
    public static final int FRAGMENT_ID_DISCUSS = 1;
    public static final int FRAGMENT_ID_NEWS = 2;
    private SecurityId securityId;
    private Context context;

    //<editor-fold desc="Constructors">
    public BuySellBottomStockPagerAdapter(Context context, FragmentManager fragmentManager)
    {
        super(fragmentManager);
        this.context = context;
    }
    //</editor-fold>

    public static ChartTimeSpan getDefaultChartTimeSpan()
    {
        return new ChartTimeSpan(ChartTimeSpan.MONTH_3);
    }

    public void linkWith(SecurityId securityId)
    {
        this.securityId = securityId;
    }

    @Override public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case FRAGMENT_ID_CHART:
                return context.getString(R.string.security_info);
            case FRAGMENT_ID_DISCUSS:
                return context.getString(R.string.security_discussions);
            case FRAGMENT_ID_NEWS:
                return context.getString(R.string.security_news);
        }
        return super.getPageTitle(position);
    }

    @Override public int getCount()
    {
        if (securityId == null)
        {
            return 0;
        }
        else
        {
            return 3;
        }
    }

    @Override public Fragment getItem(int position)
    {
        Fragment fragment;
        Bundle args = new Bundle();

        switch (position)
        {
            case FRAGMENT_ID_CHART:
                fragment = new ChartFragment();
                populateForChartFragment(args);
                break;
            case FRAGMENT_ID_DISCUSS:
                fragment = new SecurityDiscussionFragment();
                populateForSecurityDiscussionFragment(args);
                break;
            case FRAGMENT_ID_NEWS:
                fragment = new NewsHeadlineFragment();
                populateForNewsHeadlineFragment(args);
                break;

            default:
                Timber.w("Not supported index " + position);
                throw new UnsupportedOperationException("Not implemented");
        }

        fragment.setArguments(args);
        fragment.setRetainInstance(false);
        return fragment;
    }

    private void populateForChartFragment(Bundle args)
    {
        ChartFragment.putSecurityId(args, securityId);
        args.putInt(ChartFragment.BUNDLE_KEY_TIME_SPAN_BUTTON_SET_VISIBILITY, View.VISIBLE);
        args.putLong(ChartFragment.BUNDLE_KEY_TIME_SPAN_SECONDS_LONG, getDefaultChartTimeSpan().duration);
    }

    private void populateForSecurityDiscussionFragment(Bundle args)
    {
        SecurityDiscussionFragment.putSecurityId(args, securityId);
    }

    private void populateForNewsHeadlineFragment(Bundle args)
    {
        NewsHeadlineFragment.putSecurityId(args, securityId);
    }
}

package com.tradehero.th.fragments.security;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import com.tradehero.common.utils.THLog;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.WarrantDTO;
import com.tradehero.th.fragments.discussion.stock.SecurityDiscussionFragment;
import com.tradehero.th.fragments.news.NewsHeadlineFragment;
import com.tradehero.th.models.chart.ChartTimeSpan;

/** Created with IntelliJ IDEA. User: xavier Date: 10/3/13 Time: 12:42 PM To change this template use File | Settings | File Templates. */
public class BuySellBottomStockPagerAdapter extends FragmentStatePagerAdapter
{
    public static final String TAG = BuySellBottomStockPagerAdapter.class.getSimpleName();

    private final Context context;
    private SecurityCompactDTO securityCompactDTO;
    private ProviderId providerId;

    //<editor-fold desc="Constructors">
    public BuySellBottomStockPagerAdapter(Context context, FragmentManager fragmentManager)
    {
        super(fragmentManager);
        this.context = context;
    }
    //</editor-fold>

    public void linkWith(ProviderId providerId)
    {
        this.providerId = providerId;
    }

    public void linkWith(SecurityCompactDTO securityCompactDTO)
    {
        this.securityCompactDTO = securityCompactDTO;
    }

    public SecurityCompactDTO getSecurityCompactDTO()
    {
        return securityCompactDTO;
    }

    @Override public int getCount()
    {
        if (securityCompactDTO == null)
        {
            return 0;
        }
        else if (securityCompactDTO instanceof WarrantDTO)
        {
            return 3;//4 hide warrant info temp by alex
        }
        else
        {
            return 3;
        }
    }

    /**
     * Equity: Chart / StockInfo / News
     * Warrant: WarrantInfo / Chart / StockInfo / News
     *
     * @param position
     * @return
     */

    @Override public Fragment getItem(int position)
    {
        Fragment fragment;
        Bundle args = new Bundle();
        args.putBundle(SecurityId.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityCompactDTO.getSecurityId().getArgs());

        if (false && securityCompactDTO instanceof WarrantDTO && position == 0)//hide Warrant Info temp
        {
            fragment = new WarrantInfoValueFragment();
            populateForWarrantInfoFragment(args);
        }
        else
        {
            if (false && securityCompactDTO instanceof WarrantDTO)//hide warrant info temp
            {
                position--;
            }

            switch(position)
            {
                case 0:
                    fragment = new ChartFragment();
                    populateForChartFragment(args);
                    break;
                case 1:
                    fragment = new SecurityDiscussionFragment();
                    break;
                case 2:
                    fragment = new NewsHeadlineFragment();
                    break;

                default:
                    THLog.w(TAG, "Not supported index " + position);
                    throw new UnsupportedOperationException("Not implemented");
            }
        }

        fragment.setArguments(args);
        fragment.setRetainInstance(false);
        return fragment;
    }

    private void populateForWarrantInfoFragment(Bundle args)
    {
        if (providerId != null)
        {
            args.putBundle(WarrantInfoValueFragment.BUNDLE_KEY_PROVIDER_ID_KEY, providerId.getArgs());
        }
    }

    private void populateForChartFragment(Bundle args)
    {
        //args.putInt(ChartFragment.BUNDLE_KEY_TIME_SPAN_BUTTON_SET_VISIBILITY, View.GONE);
        args.putInt(ChartFragment.BUNDLE_KEY_TIME_SPAN_BUTTON_SET_VISIBILITY, View.VISIBLE);
        args.putLong(ChartFragment.BUNDLE_KEY_TIME_SPAN_SECONDS_LONG, ChartTimeSpan.MONTH_3);
    }

    @Override public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }
}

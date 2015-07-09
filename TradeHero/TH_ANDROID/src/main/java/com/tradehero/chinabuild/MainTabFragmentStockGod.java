package com.tradehero.chinabuild;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.chinabuild.fragment.AbsBaseFragment;
import com.tradehero.chinabuild.fragment.leaderboard.StockGodListBaseFragment;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.models.leaderboard.key.LeaderboardDefKeyKnowledge;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.MethodEvent;
import com.viewpagerindicator.TabPageIndicator;
import javax.inject.Inject;

public class MainTabFragmentStockGod extends AbsBaseFragment implements ViewPager.OnPageChangeListener
{
    @InjectView(R.id.pager) ViewPager pager;
    @InjectView(R.id.indicator) TabPageIndicator indicator;
    FragmentPagerAdapter adapter;
    @Inject Analytics analytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.main_tab_fragment_stockgod_layout, container, false);
        ButterKnife.inject(this, view);
        initView();
        return view;
    }

    private void initView()
    {
        adapter = new CustomAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(5);
        indicator.setViewPager(pager);
        indicator.setOutsideListener(this);
    }

    private static final String[] CONTENT = new String[] {" 推荐榜 ", " 人气榜 ", " 土豪榜 "};

    class CustomAdapter extends FragmentPagerAdapter
    {
        public CustomAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            StockGodListBaseFragment fragment;
            Bundle args = new Bundle();
            switch (position)
            {
                case 0:
                    fragment = new StockGodListBaseFragment();
                    args.putInt(StockGodListBaseFragment.BUNLDE_LEADERBOARD_KEY, LeaderboardDefKeyKnowledge.DAYS_ROI);
                    fragment.setArguments(args);

                    return fragment;
                case 1:
                    fragment = new StockGodListBaseFragment();
                    args.putInt(StockGodListBaseFragment.BUNLDE_LEADERBOARD_KEY, LeaderboardDefKeyKnowledge.POPULAR);
                    fragment.setArguments(args);

                    return fragment;
                case 2:
                    fragment = new StockGodListBaseFragment();
                    args.putInt(StockGodListBaseFragment.BUNLDE_LEADERBOARD_KEY, LeaderboardDefKeyKnowledge.WEALTH);
                    fragment.setArguments(args);

                    return fragment;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount()
        {
            return CONTENT.length;
        }
    }


    @Override public void onPageScrolled(int i, float v, int i2)
    {
    }

    @Override public void onPageSelected(int i)
    {
        if (i == 0)
        {
            analytics.addEvent(new MethodEvent(AnalyticsConstants.CHINA_BUILD_BUTTON_CLICKED, AnalyticsConstants.BUTTON_STOCK_ROI));
        }
        else if (i == 1)
        {
            analytics.addEvent(new MethodEvent(AnalyticsConstants.CHINA_BUILD_BUTTON_CLICKED, AnalyticsConstants.BUTTON_STOCK_HOT));
        }
        else if (i == 2)
        {
            analytics.addEvent(new MethodEvent(AnalyticsConstants.CHINA_BUILD_BUTTON_CLICKED, AnalyticsConstants.BUTTON_STOCK_WEALTH));
        }
    }

    @Override public void onPageScrollStateChanged(int i)
    {
    }

}

package com.tradehero.th.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.tradehero.th.R;
import com.tradehero.th.fragments.CommunityScreenFragment;
import com.tradehero.th.fragments.HomeScreenFragment;
import com.tradehero.th.fragments.PortfolioScreenFragment;
import com.tradehero.th.fragments.StoreScreenFragment;
import com.tradehero.th.fragments.TrendingFragment;
import android.view.View;

public class DashboardActivity extends SherlockFragmentActivity
{
    private static final String BUNDLE_KEY = "key";
    private FragmentTabHost mTabHost;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_with_bottom_bar);

        initiateViews();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    private void initiateViews()
    {
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        addNewTab(getString(R.string.trending), R.drawable.trending_selector, TrendingFragment.class);
        addNewTab(getString(R.string.community), R.drawable.community_selector, CommunityScreenFragment.class);
        addNewTab(getString(R.string.home), R.drawable.home_selector, HomeScreenFragment.class);
        addNewTab(getString(R.string.portfolio), R.drawable.pofilio_selector, PortfolioScreenFragment.class);
        addNewTab(getString(R.string.store), R.drawable.store_selector, StoreScreenFragment.class);

        mTabHost.setCurrentTabByTag(getString(R.string.home));
    }

    private void addNewTab(String tabTag, int tabIndicatorDrawableId, Class<?> fragmentClass)
    {
        Bundle b = new Bundle();
        b.putString(BUNDLE_KEY, tabTag);
        mTabHost.addTab(mTabHost
                .newTabSpec(tabTag)
                .setIndicator("", getResources().getDrawable(tabIndicatorDrawableId)),
                fragmentClass, b);
    }

    public void showTabs(boolean value)
    {
        if (mTabHost != null)
        {
            mTabHost.getTabWidget().setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }
}

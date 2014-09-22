package com.tradehero.th.fragments.discovery;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuInflater;
import com.tradehero.th.R;
import com.tradehero.th.fragments.base.DashboardFragment;
import javax.inject.Inject;

public class DiscoveryMainFragment extends DashboardFragment
        implements ActionBar.TabListener
{
    @Inject Context doNotRemoveOtherwiseFails; // Do not remove otherwise fails
    private DiscoverySessionPagerAdapter mDiscoverySessionPagerAdapter;
    @InjectView(R.id.pager) ViewPager mViewPager;
    private int selectedTabIndex;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.discovery_main_fragment, container, false);
        initView(view);
        return view;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setActionBarTitle(getString(R.string.discovery));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    private void initView(View view)
    {
        ButterKnife.inject(this, view);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            mDiscoverySessionPagerAdapter = new DiscoverySessionPagerAdapter(((Fragment)this).getChildFragmentManager());
            setupTabs(actionBar);
            setupPager(actionBar);
        }
    }

    private void setupTabs(ActionBar actionBar)
    {
        actionBar.removeAllTabs();
        for (int i = 0; i < mDiscoverySessionPagerAdapter.getCount(); ++i)
        {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mDiscoverySessionPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (selectedTabIndex < 0)
        {
            selectedTabIndex = actionBar.getTabCount() / 2;
            selectTabAtPosition(actionBar, selectedTabIndex);
        }
    }

    private void selectTabAtPosition(ActionBar actionBar, int position)
    {
        selectedTabIndex = position;
        actionBar.selectTab(actionBar.getTabAt(position));
    }

    private void setupPager(final ActionBar actionBar)
    {
        mViewPager.setAdapter(mDiscoverySessionPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override public void onPageSelected(int position)
            {
                if (position < actionBar.getTabCount())
                {
                    selectTabAtPosition(actionBar, position);
                }
            }
        });
    }

    private class DiscoverySessionPagerAdapter extends FragmentPagerAdapter
    {
        public DiscoverySessionPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override public Fragment getItem(int position)
        {
            DiscoveryTabType tabType = DiscoveryTabType.values()[position];
            switch (tabType)
            {
                default: // special case should be placed above
                    return Fragment.instantiate(getActivity(), tabType.fragmentClass.getName());
            }
        }

        @Override public CharSequence getPageTitle(int position)
        {
            return getString(DiscoveryTabType.values()[position].titleStringResId);
        }

        @Override public int getCount()
        {
            return DiscoveryTabType.values().length;
        }
    }

    //region TabListener
    @Override public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
    {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
    {
    }

    @Override public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
    {
    }
    //endregion
}

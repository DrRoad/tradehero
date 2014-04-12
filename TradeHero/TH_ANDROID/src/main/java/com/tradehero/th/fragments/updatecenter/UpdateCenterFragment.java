package com.tradehero.th.fragments.updatecenter;

import android.os.Bundle;
import android.support.v4.r11.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import com.tradehero.th.R;
import com.tradehero.th.fragments.base.BaseFragment;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * Created by thonguyen on 3/4/14.
 */
public class UpdateCenterFragment extends BaseFragment /*DashboardFragment*/
        implements OnTitleNumberChangeListener
{
    public static final String KEY_PAGE = "page";

    private FragmentTabHost mTabHost;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return addTabs();
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //addTabs();
        //TODO
        changeTabTitleNumber(UpdateCenterTabType.Messages, 80);
    }

    @Override public void onDestroyView()
    {
        //clearTabs();

        super.onDestroyView();
        Timber.d("onDestroyView");
    }

    @Override public void onDestroy()
    {
        super.onDestroy();
        Timber.d("onDestroy");
    }

    private View addTabs()
    {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), 11111);

        Bundle args = getArguments();
        if (args == null)
        {
            args = new Bundle();
        }

        UpdateCenterTabType[] types = UpdateCenterTabType.values();
        for (UpdateCenterTabType tabTitle : types)
        {
            args = new Bundle(args);
            args.putInt(KEY_PAGE, tabTitle.pageIndex);

            TitleTabView tabView = (TitleTabView) LayoutInflater.from(getActivity())
                    .inflate(R.layout.message_tab_item, mTabHost.getTabWidget(), false);
            String title = getString(tabTitle.titleRes, 0);
            tabView.setTitle(title);

            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(title).setIndicator(tabView);
            mTabHost.addTab(tabSpec, tabTitle.tabClass, args);
        }

        return mTabHost;
    }

    private void changeTabTitleNumber(UpdateCenterTabType tabType, int number)
    {
        TitleTabView tabView = (TitleTabView) mTabHost.getTabWidget().getChildAt(tabType.ordinal());
        tabView.setTitleNumber(number);
    }

    @Override public void onTitleNumberChanged(UpdateCenterTabType tabType, int number)
    {
        changeTabTitleNumber(tabType, number);
    }
}

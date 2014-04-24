package com.tradehero.th.fragments.social.hero;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tradehero.common.billing.ProductPurchase;
import com.tradehero.common.billing.exception.BillingException;
import com.tradehero.th.api.social.HeroIdExtWrapper;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.THPurchaseReporter;
import com.tradehero.th.billing.request.THUIBillingRequest;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.models.user.FollowUserAssistant;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class HeroManagerFragment extends /**DashboardFragment*/ BasePurchaseManagerFragment implements OnHeroesLoadedListener
{
    public static final String TAG = HeroManagerFragment.class.getSimpleName();

    static final String KEY_PAGE = "KEY_PAGE";
    static final String KEY_ID = "KEY_ID";
    static final int FRAGMENT_LAYOUT_ID = 9999;
    /**
     * We are showing the heroes of this follower
     */
    public static final String BUNDLE_KEY_FOLLOWER_ID =
            HeroManagerFragment.class.getName() + ".followerId";
    /** categories of hero:premium,free,all */
    private HeroTypeExt[] heroTypes;
    private int selectedId = -1;

    @Override protected FollowUserAssistant.OnUserFollowedListener createUserFollowedListener()
    {
        return new HeroManagerUserFollowedListener();
    }

    FragmentTabHost mTabHost;
    List<TabHost.TabSpec> tabSpecList;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        heroTypes = HeroTypeExt.getSortedList();
        Timber.d("onCreate");
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setTitle("Heros");
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                //localyticsSession.tagEvent(LocalyticsConstants.Leaderboard_Back);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        Timber.d("onCreateView");
        return addTabs();
    }

    @Override protected void initViews(View view)
    {
    }

    private void handleBuyMoreClicked()
    {
        showProductDetailListForPurchase(ProductIdentifierDomain.DOMAIN_FOLLOW_CREDITS);
    }

    @Override public THUIBillingRequest getShowProductDetailRequest(ProductIdentifierDomain domain)
    {
        THUIBillingRequest request = super.getShowProductDetailRequest(domain);
        request.purchaseReportedListener = new THPurchaseReporter.OnPurchaseReportedListener()
        {
            @Override public void onPurchaseReported(int requestCode, ProductPurchase reportedPurchase, UserProfileDTO updatedUserPortfolio)
            {
                //display(updatedUserPortfolio);
            }

            @Override public void onPurchaseReportFailed(int requestCode, ProductPurchase reportedPurchase, BillingException error)
            {
                // Anything to report?
            }
        };
        return request;
    }

    private View addTabs()
    {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), FRAGMENT_LAYOUT_ID);
        mTabHost.setOnTabChangedListener(new MyOnTouchListener());

        ActionBar.Tab lastSavedTab = null;
        int lastSelectedId = selectedId;
        HeroTypeExt[] types = heroTypes;
        tabSpecList = new ArrayList<>(types.length);
        Bundle args = getArguments();
        if (args == null)
        {
            args = new Bundle();
        }
        for (HeroTypeExt type : types)
        {
            args = new Bundle(args);
            args.putInt(KEY_PAGE, type.pageIndex);
            args.putInt(KEY_ID, type.heroType.typeId);

            String title = MessageFormat.format(getSherlockActivity().getString(type.titleRes), 0);

            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(title).setIndicator(title);
            tabSpecList.add(tabSpec);
            mTabHost.addTab(tabSpec,
                    type.fragmentClass, args);
        }

        return mTabHost;
    }

    @Override public boolean isTabBarVisible()
    {
        return false;
    }

    class MyOnTouchListener implements TabHost.OnTabChangeListener
    {
        @Override public void onTabChanged(String tabId)
        {
            Timber.d("onTabChanged tabId:%s",tabId);
            //getChildFragmentManager().executePendingTransactions();
            Fragment fragment = getFragmentManager().findFragmentByTag(tabId);
            Fragment f = getChildFragmentManager().findFragmentByTag(tabId);
            Timber.d("activity fragment:%s,child fragment:%s",fragment,f);
        }
    }

    /**
     * change the number of tab
     */
    private void changeTabTitle(int page, int number)
    {
        TabHost.TabSpec tabSpec = tabSpecList.get(page);
        HeroTypeExt heroTypeExt = HeroTypeExt.fromIndex(heroTypes,page);
        int titleRes = heroTypeExt.titleRes;
        String title = MessageFormat.format(getSherlockActivity().getString(titleRes), number);
        tabSpec.setIndicator(title);

        TextView tv = (TextView)mTabHost.getTabWidget().getChildAt(page).findViewById(android.R.id.title);
        tv.setText(title);

    }

    private void changeTabTitle(int number1, int number2, int number3)
    {
        changeTabTitle(0, number1);
        changeTabTitle(1, number2);
        changeTabTitle(2, number3);
    }

    @Override public void onDestroyView()
    {
        super.onDestroyView();
        //saveSelectedTab();
        //clearTabs();
        Timber.d("onDestroyView");
    }

    @Override public void onDestroy()
    {
        super.onDestroy();
        Timber.d("onDestroy");
    }

    @Override public void onDetach()
    {
        super.onDetach();
        Timber.d("onDetach");
    }

    @Override public void onHerosLoaded(int page, HeroIdExtWrapper value)
    {
        if (!isDetached())
        {
            changeTabTitle(0, value.herosCountGetPaid);
            changeTabTitle(1, value.herosCountNotGetPaid);
            changeTabTitle(2, (value.herosCountGetPaid + value.herosCountNotGetPaid));
        }
    }

    private void handleFollowSuccess(UserProfileDTO currentUserProfileDTO)
    {
        // TODO
    }

    protected class HeroManagerUserFollowedListener extends BasePurchaseManagerUserFollowedListener
    {
        @Override public void onUserFollowSuccess(UserBaseKey userFollowed, UserProfileDTO currentUserProfileDTO)
        {
            super.onUserFollowSuccess(userFollowed, currentUserProfileDTO);
            handleFollowSuccess(currentUserProfileDTO);
        }
    }
}



package com.tradehero.th.fragments.leaderboard;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.LeaderboardDefDTO;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import javax.inject.Inject;

/** Created with IntelliJ IDEA. User: tho Date: 11/1/13 Time: 6:24 PM Copyright (c) TradeHero */
abstract public class BaseLeaderboardFragment extends BasePurchaseManagerFragment
        implements BaseFragment.TabBarVisibilityInformer
{
    public static final String BUNDLE_KEY_LEADERBOARD_ID = BaseLeaderboardFragment.class.getName() + ".leaderboardId";
    public static final String BUNDLE_KEY_LEADERBOARD_DEF_TITLE = BaseLeaderboardFragment.class.getName() + ".leaderboardDefTitle";
    public static final String BUNDLE_KEY_LEADERBOARD_DEF_DESC = BaseLeaderboardFragment.class.getName() + ".leaderboardDefDesc";
    public static final String BUNDLE_KEY_CURRENT_SORT_TYPE = BaseLeaderboardFragment.class.getName() + ".currentSortType";
    public static final String BUNDLE_KEY_SORT_OPTION_FLAGS = BaseLeaderboardFragment.class.getName() + ".sortOptionFlags";

    @Inject protected LeaderboardSortHelper leaderboardSortHelper;

    private LeaderboardSortType currentSortType;
    private SortTypeChangedListener sortTypeChangeListener;
    private SubMenu sortSubMenu;

    protected void pushLeaderboardListViewFragment(LeaderboardDefDTO dto)
    {
        Bundle bundle = new Bundle(getArguments());
        bundle.putInt(BUNDLE_KEY_LEADERBOARD_ID, dto.id);
        bundle.putString(BUNDLE_KEY_LEADERBOARD_DEF_TITLE, dto.name);
        bundle.putInt(BUNDLE_KEY_CURRENT_SORT_TYPE, getCurrentSortType() != null ? getCurrentSortType().getFlag() : dto.getDefaultSortType().getFlag());
        bundle.putString(BUNDLE_KEY_LEADERBOARD_DEF_DESC, dto.desc);
        bundle.putInt(BUNDLE_KEY_SORT_OPTION_FLAGS, dto.getSortOptionFlags());

        switch (dto.id)
        {
            case LeaderboardDefDTO.LEADERBOARD_FRIEND_ID:
                getNavigator().pushFragment(FriendLeaderboardMarkUserListViewFragment.class, bundle);
                break;

            default:
                getNavigator().pushFragment(LeaderboardMarkUserListViewFragment.class, bundle);
                break;
        }
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        createSortSubMenu(menu);
        initSortTypeFromArguments();

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(getMenuResource(), menu);

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);

        Bundle args = getArguments();
        if (args != null)
        {
            String title = args.getString(BUNDLE_KEY_LEADERBOARD_DEF_TITLE);
            actionBar.setTitle(title == null ? "" : title);
        }
    }

    protected int getMenuResource()
    {
        return R.menu.leaderboard_menu;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        LeaderboardSortType selectedSortType = LeaderboardSortType.byFlag(item.getItemId());
        if (selectedSortType != null && selectedSortType != currentSortType)
        {
            setCurrentSortType(selectedSortType);
        }
        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>

    //<editor-fold desc="Sort menu stuffs">
    private void createSortSubMenu(Menu menu)
    {
        LeaderboardSortType sortType = getCurrentSortType();
        sortSubMenu = menu.addSubMenu("").setIcon(sortType.getSelectedResourceIcon());
        //leaderboardSortHelper.addSortMenu(sortSubMenu, getSortMenuFlag());
    }

    private int getSortMenuFlag()
    {
        return getArguments().getInt(BUNDLE_KEY_SORT_OPTION_FLAGS);
    }

    protected void setCurrentSortType(LeaderboardSortType selectedSortType)
    {
        if (sortTypeChangeListener != null && currentSortType != selectedSortType)
        {
            sortTypeChangeListener.onSortTypeChange(selectedSortType);
        }
        sortSubMenu.setIcon(selectedSortType.getSelectedResourceIcon());
        currentSortType = selectedSortType;
        onCurrentSortTypeChanged();
    }

    protected LeaderboardSortType getCurrentSortType()
    {
        return currentSortType != null ? currentSortType : LeaderboardSortType.DefaultSortType;
    }

    public void setSortTypeChangeListener(SortTypeChangedListener sortTypeChangeListener)
    {
        this.sortTypeChangeListener = sortTypeChangeListener;
    }

    protected void initSortTypeFromArguments()
    {
        setCurrentSortType(LeaderboardSortType.byFlag(getArguments().getInt(BUNDLE_KEY_CURRENT_SORT_TYPE)));
    }

    protected void onCurrentSortTypeChanged()
    {
        // do nothing
    }

    //</editor-fold>

    @Override public boolean isTabBarVisible()
    {
        return false;
    }
}

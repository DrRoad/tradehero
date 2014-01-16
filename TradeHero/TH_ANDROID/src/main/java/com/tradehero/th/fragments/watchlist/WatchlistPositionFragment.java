package com.tradehero.th.fragments.watchlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;
import com.tradehero.common.milestone.Milestone;
import com.tradehero.common.widget.TwoStateView;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.CurrentUserBaseKeyHolder;
import com.tradehero.th.base.Navigator;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.portfolio.header.PortfolioHeaderFactory;
import com.tradehero.th.fragments.security.WatchlistEditFragment;
import com.tradehero.th.fragments.trending.SearchStockPeopleFragment;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistRetrievedMilestone;
import dagger.Lazy;
import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA. User: tho Date: 1/10/14 Time: 4:11 PM Copyright (c) TradeHero
 */
public class WatchlistPositionFragment extends DashboardFragment
    implements BaseFragment.TabBarVisibilityInformer
{
    private static final String TAG = WatchlistPositionFragment.class.getName();
    private ProgressBar progressBar;

    @Inject protected Lazy<WatchlistPositionCache> watchlistCache;
    @Inject protected Lazy<UserWatchlistPositionCache> userWatchlistCache;
    @Inject protected Lazy<PortfolioHeaderFactory> headerFactory;
    @Inject protected CurrentUserBaseKeyHolder currentUserBaseKeyHolder;

    private SwipeListView watchlistListView;
    private WatchlistPortfolioHeaderView watchlistPortfolioHeaderView;
    private WatchlistAdapter watchListAdapter;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.watchlist_positions_list, container, false);

        initViews(view);
        return view;
    }

    private void initViews(View view)
    {
        if (view != null)
        {
            progressBar = (ProgressBar) view.findViewById(android.R.id.empty);
            watchlistListView = (SwipeListView) view.findViewById(android.R.id.list);
            watchlistListView.post(new Runnable()
            {
                @Override public void run()
                {
                    watchlistListView.setOffsetLeft(watchlistListView.getWidth() -
                            getResources().getDimension(R.dimen.watchlist_item_button_width) * 2);
                }
            });

            // portfolio header
            watchlistPortfolioHeaderView = (WatchlistPortfolioHeaderView) view.findViewById(R.id.watchlist_position_list_header);
        }
    }

    @Override public void onResume()
    {
        super.onResume();

        LocalBroadcastManager.getInstance(this.getActivity())
                .registerReceiver(broadcastReceiver, new IntentFilter(WatchlistItemView.WATCHLIST_ITEM_DELETED));

        // watchlist is not yet retrieved
        if (userWatchlistCache.get().get(currentUserBaseKeyHolder.getCurrentUserBaseKey()) == null)
        {
            WatchlistRetrievedMilestone watchlistRetrievedMilestone = new WatchlistRetrievedMilestone(currentUserBaseKeyHolder.getCurrentUserBaseKey());
            watchlistRetrievedMilestone.setOnCompleteListener(watchlistRetrievedMilestoneListener);

            displayProgress(true);
        }
        else
        {
            display();
        }
    }

    @Override public void onPause()
    {
        super.onPause();

        LocalBroadcastManager.getInstance(this.getActivity())
                .unregisterReceiver(broadcastReceiver);
    }

    //<editor-fold desc="ActionBar Menu Actions">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.position_watchlist_menu, menu);
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setTitle(getString(R.string.watchlist));
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.position_watchlist_add);
        View menuAddWatchlist = menuItem.getActionView().findViewById(R.id.position_watchlist_add_view);
        if (menuAddWatchlist != null)
        {
            menuAddWatchlist.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(SearchStockPeopleFragment.BUNDLE_KEY_CALLER_FRAGMENT, WatchlistPositionFragment.class.getName());
                    getNavigator().pushFragment(SearchStockPeopleFragment.class, bundle);
                }
            });
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>

    private void display()
    {
        displayProgress(false);
        displayHeader();
        displayWatchlist();
    }

    private void displayHeader()
    {
        if (watchlistPortfolioHeaderView != null)
        {
            watchlistPortfolioHeaderView.display(currentUserBaseKeyHolder.getCurrentUserBaseKey());
            watchlistPortfolioHeaderView.setOnStateChangeListener(gainLossModeListener);
        }
    }

    private void displayWatchlist()
    {
        watchListAdapter = createWatchlistAdapter();
        watchListAdapter.setItems(userWatchlistCache.get().get(currentUserBaseKeyHolder.getCurrentUserBaseKey()));
        watchlistListView.setAdapter(watchListAdapter);
        watchlistListView.setSwipeListViewListener(new BaseSwipeListViewListener()
        {
            @Override public void onClickFrontView(int position)
            {
                super.onClickFrontView(position);

                openWatchlistItemEditor(position);
            }

            @Override public void onDismiss(int[] reverseSortedPositions)
            {
                super.onDismiss(reverseSortedPositions);
                if (watchListAdapter != null)
                {
                    watchListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void openWatchlistItemEditor(int position)
    {
        SecurityId securityId = (SecurityId) watchListAdapter.getItem(position);
        Bundle args = new Bundle();
        if (securityId != null)
        {
            args.putBundle(WatchlistEditFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
            if (watchlistCache.get().get(securityId) != null)
            {
                args.putString(WatchlistEditFragment.BUNDLE_KEY_TITLE, getString(R.string.edit_in_watch_list));
            }
        }
        getNavigator().pushFragment(WatchlistEditFragment.class, args, Navigator.PUSH_UP_FROM_BOTTOM);
    }

    private WatchlistAdapter createWatchlistAdapter()
    {
        return new WatchlistAdapter(getActivity(), getActivity().getLayoutInflater(), R.layout.watchlist_item_view);
    }

    private void displayProgress(boolean show)
    {
        if (progressBar != null)
        {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override public boolean isTabBarVisible()
    {
        return false;
    }

    private Milestone.OnCompleteListener watchlistRetrievedMilestoneListener = new Milestone.OnCompleteListener()
    {
        @Override public void onComplete(Milestone milestone)
        {
            display();
        }

        @Override public void onFailed(Milestone milestone, Throwable throwable)
        {
            displayProgress(false);
        }
    };

    private TwoStateView.OnStateChange gainLossModeListener = new TwoStateView.OnStateChange()
    {
        @Override public void onStateChanged(View view, boolean state)
        {
            if (watchListAdapter != null)
            {
                watchListAdapter.setShowGainLossPercentage(!state);
                watchListAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            if (watchlistListView != null)
            {
                int deletedItemId = intent.getIntExtra(WatchlistItemView.BUNDLE_KEY_WATCHLIST_ITEM_INDEX, -1);
                if (deletedItemId != -1)
                {
                    watchlistListView.dismiss(deletedItemId);
                    watchlistListView.closeOpenedItems();
                }
            }
        }
    };
}

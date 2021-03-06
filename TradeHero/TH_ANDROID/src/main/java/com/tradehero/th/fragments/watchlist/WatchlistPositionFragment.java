package com.tradehero.th.fragments.watchlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListViewListener;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.TwoStateView;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.security.SecuritySearchWatchlistFragment;
import com.tradehero.th.fragments.security.WatchlistEditFragment;
import com.tradehero.th.persistence.portfolio.PortfolioCacheRx;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCacheRx;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.widget.MultiScrollListener;
import javax.inject.Inject;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.observers.EmptyObserver;

public class WatchlistPositionFragment extends DashboardFragment
{
    private static final String BUNDLE_KEY_SHOW_PORTFOLIO_ID_BUNDLE = WatchlistPositionFragment.class.getName() + ".showPortfolioId";
    private static final int NUMBER_OF_WATCHLIST_SWIPE_BUTTONS_BEHIND = 2;

    @Inject CurrentUserId currentUserId;
    @Inject PortfolioCacheRx portfolioCache;
    @Inject UserWatchlistPositionCacheRx userWatchlistPositionCache;
    @Inject Analytics analytics;

    @InjectView(android.R.id.empty) @Optional protected ProgressBar progressBar;
    @InjectView(R.id.watchlist_position_list_header) WatchlistPortfolioHeaderView watchlistPortfolioHeaderView;
    @InjectView(R.id.watchlist_swipe_listview) SwipeListView watchlistPositionListView;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout watchListRefreshableContainer;

    private WatchlistAdapter watchListAdapter;

    private TwoStateView.OnStateChange gainLossModeListener;
    private BroadcastReceiver broadcastReceiver;

    private OwnedPortfolioId shownPortfolioId;
    @Nullable private Subscription portfolioCacheSubscription;
    private PortfolioDTO shownPortfolioDTO;
    @Nullable private Subscription userWatchlistPositionFetchSubscription;
    private WatchlistPositionDTOList watchlistPositionDTOs;

    public static void putOwnedPortfolioId(@NonNull Bundle args, @NonNull OwnedPortfolioId ownedPortfolioId)
    {
        args.putBundle(BUNDLE_KEY_SHOW_PORTFOLIO_ID_BUNDLE, ownedPortfolioId.getArgs());
    }

    @NonNull public static OwnedPortfolioId getOwnedPortfolioId(@NonNull Bundle args)
    {
        return new OwnedPortfolioId(args.getBundle(BUNDLE_KEY_SHOW_PORTFOLIO_ID_BUNDLE));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        shownPortfolioId = getOwnedPortfolioId(getArguments());
        watchListAdapter = createWatchlistAdapter();
        gainLossModeListener = createGainLossModeListener();
        broadcastReceiver = createBroadcastReceiver();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.watchlist_positions_list, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        watchlistPositionListView.post(this::setWatchlistOffset);
        watchlistPositionListView.setEmptyView(view.findViewById(R.id.watchlist_position_list_empty_view));
        watchlistPositionListView.setOnScrollListener(createListViewScrollListener());
        watchlistPositionListView.setAdapter(watchListAdapter);
        watchlistPositionListView.setSwipeListViewListener(createSwipeListViewListener());
        watchListRefreshableContainer.setOnRefreshListener(this::refreshValues);
    }

    //<editor-fold desc="ActionBar Menu Actions">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.position_watchlist_menu, menu);
        setActionBarTitle(getString(R.string.watchlist_title));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.position_watchlist_add:
            {
                Bundle bundle = new Bundle();
                navigator.get().pushFragment(SecuritySearchWatchlistFragment.class, bundle);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //</editor-fold>

    @Override public void onStart()
    {
        super.onStart();
        fetchPortfolio();
        fetchWatchlistPositionList();
    }

    @Override public void onResume()
    {
        super.onResume();

        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Watchlist_List));

        LocalBroadcastManager.getInstance(this.getActivity())
                .registerReceiver(broadcastReceiver, new IntentFilter(WatchlistItemView.WATCHLIST_ITEM_DELETED));
    }

    @Override public void onPause()
    {
        super.onPause();

        LocalBroadcastManager.getInstance(this.getActivity())
                .unregisterReceiver(broadcastReceiver);
    }

    @Override public void onStop()
    {
        unsubscribe(portfolioCacheSubscription);
        portfolioCacheSubscription = null;
        unsubscribe(userWatchlistPositionFetchSubscription);
        userWatchlistPositionFetchSubscription = null;
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        watchlistPortfolioHeaderView.setOnStateChangeListener(null);

        watchlistPositionListView.removeCallbacks(null);
        watchlistPositionListView.setSwipeListViewListener(null);
        watchlistPositionListView.removeCallbacks(null);

        watchListRefreshableContainer.setRefreshing(false);
        watchListRefreshableContainer.setOnRefreshListener(null);

        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        userWatchlistPositionFetchSubscription = null;
        broadcastReceiver = null;
        gainLossModeListener = null;
        watchListAdapter = null;

        super.onDestroy();
    }

    @NonNull private WatchlistAdapter createWatchlistAdapter()
    {
        return new WatchlistAdapter(getActivity(), R.layout.watchlist_item_view);
    }

    @NonNull protected TwoStateView.OnStateChange createGainLossModeListener()
    {
        return new TwoStateView.OnStateChange()
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
    }

    @NonNull protected BroadcastReceiver createBroadcastReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override public void onReceive(Context context, Intent intent)
            {
                if (watchlistPositionListView != null)
                {
                    SecurityId deletedSecurityId = WatchlistItemView.getDeletedSecurityId(intent);
                    if (deletedSecurityId != null)
                    {
                        WatchlistAdapter adapter = (WatchlistAdapter) watchlistPositionListView.getAdapter();
                        adapter.remove(deletedSecurityId);
                        adapter.notifyDataSetChanged();
                        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Watchlist_Delete));
                        watchlistPositionListView.closeOpenedItems();
                        fetchWatchlistPositionList();
                    }
                }
            }
        };
    }

    @NonNull protected AbsListView.OnScrollListener createListViewScrollListener()
    {
        int trendingFilterHeight = (int) getResources().getDimension(R.dimen.watch_list_header_height);
        QuickReturnListViewOnScrollListener portfolioHearderQuickReturnListener =
                new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER, watchlistPortfolioHeaderView,
                        -trendingFilterHeight, null, 0);

        return new MultiScrollListener(portfolioHearderQuickReturnListener, dashboardBottomTabsListViewScrollListener.get());
    }

    public void setWatchlistOffset()
    {
        if (watchlistPositionListView != null)
        {
            watchlistPositionListView.setOffsetLeft(watchlistPositionListView.getWidth() -
                    getResources().getDimension(R.dimen.watchlist_item_button_width)
                            * NUMBER_OF_WATCHLIST_SWIPE_BUTTONS_BEHIND);
        }
    }

    protected SwipeListViewListener createSwipeListViewListener()
    {
        return new WatchlistPositionFragmentSwipeListViewListener();
    }

    protected class WatchlistPositionFragmentSwipeListViewListener extends BaseSwipeListViewListener
    {
        @Override public void onClickFrontView(int position)
        {
            super.onClickFrontView(position);
            openWatchlistItemEditor(position);
        }

        @Override public void onStartOpen(int position, int action, boolean right)
        {
            analytics.addEvent(new SimpleEvent(AnalyticsConstants.Watchlist_CellSwipe));
            super.onStartOpen(position, action, right);
        }

        @Override public void onDismiss(int[] reverseSortedPositions)
        {
            super.onDismiss(reverseSortedPositions);
            fetchWatchlistPositionList();
        }
    }

    protected void fetchPortfolio()
    {
        if (portfolioCacheSubscription == null)
        {
            portfolioCacheSubscription = AndroidObservable.bindFragment(
                    this,
                    portfolioCache.get(shownPortfolioId))
                    .subscribe(createPortfolioCacheObserver());
        }
    }

    protected Observer<Pair<OwnedPortfolioId, PortfolioDTO>> createPortfolioCacheObserver()
    {
        return new PortfolioCacheObserver();
    }

    protected class PortfolioCacheObserver extends EmptyObserver<Pair<OwnedPortfolioId, PortfolioDTO>>
    {
        @Override public void onNext(Pair<OwnedPortfolioId, PortfolioDTO> pair)
        {
            shownPortfolioDTO = pair.second;
            displayHeader();
        }

        @Override public void onError(Throwable e)
        {
            if (shownPortfolioDTO == null)
            {
                THToast.show(R.string.error_fetch_portfolio_info);
            }
        }
    }

    protected void fetchWatchlistPositionList()
    {
        if (userWatchlistPositionFetchSubscription == null)
        {
            userWatchlistPositionFetchSubscription = AndroidObservable.bindFragment(
                    this,
                    userWatchlistPositionCache.get(currentUserId.toUserBaseKey()))
                    .subscribe(createWatchlistObserver());
        }
    }

    protected Observer<Pair<UserBaseKey, WatchlistPositionDTOList>> createWatchlistObserver()
    {
        return new WatchlistListCacheObserver();
    }

    protected class WatchlistListCacheObserver extends EmptyObserver<Pair<UserBaseKey, WatchlistPositionDTOList>>
    {
        @Override public void onNext(Pair<UserBaseKey, WatchlistPositionDTOList> pair)
        {
            displayWatchlist(pair.second);
        }

        @Override public void onError(Throwable e)
        {
            watchListRefreshableContainer.setRefreshing(false);
            if (watchListAdapter == null || watchListAdapter.getCount() <= 0)
            {
                THToast.show(getString(R.string.error_fetch_portfolio_watchlist));
            }
        }
    }

    protected void refreshValues()
    {
        portfolioCache.invalidate(shownPortfolioId);
        portfolioCache.get(shownPortfolioId);
        userWatchlistPositionCache.invalidate(currentUserId.toUserBaseKey());
        userWatchlistPositionCache.get(currentUserId.toUserBaseKey());
    }

    private void displayHeader()
    {
        watchlistPortfolioHeaderView.linkWith(shownPortfolioDTO, true);
        watchlistPortfolioHeaderView.linkWith(watchlistPositionDTOs, true);
        watchlistPortfolioHeaderView.setOnStateChangeListener(gainLossModeListener);
    }

    private void displayWatchlist(WatchlistPositionDTOList watchlistPositionDTOs)
    {
        this.watchlistPositionDTOs = watchlistPositionDTOs;
        watchListAdapter.clear();
        watchListAdapter.addAll(watchlistPositionDTOs);
        watchListAdapter.notifyDataSetChanged();
        watchListRefreshableContainer.setRefreshing(false);
        displayHeader();
    }

    private void openWatchlistItemEditor(int position)
    {
        // TODO discover why sometimes we would get a mismatch
        if (position < watchListAdapter.getCount())
        {
            WatchlistPositionDTO watchlistPositionDTO = watchListAdapter.getItem(position);
            Bundle args = new Bundle();
            if (watchlistPositionDTO != null)
            {
                WatchlistEditFragment.putSecurityId(args, watchlistPositionDTO.securityDTO.getSecurityId());
            }
            navigator.get().pushFragment(WatchlistEditFragment.class, args, null);
        }
    }
}

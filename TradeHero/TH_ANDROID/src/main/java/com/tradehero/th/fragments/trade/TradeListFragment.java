package com.tradehero.th.fragments.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.position.OwnedPositionId;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerId;
import com.tradehero.th.api.trade.OwnedTradeId;
import com.tradehero.th.api.trade.OwnedTradeIdList;
import com.tradehero.th.api.users.CurrentUserBaseKeyHolder;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.base.Navigator;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.persistence.position.PositionCache;
import com.tradehero.th.persistence.security.SecurityCompactCache;
import com.tradehero.th.persistence.security.SecurityIdCache;
import com.tradehero.th.persistence.trade.TradeListCache;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by julien on 23/10/13
 */
public class TradeListFragment extends DashboardFragment
{
    public static final String TAG = TradeListFragment.class.getSimpleName();
    public static final String BUNDLE_KEY_OWNED_POSITION_ID_BUNDLE = TradeListFragment.class.getName() + ".ownedPositionId";
    public static final String BUNDLE_KEY_OWNED_LEADERBOARD_POSITION_ID_BUNDLE = TradeListFragment.class.getName() + ".ownedLeaderboardPositionId";

    private OwnedPositionId ownedPositionId;
    @Inject Lazy<PositionCache> positionCache;
    @Inject Lazy<TradeListCache> tradeListCache;
    @Inject Lazy<SecurityIdCache> securityIdCache;
    @Inject Lazy<SecurityCompactCache> securityCompactCache;
    @Inject CurrentUserBaseKeyHolder currentUserBaseKeyHolder;

    private TradeListOverlayHeaderView header;
    private ListView tradeListView;
    private ActionBar actionBar;
    private ProgressBar progressBar;

    private TradeListItemAdapter adapter;
    private TradeListHeaderView.TradeListHeaderClickListener buttonListener;

    private DTOCache.GetOrFetchTask<OwnedPositionId, OwnedTradeIdList> fetchTradesTask;
    private TradeListCache.Listener<OwnedPositionId, OwnedTradeIdList> getTradesListener;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_trade_list, container, false);

        initViews(view, inflater);
        return view;
    }

    private void initViews(View view, LayoutInflater inflater)
    {
        if (view != null)
        {
            progressBar = (ProgressBar) view.findViewById(android.R.id.empty);

            this.buttonListener = new TradeListHeaderView.TradeListHeaderClickListener()
            {
                @Override public void onBuyButtonClicked(TradeListHeaderView tradeListHeaderView, OwnedPositionId ownedPositionId)
                {
                    pushBuySellFragment(ownedPositionId, true);
                }

                @Override public void onSellButtonClicked(TradeListHeaderView tradeListHeaderView, OwnedPositionId ownedPositionId)
                {
                    pushBuySellFragment(ownedPositionId, false);
                }
            };

            adapter = new TradeListItemAdapter(getActivity(), getActivity().getLayoutInflater());
            adapter.setTradeListHeaderClickListener(this.buttonListener);

            tradeListView = (ListView) view.findViewById(R.id.trade_list);
            if (tradeListView != null)
            {
                tradeListView.setAdapter(adapter);
            }

            header = (TradeListOverlayHeaderView) view.findViewById(R.id.trade_list_header);
            registerOverlayHeaderListener();
        }
    }

    private void registerOverlayHeaderListener()
    {
        if (this.header == null)
        {
            return;
        }

        this.header.setListener(new TradeListOverlayHeaderView.Listener()
        {
            @Override public void onSecurityClicked(TradeListOverlayHeaderView headerView, OwnedPositionId ownedPositionId)
            {
                pushBuySellFragment(ownedPositionId, true);
            }

            @Override public void onUserClicked(TradeListOverlayHeaderView headerView, UserBaseKey userId)
            {
                openUserProfile(userId);
            }
        });
    }

    private void pushBuySellFragment(OwnedPositionId clickedOwnedPositionId, boolean isBuy)
    {
        if (clickedOwnedPositionId != null)
        {
            PositionDTO positionDTO = positionCache.get().get(clickedOwnedPositionId);
            if (positionDTO == null)
            {
                THToast.show("We have lost track of this trading position");
            }
            else
            {
                SecurityId securityId = securityIdCache.get().get(positionDTO.getSecurityIntegerId());
                if (securityId == null)
                {
                    THToast.show("Could not find this security");
                }
                else
                {
                    Bundle args = new Bundle();
                    args.putBoolean(BuySellFragment.BUNDLE_KEY_IS_BUY, isBuy);
                    args.putBundle(BuySellFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
                    navigator.pushFragment(BuySellFragment.class, args);
                }
            }
        }
        else
        {
            THLog.e(TAG, "Was passed a null clickedOwnedPositionId", new IllegalArgumentException());
        }
    }

    private void openUserProfile(UserBaseKey userId)
    {
        Bundle b = new Bundle();
        b.putInt(PushableTimelineFragment.BUNDLE_KEY_SHOW_USER_ID, userId.key);

        if (!currentUserBaseKeyHolder.getCurrentUserBaseKey().key.equals(userId.key))
        {
            navigator.pushFragment(PushableTimelineFragment.class, b);
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        displayActionBarTitle();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onResume()
    {
        super.onResume();
        Bundle args = getArguments();
        if (args != null)
        {
            Bundle ownedPositionIdBundle = args.getBundle(BUNDLE_KEY_OWNED_POSITION_ID_BUNDLE);
            if (ownedPositionIdBundle != null)
            {
                linkWith(new OwnedPositionId(ownedPositionIdBundle), true);
            }
            else
            {
                THLog.d(TAG, "ownedPositionIdBundle is null");
            }
        }
        else
        {
            THLog.d(TAG, "args is null");
        }
    }

    @Override public void onDestroyOptionsMenu()
    {
        actionBar = null;
        super.onDestroyOptionsMenu();
    }

    @Override public void onDestroyView()
    {
        if (fetchTradesTask != null)
        {
            fetchTradesTask.setListener(null);
        }
        fetchTradesTask = null;
        getTradesListener = null;
        tradeListView = null;
        buttonListener = null;
        if (adapter != null)
        {
            adapter.setTradeListHeaderClickListener(null);
        }
        adapter = null;
        super.onDestroyView();
    }

    public void linkWith(OwnedPositionId ownedPositionId, boolean andDisplay)
    {
        this.ownedPositionId = ownedPositionId;
        if (this.adapter != null)
        {
            this.adapter.setShownPositionId(this.ownedPositionId);
        }
        fetchTrades();

        if (andDisplay)
        {
            display();
        }
    }

    private void fetchTrades()
    {
        if (ownedPositionId != null && ownedPositionId.isValid())
        {
            if (getTradesListener == null)
            {
                getTradesListener = new GetTradesListener();
            }
            if (fetchTradesTask != null)
            {
                fetchTradesTask.setListener(null);
            }
            fetchTradesTask = tradeListCache.get().getOrFetch(ownedPositionId, getTradesListener);
            displayProgress(true);
            fetchTradesTask.execute();
        }
    }

    public void linkWith(List<OwnedTradeId> ownedTradeIds, boolean andDisplay)
    {
        adapter.setUnderlyingItems(ownedTradeIds);
        getView().post(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        if (andDisplay)
        {
            display();
        }
    }

    public void display()
    {
        if (this.ownedPositionId != null)
        {
            if (this.header != null)
            {
                header.bindOwnedPositionId(this.ownedPositionId);
            }
        }

        displayActionBarTitle();
    }

    public void displayActionBarTitle()
    {
        ActionBar actionBarCopy = this.actionBar;
        if (actionBarCopy != null)
        {
            if (ownedPositionId == null || positionCache.get().get(ownedPositionId) == null ||
                    securityIdCache.get().get(new SecurityIntegerId(positionCache.get().get(ownedPositionId).securityId)) == null)
            {
                actionBarCopy.setTitle(R.string.trade_list_title);
            }
            else
            {
                PositionDTO positionDTO = positionCache.get().get(ownedPositionId);
                if (positionDTO == null)
                {
                    actionBarCopy.setTitle(R.string.trade_list_title);
                }
                else
                {

                    SecurityId securityId = securityIdCache.get().get(new SecurityIntegerId(positionDTO.securityId));
                    if (securityId == null)
                    {
                        actionBarCopy.setTitle(R.string.trade_list_title);
                    }
                    else
                    {
                        SecurityCompactDTO securityCompactDTO = securityCompactCache.get().get(securityId);
                        if (securityCompactDTO == null || securityCompactDTO.name == null)
                        {
                            actionBarCopy.setTitle(
                                    String.format(getString(R.string.trade_list_title_with_security), securityId.exchange, securityId.securitySymbol));
                        }
                        else
                        {
                            actionBarCopy.setTitle(securityCompactDTO.name);
                        }
                    }
                }
            }
        }
    }

    public void displayProgress(boolean running)
    {
        if (progressBar != null)
        {
            progressBar.setVisibility(running ? View.VISIBLE : View.GONE);
        }
    }

    //<editor-fold desc="BaseFragment.TabBarVisibilityInformer">
    @Override public boolean isTabBarVisible()
    {
        return false;
    }
    //</editor-fold>

    private class GetTradesListener implements TradeListCache.Listener<OwnedPositionId, OwnedTradeIdList>
    {
        @Override public void onDTOReceived(OwnedPositionId key, OwnedTradeIdList ownedTradeIds, boolean fromCache)
        {
            if (ownedPositionId != null && ownedPositionId.equals(key))
            {
                displayProgress(false);
                linkWith(ownedTradeIds, true);
            }
        }

        @Override public void onErrorThrown(OwnedPositionId key, Throwable error)
        {
            if (ownedPositionId != null && ownedPositionId.equals(key))
            {
                displayProgress(false);
                THToast.show(getString(R.string.error_fetch_trade_list_info));
                THLog.e(TAG, "Error fetching the list of trades " + key, error);
            }
        }
    }
}

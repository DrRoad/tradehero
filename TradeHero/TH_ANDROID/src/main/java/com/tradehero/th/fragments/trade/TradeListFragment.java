package com.tradehero.th.fragments.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.ActionBar;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.position.OwnedPositionId;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.position.PositionDTOKey;
import com.tradehero.th.api.position.PositionDTOKeyFactory;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerId;
import com.tradehero.th.api.trade.OwnedTradeId;
import com.tradehero.th.api.trade.OwnedTradeIdList;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.fragments.trade.view.TradeListHeaderView;
import com.tradehero.th.fragments.trade.view.TradeListOverlayHeaderView;
import com.tradehero.th.persistence.portfolio.PortfolioCache;
import com.tradehero.th.persistence.position.PositionCache;
import com.tradehero.th.persistence.security.SecurityCompactCache;
import com.tradehero.th.persistence.security.SecurityIdCache;
import com.tradehero.th.persistence.trade.TradeListCache;
import com.tradehero.th.utils.THRouter;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

@Routable("user/:userId/portfolio/:portfolioId/position/:positionId")
public class TradeListFragment extends BasePurchaseManagerFragment
{
    private static final String BUNDLE_KEY_POSITION_DTO_KEY_BUNDLE = TradeListFragment.class.getName() + ".positionDTOKey";

    @Inject Lazy<PositionCache> positionCache;
    @Inject Lazy<TradeListCache> tradeListCache;
    @Inject Lazy<SecurityIdCache> securityIdCache;
    @Inject Lazy<SecurityCompactCache> securityCompactCache;
    @Inject PortfolioCache portfolioCache;
    @Inject CurrentUserId currentUserId;
    @Inject PositionDTOKeyFactory positionDTOKeyFactory;
    @Inject THRouter thRouter;

    @InjectView(android.R.id.empty) protected ProgressBar progressBar;
    @InjectView(R.id.trade_list_header) protected TradeListOverlayHeaderView header;
    @InjectView(R.id.trade_list) protected ListView tradeListView;

    @RouteProperty("userId") Integer routeUserId;
    @RouteProperty("portfolioId") Integer routePortfolioId;
    @RouteProperty("positionId") Integer routePositionId;

    protected PositionDTOKey positionDTOKey;
    protected PositionDTO positionDTO;
    protected OwnedTradeIdList ownedTradeIds;

    protected TradeListItemAdapter adapter;
    protected TradeListHeaderView.TradeListHeaderClickListener buttonListener;

    private DTOCacheNew.Listener<OwnedPositionId, OwnedTradeIdList> fetchTradesListener;

    public static void putPositionDTOKey(Bundle args, PositionDTOKey positionDTOKey)
    {
        args.putBundle(BUNDLE_KEY_POSITION_DTO_KEY_BUNDLE, positionDTOKey.getArgs());
    }

    private static PositionDTOKey getPositionDTOKey(Bundle args, PositionDTOKeyFactory positionDTOKeyFactory)
    {
        return positionDTOKeyFactory.createFrom(args.getBundle(BUNDLE_KEY_POSITION_DTO_KEY_BUNDLE));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        thRouter.inject(this);
        if (getArguments() != null && routeUserId != null && routePortfolioId != null && routePositionId != null)
        {
            putPositionDTOKey(getArguments(), new OwnedPositionId(routeUserId, routePortfolioId, routePositionId));
        }

        this.buttonListener = new TradeListHeaderView.TradeListHeaderClickListener()
        {
            @Override public void onBuyButtonClicked(TradeListHeaderView tradeListHeaderView)
            {
                pushBuySellFragment(true);
            }

            @Override public void onSellButtonClicked(TradeListHeaderView tradeListHeaderView)
            {
                pushBuySellFragment(false);
            }
        };
        fetchTradesListener = createTradeListeCacheListener();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_trade_list, container, false);

        initViews(view);
        return view;
    }

    @Override protected void initViews(View view)
    {
        ButterKnife.inject(this, view);

        if (view != null)
        {
            createAdapter();
            adapter.setTradeListHeaderClickListener(this.buttonListener);

            if (tradeListView != null)
            {
                tradeListView.setAdapter(adapter);
            }

            registerOverlayHeaderListener();
        }
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        linkWith(getPositionDTOKey(getArguments(), positionDTOKeyFactory), true);
    }

    protected void createAdapter()
    {
        adapter = new TradeListItemAdapter(getActivity(), getActivity().getLayoutInflater());
    }

    protected void rePurposeAdapter()
    {
        if (this.positionDTO != null && this.ownedTradeIds != null)
        {
            createAdapter();
            adapter.setTradeListHeaderClickListener(this.buttonListener);
            adapter.setShownPositionDTO(positionDTO);
            adapter.setUnderlyingItems(createUnderlyingItems());
            if (tradeListView != null)
            {
                tradeListView.setAdapter(adapter);
            }
        }
    }

    protected List<PositionTradeDTOKey> createUnderlyingItems()
    {
        List<PositionTradeDTOKey> created = new ArrayList<>();
        for (OwnedTradeId tradeId : ownedTradeIds)
        {
            created.add(new PositionTradeDTOKey(positionDTOKey, tradeId));
        }
        return created;
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
                pushBuySellFragment(true);
            }

            @Override public void onUserClicked(TradeListOverlayHeaderView headerView, UserBaseKey userId)
            {
                openUserProfile(userId);
            }
        });
    }

    private void pushBuySellFragment(boolean isBuy)
    {
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
                populateBuySellArgs(args, isBuy, securityId);
                OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();

                if (ownedPortfolioId != null)
                {
                    BuySellFragment.putApplicablePortfolioId(args, ownedPortfolioId);
                }

                getDashboardNavigator().pushFragment(BuySellFragment.class, args);
            }
        }
    }

    protected void populateBuySellArgs(Bundle args, boolean isBuy, SecurityId securityId)
    {
        args.putBoolean(BuySellFragment.BUNDLE_KEY_IS_BUY, isBuy);
        args.putBundle(BuySellFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
    }

    private void openUserProfile(UserBaseKey userId)
    {
        Bundle bundle = new Bundle();
        thRouter.save(bundle, userId);

        if (!currentUserId.toUserBaseKey().equals(userId))
        {
            getDashboardNavigator().pushFragment(PushableTimelineFragment.class, bundle);
        }
    }

    @Override public void onDetach()
    {
        setActionBarSubtitle(null);
        super.onDetach();
    }

    @Override public void onDestroyOptionsMenu()
    {
        setActionBarSubtitle(null);
        super.onDestroyOptionsMenu();
    }

    @Override public void onDestroyView()
    {
        detachFetchTradesTask();
        if (adapter != null)
        {
            adapter.setTradeListHeaderClickListener(null);
        }
        adapter = null;
        header.setListener(null);
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        buttonListener = null;
        fetchTradesListener = null;
        super.onDestroy();
    }

    protected void detachFetchTradesTask()
    {
        tradeListCache.get().unregister(fetchTradesListener);
    }

    public void linkWith(PositionDTOKey newPositionDTOKey, boolean andDisplay)
    {
        this.positionDTOKey = newPositionDTOKey;
        linkWith(positionCache.get().get(newPositionDTOKey), andDisplay);

        if (andDisplay)
        {
            display();
        }
    }

    public void linkWith(PositionDTO positionDTO, boolean andDisplay)
    {
        this.positionDTO = positionDTO;
        rePurposeAdapter();
        fetchTrades();
        if (andDisplay)
        {
            displayHeader();
        }
    }

    protected void fetchTrades()
    {
        if (positionDTO != null)
        {
            detachFetchTradesTask();
            OwnedPositionId key = positionDTO.getOwnedPositionId();
            tradeListCache.get().register(key, fetchTradesListener);
            tradeListCache.get().getOrFetchAsync(key);
            displayProgress(true);
        }
    }

    public void linkWith(OwnedTradeIdList ownedTradeIds, boolean andDisplay)
    {
        this.ownedTradeIds = ownedTradeIds;
        rePurposeAdapter();

        if (andDisplay)
        {
            display();
        }
    }

    public void display()
    {
        displayHeader();
        displayActionBarTitle();
    }

    public void displayHeader()
    {
        if (this.header != null)
        {
            if (this.positionDTO != null)
            {
                header.bindOwnedPositionId(this.positionDTO);
            }
        }
    }

    public void displayActionBarTitle()
    {
        if (positionDTO == null || securityIdCache.get().get(new SecurityIntegerId(positionDTO.securityId)) == null)
        {
            setActionBarTitle(R.string.trade_list_title);
        }
        else
        {
            SecurityId securityId = securityIdCache.get().get(new SecurityIntegerId(positionDTO.securityId));
            if (securityId == null)
            {
                setActionBarTitle(R.string.trade_list_title);
            }
            else
            {
                SecurityCompactDTO securityCompactDTO = securityCompactCache.get().get(securityId);
                if (securityCompactDTO == null || securityCompactDTO.name == null)
                {
                    setActionBarTitle(
                            String.format(getString(R.string.trade_list_title_with_security), securityId.getExchange(),
                                    securityId.getSecuritySymbol()));
                }
                else
                {
                    setActionBarTitle(securityCompactDTO.name);
                    setActionBarSubtitle(String.format(getString(R.string.trade_list_title_with_security), securityId.getExchange(),
                            securityId.getSecuritySymbol()));
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

    protected TradeListCache.Listener<OwnedPositionId, OwnedTradeIdList> createTradeListeCacheListener()
    {
        return new GetTradesListener();
    }

    private class GetTradesListener implements TradeListCache.Listener<OwnedPositionId, OwnedTradeIdList>
    {
        @Override public void onDTOReceived(@NotNull OwnedPositionId key, @NotNull OwnedTradeIdList ownedTradeIds)
        {
            displayProgress(false);
            linkWith(ownedTradeIds, true);
        }

        @Override public void onErrorThrown(@NotNull OwnedPositionId key, @NotNull Throwable error)
        {
            displayProgress(false);
            THToast.show(R.string.error_fetch_trade_list_info);
            Timber.e("Error fetching the list of trades %s", key, error);
        }
    }
}

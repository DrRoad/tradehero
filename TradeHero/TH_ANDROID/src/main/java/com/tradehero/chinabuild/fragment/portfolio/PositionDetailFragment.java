package com.tradehero.chinabuild.fragment.portfolio;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshBase;
import com.tradehero.chinabuild.fragment.security.SecurityDetailFragment;
import com.tradehero.chinabuild.listview.SecurityListView;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.R;
import com.tradehero.th.adapters.PositionTradeListAdapter;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.position.OwnedPositionId;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.position.PositionDTOKey;
import com.tradehero.th.api.position.PositionDTOKeyFactory;
import com.tradehero.th.api.trade.TradeDTOList;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.persistence.position.PositionCache;
import com.tradehero.th.persistence.trade.TradeListCache;
import com.tradehero.th.utils.DateUtils;
import com.tradehero.th.utils.StringUtils;
import com.tradehero.th.widget.TradeHeroProgressBar;
import dagger.Lazy;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/*
    单个Position的交易详情
 */
public class PositionDetailFragment extends DashboardFragment
{
    public static final String BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE =
            PositionDetailFragment.class.getName() + ".purchaseApplicablePortfolioId";
    public static final String BUNDLE_KEY_POSITION_DTO_KEY_BUNDLE = PositionDetailFragment.class.getName() + ".positionDTOKey";
    public static final String BUNLDE_KEY_NEED_SHOW_MORE = PositionDetailFragment.class.getName() + ".needShowMore";//是否需要显示行情按钮在右上角

    @Inject Lazy<PositionCache> positionCache;
    @Inject Lazy<TradeListCache> tradeListCache;
    @Inject PositionDTOKeyFactory positionDTOKeyFactory;

    protected PositionDTOKey positionDTOKey;
    protected DTOCacheNew.Listener<PositionDTOKey, PositionDTO> fetchPositionListener;
    protected PositionDTO positionDTO;
    protected TradeDTOList tradeDTOList;
    private DTOCacheNew.Listener<OwnedPositionId, TradeDTOList> fetchTradesListener;

    @InjectView(R.id.tvPositionTotalCcy) TextView tvPositionTotalCcy;//累计盈亏
    @InjectView(R.id.tvPositionSumAmont) TextView tvPositionSumAmont;//总投资
    @InjectView(R.id.tvPositionStartTime) TextView tvPositionStartTime;//建仓时间
    @InjectView(R.id.tvPositionLastTime) TextView tvPositionLastTime;//最后交易
    @InjectView(R.id.tvPositionHoldTime) TextView tvPositionHoldTime;//持有时间

    @InjectView(R.id.listTrade) SecurityListView listView;
    @InjectView(R.id.tradeheroprogressbar_my_securities_history) TradeHeroProgressBar progressBar;
    @InjectView(R.id.bvaViewAll) BetterViewAnimator betterViewAnimator;

    private PositionTradeListAdapter adapter;

    public static void putApplicablePortfolioId(@NotNull Bundle args, @NotNull OwnedPortfolioId ownedPortfolioId)
    {
        args.putBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE, ownedPortfolioId.getArgs());
    }

    public static void putPositionDTOKey(@NotNull Bundle args, @NotNull PositionDTOKey positionDTOKey)
    {
        args.putBundle(BUNDLE_KEY_POSITION_DTO_KEY_BUNDLE, positionDTOKey.getArgs());
    }

    @NotNull private static PositionDTOKey getPositionDTOKey(@NotNull Bundle args, @NotNull PositionDTOKeyFactory positionDTOKeyFactory)
    {
        return positionDTOKeyFactory.createFrom(args.getBundle(BUNDLE_KEY_POSITION_DTO_KEY_BUNDLE));
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fetchPositionListener = new TradeListFragmentPositionCacheListener();
        fetchTradesListener = new GetTradesListener();
        adapter = new PositionTradeListAdapter(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        setHeadViewMiddleMain("交易详情");
        getSecurityName();
        if (getNeedShowMore())
        {
            setHeadViewRight0("行情");
        }
    }

    public void getSecurityName()
    {
        Bundle args = getArguments();
        if (args != null)
        {
            String securityName = args.getString(SecurityDetailFragment.BUNDLE_KEY_SECURITY_NAME);
            if (!StringUtils.isNullOrEmpty(securityName))
            {
                setHeadViewMiddleMain(securityName);
            }
        }
    }

    public boolean getNeedShowMore()
    {
        Bundle args = getArguments();
        if (args != null)
        {
            boolean isNeedShow = args.getBoolean(BUNLDE_KEY_NEED_SHOW_MORE, true);
            return isNeedShow;
        }
        return true;
    }

    @Override public void onClickHeadRight0()
    {
        Timber.d("进入行情页面");
        Bundle bundle = new Bundle();
        bundle.putAll(getArguments());
        pushFragment(SecurityDetailFragment.class, bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.position_detail_fragment, container, false);
        ButterKnife.inject(this, view);

        initListView();

        if (adapter != null && adapter.getCount() == 0)
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.tradeheroprogressbar_my_securities_history);
            progressBar.startLoading();
            startTimerForView();
        }
        else
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listTrade);
        }
        return view;
    }

    public void initListView()
    {
        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.DISABLED);
    }

    @Override public void onDestroyView()
    {
        detachFetchPosition();
        detachFetchTrades();

        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onResume()
    {
        super.onResume();
        linkWith(getPositionDTOKey(getArguments(), positionDTOKeyFactory));
    }

    public void linkWith(@NotNull PositionDTOKey newPositionDTOKey)
    {
        this.positionDTOKey = newPositionDTOKey;
        fetchPosition();
    }

    protected void detachFetchPosition()
    {
        positionCache.get().unregister(fetchPositionListener);
    }

    protected void detachFetchTrades()
    {
        tradeListCache.get().unregister(fetchTradesListener);
    }

    protected void fetchPosition()
    {
        detachFetchPosition();
        positionCache.get().register(positionDTOKey, fetchPositionListener);
        positionCache.get().getOrFetchAsync(positionDTOKey);
    }

    protected class TradeListFragmentPositionCacheListener implements DTOCacheNew.Listener<PositionDTOKey, PositionDTO>
    {
        @Override public void onDTOReceived(@NotNull PositionDTOKey key, @NotNull PositionDTO value)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(@NotNull PositionDTOKey key, @NotNull Throwable error)
        {
            //THToast.show(R.string.error_fetch_position_list_info);
        }
    }

    public void linkWith(PositionDTO positionDTO, boolean andDisplay)
    {
        if(getActivity() == null )return;
        this.positionDTO = positionDTO;
        fetchTrades();
        displayPosition(positionDTO);
    }

    public void displayPosition(PositionDTO positionDTO)
    {
        try
        {
            THSignedNumber roi = THSignedPercentage.builder(positionDTO.getROISinceInception() * 100)
                    .withSign()
                    .signTypeArrow()
                    .build();
            tvPositionTotalCcy.setTextColor(getResources().getColor(roi.getColorResId()));
            tvPositionTotalCcy.setText("$" + positionDTO.getTotalScoreOfTrade() + "(" + roi.toString() + ")");
            tvPositionSumAmont.setText("$" + Math.round(positionDTO.sumInvestedAmountRefCcy));
            tvPositionStartTime.setText(DateUtils.getFormattedDate(getResources(), positionDTO.earliestTradeUtc));
            tvPositionLastTime.setText(DateUtils.getFormattedDate(getResources(), positionDTO.latestTradeUtc));
            tvPositionHoldTime.setText(getResources().getString(R.string.position_hold_days,
                    DateUtils.getNumberOfDaysBetweenDates(positionDTO.earliestTradeUtc, positionDTO.getLatestHoldDate())));
        }catch (Exception e)
        {

        }
    }

    protected void fetchTrades()
    {
        if (positionDTO != null)
        {
            detachFetchTrades();
            OwnedPositionId key = positionDTO.getOwnedPositionId();
            tradeListCache.get().register(key, fetchTradesListener);
            tradeListCache.get().getOrFetchAsync(key);
        }
        else
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listTrade);
        }
    }

    private class GetTradesListener implements TradeListCache.Listener<OwnedPositionId, TradeDTOList>
    {
        @Override public void onDTOReceived(@NotNull OwnedPositionId key, @NotNull TradeDTOList tradeDTOs)
        {
            linkWith(tradeDTOs, true);
            onFinish();
        }

        @Override public void onErrorThrown(@NotNull OwnedPositionId key, @NotNull Throwable error)
        {
            THToast.show(R.string.error_fetch_trade_list_info);
            onFinish();
        }

        public void onFinish()
        {
            if(progressBar!=null)
            {
                progressBar.stopLoading();
            }
            if(betterViewAnimator!=null)
            {
                betterViewAnimator.setDisplayedChildByLayoutId(R.id.listTrade);
            }
        }
    }

    public void linkWith(TradeDTOList tradeDTOs, boolean andDisplay)
    {
        Timber.d("Tradehero: PositionDetailFragment LinkWith");
        this.tradeDTOList = tradeDTOs;
        adapter.setTradeList(tradeDTOList);
    }

    private Handler handler = new Handler();

    private Runnable runnable;

    public void startTimerForView()
    {
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                closeTimerForView();
                onResume();
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    public void closeTimerForView()
    {
        try
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listTrade);
            handler.removeCallbacks(runnable);
        } catch (Exception e)
        {
        }
    }
}

package com.tradehero.th.fragments.watchlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.tradehero.common.widget.TwoStateView;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistPositionCache;
import com.tradehero.th.utils.DaggerUtils;
import dagger.Lazy;
import java.text.SimpleDateFormat;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WatchlistPortfolioHeaderView extends LinearLayout
        implements DTOView<UserBaseKey>
{
    @Inject protected Lazy<WatchlistPositionCache> watchlistPositionCache;
    @Inject protected Lazy<UserWatchlistPositionCache> userWatchlistPositionCache;

    private WatchlistHeaderItem gainLoss;
    private WatchlistHeaderItem valuation;
    private TextView marking;
    private WatchlistPositionDTOList watchlistPositionDTOs;
    @Nullable private UserBaseKey userBaseKey;
    @Nullable private PortfolioCompactDTO portfolioCompactDTO;
    private SimpleDateFormat markingDateFormat;
    private BroadcastReceiver watchlistItemDeletedReceiver;

    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public WatchlistPortfolioHeaderView(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public WatchlistPortfolioHeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public WatchlistPortfolioHeaderView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        init();
    }

    public void setOnStateChangeListener(TwoStateView.OnStateChange onStateChangeListener)
    {
        if (gainLoss != null)
        {
            gainLoss.setOnStateChange(onStateChangeListener);
        }

        // Only handle when user click on gain/loss area
        //
        //if (valuation != null)
        //{
        //    valuation.setOnStateChange(onStateChangeListener);
        //}
    }

    private void init()
    {
        DaggerUtils.inject(this);

        if (getChildCount() != 2)
        {
            throw new IllegalAccessError("Watchlist header view should have only 2 children, both are TwoStateView");
        }

        LinearLayout container = (LinearLayout) getChildAt(0);

        valuation = (WatchlistHeaderItem) container.getChildAt(0);
        if (valuation != null)
        {
            valuation.setFirstTitle(getContext().getString(R.string.watchlist_current_value));
            valuation.setSecondTitle(getContext().getString(R.string.watchlist_original_value));
        }
        gainLoss = (WatchlistHeaderItem) container.getChildAt(1);
        if (gainLoss != null)
        {
            gainLoss.setTitle(getContext().getString(R.string.watchlist_gain_loss));
        }

        marking = (TextView) findViewById(R.id.watchlist_position_list_marking);
        markingDateFormat = new SimpleDateFormat(getResources().getString(R.string.watchlist_marking_date_format));
    }

    @Override public void display(@NotNull UserBaseKey userBaseKey)
    {
        linkWith(userBaseKey, true);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        watchlistItemDeletedReceiver = createWatchlistItemDeletedReceiver();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(watchlistItemDeletedReceiver, new IntentFilter(WatchlistItemView.WATCHLIST_ITEM_DELETED));
    }

    @Override protected void onDetachedFromWindow()
    {
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(watchlistItemDeletedReceiver);
        watchlistItemDeletedReceiver = null;
        super.onDetachedFromWindow();
    }

    private void linkWith(@NotNull UserBaseKey userBaseKey, boolean andDisplay)
    {
        this.userBaseKey = userBaseKey;
        watchlistPositionDTOs = userWatchlistPositionCache.get().get(this.userBaseKey);

        if (andDisplay)
        {
            displayValuation();
            displayGainLoss();
        }
    }

    public void linkWith(PortfolioCompactDTO portfolioCompactDTO, boolean andDisplay)
    {
        this.portfolioCompactDTO = portfolioCompactDTO;
        if (andDisplay)
        {
            displayMarkingDate();
        }
    }

    private void displayGainLoss()
    {
        THSignedNumber firstNumber = THSignedPercentage.builder(getAbsoluteGain())
                .withOutSign()
                .build();

        THSignedNumber secondNumber = THSignedMoney.builder(getAbsoluteGain()).build();
        gainLoss.setFirstValue(firstNumber.toString());
        gainLoss.setSecondValue(secondNumber.toString());
        gainLoss.setTitle(getContext().getString(getAbsoluteGain() >= 0 ? R.string.watchlist_gain : R.string.watchlist_loss));
        gainLoss.invalidate();
    }

    private void displayValuation()
    {
        valuation.setFirstValue(formatDisplayValue(getTotalValue()));
        valuation.setSecondValue(formatDisplayValue(getTotalInvested()));
        valuation.invalidate();
    }

    private String formatDisplayValue(double value)
    {
        return THSignedMoney.builder(value).build().toString();
    }

    private double getAbsoluteGain()
    {
        return getTotalValue() - getTotalInvested();
    }

    private double getTotalValue()
    {
        double totalValue = 0.0;
        if (watchlistPositionDTOs != null)
        {
            for (@NotNull WatchlistPositionDTO watchlistItem: watchlistPositionDTOs)
            {
                if (watchlistItem.securityDTO != null
                        && watchlistItem.securityDTO.getLastPriceInUSD() != null
                        && watchlistItem.shares != null)
                {
                    totalValue += watchlistItem.securityDTO.getLastPriceInUSD() * watchlistItem.shares;
                }
            }
        }
        return totalValue;
    }

    private double getTotalInvested()
    {
        double totalInvested = 0.0;

        if (watchlistPositionDTOs != null)
        {
            for (@NotNull WatchlistPositionDTO watchlistItem: watchlistPositionDTOs)
            {
                if (watchlistItem.securityDTO != null)
                {
                    totalInvested += (watchlistItem.watchlistPrice * watchlistItem.securityDTO.toUSDRate) * watchlistItem.shares;
                }
            }
        }
        return totalInvested;
    }

    private void displayMarkingDate()
    {
        if (marking != null)
        {
            marking.setText(
                    getResources().getString(R.string.watchlist_marking_date, getMarkingDate()));
        }
    }

    private String getMarkingDate()
    {
        if (portfolioCompactDTO != null && portfolioCompactDTO.markingAsOfUtc != null)
        {
            return markingDateFormat.format(portfolioCompactDTO.markingAsOfUtc);
        }
        return getResources().getString(R.string.na);
    }

    private BroadcastReceiver createWatchlistItemDeletedReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override public void onReceive(Context context, Intent intent)
            {
                if (userBaseKey != null)
                {
                    display(userBaseKey);
                }
            }
        };
    }
}

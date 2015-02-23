package com.tradehero.th.fragments.onboarding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import butterknife.InjectView;
import com.tradehero.common.api.SelectableDTO;
import com.tradehero.common.persistence.DTO;
import com.tradehero.th.R;
import com.tradehero.th.api.market.SecuritySuperCompactDTOList;
import com.tradehero.th.api.market.WithMarketCap;
import com.tradehero.th.api.market.WithTopSecurities;
import com.tradehero.th.fragments.onboarding.exchange.TopStockListView;
import com.tradehero.th.models.number.THSignedMoney;

public class OnBoardWithMarketStocksView<T extends DTO & WithTopSecurities & WithMarketCap>
        extends OnBoardSelectableViewLinear<T>
{
    @InjectView(R.id.market_cap) TextView marketCapView;
    View marketCapSliderView;
    @InjectView(android.R.id.content) TopStockListView topStockListView;

    //<editor-fold desc="Constructors">
    public OnBoardWithMarketStocksView(Context context)
    {
        super(context);
    }

    public OnBoardWithMarketStocksView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public OnBoardWithMarketStocksView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
    //</editor-fold>

    @Override public void display(@NonNull SelectableDTO<T> dto)
    {
        super.display(dto);
        display((WithMarketCap) dto.value);
        display(dto.value.getTopSecurities());
    }

    protected void display(@Nullable WithMarketCap dto)
    {
        if (marketCapView != null)
        {
            if (dto == null)
            {
                marketCapView.setText("");
            }
            else
            {
                marketCapView.setText(getResources().getString(
                        R.string.exchange_market_cap_abbreviated,
                        THSignedMoney.builder(dto.getSumMarketCap()).build().toString()));
            }
        }
    }

    protected void display(@Nullable SecuritySuperCompactDTOList topStocks)
    {
        if (topStockListView != null)
        {
            topStockListView.display(topStocks);
        }
    }
}

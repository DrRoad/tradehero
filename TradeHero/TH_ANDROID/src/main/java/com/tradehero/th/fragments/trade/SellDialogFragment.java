package com.tradehero.th.fragments.trade;

import com.tradehero.th.R;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.security.TransactionFormDTO;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.utils.metrics.events.SharingOptionsEvent;

public class SellDialogFragment extends AbstractTransactionDialogFragment
{
    private static final boolean IS_BUY = false;

    public SellDialogFragment()
    {
        super();
    }

    @Override protected void setBuyEventFor(SharingOptionsEvent.Builder builder)
    {
        builder.setBuyEvent(IS_BUY);
    }

    @Override protected String getLabel()
    {
        String display = securityCompactDTO == null ? "-" : securityCompactDTO.currencyDisplay;
        THSignedNumber sthSignedNumber = THSignedNumber.builder(quoteDTO.bid)
                .withOutSign()
                .build();
        String sPrice = sthSignedNumber.toString();
        return getString(R.string.buy_sell_button_sell, display, sPrice);
    }

    @Override protected int getCashLeftLabelResId()
    {
        return R.string.buy_sell_share_left;
    }

    @Override public String getCashShareLeft()
    {
        String cashLeftText = getResources().getString(R.string.na);
        if (quoteDTO != null)
        {
            Double priceRefCcy = getPriceCcy();
            if (priceRefCcy != null && portfolioCompactDTO != null)
            {
                Integer maxSellableShares = getMaxSellableShares();
                if (maxSellableShares != null && maxSellableShares != 0)
                {
                    cashLeftText = String.valueOf(maxSellableShares - mTransactionQuantity);//share left
                }
            }
        }
        return cashLeftText;
    }

    @Override protected Integer getMaxValue()
    {
        return getMaxSellableShares();
    }

    @Override protected boolean hasValidInfo()
    {
        return hasValidInfoForSell();
    }

    @Override protected boolean isQuickButtonEnabled()
    {
        if ((quoteDTO == null || quoteDTO.bid == null || quoteDTO.toUSDRate == null))
        {
            return false;
        }
        return true;
    }

    @Override protected double getQuickButtonMaxValue()
    {
        Integer maxSellableShares = getMaxSellableShares();
        if (maxSellableShares != null)
        {
            // TODO see other currencies
            return maxSellableShares * quoteDTO.bid * quoteDTO.toUSDRate;
        }
        return 0;
    }

    @Override protected MiddleCallback<SecurityPositionDetailDTO> getTransactionMiddleCallback(TransactionFormDTO transactionFormDTO)
    {
        return securityServiceWrapper.doTransaction(
                securityId, transactionFormDTO, IS_BUY,
                new BuySellCallback(IS_BUY));
    }

    @Override public Double getPriceCcy()
    {
        if (quoteDTO == null)
        {
            return null;
        }

        return quoteDTO.getPriceRefCcy(portfolioCompactDTO, IS_BUY);
    }

    public Integer getMaxSellableShares()
    {
        return positionDTOCompactList == null ? null :
                positionDTOCompactList.getMaxSellableShares(
                        this.quoteDTO,
                        this.portfolioCompactDTO);
    }

    protected boolean hasValidInfoForSell()
    {
        return securityId != null
                && securityCompactDTO != null
                && quoteDTO != null
                && quoteDTO.bid != null;
    }
}

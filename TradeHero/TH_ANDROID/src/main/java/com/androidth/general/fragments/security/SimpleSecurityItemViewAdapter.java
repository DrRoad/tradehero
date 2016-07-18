package com.androidth.general.fragments.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Filter;

import com.androidth.general.api.quote.QuoteDTO;
import com.androidth.general.api.security.SecurityCompactDTO;
import com.androidth.general.api.security.SecurityIntegerId;
import com.androidth.general.api.security.compact.FxSecurityCompactDTO;
import com.androidth.general.common.widget.filter.ListCharSequencePredicateFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class SimpleSecurityItemViewAdapter extends SecurityItemViewAdapter
{
    protected final Filter filterToUse;
    @Inject ListCharSequencePredicateFilter<SecurityCompactDTO> securityCompactPredicateFilter;

    //<editor-fold desc="Constructors">
    public SimpleSecurityItemViewAdapter(Context context, int layoutResourceId)
    {
        super(context, layoutResourceId);
        filterToUse = new SecurityItemFilter(securityCompactPredicateFilter);
    }
    //</editor-fold>

    @Override public ListCharSequencePredicateFilter<SecurityCompactDTO> getPredicateFilter()
    {
        return securityCompactPredicateFilter;
    }

    @Override public Filter getFilter()
    {
        return filterToUse;
    }



    /*public void updatePrices(@NonNull Context context, @NonNull List<SecurityCompactDTO> quotes)
    {
        Map<Integer, SecurityCompactDTO> map = new HashMap<>();
        for (SecurityCompactDTO quote : quotes)
        {
            map.put(quote.id, quote);
        }
        updatePrices(map);
    }*/
    //we can improve the complexity [Later]
    public void updatePrices(@NonNull LiveQuoteDTO quoteUpdate)// Map is about mapping vertex(in listview) with SecurityCompactDTO
    {
        SecurityCompactDTO securityCompactDTO;
        if (getCount() > 0)
        {
            for (int index = 0; index < getCount(); index++)
            {
                securityCompactDTO = (SecurityCompactDTO) getItem(index);
                if (quoteUpdate != null && quoteUpdate.getSecurityId() == securityCompactDTO.id)
                {
                    Double askPrice = quoteUpdate.getAskPrice();
                    Double bidPrice = quoteUpdate.getBidPrice();
                    if (securityCompactDTO instanceof FxSecurityCompactDTO)
                    {
                        ((FxSecurityCompactDTO) securityCompactDTO).setAskPrice(askPrice);
                        ((FxSecurityCompactDTO) securityCompactDTO).setBidPrice(bidPrice);
                        ((FxSecurityCompactDTO) securityCompactDTO).currencyDisplay = quoteUpdate.getCurrencyDisplay();
                        ((FxSecurityCompactDTO) securityCompactDTO).currencyISO = quoteUpdate.getCurrencyISO();
                        ((FxSecurityCompactDTO) securityCompactDTO).volume = quoteUpdate.getVolume();
                        ((FxSecurityCompactDTO) securityCompactDTO).toUSDRate = quoteUpdate.getUsdRate();
                        ((FxSecurityCompactDTO) securityCompactDTO).lastPrice = (askPrice + bidPrice)/2;
                    }
                    else
                    {

                        securityCompactDTO.askPrice = askPrice;
                        securityCompactDTO.bidPrice = bidPrice;
                        securityCompactDTO.currencyDisplay = quoteUpdate.getCurrencyDisplay();
                        securityCompactDTO.currencyISO = quoteUpdate.getCurrencyISO();
                        securityCompactDTO.volume = quoteUpdate.getVolume();
                        securityCompactDTO.toUSDRate = quoteUpdate.getUsdRate();
                        securityCompactDTO.lastPrice = (askPrice + bidPrice)/2;
                    }
                }
            }
        }
    }

    public void updatePricesQuoteDTO(@NonNull Context context, @NonNull List<? extends QuoteDTO> quotes)
    {
        Map<SecurityIntegerId, QuoteDTO> map = new HashMap<>();
        for (QuoteDTO quote : quotes)
        {
            map.put(quote.getSecurityIntegerId(), quote);
        }
        updatePricesQuoteDTO(map);
    }

    public void updatePricesQuoteDTO(@NonNull Map<SecurityIntegerId, ? extends QuoteDTO> quotes)
    {
        SecurityCompactDTO securityCompactDTO;
        QuoteDTO quote;
        if (getCount() > 0)
        {
            for (int index = 0; index < getCount(); index++)
            {
                securityCompactDTO = (SecurityCompactDTO) getItem(index);
                quote = quotes.get(securityCompactDTO.getSecurityIntegerId());
                if (quote != null)
                {
                    if (securityCompactDTO instanceof FxSecurityCompactDTO)
                    {
                        ((FxSecurityCompactDTO) securityCompactDTO).setAskPrice(quote.ask);
                        ((FxSecurityCompactDTO) securityCompactDTO).setBidPrice(quote.bid);
                    }
                    else
                    {
                        securityCompactDTO.askPrice = quote.ask;
                        securityCompactDTO.bidPrice = quote.bid;
                    }
                }
            }
        }
    }
}
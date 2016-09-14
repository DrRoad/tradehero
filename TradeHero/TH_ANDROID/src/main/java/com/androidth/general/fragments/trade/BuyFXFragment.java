package com.androidth.general.fragments.trade;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.android.internal.util.Predicate;
import com.androidth.general.R;
import com.androidth.general.api.portfolio.PortfolioCompactDTO;
import com.androidth.general.api.portfolio.PortfolioId;
import com.androidth.general.api.position.PositionDTO;
import com.androidth.general.api.position.PositionDTOCompact;
import com.androidth.general.api.position.PositionDTOList;
import com.androidth.general.api.quote.QuoteDTO;
import com.androidth.general.api.security.SecurityCompactDTO;
import com.androidth.general.api.security.TransactionFormDTO;
import com.androidth.general.fragments.security.LiveQuoteDTO;
import com.androidth.general.models.number.THSignedNumber;
import com.androidth.general.rx.view.DismissDialogAction0;
import com.androidth.general.utils.metrics.events.SharingOptionsEvent;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;

public class BuyFXFragment extends AbstractFXTransactionFragment
{
    private static final boolean IS_BUY = true;

    @SuppressWarnings("UnusedDeclaration") @Inject Context doNotRemoveOrItFails;

    @Override protected void setBuyEventFor(SharingOptionsEvent.Builder builder)
    {
        builder.setBuyEvent(IS_BUY);
    }

    @Override protected String getLabel(@NonNull LiveQuoteDTO quoteDTO)
    {
        if (quoteDTO.getAskPrice() == null)
        {
            return getString(R.string.na);
        }
        THSignedNumber bThSignedNumber = getFormattedPrice(quoteDTO.getAskPrice());
        return bThSignedNumber.toString();
    }

    @Override @Nullable protected Double getProfitOrLossUsd(
            @Nullable PortfolioCompactDTO portfolioCompactDTO,
            @Nullable LiveQuoteDTO quoteDTO,
            @Nullable PositionDTOCompact closeablePosition,
            @Nullable Integer quantity)
    {
        if (portfolioCompactDTO == null
                || quoteDTO == null
                || quoteDTO.getAskPrice() == null)
        {
            return null;
        }
        if (closeablePosition == null || closeablePosition.averagePriceSecCcy == null || quantity == null)
        {
            return null;
        }

        return -quantity * quoteDTO.getUsdRate() * (quoteDTO.getAskPrice() - closeablePosition.averagePriceSecCcy);
    }

    @Override @NonNull public String getCashShareLeft(
            @NonNull PortfolioCompactDTO portfolioCompactDTO,
            @NonNull LiveQuoteDTO quoteDTO,
            @Nullable PositionDTOCompact closeablePosition, int quantity)
    {
        return getRemainingWhenBuy(portfolioCompactDTO, quoteDTO, closeablePosition, quantity);
    }

    @Override @Nullable protected Integer getMaxValue(
            @NonNull PortfolioCompactDTO portfolioCompactDTO,
            @NonNull LiveQuoteDTO quoteDTO,
            @Nullable PositionDTOCompact closeablePosition)
    {
        return getMaxPurchasableShares(portfolioCompactDTO, quoteDTO, closeablePosition);
    }

//    @Override protected Boolean isBuyTransaction()
//    {
//        return true;
//    }
//
//    @Override protected int getCashShareLabel()
//    {
//        return R.string.buy_sell_cash_available;
//    }

    @Override protected boolean hasValidInfo()
    {
        return hasValidInfoForBuy();
    }

    @Override protected Subscription getTransactionSubscription(TransactionFormDTO transactionFormDTO)
    {
        final ProgressDialog progressDialog = ProgressDialog.show(
                getActivity(),
                getActivity().getString(R.string.processing),
                getActivity().getString(R.string.alert_dialog_please_wait),
                true);

        return AppObservable.bindSupportFragment(
                this,
                securityServiceWrapper.doTransactionRx(requisite.securityId, transactionFormDTO, IS_BUY))
                .observeOn(AndroidSchedulers.mainThread())
                .finallyDo(new DismissDialogAction0(progressDialog))
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog))
                .subscribe(new BuySellObserver(requisite.securityId, transactionFormDTO, IS_BUY));
    }

    @Nullable @Override public Double getPriceCcy(@Nullable PortfolioCompactDTO portfolioCompactDTO,
            @Nullable LiveQuoteDTO quoteDTO)
    {
        if (quoteDTO == null)
        {
            return null;
        }

        return quoteDTO.getPriceRefCcy(portfolioCompactDTO, IS_BUY);
    }

    @NonNull @Override protected Predicate<PositionDTO> getCloseablePositionPredicate(
            @NonNull PositionDTOList positionDTOs,
            @NonNull final PortfolioId portfolioId)
    {
        return new Predicate<PositionDTO>()
        {
            @Override public boolean apply(PositionDTO positionDTO)
            {
                return positionDTO.portfolioId.equals(portfolioId.key)
                        && positionDTO.shares != null
                        && positionDTO.shares < 0;
            }
        };
    }

//    @Override protected void initSecurityRelatedInfo(@Nullable SecurityCompactDTO securityCompactDTO)
//    {
//        setActionBarTitle(getString(R.string.transaction_title_buy,
//                securityCompactDTO != null ? securityCompactDTO.getExchangeSymbol() : getString(R.string.fx)));
//    }

    protected boolean hasValidInfoForBuy()
    {
        return usedDTO.securityCompactDTO != null
                && usedDTO.quoteDTO != null
                && usedDTO.quoteDTO.getAskPrice() != null;
    }
}

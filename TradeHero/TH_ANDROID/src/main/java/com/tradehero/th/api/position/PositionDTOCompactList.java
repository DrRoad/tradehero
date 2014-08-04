package com.tradehero.th.api.position;

import com.tradehero.common.api.BaseArrayList;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.portfolio.PortfolioId;
import com.tradehero.th.api.quote.QuoteDTO;
import com.tradehero.th.utils.SecurityUtils;

public class PositionDTOCompactList extends BaseArrayList<PositionDTOCompact>
{
    //<editor-fold desc="Constructors">
    public PositionDTOCompactList()
    {
        super();
    }
    //</editor-fold>

    public Integer getShareCountIn(PortfolioId portfolioId)
    {
        if (portfolioId == null || portfolioId.key == null)
        {
            return null;
        }

        int sum = 0;
        for (PositionDTOCompact positionDTOCompact: this)
        {
            if (positionDTOCompact.portfolioId == portfolioId.key && positionDTOCompact.shares != null)
            {
                sum += positionDTOCompact.shares;
            }
        }
        return sum;
    }

    //<editor-fold desc="Max Net Sell Proceeds USD">
    /**
     * If it returns a negative number it means it will eat into the cash available.
     * @param quoteDTO
     * @param portfolioId
     * @param includeTransactionCostUsd
     * @return
     */
    public Double getMaxNetSellProceedsUsd(
            QuoteDTO quoteDTO,
            PortfolioId portfolioId,
            boolean includeTransactionCostUsd)
    {
        return getMaxNetSellProceedsUsd(
                quoteDTO,
                portfolioId,
                includeTransactionCostUsd,
                SecurityUtils.DEFAULT_TRANSACTION_COST_USD);
    }

    /**
     * If it returns a negative number it means it will eat into the cash available.
     * @param quoteDTO
     * @param portfolioId
     * @param includeTransactionCostUsd
     * @param txnCostUsd
     * @return
     */
    public Double getMaxNetSellProceedsUsd(
            QuoteDTO quoteDTO,
            PortfolioId portfolioId,
            boolean includeTransactionCostUsd,
            double txnCostUsd)
    {
        if (quoteDTO == null || portfolioId == null || portfolioId.key == null)
        {
            return null;
        }
        Double bidUsd = quoteDTO.getBidUSD();
        Integer shareCount = getShareCountIn(portfolioId);
        if (bidUsd == null || shareCount == null)
        {
            return null;
        }
        return shareCount * bidUsd - (includeTransactionCostUsd ? txnCostUsd : 0);
    }
    //</editor-fold>

    //<editor-fold desc="Max Sellable Shares">
    public Integer getMaxSellableShares(
            QuoteDTO quoteDTO,
            PortfolioCompactDTO portfolioCompactDTO)
    {
        return getMaxSellableShares(quoteDTO, portfolioCompactDTO, true);
    }

    public Integer getMaxSellableShares(
            QuoteDTO quoteDTO,
            PortfolioCompactDTO portfolioCompactDTO,
            boolean includeTransactionCost)
    {
        if (quoteDTO == null || portfolioCompactDTO == null)
        {
            return null;
        }
        double txnCostUsd = portfolioCompactDTO.getProperTxnCostUsd();
        Integer shareCount = getShareCountIn(portfolioCompactDTO.getPortfolioId());
        Double netSellProceedsUsd = getMaxNetSellProceedsUsd(quoteDTO, portfolioCompactDTO.getPortfolioId(), includeTransactionCost, txnCostUsd);
        if (netSellProceedsUsd == null)
        {
            return null;
        }
        netSellProceedsUsd += portfolioCompactDTO.getCashBalanceUsd();

        // If we are underwater after a sell, we cannot sell
        return netSellProceedsUsd < 0 ? 0 : shareCount;
    }
    //</editor-fold>
}

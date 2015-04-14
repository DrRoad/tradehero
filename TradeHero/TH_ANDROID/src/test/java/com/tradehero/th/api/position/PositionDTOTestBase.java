package com.tradehero.th.api.position;

abstract public class PositionDTOTestBase extends PositionDTOCompactTestBase
{
    public boolean haveSameFields(PositionDTO left, PositionDTO right)
    {
        boolean have = super.haveSameFields(left, right);
        have &= left.userId == right.userId;
        have &= left.securityId == right.securityId;
        have &= left.realizedPLRefCcy == null ? right.realizedPLRefCcy == null : left.realizedPLRefCcy.equals(right.realizedPLRefCcy);
        have &= left.unrealizedPLRefCcy == null ? right.unrealizedPLRefCcy == null : left.unrealizedPLRefCcy.equals(right.unrealizedPLRefCcy);
        have &= left.marketValueRefCcy == right.marketValueRefCcy;
        have &= left.earliestTradeUtc == null ? right.earliestTradeUtc == null : left.earliestTradeUtc.equals(right.earliestTradeUtc);
        have &= left.latestTradeUtc == null ? right.latestTradeUtc == null : left.latestTradeUtc.equals(right.latestTradeUtc);
        have &= left.sumInvestedAmountRefCcy == null ? right.sumInvestedAmountRefCcy == null : left.sumInvestedAmountRefCcy.equals(right.sumInvestedAmountRefCcy);
        have &= left.totalTransactionCostRefCcy == right.totalTransactionCostRefCcy;
        have &= left.aggregateCount == right.aggregateCount;
        return have;
    }
}

package com.tradehero.th.api.position;

/** Created with IntelliJ IDEA. User: xavier Date: 9/20/13 Time: 3:28 PM To change this template use File | Settings | File Templates. */
public class PositionDTO extends PositionDTOCompact
{
    public int userId;
    public int securityId;
    public Double realizedPLRefCcy;
    public Double unrealizedPLRefCcy;
    public double marketValueRefCcy;
    public String earliestTradeUtc;
    public String latestTradeUtc;

    public Double sumInvestedAmountRefCcy;

    public double totalTransactionCostRefCcy;

    // if >1, then the values above relate to a collection of positions, not a single position -- see: MaskOpenPositions()
    public int aggregateCount;

    public OwnedPositionId getOwnedPositionId()
    {
        return new OwnedPositionId(userId, securityId);
    }
}

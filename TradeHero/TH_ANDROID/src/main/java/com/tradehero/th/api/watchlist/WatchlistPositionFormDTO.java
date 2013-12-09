package com.tradehero.th.api.watchlist;

/** Created with IntelliJ IDEA. User: tho Date: 12/3/13 Time: 5:49 PM Copyright (c) TradeHero */
public class WatchlistPositionFormDTO
{
    public int securityId;
    public double price;
    public int quantity;

    public WatchlistPositionFormDTO(int securityId, double price, int quantity)
    {
        this.securityId = securityId;
        this.price = price;
        this.quantity = quantity;
    }
}

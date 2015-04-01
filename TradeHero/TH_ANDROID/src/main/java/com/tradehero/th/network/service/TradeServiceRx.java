package com.tradehero.th.network.service;

import com.tradehero.th.api.trade.TradeDTO;
import com.tradehero.th.api.trade.TradeDTOList;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface TradeServiceRx
{
    //<editor-fold desc="Get One Position Trades List">
    @GET("/users/{userId}/portfolios/{portfolioId}/positions/{positionId}/trades")
    Observable<TradeDTOList> getTrades(
            @Path("userId") int userId,
            @Path("portfolioId") int portfolioId,
            @Path("positionId") int positionId);
    //</editor-fold>

    //<editor-fold desc="Get One Security Trades List">
    @GET("/trades/{exchange}/{symbol}/history")
    Observable<TradeDTOList> getTrades(
            @Path("exchange") String exchange,
            @Path("symbol") String symbol,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage);
    //</editor-fold>

    //<editor-fold desc="Get Single Trade">
    @GET("/users/{userId}/portfolios/{portfolioId}/positions/{positionId}/trades/{tradeId}")
    Observable<TradeDTO> getTrade(
            @Path("userId") int userId,
            @Path("portfolioId") int portfolioId,
            @Path("positionId") int positionId,
            @Path("tradeId") int tradeId);
    //</editor-fold>
}

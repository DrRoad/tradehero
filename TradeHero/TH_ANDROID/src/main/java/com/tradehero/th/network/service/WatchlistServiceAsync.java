package com.tradehero.th.network.service;

import com.tradehero.th.api.security.SecurityIntegerIdListForm;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.api.watchlist.WatchlistPositionFormDTO;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

interface WatchlistServiceAsync
{
    //<editor-fold desc="Add a watch item">
    @POST("/watchlistPositions")
    void createWatchlistEntry(
            @Body WatchlistPositionFormDTO watchlistPositionFormDTO,
            Callback<WatchlistPositionDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Edit a watch item">
    @PUT("/watchlistPositions/{position}")
    void updateWatchlistEntry(
            @Path("position") int position,
            @Body WatchlistPositionFormDTO watchlistPositionFormDTO,
            Callback<WatchlistPositionDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Batch Create Watchlist Positions">
    @POST("/batchCreateWatchlistPositions")
    void batchCreate(
            @Body SecurityIntegerIdListForm securityIntegerIds,
            Callback<WatchlistPositionDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Query for watchlist">
    @GET("/watchlistPositions")
    void getAllByUser(
            @Query("pageNumber") Integer pageNumber,
            @Query("perPage") Integer perPage,
            @Query("securityId") Integer securityId,
            @Query("skipCache") Boolean skipCache,
            Callback<WatchlistPositionDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Delete Watchlist">
    @DELETE("/watchlistPositions/{watchlistId}")
    void deleteWatchlist(@Path("watchlistId") int watchlistId,
            Callback<WatchlistPositionDTO> callback);
    //</editor-fold>
}

package com.tradehero.th.network.service;

import com.tradehero.th.api.billing.PurchaseReportDTO;
import com.tradehero.th.api.portfolio.PortfolioCompactDTOList;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface PortfolioServiceRx
{
    //<editor-fold desc="Get User Portfolio List">
    @GET("/users/{userId}/portfolios")
    Observable<PortfolioCompactDTOList> getPortfolios(
            @Path("userId") int userId,
            @Query("includeWatchlist") Boolean includeWatchList);
    //</editor-fold>

    //<editor-fold desc="Get One User Portfolio">
    @GET("/users/{userId}/portfolios/{portfolioId}")
    Observable<PortfolioDTO> getPortfolio(
            @Path("userId") int userId,
            @Path("portfolioId") int portfolioId);
    //</editor-fold>

    //<editor-fold desc="Reset One User Portfolio">
    @POST("/users/{userId}/portfolios/{portfolioId}/reset")
    Observable<UserProfileDTO> resetPortfolio(
            @Path("userId") int userId,
            @Path("portfolioId") int portfolioId,
            @Body PurchaseReportDTO purchaseReportDTO);
    //</editor-fold>

    //<editor-fold desc="Add Cash">
    @POST("/users/{userId}/portfolios/{portfolioId}/addcash")
    Observable<UserProfileDTO> addCash(
            @Path("userId") int userId,
            @Path("portfolioId") int portfolioId,
            @Body PurchaseReportDTO purchaseReportDTO);
    //</editor-fold>

    //<editor-fold desc="Mark One User Portfolio">
    @POST("/users/{userId}/portfolios/{portfolioId}/mark")
    Observable<PortfolioDTO> markPortfolio(
            @Path("userId") int userId,
            @Path("portfolioId") int portfolioId);
    //</editor-fold>
}

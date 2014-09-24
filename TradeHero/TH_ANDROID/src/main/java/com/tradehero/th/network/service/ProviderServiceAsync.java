package com.tradehero.th.network.service;

import com.tradehero.th.api.competition.HelpVideoDTOList;
import com.tradehero.th.api.competition.ProviderDTOList;
import com.tradehero.th.api.competition.ProviderDisplayCellDTOList;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

interface ProviderServiceAsync
{
    //<editor-fold desc="Get Providers">
    @GET("/providers?detailed=false") void getProviders(Callback<ProviderDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Provider Securities">
    @GET("/providers/{providerId}/securities")
    void getSecurities(
            @Path("providerId") int providerId,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Provider Warrant Underlyers">
    @GET("/providers/{providerId}/warrantUnderlyers")
    void getWarrantUnderlyers(
            @Path("providerId") int providerId,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Search Competition Securities">
    @GET("/providers/{providerId}/securities")
    void searchSecurities(
            @Path("providerId") int providerId,
            @Query("q") String searchString,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Help Videos">
    @GET("/providers/{providerId}/helpVideos")
    void getHelpVideos(
            @Path("providerId") int providerId,
            Callback<HelpVideoDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Cells">
    @GET("/providers/{providerId}/displaycells")
    void getDisplayCells(
            @Path("providerId") int providerId,
            Callback<ProviderDisplayCellDTOList> callback);
    //</editor-fold>
}

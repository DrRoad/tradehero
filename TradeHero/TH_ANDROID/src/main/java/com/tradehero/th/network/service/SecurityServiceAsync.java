package com.tradehero.th.network.service;

import com.tradehero.chinabuild.data.SecurityOrderDTO;
import com.tradehero.firmbargain.ActualSecurityDTO;
import com.tradehero.firmbargain.ActualSecurityListDTO;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import com.tradehero.th.api.security.TransactionFormDTO;
import java.util.Map;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

interface SecurityServiceAsync
{
    //<editor-fold desc="Get Multiple Securities">
    @GET("/securities/multi/")
    void getMultipleSecurities(
            @Query("securityIds") String commaSeparatedIntegerIds,
            Callback<Map<Integer, SecurityCompactDTO>> callback);
    //</editor-fold>

    //<editor-fold desc="Get Basic Trending">
    @GET("/securities/trending/")
    void getTrendingSecurities(
            @Query("exchange") String exchange,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Trending By Volume">
    @GET("/securities/trendingVol/")
    void getTrendingSecuritiesByVolume(
            @Query("exchange") String exchange,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Trending By Price">
    @GET("/securities/trendingPrice/")
    void getTrendingSecuritiesByPrice(
            @Query("exchange") String exchange,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Trending For All">
    @GET("/securities/trendingExchange/")
    void getTrendingSecuritiesAllInExchange(
            @Query("exchange") String exchange,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Search Securities">
    @GET("/securities/search")
    void searchSecurities(
            @Query("q") String searchString,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<SecurityCompactDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Security">
    @GET("/securities/{exchange}/{pathSafeSecuritySymbol}")
    void getSecurity(
            @Path("exchange") String exchange,
            @Path("pathSafeSecuritySymbol") String pathSafeSecuritySymbol,
            Callback<SecurityPositionDetailDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Buy Security">
    @POST("/securities/{exchange}/{securitySymbol}/newbuy")
    void buy(
            @Path("exchange") String exchange,
            @Path("securitySymbol") String securitySymbol,
            @Body() TransactionFormDTO transactionFormDTO,
            Callback<SecurityPositionDetailDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Sell Security">
    @POST("/securities/{exchange}/{securitySymbol}/newsell")
    void sell(
            @Path("exchange") String exchange,
            @Path("securitySymbol") String securitySymbol,
            @Body() TransactionFormDTO transactionFormDTO,
            Callback<SecurityPositionDetailDTO> callback);
    //</editor-fold>


    @POST("/cn/v2/orders")void order(
            @Body()SecurityOrderDTO securityOrderDTO,
            Callback<Response> callback);

    @GET("/cn/v2/securities/searchChina") void searchSecuritySHESHA(
            @Query("q") String q,
            @Query("page") int page,
            @Query("perPage") int perPage,
            Callback<ActualSecurityListDTO> callback
    );
}

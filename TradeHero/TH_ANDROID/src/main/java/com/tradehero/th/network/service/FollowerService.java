package com.tradehero.th.network.service;

import com.tradehero.th.api.social.FollowerSummaryDTO;
import com.tradehero.th.api.social.UserFollowerDTO;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface FollowerService
{
    //<editor-fold desc="Get All Followers Summary">
    @GET("/followersSummary/all/{heroId}")
    FollowerSummaryDTO getAllFollowersSummary(
            @Path("heroId") int heroId);
    //</editor-fold>

    //<editor-fold desc="Get Follower Subscription Detail">
    @GET("/followersSummary/{heroId}/{followerId}")
    UserFollowerDTO getFollowerSubscriptionDetail(
            @Path("heroId") int heroId,
            @Path("followerId") int followerId);
    //</editor-fold>

    //<editor-fold desc="Get All Followers Summary">
    @GET("/followersSummary/all/{heroId}")
    void getFollowers(
            @Path("heroId") int heroId,
            @Query("perPage") int perPage,
            @Query("page") int page,
            Callback<FollowerSummaryDTO> callback);
    //</editor-fold>
}

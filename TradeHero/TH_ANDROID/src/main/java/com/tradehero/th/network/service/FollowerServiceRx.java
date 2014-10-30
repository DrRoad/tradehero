package com.tradehero.th.network.service;

import com.tradehero.th.api.social.FollowerSummaryDTO;
import com.tradehero.th.api.social.UserFollowerDTO;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

interface FollowerServiceRx
{
    //<editor-fold desc="Get All Followers Summary">
    @GET("/followersSummary/all/{heroId}")
    Observable<FollowerSummaryDTO> getAllFollowersSummary(
            @Path("heroId") int heroId);
    //</editor-fold>

    //<editor-fold desc="Get Follower Subscription Detail">
    @GET("/followersSummary/{heroId}/{followerId}")
    Observable<UserFollowerDTO> getFollowerSubscriptionDetail(
            @Path("heroId") int heroId,
            @Path("followerId") int followerId);
    //</editor-fold>
}

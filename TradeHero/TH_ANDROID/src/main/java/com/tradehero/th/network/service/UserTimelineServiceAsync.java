package com.tradehero.th.network.service;

import com.tradehero.chinabuild.data.AdsDTO;
import com.tradehero.chinabuild.data.TimeLineTotalInfo;
import com.tradehero.th.api.timeline.TimelineDTO;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

import java.util.List;

interface UserTimelineServiceAsync
{
    //<editor-fold desc="Get User Timeline">
    @GET("/users/{userId}/timeline?type=filtered&includeComment=false&includeTrade=true")
    void getTimelineNew(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>


    //最新动态
    @GET("/users/{userId}/timeline?type=original")
    void getTimelineSquare(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>

    //悬赏帖子
    @GET("/users/{userId}/timeline?type=question")
    void getTimelineReward(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>

    //股神动态
    @GET("/users/{userId}/timeline?type=recommended&includeComment=false&includeTrade=true")
    void getTimelineStockGodNews(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>

    //新手教学贴
    @GET("/users/{userId}/timeline?type=guide")
    void getTimelineLearning(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>

    //精华贴
    @GET("/users/{userId}/timeline?type=essential")
    void getTimelineEssential(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>

    //公告
    @GET("/users/{userId}/timeline?type=notice")
    void getTimelineNotice(
            @Path("userId") int userId,
            @Query("maxCount") Integer maxCount,
            @Query("maxId") Integer maxId,
            @Query("minId") Integer minId,
            Callback<TimelineDTO> callback);
    //</editor-fold>

    //Advertisement
    @GET("/misc/ads")
    void downloadAdvertisements(Callback<List<AdsDTO>> callback);
    //</editor-fold>

    //TimeLine Total Information
    @GET("/misc/timelineActivity")
    void retrieveTimeLineTotalInfo(Callback<TimeLineTotalInfo> callback);
}

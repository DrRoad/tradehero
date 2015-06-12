package com.tradehero.th.network.service;

import com.tradehero.chinabuild.data.SecurityCommentList;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.pagination.PaginatedDTO;
import com.tradehero.th.api.timeline.TimelineItemShareRequestDTO;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface DiscussionService
{
    //<editor-fold desc="Get Comment">
    @GET("/discussions/{commentId}")
    DiscussionDTO getComment(@Path("commentId") int commentId);
    //</editor-fold>

    //<editor-fold desc="Get Discussions">
    @Deprecated
    @GET("/discussions/{inReplyToType}/{inReplyToId}")
    PaginatedDTO<DiscussionDTO> getDiscussions(
            @Path("inReplyToType") DiscussionType inReplyToType,
            @Path("inReplyToId") int inReplyToId,
            @Query("page") Integer page, // = 1
            @Query("perPage") Integer perPage); // = 42

    @GET("/discussions/{inReplyToType}/{inReplyToId}/getMessages")
    PaginatedDTO<DiscussionDTO> getMessageThread(
            @Path("inReplyToType") DiscussionType inReplyToType,
            @Path("inReplyToId") int inReplyToId,
            @QueryMap Map<String, Object> options);
    //</editor-fold>

    //<editor-fold desc="Share">
    @POST("/discussions/{inReplyToType}/{inReplyToId}/share")
    DiscussionDTO share(
            @Path("inReplyToType") DiscussionType inReplyToType,
            @Path("inReplyToId") int inReplyToId,
            @Body TimelineItemShareRequestDTO timelineItemShareRequestDTO);
    //</editor-fold>

    @GET("/cn/v2/securities/{exchange}/{securitySymbol}/discussions")
    void getSecurityDiscussion(@Path("exchange") String exchange,
                               @Path("securitySymbol") String securitySymbol,
                               @Query("page") Integer page,
                               @Query("perPage") Integer perPage,
                               Callback<SecurityCommentList> callback);
}

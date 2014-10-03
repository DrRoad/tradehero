package com.tradehero.th.network.service;

import com.tradehero.th.api.share.TrackShareDTO;
import com.tradehero.th.api.share.wechat.WeChatTrackShareFormDTO;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

interface WeChatServiceAsync
{
    @POST("/users/{userId}/trackshare")
    void trackShare(
            @Path("userId") int userId,
            @Body WeChatTrackShareFormDTO weChatTrackShareFormDTO,
            Callback<TrackShareDTO> callback);
}

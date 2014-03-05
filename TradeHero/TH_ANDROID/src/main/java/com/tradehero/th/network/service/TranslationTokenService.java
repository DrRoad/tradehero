package com.tradehero.th.network.service;

import com.tradehero.th.models.translation.TokenData;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by tradehero on 14-3-5.
 * First step to request translation
 */
public interface TranslationTokenService {
    @FormUrlEncoded
    @POST("/v2/OAuth2-13")
    void requestToken(
            @Field("scope") String scope,
            @Field("grant_type") String grantYype,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            Callback<TokenData> callback

    );

}

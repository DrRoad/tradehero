package com.tradehero.th.network.service;

import com.tradehero.th.api.users.LoginSignUpFormDTO;
import com.tradehero.th.api.users.UserLoginDTO;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;
import rx.Observable;

interface SessionServiceRx
{
    //<editor-fold desc="Login and social register">
    @POST("/signupAndLogin")
    Observable<UserLoginDTO> signupAndLogin(
            @Header("Authorization") String authorization, @Body LoginSignUpFormDTO loginSignUpFormDTO);
    //</editor-fold>
}

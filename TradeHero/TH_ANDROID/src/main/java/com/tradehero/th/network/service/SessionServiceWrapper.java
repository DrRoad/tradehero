package com.tradehero.th.network.service;

import android.app.NotificationManager;
import android.content.Context;

import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.LoginFormDTO;
import com.tradehero.th.api.users.LoginSignUpFormDTO;
import com.tradehero.th.api.users.UserLoginDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.models.DTOProcessor;
import com.tradehero.th.models.user.DTOProcessorLogout;
import com.tradehero.th.models.user.DTOProcessorUpdateUserProfile;
import com.tradehero.th.models.user.DTOProcessorUserLogin;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.persistence.DTOCacheUtil;
import com.tradehero.th.persistence.prefs.SavedPushDeviceIdentifier;
import com.tradehero.th.persistence.system.SystemStatusCache;
import com.tradehero.th.persistence.user.UserProfileCache;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import retrofit.Callback;
import retrofit.client.Response;

@Singleton public class SessionServiceWrapper
{
    @NotNull private final CurrentUserId currentUserId;
    @NotNull private final SessionService sessionService;
    @NotNull private final SessionServiceAsync sessionServiceAsync;
    @NotNull private final UserProfileCache userProfileCache;
    @NotNull private final DTOCacheUtil dtoCacheUtil;
    @NotNull private final Context context;
    @NotNull private final StringPreference savedPushDeviceIdentifier;
    @NotNull private final Lazy<SystemStatusCache> systemStatusCache;

    //<editor-fold desc="Constructors">
    @Inject public SessionServiceWrapper(
            @NotNull CurrentUserId currentUserId,
            @NotNull SessionService sessionService,
            @NotNull SessionServiceAsync sessionServiceAsync,
            @NotNull UserProfileCache userProfileCache,
            @NotNull DTOCacheUtil dtoCacheUtil,
            @NotNull Context context,
            @NotNull @SavedPushDeviceIdentifier StringPreference savedPushDeviceIdentifier,
            @NotNull Lazy<SystemStatusCache> systemStatusCache)
    {
        this.currentUserId = currentUserId;
        this.sessionService = sessionService;
        this.sessionServiceAsync = sessionServiceAsync;
        this.userProfileCache = userProfileCache;
        this.dtoCacheUtil = dtoCacheUtil;
        this.context = context;
        this.savedPushDeviceIdentifier = savedPushDeviceIdentifier;
        this.systemStatusCache = systemStatusCache;
    }
    //</editor-fold>

    //<editor-fold desc="DTO Processors">
    protected DTOProcessor<UserLoginDTO> createUserLoginProcessor()
    {
        return new DTOProcessorUserLogin(
                systemStatusCache.get(),
                userProfileCache,
                currentUserId,
                dtoCacheUtil);
    }

    protected DTOProcessor<UserProfileDTO> createUpdateDeviceProcessor()
    {
        return new DTOProcessorUpdateUserProfile(userProfileCache);
    }

    protected DTOProcessor<UserProfileDTO> createLogoutProcessor()
    {
        return new DTOProcessorLogout(
                dtoCacheUtil,
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
    }
    //</editor-fold>

    //<editor-fold desc="Login">
    public UserLoginDTO login(String authorization, LoginFormDTO loginFormDTO)
    {
        return createUserLoginProcessor().process(sessionService.login(authorization, loginFormDTO));
    }

    public MiddleCallback<UserLoginDTO> login(String authorization, LoginFormDTO loginFormDTO, Callback<UserLoginDTO> callback)
    {
        MiddleCallback<UserLoginDTO> middleCallback = new BaseMiddleCallback<>(callback, createUserLoginProcessor());
        sessionServiceAsync.login(authorization, loginFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Login and social register">
    public UserLoginDTO signupAndLogin(String authorization, LoginSignUpFormDTO loginSignUpFormDTO)
    {
        return sessionService.signupAndLogin(authorization, loginSignUpFormDTO);
    }

    public MiddleCallback<UserLoginDTO> signupAndLogin(String authorization, LoginSignUpFormDTO loginSignUpFormDTO, Callback<UserLoginDTO> callback)
    {
        MiddleCallback<UserLoginDTO> middleCallback = new BaseMiddleCallback<>(callback, createUserLoginProcessor());
        sessionServiceAsync.signupAndLogin(authorization, loginSignUpFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Logout">
    public UserProfileDTO logout()
    {
        return createLogoutProcessor().process(sessionService.logout());
    }

    public MiddleCallback<UserProfileDTO> logout(Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createLogoutProcessor());
        sessionServiceAsync.logout(middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Update Device">

    public MiddleCallback<UserProfileDTO> updateDevice(Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createUpdateDeviceProcessor());
        sessionServiceAsync.updateDevice(savedPushDeviceIdentifier.get(), middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    public MiddleCallback<Response> updateAuthorizationTokens(UserFormDTO userFormDTO, Callback<Response> callback)
    {
        MiddleCallback<Response> middleCallback = new BaseMiddleCallback<>(callback);
        sessionServiceAsync.updateAuthorizationTokens(userFormDTO, middleCallback);
        return middleCallback;
    }
}

package com.tradehero.th.network.service;

import android.app.NotificationManager;
import android.content.Context;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.users.CurrentUserId;
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
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.Callback;

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
    @NotNull protected DTOProcessor<UserLoginDTO> createUserLoginProcessor()
    {
        return new DTOProcessorUserLogin(
                systemStatusCache.get(),
                userProfileCache,
                currentUserId,
                dtoCacheUtil);
    }

    @NotNull protected DTOProcessor<UserProfileDTO> createUpdateDeviceProcessor()
    {
        return new DTOProcessorUpdateUserProfile(userProfileCache);
    }

    @NotNull protected DTOProcessor<UserProfileDTO> createLogoutProcessor()
    {
        return new DTOProcessorLogout(
                dtoCacheUtil,
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
    }
    //</editor-fold>

    //<editor-fold desc="Login">
    @NotNull public UserLoginDTO login(
            @NotNull String authorization,
            @NotNull LoginSignUpFormDTO loginFormDTO)
    {
        return createUserLoginProcessor().process(sessionService.login(authorization, loginFormDTO));
    }

    @NotNull public MiddleCallback<UserLoginDTO> login(
            @NotNull String authorization,
            @NotNull LoginSignUpFormDTO loginFormDTO,
            @Nullable Callback<UserLoginDTO> callback)
    {
        MiddleCallback<UserLoginDTO> middleCallback = new BaseMiddleCallback<>(callback, createUserLoginProcessor());
        sessionServiceAsync.login(authorization, loginFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Login and social register">
    @NotNull public UserLoginDTO signupAndLogin(
            @NotNull String authorization,
            @NotNull LoginSignUpFormDTO loginSignUpFormDTO)
    {
        return sessionService.signupAndLogin(authorization, loginSignUpFormDTO);
    }

    @NotNull public MiddleCallback<UserLoginDTO> signupAndLogin(
            @NotNull String authorization,
            @NotNull LoginSignUpFormDTO loginSignUpFormDTO,
            @Nullable Callback<UserLoginDTO> callback)
    {
        MiddleCallback<UserLoginDTO> middleCallback = new BaseMiddleCallback<>(callback, createUserLoginProcessor());
        sessionServiceAsync.signupAndLogin(authorization, loginSignUpFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Logout">
    @NotNull public UserProfileDTO logout()
    {
        return createLogoutProcessor().process(sessionService.logout());
    }

    @NotNull public MiddleCallback<UserProfileDTO> logout(@Nullable Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createLogoutProcessor());
        sessionServiceAsync.logout(middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Update Device">
    @NotNull public UserProfileDTO updateDevice()
    {
        return sessionService.updateDevice(savedPushDeviceIdentifier.get());
    }

    @NotNull public MiddleCallback<UserProfileDTO> updateDevice(@Nullable Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createUpdateDeviceProcessor());
        sessionServiceAsync.updateDevice(savedPushDeviceIdentifier.get(), middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Update Authorization Tokens">
    @NotNull BaseResponseDTO updateAuthorizationTokens(@NotNull UserFormDTO userFormDTO)
    {
        return sessionService.updateAuthorizationTokens(userFormDTO);
    }

    @NotNull public MiddleCallback<BaseResponseDTO> updateAuthorizationTokens(
            @NotNull UserFormDTO userFormDTO,
            @Nullable Callback<BaseResponseDTO> callback)
    {
        MiddleCallback<BaseResponseDTO> middleCallback = new BaseMiddleCallback<>(callback);
        sessionServiceAsync.updateAuthorizationTokens(userFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>
}

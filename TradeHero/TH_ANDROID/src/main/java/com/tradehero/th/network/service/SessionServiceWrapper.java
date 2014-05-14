package com.tradehero.th.network.service;

import android.content.Context;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.models.DTOProcessor;
import com.tradehero.th.models.user.DTOProcessorLogout;
import com.tradehero.th.models.user.DTOProcessorUpdateUserProfile;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.persistence.DTOCacheUtil;
import com.tradehero.th.persistence.prefs.SavedPushDeviceIdentifier;
import com.tradehero.th.persistence.user.UserProfileCache;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit.Callback;

@Singleton public class SessionServiceWrapper
{
    private final SessionService sessionService;
    private final UserProfileCache userProfileCache;
    private final DTOCacheUtil dtoCacheUtil;
    private final Context context;
    private final StringPreference savedPushDeviceIdentifier;

    @Inject public SessionServiceWrapper(SessionService sessionService,
            UserProfileCache userProfileCache,
            DTOCacheUtil dtoCacheUtil, Context context,
            @SavedPushDeviceIdentifier StringPreference savedPushDeviceIdentifier)
    {
        this.sessionService = sessionService;
        this.userProfileCache = userProfileCache;
        this.dtoCacheUtil = dtoCacheUtil;
        this.context = context;
        this.savedPushDeviceIdentifier = savedPushDeviceIdentifier;
    }

    //<editor-fold desc="DTO Processors">
    protected DTOProcessor<UserProfileDTO> createUpdateDeviceProcessor()
    {
        return new DTOProcessorUpdateUserProfile(userProfileCache);
    }

    protected DTOProcessor<UserProfileDTO> createLogoutProcessor()
    {
        return new DTOProcessorLogout(userProfileCache, dtoCacheUtil, context);
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
        sessionService.logout(middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Update Device">

    public MiddleCallback<UserProfileDTO> updateDevice(Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createUpdateDeviceProcessor());
        sessionService.updateDevice(savedPushDeviceIdentifier.get(), middleCallback);
        return middleCallback;
    }
    //</editor-fold>
}

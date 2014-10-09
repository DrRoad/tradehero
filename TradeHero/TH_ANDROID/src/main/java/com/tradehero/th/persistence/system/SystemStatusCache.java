package com.tradehero.th.persistence.system;

import com.tradehero.common.persistence.StraightDTOCacheNew;
import com.tradehero.th.api.system.SystemStatusDTO;
import com.tradehero.th.api.users.LoginSignUpFormDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.network.service.SessionServiceWrapper;
import com.tradehero.th.persistence.prefs.AuthHeader;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton public class SystemStatusCache extends StraightDTOCacheNew<UserBaseKey, SystemStatusDTO>
{
    public static final int DEFAULT_MAX_SIZE = 1;

    @NotNull private final Lazy<SessionServiceWrapper> sessionService;
    @NotNull private final Provider<LoginSignUpFormDTO.Builder2> loginFormDTOBuilderProvider;
    private final String authenticationHeader;

    @Inject public SystemStatusCache(
            @NotNull Lazy<SessionServiceWrapper> sessionService,
            @NotNull Provider<LoginSignUpFormDTO.Builder2> loginFormDTOBuilderProvider,
            @AuthHeader String authenticationHeader)
    {
        super(DEFAULT_MAX_SIZE);
        this.sessionService = sessionService;
        this.loginFormDTOBuilderProvider = loginFormDTOBuilderProvider;
        this.authenticationHeader = authenticationHeader;
    }

    @Override @NotNull public SystemStatusDTO fetch(@NotNull UserBaseKey key) throws Throwable
    {
        return sessionService.get().login(authenticationHeader, loginFormDTOBuilderProvider.get().build()).systemStatusDTO;
    }
}

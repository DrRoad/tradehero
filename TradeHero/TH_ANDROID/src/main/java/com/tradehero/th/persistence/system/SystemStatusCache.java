package com.tradehero.th.persistence.system;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.system.SystemStatusDTO;
import com.tradehero.th.api.system.SystemStatusKey;
import com.tradehero.th.network.service.SessionServiceWrapper;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class SystemStatusCache extends BaseFetchDTOCacheRx<SystemStatusKey, SystemStatusDTO>
{
    public static final int DEFAULT_MAX_SIZE = 1;

    @NonNull private final Lazy<SessionServiceWrapper> sessionService;

    @Inject public SystemStatusCache(
            @NonNull Lazy<SessionServiceWrapper> sessionService,
            @NonNull DTOCacheUtilRx dtoCacheUtilRx)
    {
        super(DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE, dtoCacheUtilRx);
        this.sessionService = sessionService;
    }

    @Override @NonNull protected Observable<SystemStatusDTO> fetch(@NonNull SystemStatusKey key)
    {
        return sessionService.get().getSystemStatusRx();
    }
}

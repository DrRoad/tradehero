package com.tradehero.th.persistence.position;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.network.service.SecurityServiceWrapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class SecurityPositionDetailCacheRx extends BaseFetchDTOCacheRx<SecurityId, SecurityPositionDetailDTO>
{
    public static final int DEFAULT_MAX_VALUE_SIZE = 100;

    @NonNull protected final SecurityServiceWrapper securityServiceWrapper;

    //<editor-fold desc="Constructors">
    @Inject protected SecurityPositionDetailCacheRx(
            @NonNull SecurityServiceWrapper securityServiceWrapper,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_MAX_VALUE_SIZE, dtoCacheUtil);
        this.securityServiceWrapper = securityServiceWrapper;
    }
    //</editor-fold>

    @NonNull @Override protected Observable<SecurityPositionDetailDTO> fetch(@NonNull SecurityId key)
    {
        return securityServiceWrapper.getSecurityPositionDetailRx(key);
    }
}

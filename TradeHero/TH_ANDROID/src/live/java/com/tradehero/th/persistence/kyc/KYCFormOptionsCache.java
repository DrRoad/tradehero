package com.tradehero.th.persistence.kyc;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.kyc.KYCFormOptionsDTO;
import com.tradehero.th.api.kyc.KYCFormOptionsId;
import com.tradehero.th.network.service.LiveServiceWrapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class KYCFormOptionsCache extends BaseFetchDTOCacheRx<KYCFormOptionsId, KYCFormOptionsDTO>
{
    private static final int DEFAULT_CACHE_SIZE = 5;
    @NonNull private final LiveServiceWrapper liveServiceWrapper;

    @Inject public KYCFormOptionsCache(@NonNull DTOCacheUtilRx dtoCacheUtilRx, @NonNull LiveServiceWrapper liveServiceWrapper)
    {
        super(DEFAULT_CACHE_SIZE, dtoCacheUtilRx);
        this.liveServiceWrapper = liveServiceWrapper;
    }

    @NonNull @Override protected Observable<KYCFormOptionsDTO> fetch(@NonNull KYCFormOptionsId key)
    {
        return liveServiceWrapper.getKYCFormOptions(key);
    }
}

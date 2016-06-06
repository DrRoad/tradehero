package com.ayondo.academyapp.persistence.kyc;

import android.support.annotation.NonNull;
import com.androidth.general.common.persistence.BaseFetchDTOCacheRx;
import com.androidth.general.common.persistence.DTOCacheUtilRx;
import com.androidth.general.common.persistence.UserCache;
import com.androidth.general.api.kyc.KYCFormOptionsDTO;
import com.androidth.general.api.kyc.KYCFormOptionsId;
import com.androidth.general.network.service.LiveServiceWrapper;
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

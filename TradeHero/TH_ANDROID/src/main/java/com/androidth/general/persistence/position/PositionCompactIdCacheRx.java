package com.androidth.general.persistence.position;

import android.support.annotation.NonNull;
import com.androidth.general.common.persistence.BaseDTOCacheRx;
import com.androidth.general.common.persistence.DTOCacheUtilRx;
import com.androidth.general.common.persistence.SystemCache;
import com.androidth.general.api.position.OwnedPositionId;
import com.androidth.general.api.position.PositionCompactId;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton @SystemCache
public class PositionCompactIdCacheRx extends BaseDTOCacheRx<PositionCompactId, OwnedPositionId>
{
    public static final int DEFAULT_MAX_VALUE_SIZE = 2000;

    //<editor-fold desc="Constructors">
    @Inject public PositionCompactIdCacheRx(@NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_MAX_VALUE_SIZE, dtoCacheUtil);
    }
    //</editor-fold>
}
package com.tradehero.th.persistence.position;

import com.tradehero.common.persistence.DTOCacheUtilNew;
import com.tradehero.common.persistence.StraightDTOCacheNew;
import com.tradehero.common.persistence.SystemCache;
import com.tradehero.th.api.position.OwnedPositionId;
import com.tradehero.th.api.position.PositionCompactId;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton @SystemCache
public class PositionCompactIdCache extends StraightDTOCacheNew<PositionCompactId, OwnedPositionId>
{
    public static final int DEFAULT_MAX_SIZE = 2000;

    //<editor-fold desc="Constructors">
    @Inject public PositionCompactIdCache(@NotNull DTOCacheUtilNew dtoCacheUtil)
    {
        super(DEFAULT_MAX_SIZE, dtoCacheUtil);
    }
    //</editor-fold>

    @Override @NotNull public OwnedPositionId fetch(@NotNull PositionCompactId key)
    {
        throw new IllegalStateException("You should not fetch for OwnedPositionId");
    }
}

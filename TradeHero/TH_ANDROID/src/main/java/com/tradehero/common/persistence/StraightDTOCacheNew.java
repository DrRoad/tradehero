package com.tradehero.common.persistence;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class StraightDTOCacheNew<DTOKeyType extends DTOKey, DTOType extends DTO>
        extends PartialDTOCacheNew<DTOKeyType, DTOType>
{
    @NotNull final private THLruCache<DTOKeyType, CacheValue<DTOKeyType, DTOType>> lruCache;

    //<editor-fold desc="Constructors">
    public StraightDTOCacheNew(int maxSize)
    {
        super();
        this.lruCache = new THLruCache<>(maxSize);
    }
    //</editor-fold>

    @Override @Nullable protected CacheValue<DTOKeyType, DTOType> getCacheValue(@NotNull DTOKeyType key)
    {
        return lruCache.get(key);
    }

    @Override protected void putCacheValue(@NotNull DTOKeyType key, @NotNull CacheValue<DTOKeyType, DTOType> cacheValue)
    {
        lruCache.put(key, cacheValue);
    }

    @Override public void invalidate(@NotNull DTOKeyType key)
    {
        lruCache.remove(key);
    }

    @Override public void invalidateAll()
    {
        try{
            lruCache.evictAll();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override public void unregister(@Nullable Listener<DTOKeyType, DTOType> callback)
    {
        if (callback != null)
        {
            for (@NotNull CacheValue<DTOKeyType, DTOType> value : lruCache.snapshot().values())
            {
                value.unregisterListener(callback);
            }
        }
    }

    @NotNull protected Map<DTOKeyType, CacheValue<DTOKeyType, DTOType>> snapshot()
    {
        return lruCache.snapshot();
    }
}

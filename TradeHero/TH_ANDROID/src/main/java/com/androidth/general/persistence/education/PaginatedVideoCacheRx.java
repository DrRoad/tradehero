package com.androidth.general.persistence.education;

import android.support.annotation.NonNull;
import com.androidth.general.common.persistence.BaseFetchDTOCacheRx;
import com.androidth.general.common.persistence.DTOCacheUtilRx;
import com.androidth.general.common.persistence.UserCache;
import com.androidth.general.api.education.PaginatedVideoDTO;
import com.androidth.general.api.education.VideoCategoryId;
import com.androidth.general.api.education.VideoDTO;
import com.androidth.general.network.service.VideoServiceWrapper;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class PaginatedVideoCacheRx extends BaseFetchDTOCacheRx<VideoCategoryId, PaginatedVideoDTO>
{
    private static final int DEFAULT_MAX_VALUE_SIZE = 50;

    @NonNull private final VideoCacheRx videoCache;
    @NonNull private final VideoServiceWrapper videoServiceWrapper;

    //<editor-fold desc="Constructors">
    @Inject public PaginatedVideoCacheRx(
            @NonNull VideoCacheRx videoCache,
            @NonNull VideoServiceWrapper videoServiceWrapper,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_MAX_VALUE_SIZE, dtoCacheUtil);
        this.videoCache = videoCache;
        this.videoServiceWrapper = videoServiceWrapper;
    }
    //</editor-fold>

    @NonNull @Override protected Observable<PaginatedVideoDTO> fetch(@NonNull VideoCategoryId key)
    {
        return videoServiceWrapper.getVideosRx(key);
    }

    @Override public void onNext(@NonNull VideoCategoryId key, @NonNull PaginatedVideoDTO value)
    {
        List<VideoDTO> data = value.getData();
        if (data != null)
        {
            videoCache.onNext(data);
        }
        super.onNext(key, value);
    }
}
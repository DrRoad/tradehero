package com.tradehero.th.persistence.achievement;

import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.achievement.AchievementCategoryDTOList;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.network.service.AchievementServiceWrapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import android.support.annotation.NonNull;
import rx.Observable;

@Singleton @UserCache
public class AchievementCategoryListCacheRx extends BaseFetchDTOCacheRx<UserBaseKey, AchievementCategoryDTOList>
{
    private static final int DEFAULT_SIZE = 1;
    private final AchievementServiceWrapper achievementServiceWrapper;
    private final AchievementCategoryCacheRx achievementCategoryCache;

    //<editor-fold desc="Constructors">
    @Inject public AchievementCategoryListCacheRx(
            @NonNull AchievementServiceWrapper achievementServiceWrapper,
            @NonNull AchievementCategoryCacheRx achievementCategoryCache,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, dtoCacheUtil);
        this.achievementServiceWrapper = achievementServiceWrapper;
        this.achievementCategoryCache = achievementCategoryCache;
    }
    //</editor-fold>

    @NonNull @Override public Observable<AchievementCategoryDTOList> fetch(@NonNull UserBaseKey key)
    {
        return achievementServiceWrapper.getAchievementCategoriesRx(key);
    }

    @Override public void onNext(@NonNull UserBaseKey key, @NonNull AchievementCategoryDTOList value)
    {
        achievementCategoryCache.onNext(key, value);
        super.onNext(key, value);
    }
}

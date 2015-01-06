package com.tradehero.th.persistence.user;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.leaderboard.LeaderboardCacheRx;
import com.tradehero.th.persistence.social.VisitedFriendListPrefs;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class UserProfileCacheRx extends BaseFetchDTOCacheRx<UserBaseKey, UserProfileDTO>
{
    public static final int DEFAULT_MAX_VALUE_SIZE = 1000;
    public static final int DEFAULT_MAX_SUBJECT_SIZE = 10;

    @NonNull private final Lazy<UserServiceWrapper> userServiceWrapper;
    @NonNull private final Lazy<UserProfileCompactCacheRx> userProfileCompactCache;
    @NonNull private final Lazy<LeaderboardCacheRx> leaderboardCache;

    //<editor-fold desc="Constructors">
    @Inject public UserProfileCacheRx(
            @NonNull Lazy<UserServiceWrapper> userServiceWrapper,
            @NonNull Lazy<UserProfileCompactCacheRx> userProfileCompactCache,
            @NonNull Lazy<LeaderboardCacheRx> leaderboardCache,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_MAX_VALUE_SIZE, DEFAULT_MAX_SUBJECT_SIZE, DEFAULT_MAX_SUBJECT_SIZE, dtoCacheUtil);
        this.userServiceWrapper = userServiceWrapper;
        this.userProfileCompactCache = userProfileCompactCache;
        this.leaderboardCache = leaderboardCache;
    }
    //</editor-fold>

    @Override @NonNull protected Observable<UserProfileDTO> fetch(@NonNull UserBaseKey key)
    {
        VisitedFriendListPrefs.addVisitedId(key);
        return userServiceWrapper.get().getUserRx(key);
    }

    @Override public void onNext(@NonNull UserBaseKey key, @NonNull UserProfileDTO userProfileDTO)
    {
        if (userProfileDTO.mostSkilledLbmu != null)
        {
            leaderboardCache.get().onNext(userProfileDTO.getMostSkilledUserOnLbmuKey(), userProfileDTO.mostSkilledLbmu);
        }
        userProfileCompactCache.get().onNext(key, userProfileDTO);
        super.onNext(key, userProfileDTO);
    }

    public void updateXPIfNecessary(@NonNull UserBaseKey userBaseKey, int newXpTotal)
    {
        UserProfileDTO userProfileDTO = getValue(userBaseKey);
        if(userProfileDTO != null && userProfileDTO.currentXP < newXpTotal)
        {
            userProfileDTO.currentXP = newXpTotal;
            onNext(userBaseKey, userProfileDTO);
        }
    }

    public void addAchievements(@NonNull UserBaseKey userBaseKey, int count)
    {
        if (count <= 0)
        {
            throw new IllegalArgumentException("Cannot handle count=" + count);
        }
        UserProfileDTO userProfileDTO = getValue(userBaseKey);
        if(userProfileDTO != null)
        {
            userProfileDTO.achievementCount += count;
            onNext(userBaseKey, userProfileDTO);
        }
    }
}

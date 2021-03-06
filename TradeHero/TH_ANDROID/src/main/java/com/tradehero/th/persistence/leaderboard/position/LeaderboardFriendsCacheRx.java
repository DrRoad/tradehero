package com.tradehero.th.persistence.leaderboard.position;

import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.leaderboard.position.LeaderboardFriendsDTO;
import com.tradehero.th.api.leaderboard.position.LeaderboardFriendsKey;
import com.tradehero.th.network.service.LeaderboardServiceWrapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import android.support.annotation.NonNull;
import rx.Observable;

@Singleton @UserCache
public class LeaderboardFriendsCacheRx extends BaseFetchDTOCacheRx<LeaderboardFriendsKey, LeaderboardFriendsDTO>
{
    public static final int DEFAULT_MAX_SIZE = 1;

    @NonNull private final LeaderboardServiceWrapper leaderboardServiceWrapper;

    @Inject public LeaderboardFriendsCacheRx(
            @NonNull LeaderboardServiceWrapper leaderboardServiceWrapper,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE, dtoCacheUtil);
        this.leaderboardServiceWrapper = leaderboardServiceWrapper;
    }

    @NonNull @Override protected Observable<LeaderboardFriendsDTO> fetch(@NonNull LeaderboardFriendsKey key)
    {
        return leaderboardServiceWrapper.getNewFriendsLeaderboardRx();
    }
}

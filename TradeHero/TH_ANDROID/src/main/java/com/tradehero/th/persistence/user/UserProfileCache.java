package com.tradehero.th.persistence.user;

import com.tradehero.common.persistence.StraightDTOCacheNew;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.leaderboard.LeaderboardCache;
import com.tradehero.th.persistence.message.MessageHeaderListCache;
import com.tradehero.th.persistence.notification.NotificationListCache;
import com.tradehero.th.persistence.social.HeroListCache;
import com.tradehero.th.persistence.social.VisitedFriendListPrefs;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton public class UserProfileCache extends StraightDTOCacheNew<UserBaseKey, UserProfileDTO>
{
    public static final int DEFAULT_MAX_SIZE = 1000;

    @NotNull private final Lazy<UserServiceWrapper> userServiceWrapper;
    @NotNull private final Lazy<UserProfileCompactCache> userProfileCompactCache;
    @NotNull private final Lazy<HeroListCache> heroListCache;
    @NotNull private final Lazy<LeaderboardCache> leaderboardCache;
    @NotNull private final Lazy<MessageHeaderListCache> messageHeaderListCache;
    @NotNull private final Lazy<NotificationListCache> notificationListCache;

    //<editor-fold desc="Constructors">
    @Inject public UserProfileCache(
            @NotNull Lazy<UserServiceWrapper> userServiceWrapper,
            @NotNull Lazy<UserProfileCompactCache> userProfileCompactCache,
            @NotNull Lazy<HeroListCache> heroListCache,
            @NotNull Lazy<LeaderboardCache> leaderboardCache,
            @NotNull Lazy<MessageHeaderListCache> messageHeaderListCache,
            @NotNull Lazy<NotificationListCache> notificationListCache)
    {
        super(DEFAULT_MAX_SIZE);
        this.userServiceWrapper = userServiceWrapper;
        this.userProfileCompactCache = userProfileCompactCache;
        this.heroListCache = heroListCache;
        this.leaderboardCache = leaderboardCache;
        this.messageHeaderListCache = messageHeaderListCache;
        this.notificationListCache = notificationListCache;
    }
    //</editor-fold>

    @Override @NotNull public UserProfileDTO fetch(@NotNull UserBaseKey key) throws Throwable
    {
        VisitedFriendListPrefs.addVisitedId(key);
        return userServiceWrapper.get().getUser(key);
    }

    @Override public UserProfileDTO put(@NotNull UserBaseKey userBaseKey, @NotNull UserProfileDTO userProfileDTO)
    {
        heroListCache.get().invalidate(userBaseKey);
        if (userProfileDTO.mostSkilledLbmu != null)
        {
            leaderboardCache.get().put(userProfileDTO.getMostSkilledUserOnLbmuKey(), userProfileDTO.mostSkilledLbmu);
        }
        userProfileCompactCache.get().put(userBaseKey, userProfileDTO);
        UserProfileDTO previous = super.put(userBaseKey, userProfileDTO);
        if (previous != null)
        {
            if (previous.unreadMessageThreadsCount != userProfileDTO.unreadMessageThreadsCount)
            {
                messageHeaderListCache.get().invalidateAll();
            }
            if (previous.unreadNotificationsCount != userProfileDTO.unreadNotificationsCount)
            {
                notificationListCache.get().invalidateAll();
            }
        }
        return previous;
    }

    public void updateXPIfNecessary(@NotNull UserBaseKey userBaseKey, int newXpTotal)
    {
        UserProfileDTO userProfileDTO = get(userBaseKey);
        if(userProfileDTO != null && userProfileDTO.currentXP < newXpTotal)
        {
            userProfileDTO.currentXP = newXpTotal;
            put(userBaseKey, userProfileDTO);
        }
    }

    public void addAchievements(@NotNull UserBaseKey userBaseKey, int count)
    {
        if (count <= 0)
        {
            throw new IllegalArgumentException("Cannot handle count=" + count);
        }
        UserProfileDTO userProfileDTO = get(userBaseKey);
        if(userProfileDTO != null)
        {
            userProfileDTO.achievementCount += count;
            put(userBaseKey, userProfileDTO);
        }
    }
}

package com.tradehero.th.api.users;

import com.tradehero.th.api.alert.UserAlertPlanDTO;
import com.tradehero.th.api.leaderboard.LeaderboardDTO;
import com.tradehero.th.api.leaderboard.UserLeaderboardRankingDTO;
import com.tradehero.th.api.leaderboard.key.UserOnLeaderboardKey;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.purchase.UserCreditPlanDTO;
import com.tradehero.th.models.leaderboard.key.LeaderboardDefKeyKnowledge;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UserProfileDTO extends UserProfileCompactDTO
{
    public String email;
    public String address;
    public String biography;
    public String location;
    public String website;

    public List<Integer> heroIds;
    public List<Integer> freeHeroIds;
    public List<Integer> premiumHeroIds;
    public Integer followerCount;
    /**newly added fields*/
    public int allHeroCount;
    public int allFollowerCount;
    public int freeFollowerCount;
    public int paidFollowerCount;
    /**newly added fields*/

    public Integer ccPerMonthBalance;   // recurring monthly balance (not used, old)
    public Double ccBalance;       // non-recurring: CC spot level

    public PortfolioDTO portfolio;

    public String paypalEmailAddress;
    public String alipayAccount;
    public String alipayIdentityNumber;
    public String alipayRealName;

    public boolean pushNotificationsEnabled;
    public boolean emailNotificationsEnabled;

    public List<UserLeaderboardRankingDTO> rank;
            // // user's top-traders ranking across all LBs

    public boolean firstFollowAllTime;

    public boolean useTHPrice;

    @Deprecated
    public int unreadCount;
    public int alertCount;

    public int unreadMessageThreadsCount;
    public int unreadNotificationsCount;

    public int tradesSharedCount_FB;
    public String referralCode;
    public String inviteCode;

    public List<UserAlertPlanDTO> userAlertPlans;
    public List<UserCreditPlanDTO> userCreditPlans;

    public boolean competitionAutoEnrollOnFirstLaunch;
    public LeaderboardDTO mostSkilledLbmu;

    public int achievementCount;
    public int currentXp;

    public UserOnLeaderboardKey getMostSkilledUserOnLbmuKey()
    {
        return new UserOnLeaderboardKey(LeaderboardDefKeyKnowledge.MOST_SKILLED_ID, id);
    }

    public boolean isFollowingUser(int userId)
    {
        return this.heroIds != null && this.heroIds.contains(userId);
    }

    public boolean isFollowingUser(UserBaseKey userBaseKey)
    {
        return userBaseKey != null && isFollowingUser(userBaseKey.key);
    }

    public boolean isFollowingUser(UserBaseDTO userBaseDTO)
    {
        return userBaseDTO != null && isFollowingUser(userBaseDTO.id);
    }

    public boolean isPremiumFollowingUser(@NotNull UserBaseKey userBaseKey)
    {
        return this.premiumHeroIds != null && this.premiumHeroIds.contains(userBaseKey.key);
    }

    public int getFollowType(UserBaseKey userBaseKey)
    {
        return userBaseKey == null ? UserProfileDTOUtil.IS_NOT_FOLLOWER : getFollowType(userBaseKey.key);
    }

    public int getFollowType(UserBaseDTO userBaseDTO)
    {
        return userBaseDTO == null ? UserProfileDTOUtil.IS_NOT_FOLLOWER : getFollowType(userBaseDTO.id);
    }

    public int getFollowType(int userId)
    {
        if (this.heroIds != null)
        {
            if (this.freeHeroIds != null)
            {
                if (this.freeHeroIds.contains(userId))
                {
                    return UserProfileDTOUtil.IS_FREE_FOLLOWER;
                }
            }
            if (this.premiumHeroIds != null)
            {
                if (this.premiumHeroIds.contains(userId))
                {
                    return UserProfileDTOUtil.IS_PREMIUM_FOLLOWER;
                }
            }
        }
        return UserProfileDTOUtil.IS_NOT_FOLLOWER;
    }

    public List<UserBaseKey> getHeroBaseKeys()
    {
        if (heroIds == null)
        {
            return null;
        }
        List<UserBaseKey> heroKeys = new ArrayList<>();
        for (Integer heroId: heroIds)
        {
            if (heroId != null)
            {
                heroKeys.add(new UserBaseKey(heroId));
            }
        }
        return heroKeys;
    }

    public int getLeaderboardRanking(int leaderboardId)
    {
        for (UserLeaderboardRankingDTO userLeaderboardRankingDTO: rank)
        {
            if (userLeaderboardRankingDTO.leaderboardId == leaderboardId)
            {
                // 1st-base ranking
                return (userLeaderboardRankingDTO.ordinalPosition + 1);
            }
        }
        return 0;
    }

    public int getUserAlertPlansAlertCount()
    {
        int count = 0;
        if (userAlertPlans != null)
        {
            for (UserAlertPlanDTO userAlertPlanDTO: userAlertPlans)
            {
                if (userAlertPlanDTO != null && userAlertPlanDTO.alertPlan != null)
                {
                    count += userAlertPlanDTO.alertPlan.numberOfAlerts;
                }
            }
        }
        return count;
    }

    public boolean alreadyClaimedInvitedDollars()
    {
        return inviteCode != null && !inviteCode.isEmpty();
    }

    @Override public String toString()
    {
        return "UserProfileDTO{" +
                "displayName='" + displayName + '\'' +
                ", id=" + id +
                ", picture='" + picture + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", memberSince=" + memberSince +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", biography='" + biography + '\'' +
                ", location='" + location + '\'' +
                ", website='" + website + '\'' +
                ", heroIds=" + heroIds +
                ", followerCount=" + followerCount +
                ", ccPerMonthBalance=" + ccPerMonthBalance +
                ", ccBalance=" + ccBalance +
                ", portfolio=" + portfolio +
                ", paypalEmailAddress='" + paypalEmailAddress + '\'' +
                ", alipayAccount='" + alipayAccount + '\'' +
                ", alipayIdentityNumber='" + alipayIdentityNumber + '\'' +
                ", alipayRealName='" + alipayRealName + '\'' +
                ", pushNotificationsEnabled=" + pushNotificationsEnabled +
                ", emailNotificationsEnabled=" + emailNotificationsEnabled +
                ", rank=" + rank +
                ", firstFollowAllTime=" + firstFollowAllTime +
                ", useTHPrice=" + useTHPrice +
                ", alertCount=" + alertCount +
                ", tradesSharedCount_FB=" + tradesSharedCount_FB +
                ", userAlertPlans=" + userAlertPlans +
                '}';
    }
}
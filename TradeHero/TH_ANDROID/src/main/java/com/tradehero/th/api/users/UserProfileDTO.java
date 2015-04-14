package com.tradehero.th.api.users;

import com.tradehero.th.api.alert.UserAlertPlanDTO;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.leaderboard.UserLeaderboardRankingDTO;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserProfileDTO extends UserProfileCompactDTO
{
    public String email;
    public String address;
    public String biography;
    public String location;
    public String website;
    public boolean isVisitor;

    public List<Integer> heroIds;
    public List<Integer> freeHeroIds;
    public List<Integer> premiumHeroIds;
    public Integer followerCount;
    /** newly added fields */
    public int allHeroCount;
    public int allFollowerCount;
    /** newly added fields */

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

    public int alertCount;

    public int unreadMessageThreadsCount;
    public int unreadNotificationsCount;

    public int tradesSharedCount_FB;
    public String referralCode;
    public String inviteCode;

    public List<UserAlertPlanDTO> userAlertPlans;
    public List<ProviderDTO> enrolledProviders;


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

    public int getFollowType(UserBaseKey userBaseKey)
    {
        return userBaseKey == null ? UserProfileDTOUtil.IS_NOT_FOLLOWER : getFollowType(userBaseKey.key);
    }

    public int getFollowType(UserBaseDTO userBaseDTO)
    {
        return userBaseDTO == null ? UserProfileDTOUtil.IS_NOT_FOLLOWER : getFollowType(userBaseDTO.id);
    }

    public int getAllHeroCount()
    {
        if (heroIds != null)
        {
            return heroIds.size();
        }
        else
        {
            return allHeroCount;
        }
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
        for (Integer heroId : heroIds)
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
        for (UserLeaderboardRankingDTO userLeaderboardRankingDTO : rank)
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
            for (UserAlertPlanDTO userAlertPlanDTO : userAlertPlans)
            {
                if (userAlertPlanDTO != null && userAlertPlanDTO.alertPlan != null)
                {
                    count += userAlertPlanDTO.alertPlan.numberOfAlerts;
                }
            }
        }
        return count;
    }

    public boolean isHaveSchool()
    {
        if (StringUtils.isNullOrEmpty(school)) return false;
        return true;
    }

    @Override public String toString()
    {
        return "UserProfileDTO{" +
                "displayName='" + displayName + '\'' +
                ", id=" + id +
                ", inviteCode=" + inviteCode +
                ", referralCode=" + referralCode +
                ", isVisitor=" + isVisitor +
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
                ", enrolledProviders=" + enrolledProviders +
                ", school=" + school +
                '}';
    }

    public String getUnReadNotificationCount()
    {
        if (unreadNotificationsCount > 99)
        {
            return "99";
        }
        else
        {
            return String.valueOf(unreadNotificationsCount);
        }
    }
}
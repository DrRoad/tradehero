package com.tradehero.th.fragments.leaderboard.main;

import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTOList;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.MostSkilledLeaderboardDefListKey;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefListCache;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

class CommunityPageDTOFactory
{
    @NotNull private final LeaderboardDefListCache leaderboardDefListCache;
    @NotNull private final MainLeaderboardDefListKeyFactory leaderboardDefListKeyFactory;

    //<editor-fold desc="Constructors">
    @Inject CommunityPageDTOFactory(
            @NotNull LeaderboardDefListCache leaderboardDefListCache,
            @NotNull MainLeaderboardDefListKeyFactory leaderboardDefListKeyFactory)
    {
        this.leaderboardDefListCache = leaderboardDefListCache;
        this.leaderboardDefListKeyFactory = leaderboardDefListKeyFactory;
    }
    //</editor-fold>

    @NotNull public CommunityPageDTOList collectFromCaches(@Nullable String countryCode)
    {
        @NotNull CommunityPageDTOList collected = new CommunityPageDTOList();
        @Nullable LeaderboardDefListKey key;
        @Nullable LeaderboardDefDTOList cached;
        for (LeaderboardCommunityType type : LeaderboardCommunityType.values())
        {
            key = leaderboardDefListKeyFactory.createFrom(type);
            Timber.e("Type %s, key %s", type, key);
            if (key != null)
            {
                cached = leaderboardDefListCache.get(key);
                if (cached != null)
                {
                    collected.addAllLeaderboardDefDTO(cached);
                }
                if (countryCode != null && key.equals(new MostSkilledLeaderboardDefListKey()))
                {
                    collected.addAllLeaderboardDefDTO(collectForCountryCodeFromCaches(countryCode));
                }
            }
        }
        return collected;
    }

    @NotNull public LeaderboardDefDTOList collectForCountryCodeFromCaches(@NotNull String countryCode)
    {
        LeaderboardDefDTOList allKeys = leaderboardDefListCache.get(new LeaderboardDefListKey());
        if (allKeys != null)
        {
            return allKeys.keepForCountryCode(countryCode);
        }
        else
        {
            return new LeaderboardDefDTOList();
        }
    }
}

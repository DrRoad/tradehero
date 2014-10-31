package com.tradehero.th.persistence.leaderboard;

import com.tradehero.common.persistence.DTOCacheUtilNew;
import com.tradehero.common.persistence.StraightCutDTOCacheNew;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardDTO;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardId;
import com.tradehero.th.network.service.CompetitionServiceWrapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton @UserCache
public class CompetitionLeaderboardCache extends StraightCutDTOCacheNew<CompetitionLeaderboardId, CompetitionLeaderboardDTO, CompetitionLeaderboardCutDTO>
{
    public static final int DEFAULT_MAX_SIZE = 1000;

    @NotNull private final CompetitionServiceWrapper competitionServiceWrapper;
    @NotNull private final LeaderboardCache leaderboardCache;

    //<editor-fold desc="Constructors">
    @Inject public CompetitionLeaderboardCache(
            @NotNull CompetitionServiceWrapper competitionServiceWrapper,
            @NotNull LeaderboardCache leaderboardCache,
            @NotNull DTOCacheUtilNew dtoCacheUtil)
    {
        this(DEFAULT_MAX_SIZE, competitionServiceWrapper, leaderboardCache, dtoCacheUtil);
    }

    public CompetitionLeaderboardCache(
            int maxSize,
            @NotNull CompetitionServiceWrapper competitionServiceWrapper,
            @NotNull LeaderboardCache leaderboardCache,
            @NotNull DTOCacheUtilNew dtoCacheUtil)
    {
        super(maxSize, dtoCacheUtil);
        this.competitionServiceWrapper = competitionServiceWrapper;
        this.leaderboardCache = leaderboardCache;
    }
    //</editor-fold>

    @Override @NotNull public CompetitionLeaderboardDTO fetch(@NotNull CompetitionLeaderboardId key) throws Throwable
    {
        return competitionServiceWrapper.getCompetitionLeaderboard(key);
    }

    @NotNull @Override protected CompetitionLeaderboardCutDTO cutValue(
            @NotNull CompetitionLeaderboardId key,
            @NotNull CompetitionLeaderboardDTO value)
    {
        return new CompetitionLeaderboardCutDTO(value, leaderboardCache);
    }

    @Nullable @Override protected CompetitionLeaderboardDTO inflateValue(
            @NotNull CompetitionLeaderboardId key,
            @Nullable CompetitionLeaderboardCutDTO cutValue)
    {
        if (cutValue == null)
        {
            return null;
        }
        return cutValue.create(leaderboardCache);
    }
}

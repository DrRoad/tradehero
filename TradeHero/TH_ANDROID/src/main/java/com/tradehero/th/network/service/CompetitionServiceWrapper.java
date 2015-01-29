package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import com.tradehero.th.api.competition.CompetitionDTO;
import com.tradehero.th.api.competition.CompetitionDTOList;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.competition.key.CompetitionId;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardDTO;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardId;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class CompetitionServiceWrapper
{
    @NonNull private final CompetitionServiceRx competitionServiceRx;

    //<editor-fold desc="Constructors">
    @Inject public CompetitionServiceWrapper(
            @NonNull CompetitionServiceRx competitionServiceRx)
    {
        super();
        this.competitionServiceRx = competitionServiceRx;
    }
    //</editor-fold>

    //<editor-fold desc="Get Competitions">
    @NonNull public Observable<CompetitionDTOList> getCompetitionsRx(@NonNull ProviderId providerId)
    {
        return this.competitionServiceRx.getCompetitions(providerId.key);
    }
    //</editor-fold>

    //<editor-fold desc="Get Competition">
    @NonNull public Observable<CompetitionDTO> getCompetitionRx(@NonNull CompetitionId competitionId)
    {
        return competitionServiceRx.getCompetition(competitionId.key);
    }
    //</editor-fold>

    //<editor-fold desc="Get Competition Leaderboard">
    @NonNull public Observable<CompetitionLeaderboardDTO> getCompetitionLeaderboardRx(@NonNull CompetitionLeaderboardId competitionLeaderboardId)
    {
        return this.competitionServiceRx.getCompetitionLeaderboard(
                competitionLeaderboardId.providerId,
                competitionLeaderboardId.competitionId,
                competitionLeaderboardId.page,
                competitionLeaderboardId.perPage);
    }
    //</editor-fold>
}

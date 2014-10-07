package com.tradehero.th.network.service;

import com.tradehero.th.api.competition.CompetitionDTO;
import com.tradehero.th.api.competition.CompetitionDTOList;
import com.tradehero.th.api.competition.CompetitionFormDTO;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.competition.key.CompetitionId;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardDTO;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.models.DTOProcessor;
import com.tradehero.th.models.user.DTOProcessorUpdateUserProfile;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.persistence.home.HomeContentCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.Callback;

@Singleton public class CompetitionServiceWrapper
{
    @NotNull private final CompetitionService competitionService;
    @NotNull private final CompetitionServiceAsync competitionServiceAsync;
    @NotNull private final UserProfileCache userProfileCache;
    @NotNull private final HomeContentCache homeContentCache;

    @Inject public CompetitionServiceWrapper(
            @NotNull CompetitionService competitionService,
            @NotNull CompetitionServiceAsync competitionServiceAsync,
            @NotNull UserProfileCache userProfileCache,
            @NotNull HomeContentCache homeContentCache)
    {
        super();
        this.competitionService = competitionService;
        this.competitionServiceAsync = competitionServiceAsync;
        this.userProfileCache = userProfileCache;
        this.homeContentCache = homeContentCache;
    }

    protected DTOProcessor<UserProfileDTO> createDTOProcessorUserProfile()
    {
        return new DTOProcessorUpdateUserProfile(userProfileCache, homeContentCache);
    }

    //<editor-fold desc="Get Competitions">
    public CompetitionDTOList getCompetitions(@NotNull ProviderId providerId)
    {
        return this.competitionService.getCompetitions(providerId.key);
    }

    @NotNull public MiddleCallback<CompetitionDTOList> getCompetitions(
            @NotNull ProviderId providerId,
            @Nullable Callback<CompetitionDTOList> callback)
    {
        MiddleCallback<CompetitionDTOList> middleCallback = new BaseMiddleCallback<>(callback);
        this.competitionServiceAsync.getCompetitions(providerId.key, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Get Competition">
    public CompetitionDTO getCompetition(@NotNull CompetitionId competitionId)
    {
        return competitionService.getCompetition(competitionId.key);
    }

    public MiddleCallback<CompetitionDTO> getCompetition(
            @NotNull CompetitionId competitionId,
            @Nullable Callback<CompetitionDTO> callback)
    {
        MiddleCallback<CompetitionDTO> middleCallback = new BaseMiddleCallback<>(callback);
        competitionServiceAsync.getCompetition(competitionId.key, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Get Competition Leaderboard">
    public CompetitionLeaderboardDTO getCompetitionLeaderboard(@NotNull CompetitionLeaderboardId competitionLeaderboardId)
    {
        return this.competitionService.getCompetitionLeaderboard(
                competitionLeaderboardId.providerId,
                competitionLeaderboardId.competitionId,
                competitionLeaderboardId.page,
                competitionLeaderboardId.perPage);
    }

    @NotNull public MiddleCallback<CompetitionLeaderboardDTO> getCompetitionLeaderboard(
            @NotNull CompetitionLeaderboardId competitionLeaderboardId,
            @Nullable Callback<CompetitionLeaderboardDTO> callback)
    {
        MiddleCallback<CompetitionLeaderboardDTO> middleCallback = new BaseMiddleCallback<>(callback);
        this.competitionServiceAsync.getCompetitionLeaderboard(
                competitionLeaderboardId.providerId,
                competitionLeaderboardId.competitionId,
                competitionLeaderboardId.page,
                competitionLeaderboardId.perPage,
                middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Enroll">
    public UserProfileDTO enroll(@NotNull CompetitionFormDTO form)
    {
        return createDTOProcessorUserProfile().process(this.competitionService.enroll(form));
    }

    @NotNull public MiddleCallback<UserProfileDTO> enroll(
            @NotNull CompetitionFormDTO form,
            @Nullable Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createDTOProcessorUserProfile());
        this.competitionServiceAsync.enroll(form, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Outbound">
    public UserProfileDTO outbound(@NotNull CompetitionFormDTO form)
    {
       return createDTOProcessorUserProfile().process(this.competitionService.outbound(form));
    }

    @NotNull public MiddleCallback<UserProfileDTO> outbound(
            @NotNull CompetitionFormDTO form,
            @Nullable Callback<UserProfileDTO> callback)
    {
        MiddleCallback<UserProfileDTO> middleCallback = new BaseMiddleCallback<>(callback, createDTOProcessorUserProfile());
        this.competitionServiceAsync.outbound(form, middleCallback);
        return middleCallback;
    }
    //</editor-fold>
}

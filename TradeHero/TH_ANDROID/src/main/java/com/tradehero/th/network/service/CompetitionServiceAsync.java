package com.tradehero.th.network.service;

import com.tradehero.th.api.competition.CompetitionDTO;
import com.tradehero.th.api.competition.CompetitionDTOList;
import com.tradehero.th.api.competition.CompetitionFormDTO;
import com.tradehero.th.api.leaderboard.competition.CompetitionLeaderboardDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

interface CompetitionServiceAsync
{
    //<editor-fold desc="Get Competitions">
    @GET("/providers/{providerId}/competitions")
    void getCompetitions(
            @Path("providerId") int providerId,
            Callback<CompetitionDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Get Competition">
    @GET("/competitions/{competitionId}")
    void getCompetition(
            @Path("competitionId") int competitionId,
            Callback<CompetitionDTO> competitionDTOCallback);
    //</editor-fold>

    //<editor-fold desc="Get Competition Leaderboard">
    @GET("/providers/{providerId}/competitions/{competitionId}")
    void getCompetitionLeaderboard(
            @Path("providerId") int providerId,
            @Path("competitionId") int competitionId,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<CompetitionLeaderboardDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Enroll">
    @POST("/providers/enroll")
    void enroll(
            @Body CompetitionFormDTO form,
            Callback<UserProfileDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Outbound">
    @POST("/providers/outbound")
    void outbound(
            @Body CompetitionFormDTO form,
            Callback<UserProfileDTO> callback);
    //</editor-fold>
}

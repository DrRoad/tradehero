package com.tradehero.th.network.service;

import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.achievement.AchievementCategoryDTOList;
import com.tradehero.th.api.social.SocialShareReqFormDTO;
import com.tradehero.th.api.achievement.QuestBonusDTOList;
import com.tradehero.th.api.achievement.UserAchievementDTO;
import com.tradehero.th.api.level.LevelDefDTOList;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

interface AchievementServiceRx
{
    //<editor-fold desc="Get Trader Level Definition">
    @GET("/achievements/traderleveldefs")
    Observable<LevelDefDTOList> getLevelDefs();
    //</editor-fold>

    //<editor-fold desc="Get User Achievement Details">
    @GET("/achievements/achievement/{userAchievementId}")
    Observable<UserAchievementDTO> getUserAchievementDetails(
            @Path("userAchievementId") int userAchievementId);
    //</editor-fold>

    //<editor-fold desc="Get User Achievement List">
    @GET("/achievements/categories/{userId}")
    Observable<AchievementCategoryDTOList> getAchievementCategories(@Path("userId") int userId);
    //</editor-fold>

    //<editor-fold desc="Get Achievement Category">
    @GET("/achievements/categories/{userId}")
    Observable<AchievementCategoryDTOList> getAchievementCategory(@Query("id") int categoryId, @Path("userId") int userId);
    //</editor-fold>

    //<editor-fold desc="Get Quest Bonuses">
    @GET("/achievements/questbonus")
    Observable<QuestBonusDTOList> getQuestBonuses();
    //</editor-fold>

    //<editor-fold desc="Share Achievement">
    @POST("/achievements/share/{userAchievementId}")
    Observable<BaseResponseDTO> shareUserAchievement(
            @Path("userAchievementId") int userAchievementId,
            @Body SocialShareReqFormDTO achievementShareFormDTO);
    //</editor-fold>
}

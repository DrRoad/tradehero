package com.tradehero.th.network.service;

import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.achievement.AchievementCategoryDTO;
import com.tradehero.th.api.achievement.AchievementCategoryDTOList;
import com.tradehero.th.api.achievement.QuestBonusDTOList;
import com.tradehero.th.api.achievement.UserAchievementDTO;
import com.tradehero.th.api.achievement.key.AchievementCategoryId;
import com.tradehero.th.api.achievement.key.QuestBonusListId;
import com.tradehero.th.api.achievement.key.UserAchievementId;
import com.tradehero.th.api.level.LevelDefDTOList;
import com.tradehero.th.api.share.achievement.AchievementShareFormDTO;
import com.tradehero.th.api.users.UserBaseKey;
import javax.inject.Inject;
import android.support.annotation.NonNull;
import rx.Observable;

public class AchievementServiceWrapper
{
    @NonNull private final AchievementServiceRx achievementServiceRx;

    //<editor-fold desc="Constructors">
    @Inject public AchievementServiceWrapper(@NonNull AchievementServiceRx achievementServiceRx)
    {
        this.achievementServiceRx = achievementServiceRx;
    }
    //</editor-fold>

    //<editor-fold desc="Get Level Defs">
    @NonNull public Observable<LevelDefDTOList> getLevelDefsRx()
    {
        return achievementServiceRx.getLevelDefs();
    }
    //</editor-fold>

    //<editor-fold desc="Get User Achievement Details">
    @NonNull public Observable<UserAchievementDTO> getUserAchievementDetailsRx(@NonNull UserAchievementId userAchievementId)
    {
        return achievementServiceRx.getUserAchievementDetails(userAchievementId.key);
    }
    //</editor-fold>

    //<editor-fold desc="Get Achievement Categories">
    @NonNull public Observable<AchievementCategoryDTOList> getAchievementCategoriesRx(
            @NonNull UserBaseKey key)
    {
        return achievementServiceRx.getAchievementCategories(key.getUserId());
    }
    //</editor-fold>

    //<editor-fold desc="Get Achievement Category">
    @NonNull public Observable<AchievementCategoryDTO> getAchievementCategoryRx(
            @NonNull AchievementCategoryId achievementCategoryId)
    {
        return achievementServiceRx.getAchievementCategory(
                achievementCategoryId.categoryId,
                achievementCategoryId.userId)
                .flatMap(achievementCategoryDTOs -> {
                    if (achievementCategoryDTOs != null && !achievementCategoryDTOs.isEmpty())
                    {
                        return Observable.just(achievementCategoryDTOs.get(0));
                    }
                    else
                    {
                        return Observable.empty();
                    }
                });
    }
    //</editor-fold>

    //<editor-fold desc="Get Quest Bonuses">
    @NonNull public Observable<QuestBonusDTOList> getQuestBonusesRx(@SuppressWarnings("UnusedParameters") @NonNull QuestBonusListId questBonusListId)
    {
        return achievementServiceRx.getQuestBonuses();
    }
    //</editor-fold>

    //<editor-fold desc="Share Achievement">
    @NonNull public Observable<BaseResponseDTO> shareAchievementRx(
            @NonNull AchievementShareFormDTO achievementShareFormDTO)
    {
        return achievementServiceRx.shareUserAchievement(
                achievementShareFormDTO.userAchievementId.key,
                achievementShareFormDTO.achievementShareReqFormDTO);
    }
    //</editor-fold>
}

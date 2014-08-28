package com.tradehero.th.network.service;

import com.tradehero.th.api.achievement.AchievementCategoryDTO;
import com.tradehero.th.api.achievement.AchievementCategoryDTOList;
import com.tradehero.th.api.achievement.QuestBonusDTOList;
import com.tradehero.th.api.achievement.key.AchievementCategoryId;
import com.tradehero.th.api.achievement.UserAchievementDTO;
import com.tradehero.th.api.achievement.key.QuestBonusListId;
import com.tradehero.th.api.achievement.key.UserAchievementId;
import com.tradehero.th.api.level.LevelDefDTOList;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import com.tradehero.th.network.retrofit.MiddleCallback;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.Callback;

public class AchievementServiceWrapper
{
    @NotNull private final AchievementServiceAsync achievementServiceAsync;
    @NotNull private final AchievementService achievementService;

    @Inject public AchievementServiceWrapper(
            @NotNull AchievementService achievementService,
            @NotNull AchievementServiceAsync achievementServiceAsync
    )
    {
        this.achievementService = achievementService;
        this.achievementServiceAsync = achievementServiceAsync;
    }

    public LevelDefDTOList getLevelDefs()
    {
        return achievementService.getLevelDefs();
    }

    @NotNull public MiddleCallback<LevelDefDTOList> getLevelDefs(
            @Nullable Callback<LevelDefDTOList> callback)
    {
        MiddleCallback<LevelDefDTOList> middleCallback = new BaseMiddleCallback<>(callback);
        achievementServiceAsync.getLevelDefs(middleCallback);
        return middleCallback;
    }

    public UserAchievementDTO getUserAchievementDetails(UserAchievementId userAchievementId)
    {
        return achievementService.getUserAchievementDetails(userAchievementId.key);
    }

    @NotNull public MiddleCallback<UserAchievementDTO> getUserAchievementDetails(
            @NotNull UserAchievementId userAchievementId,
            @Nullable Callback<UserAchievementDTO> callback)
    {
        MiddleCallback<UserAchievementDTO> middleCallback = new BaseMiddleCallback<>(callback);
        achievementServiceAsync.getUserAchievementDetails(userAchievementId.key, middleCallback);
        return middleCallback;
    }

    public AchievementCategoryDTOList getAchievementCategories(@NotNull UserBaseKey key)
    {
        return achievementService.getAchievementCategories(key.getUserId());
    }

    public MiddleCallback<AchievementCategoryDTOList> getAchievementCategories(@NotNull UserBaseKey key, @Nullable Callback<AchievementCategoryDTOList> callback)
    {
        MiddleCallback<AchievementCategoryDTOList> middleCallback = new BaseMiddleCallback<>(callback);
        achievementServiceAsync.getAchievementCategories(key.getUserId(), middleCallback);
        return middleCallback;
    }

    public AchievementCategoryDTO getAchievementCategory(@NotNull AchievementCategoryId achievementCategoryId)
    {
        return achievementService.getAchievementCategory(achievementCategoryId.categoryId, achievementCategoryId.userId);
    }

    public QuestBonusDTOList getQuestBonuses(QuestBonusListId questBonusListId)
    {
        return achievementService.getQuestBonuses();
    }
}

package com.tradehero.th.persistence.achievement;

import com.tradehero.common.persistence.BaseDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.th.api.achievement.QuestBonusDTO;
import com.tradehero.th.api.achievement.QuestBonusDTOList;
import com.tradehero.th.api.achievement.key.QuestBonusId;
import javax.inject.Inject;
import javax.inject.Singleton;
import android.support.annotation.NonNull;

@Singleton @UserCache public class QuestBonusCacheRx extends BaseDTOCacheRx<QuestBonusId, QuestBonusDTO>
{
    private static final int DEFAULT_VALUE_SIZE = 50;
    private static final int DEFAULT_SUBJECT_SIZE = 50;

    //<editor-fold desc="Constructors">
    @Inject public QuestBonusCacheRx(@NonNull DTOCacheUtilRx dtoCacheUtil)
    {
    super(DEFAULT_VALUE_SIZE, DEFAULT_SUBJECT_SIZE, dtoCacheUtil);
}
    //</editor-fold>

    public void onNext(@NonNull QuestBonusDTOList value)
    {
        for (QuestBonusDTO questBonusDTO : value)
        {
            onNext(questBonusDTO.getQuestBonusId(), questBonusDTO);
        }
    }
}

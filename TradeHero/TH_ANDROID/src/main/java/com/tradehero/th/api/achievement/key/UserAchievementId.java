package com.tradehero.th.api.achievement.key;

import android.os.Bundle;
import com.tradehero.common.persistence.AbstractIntegerDTOKey;
import com.tradehero.th.utils.achievement.AchievementModule;
import com.tradehero.th.utils.broadcast.BroadcastData;
import org.jetbrains.annotations.NotNull;

public class UserAchievementId extends AbstractIntegerDTOKey implements BroadcastData
{
    private static final String BUNDLE_KEY = UserAchievementId.class.getName() + ".key";

    public UserAchievementId(Integer key)
    {
        super(key);
    }

    public UserAchievementId(@NotNull Bundle args)
    {
        super(args);
    }

    @Override public String getBundleKey()
    {
        return BUNDLE_KEY;
    }

    @Override public String getBroadcastBundleKey()
    {
        return AchievementModule.KEY_USER_ACHIEVEMENT_ID;
    }

    @Override public String getBroadcastIntentActionName()
    {
        return AchievementModule.ACHIEVEMENT_INTENT_ACTION_NAME;
    }
}

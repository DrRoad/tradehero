package com.tradehero.th.fragments;

import com.tradehero.th.fragments.achievement.DebugFragmentAchievementModule;
import com.tradehero.th.fragments.level.DebugFragmentLevelModule;
import dagger.Module;

@Module(
        includes = {
                DebugFragmentAchievementModule.class,
                DebugFragmentLevelModule.class,
        },

        complete = false,
        library = true
)
public class DebugFragmentModule
{
}

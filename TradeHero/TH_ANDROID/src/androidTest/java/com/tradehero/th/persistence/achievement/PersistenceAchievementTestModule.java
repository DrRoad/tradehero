package com.tradehero.th.persistence.achievement;

import dagger.Module;

@Module(
        injects = {
                UserAchievementCacheTest.class,
        },
        library = true,
        complete = false,
        overrides = true
)
public class PersistenceAchievementTestModule
{
}

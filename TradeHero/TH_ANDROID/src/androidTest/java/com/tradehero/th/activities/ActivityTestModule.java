package com.tradehero.th.activities;

import dagger.Module;

@Module(
        injects = {
                SplashActivityTest.class,
                DashboardActivityExtended.class,
                DashboardActivityTest.class,
        },
        complete = false,
        library = true
)
public class ActivityTestModule
{
}

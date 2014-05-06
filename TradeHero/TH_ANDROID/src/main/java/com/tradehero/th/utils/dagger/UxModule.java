package com.tradehero.th.utils.dagger;

import android.content.Context;
import com.localytics.android.LocalyticsSession;
import com.tapstream.sdk.Config;
import com.tapstream.sdk.Tapstream;
import com.tradehero.th.base.Application;
import com.tradehero.th.fragments.authentication.EmailSignUpFragment;
import com.tradehero.th.fragments.authentication.SignInFragment;
import com.tradehero.th.fragments.authentication.SignUpFragment;
import com.tradehero.th.fragments.leaderboard.filter.LeaderboardFilterSliderContainer;
import com.tradehero.th.models.chart.ChartTimeSpanMetricsCodeFactory;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.metrics.localytics.THLocalyticsSession;
import com.tradehero.th.utils.metrics.tapstream.TapStreamEvents;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;


@Module(
        injects = {
                SignInFragment.class,
                SignUpFragment.class,
                EmailSignUpFragment.class,
                LeaderboardFilterSliderContainer.class
        },
        complete = false,
        library = true
)
public class UxModule
{
    private static final String TAPSTREAM_KEY = "Om-yveoZQ7CMU7nUGKlahw";
    private static final String TAPSTREAM_APP_NAME = "tradehero";

    // localytics
    @Provides @Singleton LocalyticsSession provideLocalyticsSession(THLocalyticsSession localyticsSession)
    {
        return localyticsSession;
    }

    @Provides @Singleton THLocalyticsSession provideThLocalyticsSession(Context context, ChartTimeSpanMetricsCodeFactory chartTimeSpanMetricsCodeFactory)
    {
        return new THLocalyticsSession(context, chartTimeSpanMetricsCodeFactory);
    }

    // TapStream
    @Provides @Singleton Tapstream provideTapStream(Application app, Config config)
    {
        Tapstream.create(app, TAPSTREAM_APP_NAME, TAPSTREAM_KEY, config);
        return Tapstream.getInstance();
    }

    @Provides @Singleton Config provideTapStreamConfig()
    {
        Config config = new Config();
        config.setFireAutomaticOpenEvent(false);//this will send twice
        switch (Constants.VERSION)
        {
            case 0:
                config.setInstallEventName(TapStreamEvents.APP_INSTALL);
                break;
            case 1:
                config.setInstallEventName(TapStreamEvents.APP_INSTALL_BAIDU);
                break;
            case 2:
                config.setInstallEventName(TapStreamEvents.APP_INSTALL_TENCENT);
                break;
        }
        return config;
    }
}

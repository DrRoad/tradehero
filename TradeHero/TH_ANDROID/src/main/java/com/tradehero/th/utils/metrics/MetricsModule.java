package com.tradehero.th.utils.metrics;

import android.content.Context;
import com.mobileapptracker.MobileAppTracker;
import com.tapstream.sdk.Api;
import com.tapstream.sdk.Config;
import com.tapstream.sdk.Tapstream;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.base.THApp;
import com.tradehero.th.utils.Constants;
import dagger.Module;
import dagger.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;

import static com.tradehero.th.utils.Constants.DOGFOOD_BUILD;
import static com.tradehero.th.utils.Constants.LOCALYTICS_APP_KEY_DEBUG;
import static com.tradehero.th.utils.Constants.LOCALYTICS_APP_KEY_RELEASE;
import static com.tradehero.th.utils.Constants.RELEASE;

@Module(
        library = true,
        complete = false
)
public class MetricsModule
{
    private static final String TAPSTREAM_KEY = "Om-yveoZQ7CMU7nUGKlahw";
    private static final String TAPSTREAM_APP_NAME = "tradehero";
    private static final String MAT_APP_ID = "19686";
    private static final String MAT_APP_KEY = "c65b99d5b751944e3637593edd04ce01";
    private static final String LOCALYTICS_KEY =
            RELEASE ? (DOGFOOD_BUILD ? LOCALYTICS_APP_KEY_DEBUG : LOCALYTICS_APP_KEY_RELEASE) : null;

    @Deprecated
    public static final String TD_APP_ID_KEY = RELEASE ? "5991FF8EFB8EFF717C206FCCF9C969A8" : null;

    @Provides(type = Provides.Type.SET_VALUES) @ForAnalytics Set<String> provideAnalyticsPredefineDimensions()
    {
        Set<String> predefinedDimensions = new HashSet<>();
        predefinedDimensions.add(Constants.TAP_STREAM_TYPE.name());
        return predefinedDimensions;
    }


    @Provides @Singleton public Analytics provideAnalytics(Context context)
    {
        return Analytics.with(context)
                .withLocalytics(LOCALYTICS_KEY)
                .withTalkingData(TD_APP_ID_KEY, Constants.TAP_STREAM_TYPE.name())
                .build();
    }

    // TapStream
    @Provides @Singleton Api provideTapStream(THApp app, Config config)
    {
        Tapstream.create(app, TAPSTREAM_APP_NAME, TAPSTREAM_KEY, config);
        return Tapstream.getInstance();
    }

    @Provides @Singleton Config provideTapStreamConfig(Context context)
    {
        Config config = new Config();
        config.setFireAutomaticOpenEvent(false);//this will send twice
        config.setInstallEventName(context.getString(Constants.TAP_STREAM_TYPE.installResId));
        return config;
    }

    // MobileAppTracker
    @Provides @Singleton MobileAppTracker provideMobileAppTracker(Context context)
    {
        MobileAppTracker.init(context, MAT_APP_ID, MAT_APP_KEY);
        MobileAppTracker mobileAppTrackerInstance = MobileAppTracker.getInstance();
        mobileAppTrackerInstance.setPackageName(context.getPackageName() + "." + Constants.TAP_STREAM_TYPE.name());
        mobileAppTrackerInstance.setDebugMode(!Constants.RELEASE);
        return mobileAppTrackerInstance;
    }
}

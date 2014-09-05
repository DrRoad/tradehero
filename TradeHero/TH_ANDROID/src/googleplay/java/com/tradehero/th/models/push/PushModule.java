package com.tradehero.th.models.push;

import android.content.SharedPreferences;

import com.tradehero.common.annotation.ForUser;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.models.push.urbanairship.UrbanAirshipPushModule;
import com.tradehero.th.models.push.urbanairship.UrbanAirshipPushNotificationManager;
import com.tradehero.th.utils.Constants;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module(
        includes = {
                UrbanAirshipPushModule.class,
        },
        injects = {
                DefaultIntentReceiver.class,
        },
        complete = false,
        library = true
)
public class PushModule
{
    private static final String MAX_GROUP_NOTIFICATIONS = "MAX_GROUP_NOTIFICATIONS";

    @Provides @Singleton PushNotificationManager providePushNotificationManager(
            Provider<UrbanAirshipPushNotificationManager> urbanAirshipPushNotificationManager)
    {
        switch (Constants.TAP_STREAM_TYPE.pushProvider)
        {
            case URBAN_AIRSHIP:
                Timber.d("Using UrbanAirship Push");
                return urbanAirshipPushNotificationManager.get();

            default:
                throw new IllegalArgumentException("Unhandled PushProvider." + Constants.TAP_STREAM_TYPE.pushProvider.name());
        }
    }

    @Provides @Singleton THNotificationBuilder provideTHNotificationBuilder(CommonNotificationBuilder commonNotificationBuilder)
    {
        return commonNotificationBuilder;
    }

    @Provides @Singleton @MaxGroupNotifications IntPreference provideMaxGroupNotifications(@ForUser SharedPreferences sharedPreferences)
    {
        return new IntPreference(sharedPreferences, MAX_GROUP_NOTIFICATIONS, 3);
    }
}

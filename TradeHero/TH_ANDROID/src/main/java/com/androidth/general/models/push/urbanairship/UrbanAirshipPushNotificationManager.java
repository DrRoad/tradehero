package com.androidth.general.models.push.urbanairship;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.androidth.general.common.persistence.prefs.StringPreference;
import com.androidth.general.api.users.CurrentUserId;
import com.androidth.general.api.users.UserBaseKey;
import com.androidth.general.api.users.UserProfileDTO;
import com.androidth.general.base.THApp;
import com.androidth.general.models.push.PushNotificationManager;
import com.androidth.general.network.service.SessionServiceWrapper;
import com.androidth.general.persistence.prefs.SavedPushDeviceIdentifier;
import com.androidth.general.persistence.user.UserProfileCacheRx;
import com.androidth.general.rx.ReplaceWithFunc1;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.actions.Action;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionRegistry;
import com.urbanairship.actions.ActionResult;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Singleton public final class UrbanAirshipPushNotificationManager implements PushNotificationManager
{
    @NonNull private final AirshipConfigOptions options;
    @NonNull private final CurrentUserId currentUserId;
    @NonNull private final UserProfileCacheRx userProfileCache;
    @NonNull private final SessionServiceWrapper sessionServiceWrapper;
    @NonNull private final StringPreference savedPushDeviceIdentifier;
    @Nullable private static UAirship uAirship;

    //<editor-fold desc="Constructors">
    @Inject public UrbanAirshipPushNotificationManager(
            @NonNull AirshipConfigOptions options,
            @NonNull CurrentUserId currentUserId,
            @NonNull UserProfileCacheRx userProfileCache,
            @NonNull SessionServiceWrapper sessionServiceWrapper,
            @NonNull @SavedPushDeviceIdentifier StringPreference savedPushDeviceIdentifier)
    {
        this.options = options;
        this.currentUserId = currentUserId;
        this.userProfileCache = userProfileCache;
        this.sessionServiceWrapper = sessionServiceWrapper;
        this.savedPushDeviceIdentifier = savedPushDeviceIdentifier;
    }
    //</editor-fold>

    @Nullable public static UAirship getUAirship()
    {
        return uAirship;
    }

    @NonNull @Override public Observable<PushNotificationManager.InitialisationCompleteDTO> initialise()
    {
        final long before = System.nanoTime();
        UrbanAirshipPushModule a = new UrbanAirshipPushModule();
        UAirship.takeOff(THApp.context(), a.provideAirshipConfigOptions(THApp.context));
        while (UAirship.shared() == null)
        {
            // We have to use the direct takeOff and do this to avoid a silly NPE in com.urbanairship.analytics.EventService
            try
            {
                Thread.sleep(500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return Observable.just(UAirship.shared())
                .observeOn(Schedulers.computation())
                .flatMap(new Func1<UAirship, Observable<PushNotificationManager.InitialisationCompleteDTO>>()
                {
                    @Override public Observable<PushNotificationManager.InitialisationCompleteDTO> call(final UAirship uAirship)
                    {
                        final String channelId = uAirship.getPushManager().getChannelId();
                        UrbanAirshipPushNotificationManager.uAirship = uAirship;
                        uAirship.getPushManager().setChannelTagRegistrationEnabled(false);
                        Timber.i("My UrbanAirship Application Channel ID below");
                        Timber.i("My UrbanAirship Application Channel ID: %s", channelId);
                        uAirship.getPushManager().setPushEnabled(true);

                        registerActions(uAirship.getActionRegistry());

                        final InitialisationCompleteDTO initialisationCompleteDTO = new InitialisationCompleteDTO(channelId, uAirship);
                        Timber.d("UrbanAirship Initialisation %d milliseconds", (System.nanoTime() - before) / 1000000);
                        savedPushDeviceIdentifier.set(channelId);

                        // Wait for the user to be set properly
                        return currentUserId.getKeyObservable()
                                // Making sure we got the UserProfile before we submit the device.
                                // This is a defense mechanism to improve starting speed.
                                .flatMap(new Func1<Integer, Observable<Integer>>()
                                {
                                    @Override public Observable<Integer> call(Integer userId)
                                    {
                                        return userProfileCache.getOne(new UserBaseKey(userId))
                                                .map(new ReplaceWithFunc1<Pair<UserBaseKey, UserProfileDTO>, Integer>(userId));
                                    }
                                })
                                .flatMap(new Func1<Integer, Observable<UserProfileDTO>>()
                                {
                                    @Override public Observable<UserProfileDTO> call(Integer ignored)
                                    {
                                        return sessionServiceWrapper.updateDeviceRx(channelId);
                                    }
                                })
                                .map(new Func1<UserProfileDTO, PushNotificationManager.InitialisationCompleteDTO>()
                                {
                                    @Override public PushNotificationManager.InitialisationCompleteDTO call(UserProfileDTO profileDTO)
                                    {
                                        uAirship.getPushManager()
                                                .setUserNotificationsEnabled(profileDTO.pushNotificationsEnabled);
                                        return initialisationCompleteDTO;
                                    }
                                });
                    }
                });
    }

    private void registerActions(@NonNull ActionRegistry actionRegistry)
    {
        actionRegistry.registerAction(
                new Action()
                {
                    @Override public ActionResult perform(ActionArguments actionArguments)
                    {
                        Timber.e(new Exception("Reporting"), "argument are %s",
                                actionArguments);
                        return ActionResult.newEmptyResult();
                    }
                },
                "TODO");
    }

    @Override public void verify(@NonNull Activity activity)
    {
        UrbanAirshipTools.verify(activity);
    }

    @Override public void enablePush()
    {
        if (uAirship != null)
        {
            uAirship.getPushManager().setUserNotificationsEnabled(true);
        }
        else
        {
            Timber.e(new NullPointerException(), "Failed to enable push");
        }
    }

    @Override public void disablePush()
    {
        if (uAirship != null)
        {
            uAirship.getPushManager().setUserNotificationsEnabled(false);
        }
        else
        {
            Timber.e(new NullPointerException(), "Failed to disable push");
        }
    }

    @Override public void setSoundEnabled(boolean enabled)
    {
        if (uAirship != null)
        {
            uAirship.getPushManager().setSoundEnabled(enabled);
        }
        else
        {
            Timber.e(new NullPointerException(), "Failed to setSoundEnabled");
        }
    }

    @Override public void setVibrateEnabled(boolean enabled)
    {
        if (uAirship != null)
        {
            uAirship.getPushManager().setVibrateEnabled(enabled);
        }
        else
        {
            Timber.e(new NullPointerException(), "Failed to setVibrateEnabled");
        }
    }

    public static class InitialisationCompleteDTO extends PushNotificationManager.InitialisationCompleteDTO
    {
        @NonNull public final UAirship uAirship;

        public InitialisationCompleteDTO(@NonNull String pushId, @NonNull UAirship uAirship)
        {
            super(pushId);
            this.uAirship = uAirship;
        }
    }
}
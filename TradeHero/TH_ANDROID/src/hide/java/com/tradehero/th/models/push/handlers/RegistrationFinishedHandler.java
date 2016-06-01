package com.ayondo.academy.models.push.handlers;

import android.content.Context;
import android.content.Intent;
import com.ayondo.academy.models.push.PushConstants;
import com.urbanairship.UAirship;
import javax.inject.Inject;

public class RegistrationFinishedHandler implements PushNotificationHandler
{
    public static final String APID_UPDATED_ACTION_SUFFIX = ".apid.updated";
    private final Context context;

    @Inject public RegistrationFinishedHandler(Context context)
    {
        this.context = context;
    }

    @Override public PushConstants.THAction getAction()
    {
        return PushConstants.THAction.RegistrationFinished;
    }

    @Override public boolean handle(Intent intent)
    {
        //Timber.i("Registration complete. APID: %s. Valid: %b",
        //        intent.getStringExtra(PushManager.EXTRA_APID),
        //        intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));

        Intent launch = new Intent(UAirship.getPackageName() + APID_UPDATED_ACTION_SUFFIX);
        context.sendBroadcast(launch);
        return true;
    }
}

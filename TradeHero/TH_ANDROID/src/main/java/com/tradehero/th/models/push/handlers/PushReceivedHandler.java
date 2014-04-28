package com.tradehero.th.models.push.handlers;

import android.content.Intent;
import com.tradehero.th.persistence.notification.NotificationCache;
import com.urbanairship.push.PushManager;
import javax.inject.Inject;
import timber.log.Timber;

public class PushReceivedHandler extends PrecacheNotificationHandler
{
    @Inject public PushReceivedHandler(NotificationCache notificationCache)
    {
        super(notificationCache);
    }

    @Override public String getAction()
    {
        return PushManager.ACTION_PUSH_RECEIVED;
    }

    @Override public boolean handle(Intent intent)
    {
        super.handle(intent);

        int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);

        Timber.i("Received push notification. Alert: %s [NotificationID=%d]", intent.getStringExtra(PushManager.EXTRA_ALERT), id);
        return true;
    }

}

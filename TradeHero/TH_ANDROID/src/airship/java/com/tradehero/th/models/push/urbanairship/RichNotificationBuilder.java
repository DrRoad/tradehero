package com.tradehero.th.models.push.urbanairship;

import android.app.Notification;

import com.tradehero.th.models.push.PushConstants;
import com.tradehero.th.models.push.THNotificationBuilder;

import java.util.Map;

import javax.inject.Inject;

//public class RichNotificationBuilder implements PushNotificationBuilder
//{
//    private final THNotificationBuilder notificationBuilder;
//
//    //<editor-fold desc="Constructors">
//    @Inject RichNotificationBuilder(THNotificationBuilder notificationBuilder)
//    {
//        this.notificationBuilder = notificationBuilder;
//    }
//    //</editor-fold>
//
//    @Override
//    public Notification buildNotification(String alert, Map<String, String> extras)
//    {
//        int pushId = getNotificationIdFromBundle(extras);
//
//        return pushId > 0 ? notificationBuilder.buildNotification(pushId) : null;
//    }
//
//    @Override
//    public int getNextId(String alert, Map<String, String> extras)
//    {
//        int pushId = getNotificationIdFromBundle(extras);
//        return pushId > 0 ? notificationBuilder.getNotifyId(pushId) : 0;
//    }
//
//    private int getNotificationIdFromBundle(Map<String, String> extras)
//    {
//        try
//        {
//            return Integer.parseInt(extras.get(PushConstants.KEY_PUSH_ID));
//        }
//        catch (Exception ex)
//        {
//            return -1;
//        }
//    }
//}

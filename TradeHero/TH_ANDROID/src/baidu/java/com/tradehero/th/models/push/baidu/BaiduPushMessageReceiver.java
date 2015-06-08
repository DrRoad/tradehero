package com.tradehero.th.models.push.baidu;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.tradehero.common.persistence.prefs.BooleanPreference;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.base.THApp;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.models.push.PushConstants;
import com.tradehero.th.models.push.THNotificationBuilder;
import com.tradehero.th.network.service.SessionServiceWrapper;
import com.tradehero.th.persistence.prefs.BaiduPushDeviceIdentifierSentFlag;
import com.tradehero.th.persistence.prefs.SavedPushDeviceIdentifier;
import java.util.List;
import javax.inject.Inject;
import retrofit.RetrofitError;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedString;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class BaiduPushMessageReceiver extends FrontiaPushMessageReceiver
{
    public static final int CODE_OK = 0;

    @Inject CurrentUserId currentUserId;
    @Inject SessionServiceWrapper sessionServiceWrapper;
    @Inject THNotificationBuilder thNotificationBuilder;
    @Inject Converter converter;

    @Inject @BaiduPushDeviceIdentifierSentFlag BooleanPreference pushDeviceIdentifierSentFlag;
    @Inject @SavedPushDeviceIdentifier StringPreference savedPushDeviceIdentifier;

    public BaiduPushMessageReceiver()
    {
        HierarchyInjector.inject(THApp.context(), this);
    }

    /**
     * After calling PushManager.startWork (or BaiduPushManager.enablePush), BaiduSDK will send to server a asynchronous request asking for binding.
     * Binding request results returned by onBind.
     *
     * @param channelId: used for unicast push (push to QQ/Wechat/... network
     * @param userId: used for unicast push
     */
    @Override public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId)
    {
        Timber.d("onBind appId: %s, userId: %s, channelId: %s, requestId: %s", appId, userId, channelId, requestId);
        if (errorCode == CODE_OK)
        {
            updateDeviceIdentifier(appId, userId, channelId);
        }
    }

    /**
     * Callback for PushManager.stopWork() (or BaiduPushManager.disablePush)
     */
    @Override public void onUnbind(Context context, int errorCode, String requestId)
    {
        Timber.d("onUnbind errorCode:%s", errorCode);
        if (errorCode == CODE_OK)
        {
            setPushDeviceIdentifierSentFlag(false);
        }
    }

    /**
     * When a message is received
     */
    @Override public void onMessage(Context context, String message, String customContentString)
    {
        Timber.d("onMessage message: %s, customContentString: %s", message, customContentString);
        if (!TextUtils.isEmpty(message))
        {
            handleReceiveMessage(context, message);
        }
    }

    private void handleReceiveMessage(Context context, String message)
    {
        BaiduPushMessageDTO baiduPushMessageDTO;
        try
        {
            TypedString typedString = new TypedString(message);
            baiduPushMessageDTO = (BaiduPushMessageDTO) converter.fromBody(typedString, BaiduPushMessageDTO.class);
        }
        catch (ConversionException e)
        {
            return;
        }

        if (baiduPushMessageDTO != null)
        {
            if (baiduPushMessageDTO.getDiscussionType() != null)
            {
                switch (baiduPushMessageDTO.getDiscussionType())
                {
                    case BROADCAST_MESSAGE:
                    case PRIVATE_MESSAGE:
                        Intent requestUpdateIntent = new Intent(PushConstants.ACTION_MESSAGE_RECEIVED);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(requestUpdateIntent);
                        break;
                }
            }
            createAndNotify(context, baiduPushMessageDTO.getId());
        }
    }

    private void createAndNotify(Context context, int pushId)
    {
        Notification notification = thNotificationBuilder.buildNotification(pushId);

        if (notification != null)
        {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(thNotificationBuilder.getNotifyId(pushId), notification);
        }
    }

    public void updateDeviceIdentifier(String appId, String userId, String channelId)
    {
        if (currentUserId == null)
        {
            Timber.e("Current user is null, quit");
            return;
        }
        if (pushDeviceIdentifierSentFlag.get())
        {
            Timber.d("Already send the device token to the server, quit");
            return;
        }

        BaiduDeviceMode deviceMode = new BaiduDeviceMode(channelId, userId, appId);
        savedPushDeviceIdentifier.set(deviceMode.token);
        sessionServiceWrapper.updateDeviceRx(deviceMode.token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateDeviceIdentifierObserver());
    }

    public void setPushDeviceIdentifierSentFlag(boolean bind)
    {
        pushDeviceIdentifierSentFlag.set(bind);
    }

    class UpdateDeviceIdentifierObserver implements Observer<UserProfileDTO>
    {
        @Override public void onNext(UserProfileDTO userProfileDTO)
        {
            Timber.d("UpdateDeviceIdentifierCallback send success");
            setPushDeviceIdentifierSentFlag(true);
        }

        @Override public void onError(Throwable e)
        {
            Timber.e((RetrofitError) e, "UpdateDeviceIdentifierCallback send failure");
            setPushDeviceIdentifierSentFlag(false);
        }

        @Override public void onCompleted()
        {
        }
    }

    //<editor-fold desc="Not being used for the time being">

    /**
     * when user click the notification
     */
    @Override public void onNotificationClicked(Context context, String title, String description, String customContentString)
    {
        Timber.d("onNotificationClicked title:%s, description:%s, customContentString:%s", title, description, customContentString);
    }

    /**
     * Callback for setTags()
     */
    @Override public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId)
    {
        Timber.d("onSetTags successTags: %s, failTags: %s, requestId:%s", successTags, failTags, requestId);
    }

    /**
     * Callback for delTags()
     */
    @Override public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId)
    {
        Timber.d("onDelTags successTags:%s failTags:%s requestId:%s", successTags, failTags, requestId);
    }

    /**
     * Callback for listTags()
     */
    @Override public void onListTags(Context context, int errorCode, List<String> tags, String requestId)
    {
        Timber.d("onListTags tags:%s", tags);
    }
    //</editor-fold>
}

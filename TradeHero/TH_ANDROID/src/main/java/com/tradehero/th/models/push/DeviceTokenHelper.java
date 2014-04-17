package com.tradehero.th.models.push;

import android.content.Context;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.common.utils.MetaHelper;
import com.tradehero.th.persistence.prefs.SavedBaiduPushDeviceIdentifier;
import com.urbanairship.push.PushManager;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by wangliang on 14-4-16.
 */
public class DeviceTokenHelper
{
    @Inject @SavedBaiduPushDeviceIdentifier static StringPreference savedPushDeviceIdentifier;
    @Inject static Context context;

    public static String getDeviceToken()
    {
        boolean isChineseLocale = MetaHelper.isChineseLocale(context.getApplicationContext());
        if (isChineseLocale)
        {
            String token = savedPushDeviceIdentifier.get();
            Timber.d("get saved the token %s", token);
            if (token != null)
            {
                return token;
            }

        }
        return PushManager.shared().getAPID();
    }
}

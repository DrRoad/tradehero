package com.androidth.general.utils.metrics.appsflyer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.appsflyer.AppsFlyerLib;

import java.util.Map;

public class THAppsFlyer
{
    public static void setAppsFlyerKey(@NonNull Context applicationContext, @NonNull String key)
    {
    }

    public static void sendTracking(@NonNull Context applicationContext)
    {
    }

    public static void sendTrackingWithEvent(@NonNull Context applicationContext, @NonNull String eventName, @NonNull Map<String, Object> eventValue)
    {
        AppsFlyerLib.getInstance().trackEvent(applicationContext, eventName, eventValue);
    }

//    public static void sendTrackingWithEvent(@NonNull Context applicationContext, @NonNull String eventName, @NonNull String eventRevenueValue)
//    {
//    }
}
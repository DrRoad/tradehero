package com.tradehero.th.base;

import android.app.Activity;
import android.content.Intent;
import com.tradehero.common.Logger;
import com.tradehero.common.application.PApplication;
import com.tradehero.common.log.CrashReportingTree;
import com.tradehero.common.log.EasyDebugTree;
import com.tradehero.common.thread.KnownExecutorServices;
import com.tradehero.common.utils.THLog;
import com.tradehero.th.models.push.PushNotificationManager;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.EmailSignUtils;
import javax.inject.Inject;
import timber.log.Timber;


public class Application extends PApplication
{
    public static final String TAG = Application.class.getSimpleName();

    @Inject protected PushNotificationManager pushNotificationManager;

    @Override protected void init()
    {
        super.init();

        if (Constants.RELEASE)
        {
            Timber.plant(new CrashReportingTree());
        }
        else
        {
            Timber.plant(new EasyDebugTree()
            {
                @Override public String createTag()
                {
                    return String.format("TradeHero-%s", super.createTag());
                }
            });
        }

        // Supposedly get the count of cores
        KnownExecutorServices.setCpuThreadCount(Runtime.getRuntime().availableProcessors());
        Timber.d("Available Processors Count: %d", KnownExecutorServices.getCpuThreadCount());

        DaggerUtils.initialize(this);
        DaggerUtils.inject(this);

        THUser.initialize();

        EmailSignUtils.initialize();

        pushNotificationManager.initialise();

        THLog.showDeveloperKeyHash();
        Logger.logAll("wangliang","init");
    }

    public void restartActivity(Class<? extends Activity> activityClass)
    {
        Intent newApp = new Intent(this, activityClass);
        newApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(newApp);

        DaggerUtils.initialize(this);
        DaggerUtils.inject(this);
    }
}

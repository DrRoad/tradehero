package com.tradehero.th.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockActivity;
import com.crashlytics.android.Crashlytics;
import com.tradehero.common.utils.THLog;
import com.tradehero.th.R;
import com.tradehero.th.base.THUser;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.TestFlightUtils;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends SherlockActivity
{
    public static final String TAG = SplashActivity.class.getSimpleName();

    private Timer timerToShiftActivity;
    private AsyncTask<Void, Void, Void> initialAsyncTask;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Constants.RELEASE)
        {
            Crashlytics.start(this);
        }

        setContentView(R.layout.splash_screen);
    }

    @Override protected void onResume()
    {
        super.onResume();
        initialAsyncTask = new AsyncTask<Void, Void, Void>()
        {
            @Override protected Void doInBackground(Void... params)
            {
                initialisation();
                return null;
            }
        };
        initialAsyncTask.execute();
    }

    protected void initialisation()
    {
        TestFlightUtils.initialize();

        if (THUser.getSessionToken() != null)
        {
            ActivityHelper.launchDashboard(SplashActivity.this);
            finish();
        }
        else
        {
            timerToShiftActivity = new Timer();
            timerToShiftActivity.schedule(new TimerTask()
            {
                public void run()
                {
                    timerToShiftActivity.cancel();
                    ActivityHelper.launchAuthentication(SplashActivity.this);
                    finish();
                }
            }, 1500);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        THLog.d(TAG, "onActivityResult " + requestCode + ", " + resultCode + ", " + data);
    }

    @Override protected void onDestroy()
    {
        initialAsyncTask = null;
        super.onDestroy();
    }
}

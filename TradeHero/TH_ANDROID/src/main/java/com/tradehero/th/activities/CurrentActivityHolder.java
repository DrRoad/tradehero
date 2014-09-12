package com.tradehero.th.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * instead of using CurrentActivityHolder, we should be able to inject activity
 */
@Deprecated
public class CurrentActivityHolder
{
    @NotNull protected WeakReference<Activity> currentActivityWeak = new WeakReference<>(null);
    protected final Handler currentHandler;

    public CurrentActivityHolder(Handler handler)
    {
        this.currentHandler = handler;
    }

    @Nullable public Activity getCurrentActivity()
    {
        return currentActivityWeak.get();
    }

    @Nullable public Context getCurrentContext()
    {
        return getCurrentActivity();
    }

    public void setCurrentActivity(Activity currentActivity)
    {
        this.currentActivityWeak = new WeakReference<>(currentActivity);
    }

    public void unsetActivity(Activity toUnset)
    {
        if (currentActivityWeak.get() == toUnset)
        {
            setCurrentActivity(null);
        }
    }

    public Handler getCurrentHandler()
    {
        return currentHandler;
    }
}

package com.tradehero.th.utils.broadcast;

import android.support.v4.content.LocalBroadcastManager;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import android.support.annotation.NonNull;

@Singleton public class BroadcastUtils implements BroadcastTaskNew.TaskListener
{
    @NonNull private final LocalBroadcastManager localBroadcastManager;
    @NonNull private ArrayDeque<BroadcastData> broadcastQueue = new ArrayDeque<>();
    @NonNull private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    //<editor-fold desc="Constructors">
    @Inject public BroadcastUtils(@NonNull LocalBroadcastManager localBroadcastManager)
    {
        this.localBroadcastManager = localBroadcastManager;
    }
    //</editor-fold>

    public BroadcastTaskNew enqueue(@NonNull BroadcastData broadcastData)
    {
        broadcastQueue.add(broadcastData);
        if (!isProcessing.get())
        {
            return broadcast(broadcastQueue.pop());
        }
        else
        {
            return null;
        }
    }

    public void nextPlease()
    {
        if (isProcessing.get())
        {
            isProcessing.set(false);
            if (!broadcastQueue.isEmpty())
            {
                broadcast(broadcastQueue.pop());
            }
        }
    }

    private BroadcastTaskNew broadcast(@NonNull BroadcastData broadcastData)
    {
        isProcessing.set(true);
        BroadcastTaskNew task = new BroadcastTaskNew(broadcastData, localBroadcastManager, this);
        task.start();
        return task;
    }

    @Override public void onStartBroadcast(@NonNull BroadcastData broadcastData)
    {
        isProcessing.compareAndSet(false, true);
    }

    @Override public void onFinishBroadcast(@NonNull BroadcastData broadcastData, boolean isSuccessful)
    {
        if (!isSuccessful)
        {
            broadcastQueue.addLast(broadcastData);
            isProcessing.set(false);
        }
    }

    public void clear()
    {
        broadcastQueue.clear();
        isProcessing.set(false);
    }
}

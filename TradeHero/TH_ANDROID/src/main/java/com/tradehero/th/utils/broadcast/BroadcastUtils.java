package com.tradehero.th.utils.broadcast;

import android.support.v4.content.LocalBroadcastManager;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton public class BroadcastUtils implements BroadcastTaskNew.TaskListener
{
    @NotNull private final LocalBroadcastManager localBroadcastManager;

    private ArrayDeque<BroadcastData> broadcastQueue = new ArrayDeque<>();
    private final AtomicBoolean flag = new AtomicBoolean(false);

    @Inject public BroadcastUtils(@NotNull LocalBroadcastManager localBroadcastManager)
    {
        this.localBroadcastManager = localBroadcastManager;
    }

    public BroadcastTaskNew enqueue(BroadcastData broadcastData)
    {
        broadcastQueue.add(broadcastData);
        if (!flag.get())
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
        if (flag.get())
        {
            flag.set(false);
            if (!broadcastQueue.isEmpty())
            {
                broadcast(broadcastQueue.pop());
            }
        }
    }

    private BroadcastTaskNew broadcast(BroadcastData broadcastData)
    {
        flag.set(true);
        BroadcastTaskNew task = new BroadcastTaskNew(broadcastData, localBroadcastManager, this);
        task.start();
        return task;
    }

    @Override public void onStartBroadcast(BroadcastData broadcastData)
    {
        flag.compareAndSet(false, true);
    }

    @Override public void onFinishBroadcast(BroadcastData broadcastData, boolean isSuccessful)
    {
        if (!isSuccessful)
        {
            broadcastQueue.addLast(broadcastData);
        }
    }
}

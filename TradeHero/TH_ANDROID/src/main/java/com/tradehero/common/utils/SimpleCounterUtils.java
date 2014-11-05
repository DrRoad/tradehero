package com.tradehero.common.utils;

import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class SimpleCounterUtils
{
    private final int expectedCount;
    private int currentCount;
    @NotNull private SimpleCounter simpleCounter;

    public SimpleCounterUtils(int expectedCount, @NotNull SimpleCounter simpleCounterListener)
    {
        this.expectedCount = expectedCount;
        this.simpleCounter = simpleCounterListener;
    }

    public void increment()
    {
        currentCount++;
        Timber.d("CounterUtils %d", currentCount);
        check();
    }

    public void reset()
    {
        currentCount = 0;
    }

    public int getCurrentCount()
    {
        return currentCount;
    }

    private void check()
    {
        if (currentCount == expectedCount)
        {
            simpleCounter.onCountFinished(currentCount);
        }
    }

    public void setListener(SimpleCounter listener)
    {
        this.simpleCounter = listener;
    }

    public interface SimpleCounter
    {
        void onCountFinished(int count);
    }
}

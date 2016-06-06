package com.androidth.general.common.time;

import org.ocpsoft.prettytime.TimeUnit;

abstract public class TimeUnitSecond implements TimeUnit
{
    public static final long MILLIS_PER_SECOND = 1000;

    public TimeUnitSecond()
    {
    }

    @Override public long getMillisPerUnit()
    {
        return MILLIS_PER_SECOND;
    }
}

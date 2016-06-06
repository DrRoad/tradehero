package com.androidth.general.common.time;

import org.ocpsoft.prettytime.TimeUnit;

abstract public class TimeUnitMinute implements TimeUnit
{
    public static final long MILLIS_PER_MINUTE = 60000;

    public TimeUnitMinute()
    {
    }

    @Override public long getMillisPerUnit()
    {
        return MILLIS_PER_MINUTE;
    }
}

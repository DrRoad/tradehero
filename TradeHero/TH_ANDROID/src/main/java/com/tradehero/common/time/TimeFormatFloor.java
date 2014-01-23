package com.tradehero.common.time;

import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.TimeFormat;

/**
 * Created by xavier on 1/23/14.
 */
public class TimeFormatFloor implements TimeFormat
{
    public static final String TAG = TimeFormatFloor.class.getSimpleName();

    @Override public String format(Duration duration)
    {
        return "";
    }

    @Override public String formatUnrounded(Duration duration)
    {
        return "";
    }

    @Override public String decorate(Duration duration, String s)
    {
        return s + format(duration);
    }

    @Override public String decorateUnrounded(Duration duration, String s)
    {
        return s + formatUnrounded(duration);
    }
}

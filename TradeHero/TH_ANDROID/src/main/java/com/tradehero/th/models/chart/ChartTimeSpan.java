package com.ayondo.academy.models.chart;

/**
 * Indicates a duration in seconds
 */
public class ChartTimeSpan
{
    public static final long MIN_1 =        60;
    public static final long MIN_5 =       300;
    public static final long MIN_15 =      900;
    public static final long MIN_30 =     1800;
    public static final long HOUR_1 =     3600;
    public static final long HOUR_4 =    14400;
    public static final long DAY_1 =     86400;
    public static final long DAY_5 =    432000;
    public static final long MONTH_3 = 2629800;
    public static final long MONTH_6 = 5259600;
    public static final long YEAR_1 = 10519200;
    public static final long YEAR_2 = 21038400;
    public static final long YEAR_5 = 52596000;
    public static final long MAX = Long.MAX_VALUE;

    public final long duration;

    public ChartTimeSpan(long duration)
    {
        super();
        this.duration = duration;
    }

    @Override public int hashCode()
    {
        return Long.valueOf(duration).hashCode();
    }

    @Override public boolean equals(Object other)
    {
        return other instanceof ChartTimeSpan && equals((ChartTimeSpan) other);
    }

    public boolean equals(ChartTimeSpan other)
    {
        return other != null && Long.valueOf(duration).equals(other.duration);
    }
}

package com.tradehero.th.models.chart.yahoo;

import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.models.chart.ChartDTO;
import com.tradehero.th.models.chart.ChartSize;
import com.tradehero.th.models.chart.ChartTimeSpan;
import java.util.ArrayList;
import java.util.List;

public class YahooChartDTO implements ChartDTO
{
    public static final ChartSize DEFAULT_CHART_SIZE = new ChartSize(YahooChartSize.small.yahooPixelWidth, YahooChartSize.small.yahooPixelHeight);
    public static final ChartTimeSpan DEFAULT_TIME_SPAN = new ChartTimeSpan(YahooTimeSpan.month3.chartTimeSpanDuration);

    public String yahooSymbol;
    public YahooChartSize size;
    public YahooTimeSpan timeSpan;
    public boolean includeVolume;
    public final List<YahooMovingAverageInterval> movingAverageIntervals;

    //<editor-fold desc="Constructors">
    public YahooChartDTO()
    {
        this(null);
    }

    public YahooChartDTO(SecurityCompactDTO securityCompactDTO)
    {
        this(
                securityCompactDTO,
                DEFAULT_CHART_SIZE);
    }

    public YahooChartDTO(SecurityCompactDTO securityCompactDTO, ChartSize chartSize)
    {
        this(
                securityCompactDTO,
                chartSize,
                DEFAULT_TIME_SPAN);
    }

    public YahooChartDTO(SecurityCompactDTO securityCompactDTO, ChartSize chartSize, ChartTimeSpan chartTimeSpan)
    {
        this(
                securityCompactDTO,
                chartSize,
                chartTimeSpan,
                defaultMovingAverageIntervals());
    }

    public YahooChartDTO(SecurityCompactDTO securityCompactDTO, ChartSize chartSize, ChartTimeSpan chartTimeSpan, List<YahooMovingAverageInterval> movingAverageIntervals)
    {
        setSecurityCompactDTO(securityCompactDTO);
        setChartSize(chartSize);
        setChartTimeSpan(chartTimeSpan);
        this.movingAverageIntervals = movingAverageIntervals;
    }

    public YahooChartDTO(String yahooSymbol, YahooChartSize size, YahooTimeSpan timeSpan)
    {
        this.yahooSymbol = yahooSymbol;
        this.size = size;
        this.timeSpan = timeSpan;
        this.movingAverageIntervals = defaultMovingAverageIntervals();
    }

    public YahooChartDTO(String yahooSymbol, YahooChartSize size, YahooTimeSpan timeSpan,
            List<YahooMovingAverageInterval> movingAverageIntervals)
    {
        this.yahooSymbol = yahooSymbol;
        this.size = size;
        this.timeSpan = timeSpan;
        this.movingAverageIntervals = movingAverageIntervals;
    }
    //</editor-fold>

    @Override public void setSecurityCompactDTO(SecurityCompactDTO securityCompactDTO)
    {
        this.yahooSymbol = securityCompactDTO == null ? "" : securityCompactDTO.yahooSymbol;
    }

    @Override public ChartSize getChartSize()
    {
        return size.getChartSize();
    }

    @Override public void setChartSize(ChartSize chartSize)
    {
        this.size = YahooChartSize.getPreferredSize(chartSize.width, chartSize.height);
    }

    @Override public ChartTimeSpan getChartTimeSpan()
    {
        return timeSpan.getChartTimeSpan();
    }

    @Override public void setChartTimeSpan(ChartTimeSpan chartTimeSpan)
    {
        this.timeSpan = YahooTimeSpan.getBestApproximation(chartTimeSpan);
    }

    @Override public void setIncludeVolume(boolean includeVolume)
    {
        this.includeVolume = includeVolume;
    }

    @Override public boolean isIncludeVolume()
    {
        return includeVolume;
    }

    public static List<YahooMovingAverageInterval> defaultMovingAverageIntervals()
    {
        ArrayList<YahooMovingAverageInterval> created = new ArrayList<>();
        created.add(YahooMovingAverageInterval.m50);
        created.add(YahooMovingAverageInterval.m200);
        return created;
    }

    public String getChartUrl()
    {
        return String.format(
                "http://chart.finance.yahoo.com/z?s=%s&t=%s&q=l&z=%s&p=%s%s",
                yahooSymbol,
                timeSpan.code,
                size.code,
                YahooMovingAverageInterval.concat(movingAverageIntervals),
                includeVolume ? "&a=v" : "");
    }
}

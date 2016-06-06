package com.androidth.general.widget.news.yahoo;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.androidth.general.R;
import com.androidth.general.models.chart.yahoo.YahooTimeSpan;
import com.androidth.general.widget.news.TimeSpanButton;

public class YahooTimeSpanButton extends TimeSpanButton
{
    //<editor-fold desc="Constructors">
    public YahooTimeSpanButton(Context context)
    {
        super(context);
    }

    public YahooTimeSpanButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public YahooTimeSpanButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    protected void init(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimeSpanButton);
        YahooTimeSpan yahooTimeSpan = YahooTimeSpan.valueOf(a.getString(R.styleable.TimeSpanButton_timeSpan));
        setChartTimeSpan(yahooTimeSpan.getChartTimeSpan());
        a.recycle();
    }
}

package com.tradehero.th.fragments.billing;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.tradehero.thm.R;
import timber.log.Timber;

public class StoreItemHasFurther extends RelativeLayout
{
    protected TextView title;
    protected int titleResId;

    protected ImageView icon;
    protected int iconResId;

    //<editor-fold desc="Constructors">
    public StoreItemHasFurther(Context context)
    {
        super(context);
    }

    public StoreItemHasFurther(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public StoreItemHasFurther(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        initViews();
    }

    protected void initViews()
    {
        title = (TextView) findViewById(R.id.title);
        icon = (ImageView) findViewById(R.id.icon);
    }

    public void setTitleResId(int titleResId)
    {
        this.titleResId = titleResId;
        displayTitle();
    }

    public void setIconResId(int iconResId)
    {
        this.iconResId = iconResId;
        displayIcon();
    }

    public void display()
    {
        displayTitle();
        displayIcon();
    }

    protected void displayTitle()
    {
        if (title != null)
        {
            title.setText(titleResId);
        }
    }

    protected void displayIcon()
    {
        if (icon != null)
        {
            try
            {
                icon.setImageResource(iconResId);
            }
            catch (OutOfMemoryError e)
            {
                Timber.e(e, "");
            }
        }
    }
}

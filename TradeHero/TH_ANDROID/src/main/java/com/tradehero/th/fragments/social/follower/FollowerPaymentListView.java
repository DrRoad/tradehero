package com.tradehero.th.fragments.social.follower;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/** Created with IntelliJ IDEA. User: xavier Date: 10/14/13 Time: 11:42 AM To change this template use File | Settings | File Templates. */
public class FollowerPaymentListView extends ListView
{
    //<editor-fold desc="Constructors">
    public FollowerPaymentListView(Context context)
    {
        super(context);
    }

    public FollowerPaymentListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FollowerPaymentListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        init();
    }

    protected void init ()
    {
    }
}

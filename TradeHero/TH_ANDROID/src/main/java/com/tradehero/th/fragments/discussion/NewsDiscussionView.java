package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.util.AttributeSet;

public class NewsDiscussionView extends DiscussionView
{
    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public NewsDiscussionView(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public NewsDiscussionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public NewsDiscussionView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void setLoading()
    {
        super.setLoading();

        if (discussionStatus != null)
        {
            discussionStatus.setVisibility(VISIBLE);
        }
    }

    @Override protected void setLoaded()
    {
        super.setLoaded();

        if (discussionStatus != null)
        {
            discussionStatus.setVisibility(GONE);
        }
    }
}

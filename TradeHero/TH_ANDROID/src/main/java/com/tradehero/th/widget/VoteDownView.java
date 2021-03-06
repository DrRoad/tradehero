package com.tradehero.th.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.VoteDirection;

public class VoteDownView extends VoteView
{
    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public VoteDownView(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public VoteDownView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public VoteDownView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override public void display(AbstractDiscussionCompactDTO discussionDTO)
    {
        if (discussionDTO != null)
        {
            setValue(discussionDTO.downvoteCount);
            setChecked(discussionDTO.voteDirection == VoteDirection.DownVote.value);
        }
        else
        {
            setValue(R.integer.messages_initial_vote_count);
            setChecked(false);
        }
    }

    @Override protected int getCheckedColor()
    {
        return Color.RED;
    }
}

package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import com.tradehero.th.api.discussion.key.CommentKey;

public class CommentItemViewLinear extends DiscussionItemViewLinear<CommentKey>
{
    //<editor-fold desc="Constructors">
    public CommentItemViewLinear(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    //</editor-fold>

    protected void openDiscussion()
    {
        if (abstractDiscussionCompactDTO != null)
        {
            Bundle args = new Bundle();
            NewsDiscussionFragment.putDiscussionKey(args, abstractDiscussionCompactDTO.getDiscussionKey());
            getNavigator().pushFragment(NewsDiscussionFragment.class, args);
        }
    }

    @Override
    protected AbstractDiscussionCompactItemViewHolder.OnMenuClickedListener createViewHolderMenuClickedListener()
    {
        return new CommentItemViewHolderMenuClickedListener()
        {
            @Override public void onShareButtonClicked()
            {
                // Nothing to do
            }

            @Override public void onTranslationRequested()
            {
                // Nothing to do
            }
        };
    }

    abstract protected class CommentItemViewHolderMenuClickedListener extends DiscussionItemViewMenuClickedListener
    {
        @Override public void onCommentButtonClicked()
        {
            openDiscussion();
        }
    }
}

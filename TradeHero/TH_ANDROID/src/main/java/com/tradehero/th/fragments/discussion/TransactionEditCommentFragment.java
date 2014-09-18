package com.tradehero.th.fragments.discussion;

import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import com.tradehero.th.R;

public class TransactionEditCommentFragment extends SecurityDiscussionEditPostFragment
{
    private Editable unSpanedComment;

    @Override protected void initView()
    {
        super.initView();

        discussionPostActionButtonsView.hideSocialButtons();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        postMenuButton.setTitle(R.string.done);
        setActionBarTitle(R.string.trade_comment);
    }

    public Editable getComment()
    {
        return unSpanedComment;
    }

    @Override protected void postDiscussion()
    {
        unSpanedComment = unSpanText(discussionPostContent.getText());
        navigator.popFragment();
    }
}

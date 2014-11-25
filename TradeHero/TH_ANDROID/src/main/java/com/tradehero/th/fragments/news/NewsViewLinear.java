package com.tradehero.th.fragments.news;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.news.NewsItemCompactDTO;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.api.news.key.NewsItemDTOKey;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewHolder;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewLinear;
import com.tradehero.th.fragments.discussion.DiscussionEditPostFragment;
import com.tradehero.th.fragments.trade.BuySellFragment;
import com.tradehero.th.fragments.web.WebViewFragment;

public class NewsViewLinear extends AbstractDiscussionCompactItemViewLinear<NewsItemDTOKey>
{
    //<editor-fold desc="Constructors">
    public NewsViewLinear(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    //</editor-fold>

    @Override protected NewsItemViewHolder createViewHolder()
    {
        return new NewsItemViewHolder<>(getContext());
    }

    @Override protected void fetchDiscussionDetail()
    {
        refresh(); // Just to make sure we get the complete NewsItemDTO if it was not fetched yet.
    }

    public void setTitleBackground(int resId)
    {
        if (viewHolder != null)
        {
            viewHolder.setBackgroundResource(resId);
        }
    }

    protected void pushWebFragment()
    {
        if (abstractDiscussionCompactDTO != null
                && ((NewsItemCompactDTO) abstractDiscussionCompactDTO).url != null)
        {
            Bundle bundle = new Bundle();
            WebViewFragment.putUrl(bundle, ((NewsItemCompactDTO) abstractDiscussionCompactDTO).url);
            getNavigator().pushFragment(WebViewFragment.class, bundle);
        }
    }

    protected void pushBuySellFragment(SecurityId securityId)
    {
        Bundle args = new Bundle();
        BuySellFragment.putSecurityId(args, securityId);
        getNavigator().pushFragment(BuySellFragment.class, args);
    }

    protected void pushNewDiscussion()
    {
        Bundle bundle = new Bundle();
        bundle.putBundle(DiscussionKey.BUNDLE_KEY_DISCUSSION_KEY_BUNDLE, discussionKey.getArgs());
        getNavigator().pushFragment(DiscussionEditPostFragment.class, bundle);
    }

    @Override
    protected AbstractDiscussionCompactItemViewHolder.OnMenuClickedListener createViewHolderMenuClickedListener()
    {
        return new NewsViewHolderClickedListener()
        {
            @Override public void onShareButtonClicked()
            {
                // Nothing to do
            }

            @Override public void onUserClicked(UserBaseKey userClicked)
            {
                // Nothing to do
            }

            @Override public void onTranslationRequested()
            {
                // Nothing to do
            }
        };
    }

    abstract protected class NewsViewHolderClickedListener extends AbstractDiscussionViewHolderClickedListener
        implements NewsItemViewHolder.OnMenuClickedListener
    {
        @Override public void onCommentButtonClicked()
        {
            pushNewDiscussion();
        }

        @Override public void onOpenOnWebClicked()
        {
            pushWebFragment();
        }

        @Override public void onSecurityClicked(SecurityId securityId)
        {
            pushBuySellFragment(securityId);
        }
    }
}

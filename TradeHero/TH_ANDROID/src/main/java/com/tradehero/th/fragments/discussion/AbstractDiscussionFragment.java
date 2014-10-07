package com.tradehero.th.fragments.discussion;

import android.os.Bundle;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.BottomTabs;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.discussion.key.DiscussionKeyFactory;
import com.tradehero.th.fragments.DashboardTabHost;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class AbstractDiscussionFragment extends BasePurchaseManagerFragment
{
    private static final String DISCUSSION_KEY_BUNDLE_KEY = AbstractDiscussionFragment.class.getName() + ".discussionKey";

    @InjectView(R.id.discussion_view) protected DiscussionView discussionView;

    @Inject @NotNull protected DiscussionKeyFactory discussionKeyFactory;
    @Inject @BottomTabs protected DashboardTabHost dashboardTabHost;

    private DiscussionKey discussionKey;

    //region Inflow bundling
    public static void putDiscussionKey(@NotNull Bundle args, @NotNull DiscussionKey discussionKey)
    {
        args.putBundle(DISCUSSION_KEY_BUNDLE_KEY, discussionKey.getArgs());
    }

    @Nullable protected static DiscussionKey getDiscussionKey(@NotNull Bundle args, @NotNull DiscussionKeyFactory discussionKeyFactory)
    {
        if (args.containsKey(DISCUSSION_KEY_BUNDLE_KEY))
        {
            return discussionKeyFactory.fromBundle(args.getBundle(DISCUSSION_KEY_BUNDLE_KEY));
        }
        return null;
    }
    //endregion

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.discussionKey = getDiscussionKey(getArguments(), discussionKeyFactory);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        ButterKnife.inject(this, view);
        discussionView.discussionList.setOnScrollListener(dashboardBottomTabsListViewScrollListener.get());
        super.onViewCreated(view, savedInstanceState);
    }

    @Override protected void initViews(View view)
    {
        if (discussionView != null)
        {
            discussionView.setCommentPostedListener(createCommentPostedListener());
        }
    }

    @Override public void onDestroyView()
    {
        if (discussionView != null)
        {
            discussionView.discussionList.setOnScrollListener(null);
        }
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onResume()
    {
        super.onResume();

        if (discussionKey != null)
        {
            linkWith(discussionKey, true);
        }

        dashboardTabHost.setOnTranslate(new DashboardTabHost.OnTranslateListener()
        {
            @Override public void onTranslate(float x, float y)
            {
                if (discussionView.postCommentView != null)
                {
                    discussionView.postCommentView.setTranslationY(y);
                }
            }
        });
    }

    @Override public void onPause()
    {
        dashboardTabHost.setOnTranslate(null);
        super.onPause();
    }

    public DiscussionKey getDiscussionKey()
    {
        return discussionKey;
    }

    protected void linkWith(DiscussionKey discussionKey, boolean andDisplay)
    {
        this.discussionKey = discussionKey;
        if (andDisplay && discussionView != null)
        {
            discussionView.display(discussionKey);
        }
    }

    protected PostCommentView.CommentPostedListener createCommentPostedListener()
    {
        return new AbstractDiscussionCommentPostedListener();
    }

    protected class AbstractDiscussionCommentPostedListener implements PostCommentView.CommentPostedListener
    {
        @Override public void success(DiscussionDTO discussionDTO)
        {
            handleCommentPosted(discussionDTO);
        }

        @Override public void failure(Exception exception)
        {
            // Nothing to do
        }
    }

    abstract protected void handleCommentPosted(DiscussionDTO discussionDTO);
}

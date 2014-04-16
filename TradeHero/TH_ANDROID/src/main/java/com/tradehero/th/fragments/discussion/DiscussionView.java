package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.discussion.AbstractDiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionKeyList;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.discussion.key.DiscussionKeyFactory;
import com.tradehero.th.api.discussion.key.DiscussionListKey;
import com.tradehero.th.api.discussion.key.PaginatedDiscussionListKey;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.persistence.discussion.DiscussionCache;
import com.tradehero.th.persistence.discussion.DiscussionListCache;
import com.tradehero.th.utils.DaggerUtils;
import javax.inject.Inject;
import timber.log.Timber;

public class DiscussionView extends FrameLayout
    implements DTOView<DiscussionKey>
{
    @InjectView(android.R.id.list) ListView discussionList;
    @InjectView(R.id.discussion_comment_widget) protected PostCommentView postCommentView;

    private int listItemLayout;
    private int topicLayout;

    @Inject DiscussionListCache discussionListCache;
    @Inject DiscussionCache discussionCache;
    @Inject DiscussionKeyFactory discussionKeyFactory;

    protected TextView discussionStatus;
    private DiscussionKey discussionKey;

    private DTOCache.Listener<DiscussionListKey, DiscussionKeyList> discussionFetchTaskListener;

    private DTOCache.GetOrFetchTask<DiscussionListKey, DiscussionKeyList> discussionFetchTask;
    private DiscussionListAdapter discussionListAdapter;
    private DiscussionListKey discussionListKey;
    private int nextPageDelta;
    private PaginatedDiscussionListKey paginatedDiscussionListKey;
    private View topicView;

    //<editor-fold desc="Constructors">
    public DiscussionView(Context context)
    {
        super(context);
    }

    public DiscussionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs);
    }

    public DiscussionView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(attrs);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();

        ButterKnife.inject(this);

        inflateDiscussionTopic();
        inflateDiscussionStatus();

        DaggerUtils.inject(this);

        discussionFetchTaskListener = new DiscussionFetchListener();
        discussionListAdapter = createDiscussionListAdapter();
    }

    protected DiscussionListAdapter createDiscussionListAdapter()
    {
        return new DiscussionListAdapter(
                getContext(),
                LayoutInflater.from(getContext()),
                listItemLayout);
    }

    private void init(AttributeSet attrs)
    {
        if (attrs != null)
        {
            TypedArray styled = getContext().obtainStyledAttributes(attrs, R.styleable.DiscussionView);
            listItemLayout = styled.getResourceId(R.styleable.DiscussionView_listItemLayout, 0);
            topicLayout = styled.getResourceId(R.styleable.DiscussionView_topicLayout, 0);
            styled.recycle();

            ensureStyle();
        }
    }

    private void ensureStyle()
    {
        if (listItemLayout == 0)
        {
            throw new IllegalStateException("listItemLayout should be set to a layout");
        }
    }

    private void inflateDiscussionTopic()
    {
        if (topicLayout != 0)
        {
            topicView = LayoutInflater.from(getContext()).inflate(topicLayout, null);

            if (topicView != null)
            {
                discussionList.addHeaderView(topicView);
            }
        }
    }

    private void inflateDiscussionStatus()
    {
        View commentListStatusView = LayoutInflater.from(getContext()).inflate(R.layout.discussion_load_status, null);

        if (commentListStatusView != null)
        {
            discussionStatus = (TextView) commentListStatusView.findViewById(R.id.discussion_load_status);
            discussionList.addHeaderView(commentListStatusView);
        }
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        discussionFetchTaskListener = new DiscussionFetchListener();
        discussionList.setAdapter(discussionListAdapter);
        postCommentView.setCommentPostedListener(new DiscussionViewCommentPostedListener());
    }

    @Override protected void onDetachedFromWindow()
    {
        detachDiscussionFetchTask();
        postCommentView.setCommentPostedListener(null);
        discussionList.setAdapter(null);
        discussionFetchTaskListener = null;

        ButterKnife.reset(this);
        super.onDetachedFromWindow();
    }

    @Override public void display(DiscussionKey discussionKey)
    {
        this.discussionKey = discussionKey;

        linkWith(discussionKey, true);
    }

    private void linkWith(DiscussionKey discussionKey, boolean andDisplay)
    {
        postCommentView.linkWith(discussionKey);

        if (discussionKey != null)
        {
            this.discussionListKey = discussionKeyFactory.toListKey(discussionKey);

            fetchDiscussionListIfNecessary();
        }

        if (andDisplay)
        {
            displayTopicView();
        }
    }

    private void displayTopicView()
    {
        if (topicView instanceof DTOView)
        {
            try
            {
                ((DTOView<DiscussionKey>) topicView).display(discussionKey);
            }
            catch (Exception ex)
            {
                Timber.e(ex, "topicView should implement DTOView<DiscussionKey>");
            }
        }
    }

    private void linkWith(DiscussionKeyList discussionKeyList, boolean andDisplay)
    {
        if (discussionKeyList != null)
        {
            nextPageDelta = discussionKeyList.isEmpty() ? -1 : 1;

            discussionListAdapter.appendMore(discussionKeyList);
        }

        if (andDisplay)
        {
            discussionStatus.setText(R.string.discussion_loaded);
        }
    }

    private void fetchDiscussionListIfNecessary()
    {
        detachDiscussionFetchTask();

        if (paginatedDiscussionListKey == null)
        {
            paginatedDiscussionListKey = new PaginatedDiscussionListKey(discussionListKey, 1);
        }

        if (nextPageDelta >= 0)
        {
            paginatedDiscussionListKey = paginatedDiscussionListKey.next(nextPageDelta);

            setLoading();
            discussionFetchTask = discussionListCache.getOrFetch(paginatedDiscussionListKey, false, discussionFetchTaskListener);
            discussionFetchTask.execute();
        }
    }

    /**
     * This method is called when there is a new comment for the current discussion
     * @param newDiscussion
     */
    protected void addComment(DiscussionDTO newDiscussion)
    {
        DiscussionKey newDiscussionKey = newDiscussion.getDiscussionKey();
        discussionCache.put(newDiscussionKey, newDiscussion);
        updateCommentCount();

        if (discussionListAdapter != null)
        {
            discussionListAdapter.addItem(newDiscussionKey);
            discussionListAdapter.notifyDataSetChanged();
        }

    }

    private void updateCommentCount()
    {
        if (discussionKey != null)
        {
            AbstractDiscussionDTO discussionDTO = discussionCache.get(discussionKey);
            if (discussionDTO != null)
            {
                ++discussionDTO.commentCount;
                displayTopicView();
            }
        }
    }

    protected void setLoading()
    {
        if (discussionStatus != null)
        {
            discussionStatus.setText(R.string.discussion_loading);
        }
    }

    protected void setLoaded()
    {
        if (discussionStatus != null)
        {
            discussionStatus.setText(R.string.discussion_loaded);
        }
    }

    private void detachDiscussionFetchTask()
    {
        if (discussionFetchTask != null)
        {
            discussionFetchTask.setListener(null);
        }
        discussionFetchTask = null;
    }

    private class DiscussionFetchListener implements DTOCache.Listener<DiscussionListKey, DiscussionKeyList>
    {
        @Override public void onDTOReceived(DiscussionListKey key, DiscussionKeyList value, boolean fromCache)
        {
            onFinish();

            linkWith(value, true);
        }

        @Override public void onErrorThrown(DiscussionListKey key, Throwable error)
        {
            onFinish();

            THToast.show(new THException(error));
        }

        private void onFinish()
        {
            setLoaded();
        }
    }

    protected class DiscussionViewCommentPostedListener implements PostCommentView.CommentPostedListener
    {
        @Override public void success(DiscussionDTO discussionDTO)
        {
            addComment(discussionDTO);
        }

        @Override public void failure(Exception exception)
        {
            THToast.show(R.string.error_unknown);
        }
    }
}

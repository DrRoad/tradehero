package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.discussion.AbstractDiscussionDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.persistence.discussion.DiscussionCache;
import com.tradehero.th.widget.VotePair;
import javax.inject.Inject;
import javax.inject.Provider;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * Created by thonguyen on 10/4/14.
 */
public class AbstractDiscussionItemView<T extends DiscussionKey> extends LinearLayout
        implements DTOView<T>
{
    @InjectView(R.id.discussion_content) TextView content;
    @InjectView(R.id.vote_pair) VotePair votePair;
    @InjectView(R.id.discussion_time) TextView time;

    @Inject DiscussionCache discussionCache;
    @Inject Provider<PrettyTime> prettyTime;

    protected T discussionKey;

    private DTOCache.Listener<DiscussionKey, AbstractDiscussionDTO> discussionFetchListener;
    private DTOCache.GetOrFetchTask<DiscussionKey, AbstractDiscussionDTO> discussionFetchTask;

    //<editor-fold desc="Constructors">
    public AbstractDiscussionItemView(Context context)
    {
        super(context);
    }

    public AbstractDiscussionItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AbstractDiscussionItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();

        ButterKnife.inject((AbstractDiscussionItemView) this, this);

        discussionFetchListener = new DiscussionFetchListener();
    }

    @Override protected void onDetachedFromWindow()
    {
        detachFetchDiscussionTask();

        super.onDetachedFromWindow();
    }

    @Override public void display(T discussionKey)
    {
        this.discussionKey = discussionKey;

        fetchDiscussionDetail();
    }

    private void fetchDiscussionDetail()
    {
        detachFetchDiscussionTask();

        discussionFetchTask = discussionCache.getOrFetch(discussionKey, false, discussionFetchListener);
        discussionFetchTask.execute();
    }

    private void detachFetchDiscussionTask()
    {
        if (discussionFetchTask != null)
        {
            discussionFetchTask.setListener(null);
        }
        discussionFetchTask = null;
    }

    protected void linkWith(AbstractDiscussionDTO abstractDiscussionDTO, boolean andDisplay)
    {
        if (andDisplay && abstractDiscussionDTO != null)
        {
            display(abstractDiscussionDTO);
        }
    }


    private void display(AbstractDiscussionDTO abstractDiscussionDTO)
    {
        // markup text
        displayContent(abstractDiscussionDTO);

        // timeline time
        displayTime(abstractDiscussionDTO);

        votePair.display(abstractDiscussionDTO);
    }

    private void displayContent(AbstractDiscussionDTO item)
    {
        content.setText(item.text);
    }

    private void displayTime(AbstractDiscussionDTO abstractDiscussionDTO)
    {
        if (abstractDiscussionDTO.createdAtUtc != null)
        {
            time.setText(prettyTime.get().formatUnrounded(abstractDiscussionDTO.createdAtUtc));
        }
    }

    private class DiscussionFetchListener implements DTOCache.Listener<DiscussionKey, AbstractDiscussionDTO>
    {
        @Override public void onDTOReceived(DiscussionKey key, AbstractDiscussionDTO value, boolean fromCache)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(DiscussionKey key, Throwable error)
        {
            THToast.show(new THException(error));
        }
    }
}

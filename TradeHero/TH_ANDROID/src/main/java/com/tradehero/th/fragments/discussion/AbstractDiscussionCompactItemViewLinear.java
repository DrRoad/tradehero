package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.share.SocialShareFormDTO;
import com.tradehero.th.api.share.SocialShareResultDTO;
import com.tradehero.th.api.translation.TranslationResult;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.share.SocialShareTranslationHelper;
import com.tradehero.th.persistence.discussion.DiscussionCache;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

abstract public class AbstractDiscussionCompactItemViewLinear<T extends DiscussionKey>
        extends LinearLayout
        implements DTOView<T>
{
    @Inject protected DiscussionCache discussionCache;
    @Inject protected SocialShareTranslationHelper socialShareHelper;
    protected AbstractDiscussionCompactItemViewHolder viewHolder;
    protected T discussionKey;
    protected AbstractDiscussionCompactDTO abstractDiscussionCompactDTO;

    private DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> discussionFetchListener;

    //<editor-fold desc="Constructors">
    public AbstractDiscussionCompactItemViewLinear(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        HierarchyInjector.inject(this);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        if (!isInEditMode())
        {
            discussionFetchListener = createDiscussionFetchListener();
            viewHolder = createViewHolder();
            viewHolder.onFinishInflate(this);
            socialShareHelper.setMenuClickedListener(createSocialShareMenuClickedListener());
        }
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (discussionFetchListener == null)
        {
            discussionFetchListener = createDiscussionFetchListener();
        }
        if (!isInEditMode())
        {
            viewHolder.onAttachedToWindow(this);
            viewHolder.linkWith(abstractDiscussionCompactDTO, true);
            viewHolder.setMenuClickedListener(createViewHolderMenuClickedListener());
            socialShareHelper.setMenuClickedListener(createSocialShareMenuClickedListener());
        }
    }

    @Override protected void onDetachedFromWindow()
    {
        detachFetchDiscussionTask();
        socialShareHelper.onDetach();
        viewHolder.setMenuClickedListener(null);
        viewHolder.onDetachedFromWindow();
        discussionFetchListener = null;
        super.onDetachedFromWindow();
    }

    protected AbstractDiscussionCompactItemViewHolder createViewHolder()
    {
        return new AbstractDiscussionCompactItemViewHolder<AbstractDiscussionCompactDTO>(getContext());
    }

    @Override public void display(T discussionKey)
    {
        this.discussionKey = discussionKey;

        fetchDiscussionDetail(false);
    }

    public void refresh()
    {
        fetchDiscussionDetail(true);
    }

    private void fetchDiscussionDetail(boolean force)
    {
        detachFetchDiscussionTask();
        discussionCache.register(discussionKey, discussionFetchListener);
        discussionCache.getOrFetchAsync(discussionKey, force);
    }

    private void detachFetchDiscussionTask()
    {
        discussionCache.unregister(discussionFetchListener);
    }

    protected void linkWith(AbstractDiscussionCompactDTO abstractDiscussionDTO, boolean andDisplay)
    {
        this.abstractDiscussionCompactDTO = abstractDiscussionDTO;
        viewHolder.linkWith(abstractDiscussionDTO, andDisplay);
        if (andDisplay)
        {
        }
    }

    protected DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> createDiscussionFetchListener()
    {
        return new DiscussionFetchListener();
    }

    private class DiscussionFetchListener
            implements DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO>
    {
        @Override
        public void onDTOReceived(@NotNull DiscussionKey key, @NotNull AbstractDiscussionCompactDTO value)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(@NotNull DiscussionKey key, @NotNull Throwable error)
        {
            THToast.show(new THException(error));
        }
    }

    //<editor-fold desc="Navigation">
    protected DashboardNavigator getNavigator()
    {
        return ((DashboardNavigatorActivity) getContext()).getDashboardNavigator();
    }
    //</editor-fold>

    protected AbstractDiscussionCompactItemViewHolder.OnMenuClickedListener createViewHolderMenuClickedListener()
    {
        return new AbstractDiscussionViewHolderClickedListener()
        {
            @Override public void onShareButtonClicked()
            {
                // Nothing to do
            }

            @Override public void onCommentButtonClicked()
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

    abstract protected class AbstractDiscussionViewHolderClickedListener implements AbstractDiscussionItemViewHolder.OnMenuClickedListener
    {
        @Override public void onMoreButtonClicked()
        {
            socialShareHelper.shareOrTranslate(abstractDiscussionCompactDTO);
        }
    }

    protected SocialShareTranslationHelper.OnMenuClickedListener createSocialShareMenuClickedListener()
    {
        return new AbstractDiscussionItemViewShareTranslationMenuClickListener();
    }

    protected class AbstractDiscussionItemViewShareTranslationMenuClickListener implements SocialShareTranslationHelper.OnMenuClickedListener
    {
        @Override public void onCancelClicked()
        {
        }

        @Override public void onShareRequestedClicked(SocialShareFormDTO socialShareFormDTO)
        {
        }

        @Override public void onConnectRequired(SocialShareFormDTO shareFormDTO)
        {
        }

        @Override public void onShared(SocialShareFormDTO shareFormDTO,
                SocialShareResultDTO socialShareResultDTO)
        {
        }

        @Override public void onShareFailed(SocialShareFormDTO shareFormDTO, Throwable throwable)
        {
        }

        @Override public void onTranslationClicked(AbstractDiscussionCompactDTO toTranslate)
        {
        }

        @Override public void onTranslatedOneAttribute(AbstractDiscussionCompactDTO toTranslate,
                TranslationResult translationResult)
        {
        }

        @Override public void onTranslatedAllAtributes(AbstractDiscussionCompactDTO toTranslate,
                AbstractDiscussionCompactDTO translated)
        {
        }

        @Override public void onTranslateFailed(AbstractDiscussionCompactDTO toTranslate,
                Throwable error)
        {
        }
    }
}

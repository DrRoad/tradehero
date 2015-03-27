package com.tradehero.th.fragments.news;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import com.tradehero.th.api.news.NewsItemCompactDTO;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewHolder;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewLinear;
import org.ocpsoft.prettytime.PrettyTime;

public class NewsHeadlineViewLinear extends AbstractDiscussionCompactItemViewLinear
{
    //<editor-fold desc="Constructors">
    public NewsHeadlineViewLinear(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    //</editor-fold>

    @NonNull @Override protected NewsItemCompactViewHolder createViewHolder()
    {
        return new NewsItemCompactViewHolder();
    }

    public void setNewsBackgroundResource(@DrawableRes int resId)
    {
        viewHolder.setBackgroundResource(resId);
    }

    public static class Requisite extends AbstractDiscussionCompactItemViewLinear.Requisite
    {
        public Requisite(
                @NonNull Resources resources,
                @NonNull PrettyTime prettyTime,
                @NonNull NewsItemCompactDTO discussionDTO,
                boolean canTranslate,
                boolean isAutoTranslate)
        {
            super(resources, prettyTime, discussionDTO, canTranslate, isAutoTranslate);
        }
    }

    public static class DTO extends AbstractDiscussionCompactItemViewLinear.DTO
    {
        public DTO(@NonNull Requisite requisite)
        {
            super(requisite);
        }

        @NonNull @Override protected AbstractDiscussionCompactItemViewHolder.DTO createViewHolderDTO(
                @NonNull AbstractDiscussionCompactItemViewLinear.Requisite requisite)
        {
            return new NewsItemCompactViewHolder.DTO(
                    new NewsItemCompactViewHolder.Requisite(
                            requisite.resources,
                            requisite.prettyTime,
                            (NewsItemCompactDTO) requisite.discussionDTO,
                            requisite.canTranslate,
                            requisite.isAutoTranslate));
        }
    }
}

package com.tradehero.th.persistence.discussion;

import com.tradehero.common.persistence.StraightCutDTOCacheNew;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.DiscussionDTOList;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.news.key.NewsItemDTOKey;
import com.tradehero.th.api.timeline.key.TimelineItemDTOKey;
import com.tradehero.th.network.service.DiscussionServiceWrapper;
import com.tradehero.th.network.service.NewsServiceWrapper;
import com.tradehero.th.network.service.UserTimelineServiceWrapper;
import com.tradehero.th.persistence.SingleCacheMaxSize;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class DiscussionCache extends StraightCutDTOCacheNew<DiscussionKey, AbstractDiscussionCompactDTO, AbstractDiscussionCompactCutDTO>
{
    @NotNull private final DiscussionServiceWrapper discussionServiceWrapper;
    @NotNull private final NewsServiceWrapper newsServiceWrapper;
    @NotNull private final UserTimelineServiceWrapper timelineServiceWrapper;
    @NotNull private final AbstractDiscussionCompactCutDTOFactory cutDTOFactory;

    //<editor-fold desc="Constructors">
    @Inject public DiscussionCache(
            @SingleCacheMaxSize IntPreference maxSize,
            @NotNull NewsServiceWrapper newsServiceWrapper,
            @NotNull UserTimelineServiceWrapper userTimelineServiceWrapper,
            @NotNull DiscussionServiceWrapper discussionServiceWrapper,
            @NotNull AbstractDiscussionCompactCutDTOFactory cutDTOFactory)
    {
        super(maxSize.get());

        this.discussionServiceWrapper = discussionServiceWrapper;
        this.newsServiceWrapper = newsServiceWrapper;
        this.timelineServiceWrapper = userTimelineServiceWrapper;
        this.cutDTOFactory = cutDTOFactory;
    }
    //</editor-fold>

    @Override @NotNull public AbstractDiscussionCompactDTO fetch(@NotNull DiscussionKey discussionKey) throws Throwable
    {
        if (discussionKey instanceof TimelineItemDTOKey)
        {
            return timelineServiceWrapper.getTimelineDetail((TimelineItemDTOKey) discussionKey);
        }
        else if (discussionKey instanceof NewsItemDTOKey)
        {
            return newsServiceWrapper.getSecurityNewsDetail(discussionKey);
        }
        return discussionServiceWrapper.getComment(discussionKey);
    }

    @NotNull @Override protected AbstractDiscussionCompactCutDTO cutValue(
            @NotNull DiscussionKey key,
            @NotNull AbstractDiscussionCompactDTO value)
    {
        return cutDTOFactory.shrinkValue(value);
    }

    @Nullable @Override protected AbstractDiscussionCompactDTO inflateValue(
            @NotNull DiscussionKey key,
            @Nullable AbstractDiscussionCompactCutDTO cutValue)
    {
        return cutDTOFactory.inflate(cutValue);
    }

    public DiscussionDTOList put(List<? extends AbstractDiscussionCompactDTO> discussionList)
    {
        DiscussionDTOList<? super AbstractDiscussionCompactDTO> previous = new DiscussionDTOList<>();
        for (AbstractDiscussionCompactDTO discussionDTO : discussionList)
        {
            previous.add(put(discussionDTO.getDiscussionKey(), discussionDTO));
        }
        return previous;
    }

    public DiscussionDTOList<? super AbstractDiscussionCompactDTO> get(List<DiscussionKey> discussionKeys)
    {
        if (discussionKeys == null)
        {
            return null;
        }
        DiscussionDTOList<? super AbstractDiscussionCompactDTO> dtos = new DiscussionDTOList<>();
        for (DiscussionKey discussionKey : discussionKeys)
        {
            dtos.add(get(discussionKey));
        }
        return dtos;
    }
}

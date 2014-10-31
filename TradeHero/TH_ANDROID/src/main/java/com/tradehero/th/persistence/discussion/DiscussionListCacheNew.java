package com.tradehero.th.persistence.discussion;

import com.tradehero.common.persistence.DTOCacheUtilNew;
import com.tradehero.common.persistence.StraightDTOCacheNew;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.api.discussion.AbstractDiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionKeyList;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.discussion.key.DiscussionListKey;
import com.tradehero.th.api.discussion.key.MessageDiscussionListKey;
import com.tradehero.th.api.discussion.key.PaginatedDiscussionListKey;
import com.tradehero.th.api.pagination.PaginatedDTO;
import com.tradehero.th.network.service.DiscussionServiceWrapper;
import com.tradehero.th.persistence.ListCacheMaxSize;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton @UserCache
public class DiscussionListCacheNew extends StraightDTOCacheNew<DiscussionListKey, DiscussionKeyList>
{
    @NotNull private final DiscussionCache discussionCache;
    @NotNull private final DiscussionServiceWrapper discussionServiceWrapper;

    @Inject public DiscussionListCacheNew(
            @ListCacheMaxSize IntPreference maxSize,
            @NotNull DiscussionServiceWrapper discussionServiceWrapper,
            @NotNull DiscussionCache discussionCache,
            @NotNull DTOCacheUtilNew dtoCacheUtil)
    {
        super(maxSize.get(), dtoCacheUtil);
        this.discussionServiceWrapper = discussionServiceWrapper;
        this.discussionCache = discussionCache;
    }

    @Override @NotNull public DiscussionKeyList fetch(@NotNull DiscussionListKey discussionListKey) throws Throwable
    {
        if (discussionListKey instanceof MessageDiscussionListKey)
        {
            return putInternal(discussionServiceWrapper.getMessageThread((MessageDiscussionListKey) discussionListKey));
        }
        else if (discussionListKey instanceof PaginatedDiscussionListKey)
        {
            return putInternal(discussionServiceWrapper.getDiscussions((PaginatedDiscussionListKey) discussionListKey));
        }
        throw new IllegalStateException("Unhandled key " + discussionListKey);
    }

    @NotNull private DiscussionKeyList putInternal(@NotNull PaginatedDTO<DiscussionDTO> paginatedDTO)
    {
        List<DiscussionDTO> data = paginatedDTO.getData();

        discussionCache.put(data);

        DiscussionKeyList discussionKeyList = new DiscussionKeyList();
        for (@NotNull AbstractDiscussionDTO abstractDiscussionDTO: data)
        {
            discussionKeyList.add(abstractDiscussionDTO.getDiscussionKey());
        }

        return discussionKeyList;
    }

    public void invalidateAllPagesFor(@Nullable DiscussionKey discussionKey)
    {
        for (DiscussionListKey discussionListKey : new ArrayList<>(snapshot().keySet()))
        {
            if (discussionListKey.equivalentFields(discussionKey))
            {
                invalidate(discussionListKey);
            }
        }
    }
    /**
     * TODO right
     * @param discussionType
     */
    public void invalidateAllForDiscussionType(DiscussionType discussionType)
    {
        for (DiscussionListKey discussionListKey : new ArrayList<>(snapshot().keySet()))
        {
            if (discussionListKey.inReplyToType == discussionType)
            {
                invalidate(discussionListKey);
            }
        }
    }

    public void getOrFetchAsyncWhereSameField(@NotNull DiscussionKey originatingDiscussion)
    {
        for (DiscussionListKey key : snapshot().keySet())
        {
            if (key.equivalentFields(originatingDiscussion))
            {
                getOrFetchAsync(key, true);
            }
        }
    }

    public static interface DiscussionKeyListListener extends Listener<DiscussionListKey, DiscussionKeyList>
    {
    }
}

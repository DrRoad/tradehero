package com.tradehero.th.models.discussion;

import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionDTOFactory;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.persistence.discussion.DiscussionCacheRx;
import com.tradehero.th.persistence.user.UserMessagingRelationshipCacheRx;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DTOProcessorDiscussionCreate extends DTOProcessorDiscussion
{
    @NonNull protected final CurrentUserId currentUserId;
    @NonNull protected final DiscussionCacheRx discussionCache;
    @NonNull protected final UserMessagingRelationshipCacheRx userMessagingRelationshipCache;
    @Nullable protected final DiscussionKey stubKey;

    //<editor-fold desc="Constructors">
    public DTOProcessorDiscussionCreate(
            @NonNull DiscussionDTOFactory discussionDTOFactory,
            @NonNull CurrentUserId currentUserId,
            @NonNull DiscussionCacheRx discussionCache,
            @NonNull UserMessagingRelationshipCacheRx userMessagingRelationshipCache,
            @Nullable DiscussionKey stubKey)
    {
        super(discussionDTOFactory);
        this.currentUserId = currentUserId;
        this.discussionCache = discussionCache;
        this.userMessagingRelationshipCache = userMessagingRelationshipCache;
        this.stubKey = stubKey;
    }
    //</editor-fold>

    @Override public DiscussionDTO process(DiscussionDTO discussionDTO)
    {
        DiscussionDTO processed = super.process(discussionDTO);
        if (stubKey != null)
        {
            discussionCache.invalidate(stubKey);
        }
        if (processed != null && processed.userId <= 0)
        {
            // HACK
            processed.userId = currentUserId.toUserBaseKey().key;
        }
        if (processed != null)
        {
            discussionCache.onNext(processed.getDiscussionKey(), processed);
        }
        if (processed != null)
        {
            processed.stubKey = stubKey;
            userMessagingRelationshipCache.invalidate(new UserBaseKey(discussionDTO.userId));
        }
        return processed;
    }
}

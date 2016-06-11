package com.androidth.general.models.discussion;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.androidth.general.api.discussion.DiscussionDTO;
import com.androidth.general.api.discussion.key.DiscussionKey;
import com.androidth.general.api.users.CurrentUserId;
import com.androidth.general.api.users.UserBaseKey;
import com.androidth.general.persistence.discussion.DiscussionCacheRx;
import com.androidth.general.persistence.user.UserMessagingRelationshipCacheRx;

public class DTOProcessorDiscussionCreate extends DTOProcessorDiscussion
{
    @NonNull protected final CurrentUserId currentUserId;
    @NonNull protected final DiscussionCacheRx discussionCache;
    @NonNull protected final UserMessagingRelationshipCacheRx userMessagingRelationshipCache;
    @Nullable protected final DiscussionKey stubKey;

    //<editor-fold desc="Constructors">
    public DTOProcessorDiscussionCreate(
            @NonNull CurrentUserId currentUserId,
            @NonNull DiscussionCacheRx discussionCache,
            @NonNull UserMessagingRelationshipCacheRx userMessagingRelationshipCache,
            @Nullable DiscussionKey stubKey)
    {
        this.currentUserId = currentUserId;
        this.discussionCache = discussionCache;
        this.userMessagingRelationshipCache = userMessagingRelationshipCache;
        this.stubKey = stubKey;
    }
    //</editor-fold>

    @Override public DiscussionDTO process(@NonNull DiscussionDTO discussionDTO)
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
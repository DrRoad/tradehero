package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionDTOFactory;
import com.tradehero.th.api.discussion.form.DiscussionFormDTO;
import com.tradehero.th.api.discussion.form.ReplyDiscussionFormDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.discussion.key.DiscussionKeyFactory;
import com.tradehero.th.api.discussion.key.DiscussionListKey;
import com.tradehero.th.api.discussion.key.DiscussionVoteKey;
import com.tradehero.th.api.discussion.key.MessageDiscussionListKey;
import com.tradehero.th.api.discussion.key.PaginatedDiscussionListKey;
import com.tradehero.th.api.pagination.PaginatedDTO;
import com.tradehero.th.api.timeline.TimelineItemShareRequestDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.models.discussion.DTOProcessorDiscussion;
import com.tradehero.th.models.discussion.DTOProcessorDiscussionReply;
import com.tradehero.th.network.DelayRetriesOrFailFunc1;
import com.tradehero.th.persistence.discussion.DiscussionCacheRx;
import com.tradehero.th.persistence.discussion.DiscussionListCacheRx;
import com.tradehero.th.persistence.user.UserMessagingRelationshipCacheRx;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class DiscussionServiceWrapper
{
    private static final int RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MILLIS = 1000;

    @NonNull private final DiscussionServiceRx discussionServiceRx;
    @NonNull private final DiscussionKeyFactory discussionKeyFactory;
    @NonNull private final DiscussionDTOFactory discussionDTOFactory;
    @NonNull private final CurrentUserId currentUserId;

    // It has to be lazy to avoid infinite dependency
    @NonNull private final Lazy<DiscussionListCacheRx> discussionListCache;
    @NonNull private final Lazy<DiscussionCacheRx> discussionCache;
    @NonNull private final Lazy<UserMessagingRelationshipCacheRx> userMessagingRelationshipCache;

    //<editor-fold desc="Constructors">
    @Inject public DiscussionServiceWrapper(
            @NonNull DiscussionServiceRx discussionServiceRx,
            @NonNull DiscussionKeyFactory discussionKeyFactory,
            @NonNull DiscussionDTOFactory discussionDTOFactory,
            @NonNull CurrentUserId currentUserId,
            @NonNull Lazy<DiscussionListCacheRx> discussionListCache,
            @NonNull Lazy<DiscussionCacheRx> discussionCache,
            @NonNull Lazy<UserMessagingRelationshipCacheRx> userMessagingRelationshipCache)
    {
        this.discussionServiceRx = discussionServiceRx;
        this.discussionKeyFactory = discussionKeyFactory;
        this.discussionDTOFactory = discussionDTOFactory;
        this.currentUserId = currentUserId;
        this.discussionCache = discussionCache;
        this.discussionListCache = discussionListCache;
        this.userMessagingRelationshipCache = userMessagingRelationshipCache;
    }
    //</editor-fold>

    //<editor-fold desc="DTO Processors">
    @NonNull protected DTOProcessorDiscussionReply createDiscussionReplyProcessor(@NonNull DiscussionKey initiatingKey,
            @Nullable DiscussionKey stubKey)
    {
        return new DTOProcessorDiscussionReply(
                discussionDTOFactory,
                currentUserId,
                discussionCache.get(),
                userMessagingRelationshipCache.get(),
                stubKey,
                discussionListCache.get(),
                initiatingKey);
    }
    //</editor-fold>

    // TODO add providers in RetrofitModule and RetrofitProtectedModule
    // TODO add methods based on DiscussionServiceAsync and MiddleCallback implementations

    //<editor-fold desc="Get Comment">
    @NonNull public Observable<DiscussionDTO> getCommentRx(@NonNull DiscussionKey discussionKey)
    {
        return discussionServiceRx.getComment(discussionKey.id)
                .map(new DTOProcessorDiscussion(discussionDTOFactory));
    }
    //</editor-fold>

    //<editor-fold desc="Create Discussion">
    @NonNull public Observable<DiscussionDTO> createDiscussionRx(@NonNull DiscussionFormDTO discussionFormDTO)
    {
        if (discussionFormDTO instanceof ReplyDiscussionFormDTO)
        {
            return discussionServiceRx.createDiscussion(discussionFormDTO)
                    .retryWhen(new DelayRetriesOrFailFunc1(RETRY_COUNT, RETRY_DELAY_MILLIS))
                    .map(createDiscussionReplyProcessor(
                            ((ReplyDiscussionFormDTO) discussionFormDTO).getInitiatingDiscussionKey(),
                            discussionFormDTO.stubKey));
        }
        return postToTimelineRx(
                currentUserId.toUserBaseKey(),
                discussionFormDTO);
    }
    //</editor-fold>

    //<editor-fold desc="Get Discussions">
    @NonNull public Observable<PaginatedDTO<DiscussionDTO>> getDiscussionsRx(@NonNull PaginatedDiscussionListKey discussionsKey)
    {
        return discussionServiceRx.getDiscussions(
                discussionsKey.inReplyToType,
                discussionsKey.inReplyToId,
                discussionsKey.page,
                discussionsKey.perPage);
    }

    @NonNull public Observable<PaginatedDTO<DiscussionDTO>> getMessageThreadRx(@NonNull MessageDiscussionListKey discussionsKey)
    {
        return discussionServiceRx.getMessageThread(
                discussionsKey.inReplyToType,
                discussionsKey.inReplyToId,
                discussionsKey.toMap());
    }
    //</editor-fold>

    //<editor-fold desc="Vote">
    @NonNull public Observable<DiscussionDTO> voteRx(@NonNull DiscussionVoteKey discussionVoteKey)
    {
        return discussionServiceRx.vote(
                discussionVoteKey.inReplyToType,
                discussionVoteKey.inReplyToId,
                discussionVoteKey.voteDirection)
                .map(createDiscussionReplyProcessor(
                        discussionKeyFactory.create(discussionVoteKey.inReplyToType, discussionVoteKey.inReplyToId),
                        null));
    }
    //</editor-fold>

    //<editor-fold desc="Share">
    @NonNull public Observable<BaseResponseDTO> shareRx(
            @NonNull DiscussionListKey discussionKey,
            @NonNull TimelineItemShareRequestDTO timelineItemShareRequestDTO)
    {
        return discussionServiceRx.share(
                discussionKey.inReplyToType,
                discussionKey.inReplyToId,
                timelineItemShareRequestDTO);
    }
    //</editor-fold>

    //<editor-fold desc="Post to Timeline">
    @NonNull public Observable<DiscussionDTO> postToTimelineRx(
            @NonNull UserBaseKey userBaseKey,
            @NonNull DiscussionFormDTO discussionFormDTO)
    {
        return discussionServiceRx.postToTimeline(
                userBaseKey.key,
                discussionFormDTO)
                .retryWhen(new DelayRetriesOrFailFunc1(RETRY_COUNT, RETRY_DELAY_MILLIS))
                .map(new DTOProcessorDiscussion(discussionDTOFactory));
    }
    //</editor-fold>
}

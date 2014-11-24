package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.pagination.RangeDTO;
import com.tradehero.th.api.timeline.TimelineDTO;
import com.tradehero.th.api.timeline.TimelineItemDTO;
import com.tradehero.th.api.timeline.TimelineItemShareRequestDTO;
import com.tradehero.th.api.timeline.TimelineSection;
import com.tradehero.th.api.timeline.key.TimelineItemDTOKey;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.models.timeline.TimelineDTOProcessor;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class UserTimelineServiceWrapper
{
    @NonNull private final UserTimelineService userTimelineService;
    @NonNull private final UserTimelineServiceRx userTimelineServiceRx;
    @NonNull private final Provider<TimelineDTOProcessor> timelineProcessorProvider;

    //<editor-fold desc="Constructors">
    @Inject public UserTimelineServiceWrapper(
            @NonNull UserTimelineService userTimelineService,
            @NonNull UserTimelineServiceRx userTimelineServiceRx,
            @NonNull Provider<TimelineDTOProcessor> timelineProcessorProvider)
    {
        super();
        this.userTimelineService = userTimelineService;
        this.userTimelineServiceRx = userTimelineServiceRx;
        this.timelineProcessorProvider = timelineProcessorProvider;
    }
    //</editor-fold>

    //<editor-fold desc="Get Global Timeline">
    // TODO create a proper key that contains the values max / min
    @NonNull protected Observable<TimelineDTO> getGlobalTimelineRx(Integer maxCount, Integer maxId, Integer minId)
    {
        return userTimelineServiceRx.getGlobalTimeline(maxCount, maxId, minId);
    }

    @NonNull public Observable<TimelineItemDTO> getTimelineDetailRx(@NonNull TimelineItemDTOKey key)
    {
        return userTimelineServiceRx.getTimelineDetail(key.id);
    }
    //</editor-fold>

    //<editor-fold desc="Get User Timeline">
    @NonNull public Observable<TimelineDTO> getDefaultTimelineRx(@NonNull UserBaseKey userId, Integer maxCount, Integer maxId, Integer minId)
    {
        // Make a key that contains all info.
        return userTimelineServiceRx.getTimeline(TimelineSection.Timeline, userId.key, maxCount, maxId, minId);
    }

    public TimelineDTO getTimelineBySection(TimelineSection section,
            @NonNull UserBaseKey userId, Integer maxCount, Integer maxId, Integer minId)
    {
        // Make a key that contains all info.
        return userTimelineService.getTimeline(section, userId.key, maxCount, maxId, minId);
    }

    public Observable<TimelineDTO> getTimelineBySectionRx(TimelineSection section,
            @NonNull UserBaseKey userId, Integer maxCount, Integer maxId, Integer minId)
    {
        // Make a key that contains all info.
        return userTimelineServiceRx.getTimeline(section, userId.key, maxCount, maxId, minId)
                .doOnNext(timelineProcessorProvider.get());
    }

    public Observable<TimelineDTO> getTimelineBySectionRx(TimelineSection section, @NonNull UserBaseKey userBaseKey, RangeDTO rangeDTO)
    {
        return getTimelineBySectionRx(section, userBaseKey, rangeDTO.maxCount, rangeDTO.maxId, rangeDTO.minId);
    }
    //</editor-fold>

    //<editor-fold desc="Share Timeline Item">
    @NonNull public Observable<BaseResponseDTO> shareTimelineItemRx(
            @NonNull UserBaseKey userId,
            @NonNull TimelineItemDTOKey timelineItemId,
            @NonNull TimelineItemShareRequestDTO timelineItemShareRequestDTO)
    {
        return userTimelineServiceRx.shareTimelineItem(userId.key, timelineItemId.id, timelineItemShareRequestDTO);
    }
    //</editor-fold>

    //<editor-fold desc="Delete Timeline Item">
    @NonNull public Observable<BaseResponseDTO> deleteTimelineItemRx(
            @NonNull UserBaseKey userId,
            @NonNull TimelineItemDTOKey timelineItemId)
    {
        return userTimelineServiceRx.deleteTimelineItem(userId.key, timelineItemId.id);
    }
    //</editor-fold>
}

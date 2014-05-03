package com.tradehero.th.persistence.timeline;

import com.tradehero.common.cache.DatabaseCache;
import com.tradehero.common.persistence.Query;
import com.tradehero.th.api.timeline.key.TimelineItemDTOKey;
import dagger.Lazy;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;


public class TimelineManager
{
    @Inject DatabaseCache dbCache;
    @Inject Lazy<TimelineStore.Factory> allTimelineStores;

    public List<TimelineItemDTOKey> getTimeline(Query query, boolean forceReload) throws IOException
    {
        // TODO scope locking for current timeline of user
        TimelineStore timelineStore = allTimelineStores.get().under((Integer) query.getId());
        if (query.getId() == null)
        {
            Timber.e(new NullPointerException("query.getId was null"), "");
        }
        if (timelineStore == null)
        {
            Timber.e(new NullPointerException("timelineStore was null"), "");
        }
        if (query == null)
        {
            Timber.e(new NullPointerException("query was null"), "");
        }
        timelineStore.setQuery(query);
        return forceReload ? dbCache.requestAndStore(timelineStore) : dbCache.loadOrRequest(timelineStore);
        // and unlock the scope
    }
}

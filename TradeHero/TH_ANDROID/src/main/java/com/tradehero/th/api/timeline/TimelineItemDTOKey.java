package com.tradehero.th.api.timeline;

import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.key.DiscussionKey;

/**
 * Created with IntelliJ IDEA. User: tho Date: 3/11/14 Time: 11:22 AM Copyright (c) TradeHero
 */
public class TimelineItemDTOKey extends DiscussionKey
{
    private static DiscussionType TYPE = DiscussionType.TIMELINE_ITEM;

    public TimelineItemDTOKey(Integer id)
    {
        super(TYPE, id);
    }
}

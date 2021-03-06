package com.tradehero.th.api.discussion.key;

import android.os.Bundle;
import com.tradehero.th.api.discussion.DiscussionType;
import android.support.annotation.NonNull;

public class BroadcastDiscussionKey extends DiscussionKey<BroadcastDiscussionKey>
{
    private static final DiscussionType TYPE = DiscussionType.BROADCAST_MESSAGE;

    //<editor-fold desc="Constructors">
    public BroadcastDiscussionKey(@NonNull Integer id)
    {
        super(id);
    }

    protected BroadcastDiscussionKey(@NonNull Bundle args)
    {
        super(args);
    }
    //</editor-fold>

    @Override public DiscussionType getType()
    {
        return TYPE;
    }
}

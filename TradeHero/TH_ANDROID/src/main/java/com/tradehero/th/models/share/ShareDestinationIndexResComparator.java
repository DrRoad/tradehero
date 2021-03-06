package com.tradehero.th.models.share;

import android.content.Context;
import android.content.res.Resources;
import com.tradehero.th.R;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import android.support.annotation.NonNull;

public class ShareDestinationIndexResComparator implements Comparator<ShareDestination>
{
    public static final int ORDERED_SHARE_DESTINATION_IDS = R.array.ordered_share_destinations;

    @NonNull private final Resources resources;
    @NonNull private final List<Integer> destinationIds;

    @Inject public ShareDestinationIndexResComparator(
            @NonNull Context context,
            @NonNull @ShareDestinationId Set<Integer> destinationIds)
    {
        this.resources = context.getResources();
        this.destinationIds = new ArrayList<>(destinationIds);
    }

    @Override public int compare(ShareDestination left, ShareDestination right)
    {
        if (left == right)
        {
            return 0;
        }
        return indexOf(left).compareTo(indexOf(right));
    }

    @NonNull protected Integer indexOf(@NonNull ShareDestination shareDestination)
    {
        return this.destinationIds.indexOf(resources.getInteger(shareDestination.getIdResId()));
    }
}

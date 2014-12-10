package com.tradehero.th.fragments.competition;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.tradehero.th.utils.broadcast.BroadcastConstants;
import com.tradehero.th.utils.broadcast.BroadcastData;

public class CompetitionEnrollmentBroadcastSignal implements BroadcastData
{
    @NonNull @Override public Bundle getArgs()
    {
        return new Bundle();
    }

    @NonNull @Override public String getBroadcastBundleKey()
    {
        return BroadcastConstants.KEY_ENROLLMENT_BROADCAST;
    }

    @NonNull @Override public String getBroadcastIntentActionName()
    {
        return BroadcastConstants.ENROLLMENT_INTENT_ACTION_NAME;
    }
}
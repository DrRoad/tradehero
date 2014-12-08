package com.tradehero.th.fragments.competition;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.tradehero.th.fragments.onboarding.FragmentOnBoardComponent;
import com.tradehero.th.utils.broadcast.BroadcastData;

public class CompetitionEnrollmentBroadcastSignal implements BroadcastData
{
    @NonNull @Override public Bundle getArgs()
    {
        return new Bundle();
    }

    @NonNull @Override public String getBroadcastBundleKey()
    {
        return FragmentOnBoardComponent.KEY_ENROLLMENT_BROADCAST;
    }

    @NonNull @Override public String getBroadcastIntentActionName()
    {
        return FragmentOnBoardComponent.ENROLLMENT_INTENT_ACTION_NAME;
    }
}
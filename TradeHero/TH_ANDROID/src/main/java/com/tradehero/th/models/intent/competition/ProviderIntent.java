package com.tradehero.th.models.intent.competition;

import android.content.res.Resources;
import com.tradehero.th.R;
import com.tradehero.th.fragments.dashboard.RootFragmentType;
import com.tradehero.th.models.intent.THIntent;
import org.jetbrains.annotations.NotNull;

public class ProviderIntent extends THIntent
{
    //<editor-fold desc="Constructors">
    public ProviderIntent(@NotNull Resources resources)
    {
        super(resources);
    }
    //</editor-fold>

    @Override public String getUriPath()
    {
        return getHostUriPath(resources, R.string.intent_host_providers);
    }

    @Override public RootFragmentType getDashboardType()
    {
        return RootFragmentType.COMMUNITY;
    }
}

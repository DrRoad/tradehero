package com.tradehero.th.api.competition.key;

import com.tradehero.common.persistence.DTOKey;
import com.tradehero.th.api.competition.ProviderId;
import android.support.annotation.NonNull;

public class ProviderDisplayCellListKey implements DTOKey
{
    @NonNull private final ProviderId providerId;

    //<editor-fold desc="Constructor">
    public ProviderDisplayCellListKey(@NonNull ProviderId providerId)
    {
        this.providerId = providerId;
    }
    //</editor-fold>

    @NonNull public ProviderId getProviderId()
    {
        return providerId;
    }

    @Override public int hashCode()
    {
        return providerId == null ? 0 : providerId.hashCode();
    }

    @Override public boolean equals(Object other)
    {
        return (other instanceof ProviderDisplayCellListKey) && equals((ProviderDisplayCellListKey) other);
    }

    public boolean equals(ProviderDisplayCellListKey other)
    {
        return other != null &&
                (providerId == null ? other.providerId == null : providerId.equals(other.providerId));
    }
}

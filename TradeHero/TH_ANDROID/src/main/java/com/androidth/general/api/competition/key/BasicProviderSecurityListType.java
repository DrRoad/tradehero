package com.androidth.general.api.competition.key;

import android.support.annotation.NonNull;
import com.androidth.general.api.competition.ProviderId;
import com.androidth.general.api.security.key.SecurityListType;

public class BasicProviderSecurityListType extends ProviderSecurityListType
{
    //<editor-fold desc="Constructors">
    public BasicProviderSecurityListType(ProviderSecurityListType other)
    {
        super(other);
    }

    public BasicProviderSecurityListType(ProviderId providerId, Integer page, Integer perPage)
    {
        super(providerId, page, perPage);
    }

    public BasicProviderSecurityListType(ProviderId providerId, Integer page)
    {
        super(providerId, page);
    }

    public BasicProviderSecurityListType(ProviderId providerId)
    {
        super(providerId);
    }
    //</editor-fold>

    @Override protected boolean equalFields(@NonNull SecurityListType other)
    {
        return super.equalFields(other)
            && other instanceof BasicProviderSecurityListType;
    }
}
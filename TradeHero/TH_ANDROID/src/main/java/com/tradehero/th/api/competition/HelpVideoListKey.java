package com.tradehero.th.api.competition;

import com.tradehero.common.persistence.DTOKey;

/** Created with IntelliJ IDEA. User: xavier Date: 10/3/13 Time: 5:07 PM To change this template use File | Settings | File Templates. */
public class HelpVideoListKey implements DTOKey
{
    public static final String TAG = HelpVideoListKey.class.getSimpleName();

    private ProviderId providerId;

    //<editor-fold desc="Constructor">
    public HelpVideoListKey(ProviderId providerId)
    {
        this.providerId = providerId;
        this.validate();
    }
    //</editor-fold>

    public void validate()
    {
        if (this.providerId == null)
        {
            throw new IllegalArgumentException("ProviderId cannot be null");
        }
    }

    public ProviderId getProviderId()
    {
        return providerId;
    }
}

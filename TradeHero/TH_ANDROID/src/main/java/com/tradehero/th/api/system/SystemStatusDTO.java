package com.tradehero.th.api.system;

import com.tradehero.common.persistence.DTO;

public class SystemStatusDTO implements DTO
{
    public static final boolean DEFAULT_ALERTS_ARE_FREE = true;

    /**
     * When true, alerts are free and need no IAP
     */
    public boolean alertsAreFree = DEFAULT_ALERTS_ARE_FREE;
    public String androidAppPackageNameInUse;

    public PriceDTO friendReferralAward;

    //<editor-fold desc="Constructors">
    public SystemStatusDTO()
    {
        super();
    }
    //</editor-fold>
}

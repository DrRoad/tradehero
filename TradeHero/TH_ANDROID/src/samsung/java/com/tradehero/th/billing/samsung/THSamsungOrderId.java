package com.tradehero.th.billing.samsung;

import com.tradehero.common.billing.samsung.SamsungOrderId;
import com.tradehero.th.billing.THOrderId;

public class THSamsungOrderId
        extends SamsungOrderId
        implements THOrderId
{
    public THSamsungOrderId(String purchaseId)
    {
        super(purchaseId);
    }
}

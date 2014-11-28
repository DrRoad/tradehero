package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exception.IABException;

interface THIABPurchaseReporterHolder extends
        IABPurchaseReporterHolder<
                IABSKU,
                THIABOrderId,
                THIABPurchase,
                IABException>
{
}
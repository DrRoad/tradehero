package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.th.billing.THPurchaseReporter;

/**
 * Created by xavier on 3/27/14.
 */
public interface THIABPurchaseReporter
    extends THPurchaseReporter<
            IABSKU,
            THIABOrderId,
            THIABPurchase,
            IABException>
{
}

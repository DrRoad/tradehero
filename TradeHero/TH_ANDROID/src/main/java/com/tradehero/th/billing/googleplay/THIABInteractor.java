package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.googleplay.IABInteractor;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exception.IABException;

/** Created with IntelliJ IDEA. User: xavier Date: 11/8/13 Time: 11:06 AM To change this template use File | Settings | File Templates. */
public interface THIABInteractor extends IABInteractor<
        IABSKU,
        THIABProductDetail,
        THIABPurchaseOrder,
        THIABOrderId,
        THIABPurchase,
        THIABLogicHolder,
        THIABBillingRequest,
        IABException>
{
}

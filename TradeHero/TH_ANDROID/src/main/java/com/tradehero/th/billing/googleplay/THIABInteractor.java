package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.googleplay.IABInteractor;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.IABSKUList;
import com.tradehero.common.billing.googleplay.IABSKUListKey;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.th.billing.THBillingInteractor;
import com.tradehero.th.billing.googleplay.request.THUIIABRequest;
import com.tradehero.th.billing.googleplay.request.THIABBillingRequestFull;

interface THIABInteractor
        extends
        IABInteractor<
                IABSKUListKey,
                IABSKU,
                IABSKUList,
                THIABProductDetail,
                THIABPurchaseOrder,
                THIABOrderId,
                THIABPurchase,
                THIABLogicHolder,
                THIABBillingRequestFull,
                THUIIABRequest,
                IABException>,
        THBillingInteractor<
                IABSKUListKey,
                IABSKU,
                IABSKUList,
                THIABProductDetail,
                THIABPurchaseOrder,
                THIABOrderId,
                THIABPurchase,
                THIABLogicHolder,
                THIABBillingRequestFull,
                THUIIABRequest,
                IABException>
{
}

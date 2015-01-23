package com.tradehero.common.billing.samsung.purchasefetch;

import com.tradehero.common.billing.purchasefetch.BillingPurchaseFetcherHolderRx;
import com.tradehero.common.billing.samsung.SamsungOrderId;
import com.tradehero.common.billing.samsung.SamsungPurchase;
import com.tradehero.common.billing.samsung.SamsungSKU;

public interface SamsungPurchaseFetcherHolderRx<
        SamsungSKUType extends SamsungSKU,
        SamsungOrderIdType extends SamsungOrderId,
        SamsungPurchaseType extends SamsungPurchase<SamsungSKUType, SamsungOrderIdType>>
        extends BillingPurchaseFetcherHolderRx<
        SamsungSKUType,
        SamsungOrderIdType,
        SamsungPurchaseType>
{
}
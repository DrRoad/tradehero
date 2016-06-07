package com.androidth.general.common.billing.googleplay.purchasefetch;

import com.androidth.general.common.billing.googleplay.IABOrderId;
import com.androidth.general.common.billing.googleplay.IABPurchase;
import com.androidth.general.common.billing.googleplay.IABSKU;
import com.androidth.general.common.billing.purchasefetch.BillingPurchaseFetcherRx;

public interface IABPurchaseFetcherRx<
        IABSKUType extends IABSKU,
        IABOrderIdType extends IABOrderId,
        IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>>
        extends BillingPurchaseFetcherRx<
        IABSKUType,
        IABOrderIdType,
        IABPurchaseType>
{
}

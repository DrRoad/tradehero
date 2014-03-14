package com.tradehero.common.billing.googleplay;

import com.tradehero.common.billing.ProductPurchaseCache;

/**
 * Created by xavier on 2/11/14.
 */
public class IABPurchaseCache<
            IABSKUType extends IABSKU,
            IABOrderIdType extends IABOrderId,
            IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>>
        extends ProductPurchaseCache<
            IABSKUType,
            IABOrderIdType,
            IABPurchaseType>
{
    public static final String TAG = IABPurchaseCache.class.getSimpleName();

    public IABPurchaseCache(int maxSize)
    {
        super(maxSize);
    }

    @Override protected IABPurchaseType fetch(IABSKUType key) throws Throwable
    {
        throw new IllegalStateException("You cannot fetch on this cache");
    }
}

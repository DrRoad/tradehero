package com.tradehero.common.billing.googleplay;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.BaseBillingPurchaseFetcherHolder;
import com.tradehero.common.billing.BillingPurchaseFetcher;
import com.tradehero.common.billing.googleplay.exception.IABException;
import java.util.HashMap;
import java.util.Map;

abstract public class BaseIABPurchaseFetcherHolder<
        IABSKUType extends IABSKU,
        IABOrderIdType extends IABOrderId,
        IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>,
        IABPurchaseFetcherType extends IABPurchaseFetcher<
                        IABSKUType,
                        IABOrderIdType,
                        IABPurchaseType,
                        IABException>>
    extends BaseBillingPurchaseFetcherHolder<
        IABSKUType,
        IABOrderIdType,
        IABPurchaseType,
        IABException>
    implements IABPurchaseFetcherHolder<
        IABSKUType,
        IABOrderIdType,
        IABPurchaseType,
        IABException>
{
    @NonNull protected final Map<Integer /*requestCode*/, IABPurchaseFetcherType> purchaseFetchers;

    //<editor-fold desc="Constructors">
    public BaseIABPurchaseFetcherHolder()
    {
        super();
        purchaseFetchers = new HashMap<>();
    }
    //</editor-fold>

    @Override public boolean isUnusedRequestCode(int requestCode)
    {
        return super.isUnusedRequestCode(requestCode) &&
            !purchaseFetchers.containsKey(requestCode);
    }

    @Override public void forgetRequestCode(int requestCode)
    {
        super.forgetRequestCode(requestCode);
        IABPurchaseFetcherType purchaseFetcher = purchaseFetchers.get(requestCode);
        if (purchaseFetcher != null)
        {
            purchaseFetcher.setListener(null);
            purchaseFetcher.setPurchaseFetchedListener(null);
        }
        purchaseFetchers.remove(requestCode);
    }

    @Override public void launchFetchPurchaseSequence(int requestCode)
    {
        BillingPurchaseFetcher.OnPurchaseFetchedListener<IABSKUType, IABOrderIdType, IABPurchaseType, IABException> purchaseFetchedListener = createPurchaseFetchedListener();
        IABPurchaseFetcherType purchaseFetcher = createPurchaseFetcher(requestCode);
        purchaseFetcher.setPurchaseFetchedListener(purchaseFetchedListener);
        purchaseFetchers.put(requestCode, purchaseFetcher);
        purchaseFetcher.fetchPurchases();
    }

    @Override public void onDestroy()
    {
        for (IABPurchaseFetcherType purchaseFetcher : purchaseFetchers.values())
        {
            if (purchaseFetcher != null)
            {
                purchaseFetcher.onDestroy();
            }
        }
        purchaseFetchers.clear();
        super.onDestroy();
    }

    abstract protected IABPurchaseFetcherType createPurchaseFetcher(int requestCode);
}

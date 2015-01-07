package com.tradehero.common.billing.purchasefetch;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.BaseRequestCodeHolder;
import com.tradehero.common.billing.OrderId;
import com.tradehero.common.billing.ProductIdentifier;
import com.tradehero.common.billing.ProductPurchase;
import rx.Observable;

abstract public class BaseBillingPurchaseFetcherHolderRx<
        ProductIdentifierType extends ProductIdentifier,
        OrderIdType extends OrderId,
        ProductPurchaseType extends ProductPurchase<ProductIdentifierType, OrderIdType>>
        extends BaseRequestCodeHolder<BillingPurchaseFetcherRx<
        ProductIdentifierType,
        OrderIdType,
        ProductPurchaseType>>
        implements BillingPurchaseFetcherHolderRx<
        ProductIdentifierType,
        OrderIdType,
        ProductPurchaseType>
{
    //<editor-fold desc="Constructors">
    public BaseBillingPurchaseFetcherHolderRx()
    {
        super();
    }
    //</editor-fold>

    @NonNull @Override public Observable<PurchaseFetchResult<ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> get(int requestCode)
    {
        BillingPurchaseFetcherRx<ProductIdentifierType,
                OrderIdType,
                ProductPurchaseType> fetcher = actors.get(requestCode);
        if (fetcher == null)
        {
            fetcher = createFetcher(requestCode);
            actors.put(requestCode, fetcher);
        }
        return fetcher.get();
    }

    @NonNull protected abstract BillingPurchaseFetcherRx<ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType> createFetcher(int requestCode);
}
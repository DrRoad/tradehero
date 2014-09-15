package com.tradehero.common.billing;

import com.tradehero.common.billing.exception.BillingException;

import java.util.List;

public interface BillingPurchaseFetcher<
        ProductIdentifierType extends ProductIdentifier,
        OrderIdType extends OrderId,
        ProductPurchaseType extends ProductPurchase<ProductIdentifierType, OrderIdType>,
        BillingExceptionType extends BillingException>
        extends RequestCodeActor
{
    OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> getFetchListener();
    void setPurchaseFetchedListener(OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> fetchListener);
    void fetchPurchases(int requestCode);

    public static interface OnPurchaseFetchedListener<
            ProductIdentifierType extends ProductIdentifier,
            OrderIdType extends OrderId,
            ProductPurchaseType extends ProductPurchase<ProductIdentifierType, OrderIdType>,
            BillingExceptionType extends BillingException>
    {
        void onFetchedPurchases(int requestCode, List<ProductPurchaseType> purchases);
        void onFetchPurchasesFailed(int requestCode, BillingExceptionType exception);
    }
}

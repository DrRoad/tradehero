package com.tradehero.common.billing;

import com.tradehero.common.billing.exception.BillingException;

public interface BillingPurchaseRestorerHolder<
        ProductIdentifierType extends ProductIdentifier,
        OrderIdType extends OrderId,
        ProductPurchaseType extends ProductPurchase<ProductIdentifierType, OrderIdType>,
        BillingExceptionType extends BillingException>
    extends RequestCodeHolder
{
    BillingPurchaseRestorer.OnPurchaseRestorerListener<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType,
            BillingExceptionType> getPurchaseRestorerListener(int requestCode);
    void registerPurchaseRestorerListener(int requestCode, BillingPurchaseRestorer.OnPurchaseRestorerListener<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType,
            BillingExceptionType> purchaseFetchedListener);
}

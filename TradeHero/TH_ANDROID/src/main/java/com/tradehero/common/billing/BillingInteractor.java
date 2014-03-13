package com.tradehero.common.billing;

import android.app.AlertDialog;
import com.tradehero.common.billing.exception.BillingException;
import com.tradehero.th.billing.googleplay.THIABProductDetailDomainInformer;

/** Created with IntelliJ IDEA. User: xavier Date: 11/8/13 Time: 11:06 AM To change this template use File | Settings | File Templates. */
public interface BillingInteractor<
        ProductIdentifierType extends ProductIdentifier,
        ProductDetailType extends ProductDetail<ProductIdentifierType>,
        PurchaseOrderType extends PurchaseOrder<ProductIdentifierType>,
        OrderIdType extends OrderId,
        ProductPurchaseType extends ProductPurchase<
                ProductIdentifierType,
                OrderIdType>,
        BillingLogicHolderType extends BillingLogicHolder<
                        ProductIdentifierType,
                        ProductDetailType,
                        PurchaseOrderType,
                        OrderIdType,
                        ProductPurchaseType,
                        BillingExceptionType>,
        BillingRequestType extends BillingRequest<
                ProductIdentifierType,
                ProductDetailType,
                PurchaseOrderType,
                OrderIdType,
                ProductPurchaseType,
                BillingExceptionType>,
        BillingExceptionType extends BillingException>
{
    BillingLogicHolderType getBillingLogicHolder();

    Boolean isBillingAvailable();
    AlertDialog conditionalPopBillingNotAvailable();
    AlertDialog popBillingUnavailable();
    int run(BillingRequestType request);
}

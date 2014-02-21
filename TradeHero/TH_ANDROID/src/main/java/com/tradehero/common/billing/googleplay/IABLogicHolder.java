package com.tradehero.common.billing.googleplay;

import com.tradehero.common.billing.BillingInventoryFetcher;
import com.tradehero.common.billing.BillingLogicHolder;
import com.tradehero.common.billing.BillingPurchaser;
import com.tradehero.common.billing.ProductIdentifierFetcher;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.th.billing.googleplay.IABProductIdentifierFetcherHolder;

/** Created with IntelliJ IDEA. User: xavier Date: 11/8/13 Time: 11:06 AM To change this template use File | Settings | File Templates. */
public interface IABLogicHolder<
        IABSKUType extends IABSKU,
        OnProductIdentifierFetchedListenerType extends ProductIdentifierFetcher.OnProductIdentifierFetchedListener<IABSKUType, IABExceptionType>,
        IABProductDetailType extends IABProductDetail<IABSKUType>,
        InventoryFetchedListenerType extends BillingInventoryFetcher.OnInventoryFetchedListener<IABSKUType, IABProductDetailType, IABExceptionType>,
        IABPurchaseOrderType extends IABPurchaseOrder<IABSKUType>,
        IABOrderIdType extends IABOrderId,
        IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>,
        IABPurchaseFetchedListenerType extends IABPurchaseFetcher.OnPurchaseFetchedListener<
                IABSKUType,
                IABOrderIdType,
                IABPurchaseType>,
        IABPurchaseFinishedListenerType extends BillingPurchaser.OnPurchaseFinishedListener<
                IABSKUType,
                IABPurchaseOrderType,
                IABOrderIdType,
                IABPurchaseType,
                IABExceptionType>,
        IABConsumeFinishedListenerType extends IABPurchaseConsumer.OnIABConsumptionFinishedListener<
                IABSKUType,
                IABOrderIdType,
                IABPurchaseType,
                IABExceptionType>,
        IABExceptionType extends IABException>
    extends
        BillingLogicHolder<
                        IABSKUType,
                        OnProductIdentifierFetchedListenerType,
                        IABProductDetailType,
                        InventoryFetchedListenerType,
                        IABPurchaseOrderType,
                        IABOrderIdType,
                        IABPurchaseType,
                        IABPurchaseFinishedListenerType,
                        IABExceptionType>,
        IABProductIdentifierFetcherHolder< // This one is redundant but serves as a highlight to the reader
                        IABSKUType,
                        OnProductIdentifierFetchedListenerType,
                        IABExceptionType>,
        IABInventoryFetcherHolder< // This one is redundant but serves as a highlight to the reader
                        IABSKUType,
                        IABProductDetailType,
                        InventoryFetchedListenerType,
                        IABExceptionType>,
        IABPurchaserHolder< // This one is redundant but serves as a highlight to the reader
                        IABSKUType,
                        IABPurchaseOrderType,
                        IABOrderIdType,
                        IABPurchaseType,
                        IABPurchaseFinishedListenerType,
                        IABExceptionType>,
        IABPurchaseFetcherHolder<
                        IABSKUType,
                        IABOrderIdType,
                        IABPurchaseType,
                        IABPurchaseFetchedListenerType>,
        IABPurchaseConsumerHolder<
                        IABSKUType,
                        IABOrderIdType,
                        IABPurchaseType,
                        IABConsumeFinishedListenerType,
                        IABExceptionType>
{
}

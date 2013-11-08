package com.tradehero.common.billing.googleplay;

import com.tradehero.common.billing.BillingActor;
import com.tradehero.common.billing.ProductDetails;
import com.tradehero.common.billing.googleplay.exceptions.IABException;

/** Created with IntelliJ IDEA. User: xavier Date: 11/8/13 Time: 11:06 AM To change this template use File | Settings | File Templates. */
public interface IABActor<ProductDetailsType extends ProductDetails<IABSKU>,
                        IABExceptionType extends IABException,
                        IABPurchaseHandlerType extends IABPurchaseHandler<IABExceptionType>>
    extends BillingActor<IABSKU, ProductDetailsType, IABExceptionType, IABOrderId, IABPurchase, IABPurchaseHandlerType>
{
}

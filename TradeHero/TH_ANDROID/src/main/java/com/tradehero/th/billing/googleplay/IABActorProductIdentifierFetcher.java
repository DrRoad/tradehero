package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.ProductIdentifierFetcher;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.th.billing.ActorProductIdentifierFetcher;

/** Created with IntelliJ IDEA. User: xavier Date: 11/27/13 Time: 1:23 PM To change this template use File | Settings | File Templates. */
public interface IABActorProductIdentifierFetcher<
        IABSKUType extends IABSKU,
        OnSKUFetchedListenerType extends ProductIdentifierFetcher.OnProductIdentifierFetchedListener<IABSKUType, IABExceptionType>,
        IABExceptionType extends IABException>
    extends ActorProductIdentifierFetcher<IABSKUType, OnSKUFetchedListenerType, IABExceptionType>
{
}

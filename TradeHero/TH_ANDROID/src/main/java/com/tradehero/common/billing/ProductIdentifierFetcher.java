package com.tradehero.common.billing;

import com.tradehero.common.billing.exception.BillingException;
import java.util.List;
import java.util.Map;

/** Created with IntelliJ IDEA. User: xavier Date: 11/27/13 Time: 1:15 PM To change this template use File | Settings | File Templates. */
public interface ProductIdentifierFetcher<
        ProductIdentifierType extends ProductIdentifier,
        OnProductIdentifierFetchedListenerType extends ProductIdentifierFetcher.OnProductIdentifierFetchedListener<ProductIdentifierType, BillingExceptionType>,
        BillingExceptionType extends BillingException>
{
    int getRequestCode();
    OnProductIdentifierFetchedListenerType getProductIdentifierListener();
    void setProductIdentifierListener(OnProductIdentifierFetchedListenerType listener);
    void fetchProductIdentifiers(int requestCode);
    Map<String, List<ProductIdentifierType>> fetchProductIdentifiersSync();

    public static interface OnProductIdentifierFetchedListener<
            ProductIdentifierType,
            BillingExceptionType>
    {
        void onFetchedProductIdentifiers(int requestCode,
                Map<String, List<ProductIdentifierType>> availableProductIdentifiers);
        void onFetchProductIdentifiersFailed(int requestCode, BillingExceptionType exception);
    }
}

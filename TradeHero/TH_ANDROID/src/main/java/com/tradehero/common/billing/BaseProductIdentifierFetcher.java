package com.tradehero.common.billing;

import com.tradehero.common.billing.exception.BillingException;

import android.support.annotation.Nullable;

import java.util.Map;

abstract public class BaseProductIdentifierFetcher<
        ProductIdentifierListKeyType extends ProductIdentifierListKey,
        ProductIdentifierType extends ProductIdentifier,
        ProductIdentifierListType extends BaseProductIdentifierList<ProductIdentifierType>,
        BillingExceptionType extends BillingException>
        implements ProductIdentifierFetcher<
        ProductIdentifierListKeyType,
        ProductIdentifierType,
        ProductIdentifierListType,
        BillingExceptionType>
{
    protected int requestCode;
    @Nullable protected ProductIdentifierFetcher.OnProductIdentifierFetchedListener<ProductIdentifierListKeyType, ProductIdentifierType, ProductIdentifierListType, BillingExceptionType> productIdentifierFetchedListener;

    //<editor-fold desc="Constructors">
    public BaseProductIdentifierFetcher()
    {
        super();
    }
    //</editor-fold>

    @Override public void fetchProductIdentifiers(int requestCode)
    {
        this.requestCode = requestCode;
    }

    @Override public int getRequestCode()
    {
        return requestCode;
    }

    @Override @Nullable public ProductIdentifierFetcher.OnProductIdentifierFetchedListener<ProductIdentifierListKeyType, ProductIdentifierType, ProductIdentifierListType, BillingExceptionType> getProductIdentifierListener()
    {
        return productIdentifierFetchedListener;
    }

    @Override public void setProductIdentifierListener(
            @Nullable ProductIdentifierFetcher.OnProductIdentifierFetchedListener<ProductIdentifierListKeyType, ProductIdentifierType, ProductIdentifierListType, BillingExceptionType> listener)
    {
        this.productIdentifierFetchedListener = listener;
    }

    protected void notifyListenerFetched(Map<ProductIdentifierListKeyType, ProductIdentifierListType> productIdentifiers)
    {
        OnProductIdentifierFetchedListener<ProductIdentifierListKeyType, ProductIdentifierType, ProductIdentifierListType, BillingExceptionType> listenerCopy = getProductIdentifierListener();
        if (listenerCopy != null)
        {
            listenerCopy.onFetchedProductIdentifiers(requestCode, productIdentifiers);
        }
    }

    protected void notifyListenerFetchFailed(BillingExceptionType exception)
    {
        OnProductIdentifierFetchedListener<ProductIdentifierListKeyType, ProductIdentifierType, ProductIdentifierListType, BillingExceptionType> listenerCopy = getProductIdentifierListener();
        if (listenerCopy != null)
        {
            listenerCopy.onFetchProductIdentifiersFailed(requestCode, exception);
        }
    }
}

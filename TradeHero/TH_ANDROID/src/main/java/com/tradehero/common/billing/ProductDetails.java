package com.tradehero.common.billing;

/**
 * Created by julien on 4/11/13
 */
public interface ProductDetails<ProductIdentifierType extends ProductIdentifier>
{
    ProductIdentifierType getProductIdentifier();
}

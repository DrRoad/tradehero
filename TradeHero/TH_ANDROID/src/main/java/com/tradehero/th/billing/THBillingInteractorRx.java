package com.tradehero.th.billing;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.BaseProductIdentifierList;
import com.tradehero.common.billing.BillingInteractorRx;
import com.tradehero.common.billing.ProductIdentifier;
import com.tradehero.common.billing.ProductIdentifierListKey;
import com.tradehero.common.billing.inventory.ProductInventoryResult;
import com.tradehero.common.billing.purchase.PurchaseResult;
import com.tradehero.th.api.users.UserBaseKey;
import rx.Observable;

public interface THBillingInteractorRx<
        ProductIdentifierListKeyType extends ProductIdentifierListKey,
        ProductIdentifierType extends ProductIdentifier,
        ProductIdentifierListType extends BaseProductIdentifierList<ProductIdentifierType>,
        THProductDetailType extends THProductDetail<ProductIdentifierType>,
        THPurchaseOrderType extends THPurchaseOrder<ProductIdentifierType>,
        THOrderIdType extends THOrderId,
        THProductPurchaseType extends THProductPurchase<
                ProductIdentifierType,
                THOrderIdType>,
        THBillingLogicHolderType extends THBillingLogicHolderRx<
                ProductIdentifierListKeyType,
                ProductIdentifierType,
                ProductIdentifierListType,
                THProductDetailType,
                THPurchaseOrderType,
                THOrderIdType,
                THProductPurchaseType>>
        extends BillingInteractorRx<
        ProductIdentifierListKeyType,
        ProductIdentifierType,
        ProductIdentifierListType,
        THProductDetailType,
        THPurchaseOrderType,
        THOrderIdType,
        THProductPurchaseType,
        THBillingLogicHolderType>
{
    @NonNull Observable<PurchaseResult<
            ProductIdentifierType,
            THPurchaseOrderType,
            THOrderIdType,
            THProductPurchaseType>> purchaseAndClear(@NonNull ProductIdentifierDomain domain);

    @NonNull Observable<PurchaseResult<
            ProductIdentifierType,
            THPurchaseOrderType,
            THOrderIdType,
            THProductPurchaseType>> purchase(@NonNull ProductIdentifierDomain domain);

    @NonNull Observable<THPurchaseOrderType> createPurchaseOrder(
            @NonNull ProductInventoryResult<
                    ProductIdentifierType,
                    THProductDetailType> inventoryResult,
            @NonNull UserBaseKey heroId);

    @NonNull Observable<PurchaseResult<
        ProductIdentifierType,
        THPurchaseOrderType,
        THOrderIdType,
        THProductPurchaseType>> purchaseAndPremiumFollowAndClear(@NonNull UserBaseKey heroId);

    @NonNull Observable<PurchaseResult<
        ProductIdentifierType,
        THPurchaseOrderType,
        THOrderIdType,
        THProductPurchaseType>> purchaseAndPremiumFollow(@NonNull UserBaseKey heroId);
}

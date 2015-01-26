package com.tradehero.common.billing;

import android.content.Intent;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.identifier.ProductIdentifierListResult;
import com.tradehero.common.billing.inventory.ProductInventoryResult;
import com.tradehero.common.billing.purchase.PurchaseResult;
import com.tradehero.common.billing.purchasefetch.PurchaseFetchResult;
import com.tradehero.common.billing.restore.PurchaseRestoreResult;
import com.tradehero.common.billing.restore.PurchaseRestoreTotalResult;
import com.tradehero.common.billing.tester.BillingTestResult;
import java.util.List;
import rx.Observable;

abstract public class BaseBillingInteractorRx<
        ProductIdentifierListKeyType extends ProductIdentifierListKey,
        ProductIdentifierType extends ProductIdentifier,
        ProductIdentifierListType extends BaseProductIdentifierList<ProductIdentifierType>,
        ProductDetailType extends ProductDetail<ProductIdentifierType>,
        PurchaseOrderType extends PurchaseOrder<ProductIdentifierType>,
        OrderIdType extends OrderId,
        ProductPurchaseType extends ProductPurchase<
                ProductIdentifierType,
                OrderIdType>,
        BillingLogicHolderType extends BillingLogicHolderRx<
                ProductIdentifierListKeyType,
                ProductIdentifierType,
                ProductIdentifierListType,
                ProductDetailType,
                PurchaseOrderType,
                OrderIdType,
                ProductPurchaseType>>
        implements BillingInteractorRx<
        ProductIdentifierListKeyType,
        ProductIdentifierType,
        ProductIdentifierListType,
        ProductDetailType,
        PurchaseOrderType,
        OrderIdType,
        ProductPurchaseType,
        BillingLogicHolderType>
{
    @NonNull protected final BillingLogicHolderType billingLogicHolder;

    //<editor-fold desc="Constructors">
    public BaseBillingInteractorRx(@NonNull BillingLogicHolderType billingLogicHolder)
    {
        super();
        this.billingLogicHolder = billingLogicHolder;
    }
    //</editor-fold>

    //<editor-fold desc="Life Cycle">
    public void onDestroy()
    {
        billingLogicHolder.onDestroy();
    }
    //</editor-fold>

    //<editor-fold desc="Test Billing">
    @NonNull @Override public Observable<BillingTestResult> testAndClear()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.testAndClear(requestCode);
    }

    @NonNull @Override public Observable<BillingTestResult> test()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.test(requestCode);
    }
    //</editor-fold>

    //<editor-fold desc="Get Product Identifiers">
    @NonNull @Override public Observable<ProductIdentifierListResult<
            ProductIdentifierListKeyType,
            ProductIdentifierType,
            ProductIdentifierListType>> getIdsAndClear()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getIdsAndClear(requestCode);
    }

    @NonNull @Override public Observable<ProductIdentifierListResult<
            ProductIdentifierListKeyType,
            ProductIdentifierType,
            ProductIdentifierListType>> getIds()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getIds(requestCode);
    }
    //</editor-fold>

    //<editor-fold desc="Get Inventory">
    @NonNull @Override public Observable<ProductInventoryResult<
            ProductIdentifierType,
            ProductDetailType>> getInventoryAndClear(
            @NonNull List<ProductIdentifierType> productIdentifiers)
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getInventoryAndClear(requestCode, productIdentifiers);
    }

    @NonNull @Override public Observable<ProductInventoryResult<
            ProductIdentifierType,
            ProductDetailType>> getInventory(
            @NonNull List<ProductIdentifierType> productIdentifiers)
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getInventory(requestCode, productIdentifiers);
    }

    @NonNull @Override public Observable<ProductInventoryResult<
            ProductIdentifierType,
            ProductDetailType>> getInventoryAndClear()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getInventoryAndClear(requestCode);
    }

    @NonNull @Override public Observable<ProductInventoryResult<
            ProductIdentifierType,
            ProductDetailType>> getInventory()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getInventory(requestCode);
    }
    //</editor-fold>

    //<editor-fold desc="Get Purchases">
    @NonNull @Override public Observable<PurchaseFetchResult<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> getPurchasesAndClear()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getPurchasesAndClear(requestCode);
    }

    @NonNull @Override public Observable<PurchaseFetchResult<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> getPurchases()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.getPurchases(requestCode);
    }
    //</editor-fold>

    //<editor-fold desc="Purchase">
    @NonNull @Override public Observable<PurchaseResult<
            ProductIdentifierType,
            PurchaseOrderType,
            OrderIdType,
            ProductPurchaseType>> purchaseAndClear(
            @NonNull PurchaseOrderType purchaseOrder)
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.purchaseAndClear(requestCode, purchaseOrder);
    }

    @NonNull @Override public Observable<PurchaseResult<
            ProductIdentifierType,
            PurchaseOrderType,
            OrderIdType,
            ProductPurchaseType>> purchase(
            @NonNull PurchaseOrderType purchaseOrder)
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.purchase(requestCode, purchaseOrder);
    }
    //</editor-fold>

    //<editor-fold desc="Restore Purchases">
    @NonNull @Override public Observable<PurchaseRestoreResult<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> restorePurchaseAndClear(
            @NonNull ProductPurchaseType purchase)
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.restorePurchaseAndClear(requestCode, purchase);
    }

    @NonNull @Override public Observable<PurchaseRestoreResult<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> restorePurchase(
            @NonNull ProductPurchaseType purchase)
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.restorePurchase(requestCode, purchase);
    }

    @NonNull @Override public Observable<PurchaseRestoreTotalResult<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> restorePurchasesAndClear()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.restorePurchasesAndClear(requestCode);
    }

    @NonNull @Override public Observable<PurchaseRestoreTotalResult<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType>> restorePurchases()
    {
        int requestCode = billingLogicHolder.getUnusedRequestCode();
        return billingLogicHolder.restorePurchases(requestCode);
    }
    //</editor-fold>

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        billingLogicHolder.onActivityResult(requestCode, resultCode, data);
    }
}

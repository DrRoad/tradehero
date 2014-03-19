package com.tradehero.common.billing;

import com.tradehero.common.billing.exception.BillingException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xavier on 3/13/14.
 */
public class BillingRequest<
        ProductIdentifierType extends ProductIdentifier,
        ProductDetailType extends ProductDetail<ProductIdentifierType>,
        PurchaseOrderType extends PurchaseOrder<ProductIdentifierType>,
        OrderIdType extends OrderId,
        ProductPurchaseType extends ProductPurchase<ProductIdentifierType, OrderIdType>,
        BillingExceptionType extends BillingException>
{
    public static final String TAG = BillingRequest.class.getSimpleName();

    //<editor-fold desc="Listeners">
    private OnBillingAvailableListener<BillingExceptionType> billingAvailableListener;
    private BillingInventoryFetcher.OnInventoryFetchedListener<
            ProductIdentifierType,
            ProductDetailType,
            BillingExceptionType> inventoryFetchedListener;
    private BillingPurchaseFetcher.OnPurchaseFetchedListener<
            ProductIdentifierType,
            OrderIdType,
            ProductPurchaseType,
            BillingExceptionType> purchaseFetchedListener;
    private BillingPurchaser.OnPurchaseFinishedListener<
            ProductIdentifierType,
            PurchaseOrderType,
            OrderIdType,
            ProductPurchaseType,
            BillingExceptionType> purchaseFinishedListener;
    //</editor-fold>

    private Boolean billingAvailable;
    private List<ProductIdentifierType> productIdentifiersForInventory;
    private Boolean fetchPurchase;
    private PurchaseOrderType purchaseOrder;

    protected BillingRequest(
            OnBillingAvailableListener<BillingExceptionType> billingAvailableListener,
            BillingInventoryFetcher.OnInventoryFetchedListener<ProductIdentifierType, ProductDetailType, BillingExceptionType> inventoryFetchedListener,
            BillingPurchaseFetcher.OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> purchaseFetchedListener,
            BillingPurchaser.OnPurchaseFinishedListener<ProductIdentifierType, PurchaseOrderType, OrderIdType, ProductPurchaseType, BillingExceptionType> purchaseFinishedListener,
            Boolean billingAvailable,
            List<ProductIdentifierType> productIdentifiersForInventory,
            Boolean fetchPurchase,
            PurchaseOrderType purchaseOrder)
    {
        this.billingAvailableListener = billingAvailableListener;
        this.inventoryFetchedListener = inventoryFetchedListener;
        this.purchaseFetchedListener = purchaseFetchedListener;
        this.purchaseFinishedListener = purchaseFinishedListener;

        this.billingAvailable = billingAvailable;
        this.productIdentifiersForInventory = productIdentifiersForInventory;
        this.fetchPurchase = fetchPurchase;
        this.purchaseOrder = purchaseOrder;
    }

    //<editor-fold desc="Accessors">

    public OnBillingAvailableListener<BillingExceptionType> getBillingAvailableListener()
    {
        return billingAvailableListener;
    }

    public void setBillingAvailableListener(OnBillingAvailableListener<BillingExceptionType> billingAvailableListener)
    {
        this.billingAvailableListener = billingAvailableListener;
    }

    public BillingInventoryFetcher.OnInventoryFetchedListener<ProductIdentifierType, ProductDetailType, BillingExceptionType> getInventoryFetchedListener()
    {
        return inventoryFetchedListener;
    }

    public void setInventoryFetchedListener(
            BillingInventoryFetcher.OnInventoryFetchedListener<ProductIdentifierType, ProductDetailType, BillingExceptionType> inventoryFetchedListener)
    {
        this.inventoryFetchedListener = inventoryFetchedListener;
    }

    public BillingPurchaseFetcher.OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> getPurchaseFetchedListener()
    {
        return purchaseFetchedListener;
    }

    public void setPurchaseFetchedListener(
            BillingPurchaseFetcher.OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> purchaseFetchedListener)
    {
        this.purchaseFetchedListener = purchaseFetchedListener;
    }

    public BillingPurchaser.OnPurchaseFinishedListener<ProductIdentifierType, PurchaseOrderType, OrderIdType, ProductPurchaseType, BillingExceptionType> getPurchaseFinishedListener()
    {
        return purchaseFinishedListener;
    }

    public void setPurchaseFinishedListener(
            BillingPurchaser.OnPurchaseFinishedListener<ProductIdentifierType, PurchaseOrderType, OrderIdType, ProductPurchaseType, BillingExceptionType> purchaseFinishedListener)
    {
        this.purchaseFinishedListener = purchaseFinishedListener;
    }

    public PurchaseOrderType getPurchaseOrder()
    {
        return purchaseOrder;
    }

    public Boolean getFetchPurchase()
    {
        return fetchPurchase;
    }
    //</editor-fold>

    public static class Builder<
            ProductIdentifierType extends ProductIdentifier,
            ProductDetailType extends ProductDetail<ProductIdentifierType>,
            PurchaseOrderType extends PurchaseOrder<ProductIdentifierType>,
            OrderIdType extends OrderId,
            ProductPurchaseType extends ProductPurchase<ProductIdentifierType, OrderIdType>,
            BillingExceptionType extends BillingException>
    {
        //<editor-fold desc="Listeners">
        private OnBillingAvailableListener<BillingExceptionType> billingAvailableListener;
        private BillingInventoryFetcher.OnInventoryFetchedListener<
                ProductIdentifierType,
                ProductDetailType,
                BillingExceptionType> inventoryFetchedListener;
        private BillingPurchaseFetcher.OnPurchaseFetchedListener<
                ProductIdentifierType,
                OrderIdType,
                ProductPurchaseType,
                BillingExceptionType> purchaseFetchedListener;
        private BillingPurchaser.OnPurchaseFinishedListener<
                ProductIdentifierType,
                PurchaseOrderType,
                OrderIdType,
                ProductPurchaseType,
                BillingExceptionType> purchaseFinishedListener;
        //</editor-fold>

        private Boolean billingAvailable;
        private List<ProductIdentifierType> productIdentifiersForInventory;
        private Boolean fetchPurchase;
        private PurchaseOrderType purchaseOrder;

        public Builder()
        {
        }

        public BillingRequest<ProductIdentifierType, ProductDetailType, PurchaseOrderType, OrderIdType, ProductPurchaseType, BillingExceptionType> build()
        {
            if (!isValid())
            {
                throw new IllegalArgumentException("Invalid elements");
            }
            return new BillingRequest<>(
                    billingAvailableListener,
                    inventoryFetchedListener,
                    purchaseFetchedListener,
                    purchaseFinishedListener,
                    billingAvailable,
                    productIdentifiersForInventory,
                    fetchPurchase,
                    purchaseOrder);
        }

        /**
         * It is valid when 1 and only 1 value is not null
         * @return
         */
        public boolean isValid()
        {
            boolean hasOneAction = false;
            List<Object> tests = getTests();
            for (Object test : tests)
            {
                if (hasOneAction && test != null)
                {
                    return false;
                }
                hasOneAction |= test != null;
            }
            return hasOneAction;
        }

        protected List<Object> getTests()
        {
            return Arrays.asList(billingAvailable, productIdentifiersForInventory, fetchPurchase, purchaseOrder);
        }

        //<editor-fold desc="Accessors">
        public OnBillingAvailableListener<BillingExceptionType> getBillingAvailableListener()
        {
            return billingAvailableListener;
        }

        public void setBillingAvailableListener(OnBillingAvailableListener<BillingExceptionType> billingAvailableListener)
        {
            this.billingAvailableListener = billingAvailableListener;
        }

        public BillingInventoryFetcher.OnInventoryFetchedListener<ProductIdentifierType, ProductDetailType, BillingExceptionType> getInventoryFetchedListener()
        {
            return inventoryFetchedListener;
        }

        public void setInventoryFetchedListener(
                BillingInventoryFetcher.OnInventoryFetchedListener<ProductIdentifierType, ProductDetailType, BillingExceptionType> inventoryFetchedListener)
        {
            this.inventoryFetchedListener = inventoryFetchedListener;
        }

        public BillingPurchaseFetcher.OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> getPurchaseFetchedListener()
        {
            return purchaseFetchedListener;
        }

        public void setPurchaseFetchedListener(
                BillingPurchaseFetcher.OnPurchaseFetchedListener<ProductIdentifierType, OrderIdType, ProductPurchaseType, BillingExceptionType> purchaseFetchedListener)
        {
            this.purchaseFetchedListener = purchaseFetchedListener;
        }

        public BillingPurchaser.OnPurchaseFinishedListener<ProductIdentifierType, PurchaseOrderType, OrderIdType, ProductPurchaseType, BillingExceptionType> getPurchaseFinishedListener()
        {
            return purchaseFinishedListener;
        }

        public void setPurchaseFinishedListener(
                BillingPurchaser.OnPurchaseFinishedListener<ProductIdentifierType, PurchaseOrderType, OrderIdType, ProductPurchaseType, BillingExceptionType> purchaseFinishedListener)
        {
            this.purchaseFinishedListener = purchaseFinishedListener;
        }

        public Boolean getBillingAvailable()
        {
            return billingAvailable;
        }

        public void setBillingAvailable(Boolean billingAvailable)
        {
            this.billingAvailable = billingAvailable;
        }

        public List<ProductIdentifierType> getProductIdentifiersForInventory()
        {
            return productIdentifiersForInventory;
        }

        public void setProductIdentifiersForInventory(List<ProductIdentifierType> productIdentifiersForInventory)
        {
            this.productIdentifiersForInventory = productIdentifiersForInventory;
        }

        public Boolean getFetchPurchase()
        {
            return fetchPurchase;
        }

        public void setFetchPurchase(Boolean fetchPurchase)
        {
            this.fetchPurchase = fetchPurchase;
        }

        public PurchaseOrderType getPurchaseOrder()
        {
            return purchaseOrder;
        }

        public void setPurchaseOrder(PurchaseOrderType purchaseOrder)
        {
            this.purchaseOrder = purchaseOrder;
        }
        //</editor-fold>
    }
}

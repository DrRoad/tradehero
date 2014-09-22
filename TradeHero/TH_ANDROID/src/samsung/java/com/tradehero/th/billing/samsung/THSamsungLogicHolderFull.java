package com.tradehero.th.billing.samsung;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import com.tradehero.common.billing.samsung.BaseSamsungSKUList;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.SamsungSKUList;
import com.tradehero.common.billing.samsung.SamsungSKUListKey;
import com.tradehero.common.billing.samsung.exception.SamsungException;
import com.tradehero.common.utils.CollectionUtils;
import com.tradehero.th.R;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.THBaseBillingLogicHolder;
import com.tradehero.th.billing.THProductDetailDomainPredicate;
import com.tradehero.th.billing.samsung.persistence.THSamsungGroupItemCache;
import com.tradehero.th.billing.samsung.request.THSamsungRequestFull;
import com.tradehero.th.persistence.billing.samsung.SamsungSKUListCache;
import com.tradehero.th.persistence.billing.samsung.THSamsungProductDetailCache;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class THSamsungLogicHolderFull
    extends THBaseBillingLogicHolder<
        SamsungSKUListKey,
        SamsungSKU,
        SamsungSKUList,
        THSamsungProductDetail,
        THSamsungProductDetailTuner,
        THSamsungPurchaseOrder,
        THSamsungOrderId,
        THSamsungPurchase,
        THSamsungRequestFull,
        SamsungException>
    implements THSamsungLogicHolder
{
    @NotNull protected final THSamsungGroupItemCache groupItemCache;
    @NotNull protected final Handler uiHandler;

    //<editor-fold desc="Constructors">
    @Inject public THSamsungLogicHolderFull(
            @NotNull SamsungSKUListCache samsungSKUListCache,
            @NotNull THSamsungProductDetailCache thskuDetailCache,
            @NotNull THSamsungBillingAvailableTesterHolder thSamsungBillingAvailableTesterHolder,
            @NotNull THSamsungProductIdentifierFetcherHolder thSamsungProductIdentifierFetcherHolder,
            @NotNull THSamsungInventoryFetcherHolder thSamsungInventoryFetcherHolder,
            @NotNull THSamsungPurchaseFetcherHolder thSamsungPurchaseFetcherHolder,
            @NotNull THSamsungPurchaserHolder thSamsungPurchaserHolder,
            @NotNull THSamsungPurchaseReporterHolder thSamsungPurchaseReporterHolder,
            @NotNull THSamsungGroupItemCache groupItemCache)
    {
        super(
                samsungSKUListCache,
                thskuDetailCache,
                thSamsungBillingAvailableTesterHolder,
                thSamsungProductIdentifierFetcherHolder,
                thSamsungInventoryFetcherHolder,
                thSamsungPurchaseFetcherHolder,
                thSamsungPurchaserHolder,
                thSamsungPurchaseReporterHolder);
        this.groupItemCache = groupItemCache;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }
    //</editor-fold>

    @Override public String getBillingHolderName(Resources resources)
    {
        return resources.getString(R.string.th_samsung_logic_holder_name);
    }

    @Override public List<THSamsungProductDetail> getDetailsOfDomain(ProductIdentifierDomain domain)
    {
        List<THSamsungProductDetail> details = productDetailCache.get(getAllSkus());
        if (details == null)
        {
            return null;
        }
        return CollectionUtils.filter(
                details,
                new THProductDetailDomainPredicate<SamsungSKU, THSamsungProductDetail>(domain));
    }

    protected BaseSamsungSKUList<SamsungSKU> getAllSkus()
    {
        return productIdentifierCache.get(SamsungSKUListKey.getAllKey());
    }

    //<editor-fold desc="Run Logic">
    @Override protected boolean runInternal(int requestCode)
    {
        boolean launched = super.runInternal(requestCode);
        THSamsungRequestFull billingRequest = billingRequests.get(requestCode);
        if (!launched && billingRequest != null)
        {
            if (billingRequest.restorePurchase && billingRequest.fetchedPurchases != null)
            {
                boolean prepared = billingRequest.fetchedPurchases.size() > 0 && prepareToRestoreOnePurchase(requestCode, billingRequest);
                if (prepared)
                {
                    launched = runInternal(requestCode);
                }

                if (!launched)
                {
                    notifyPurchaseRestored(requestCode, billingRequest.restoredPurchases, billingRequest.restoreFailedPurchases, billingRequest.restoreFailedErrors);
                }
            }
        }
        return launched;
    }
    //</editor-fold>

    //<editor-fold desc="Launch Sequence Methods">
    /**
     * We need to post otherwise the iap helper may forget the listener if this method is launched right after another
     * @param requestCode
     */
    @Override public void launchBillingAvailableTestSequence(final int requestCode)
    {
        uiHandler.post(new Runnable()
        {
            public void run()
            {
                THSamsungLogicHolderFull.super.launchBillingAvailableTestSequence(requestCode);
            }
        });
    }

    /**
     * We need to post otherwise the iap helper may forget the listener if this method is launched right after another
     * @param requestCode
     */
    @Override public void launchProductIdentifierFetchSequence(final int requestCode)
    {
        uiHandler.post(new Runnable()
        {
            public void run()
            {
                THSamsungLogicHolderFull.super.launchProductIdentifierFetchSequence(requestCode);
            }
        });
    }

    /**
     * We need to post otherwise the iap helper may forget the listener if this method is launched right after another
     * @param requestCode
     * @param allIds
     */
    @Override public void launchInventoryFetchSequence(final int requestCode, final List<SamsungSKU> allIds)
    {
        List<SamsungSKU> groupValues = allIds == null ? groupItemCache.get(THSamsungConstants.getItemGroupId()) : null;
        boolean allIn = true;
        if (groupValues != null)
        {
            for (SamsungSKU id : groupValues)
            {
                allIn &= groupValues.contains(id);
            }
        }
        else
        {
            allIn = false;
        }

        Map<SamsungSKU, THSamsungProductDetail> details = productDetailCache.getMap(groupValues);
        if (groupValues != null && details != null)
        {
            for (SamsungSKU id : groupValues)
            {
                allIn &= details.containsKey(id) && details.get(id) != null;
            }
        }
        else
        {
            allIn = false;
        }

        if (allIn)
        {
            handleInventoryFetchedSuccess(requestCode, groupValues, details);
        }
        else
        {
            uiHandler.post(new Runnable()
            {
                public void run()
                {
                    THSamsungLogicHolderFull.super.launchInventoryFetchSequence(requestCode, allIds);
                }
            });
        }
    }

    /**
     * We need to post otherwise the iap helper may forget the listener if this method is launched right after another
     * @param requestCode
     * @param purchaseOrder
     */
    @Override public void launchPurchaseSequence(final int requestCode, final THSamsungPurchaseOrder purchaseOrder)
    {
        uiHandler.post(new Runnable()
        {
            public void run()
            {
                THSamsungLogicHolderFull.super.launchPurchaseSequence(requestCode, purchaseOrder);
            }
        });
    }

    /**
     * We need to post otherwise the iap helper may forget the listener if this method is launched right after another
     * @param requestCode
     */
    @Override public void launchFetchPurchaseSequence(final int requestCode)
    {
        uiHandler.post(new Runnable()
        {
            @Override public void run()
            {
                THSamsungLogicHolderFull.super.launchFetchPurchaseSequence(requestCode);
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Sequence Logic">
    @Override protected void prepareRequestForNextRunAfterPurchaseFetchedSuccess(int requestCode, List<THSamsungPurchase> purchases)
    {
        super.prepareRequestForNextRunAfterPurchaseFetchedSuccess(requestCode, purchases);
    }

    @Override protected void prepareRequestForNextRunAfterPurchaseFinished(int requestCode, THSamsungPurchaseOrder purchaseOrder, THSamsungPurchase purchase)
    {
        super.prepareRequestForNextRunAfterPurchaseFinished(requestCode, purchaseOrder, purchase);
        THSamsungRequestFull billingRequest = billingRequests.get(requestCode);
        if (billingRequest != null)
        {
            billingRequest.purchaseToReport = purchase;
        }
    }

    @Override protected void prepareRequestForNextRunAfterPurchaseReportedSuccess(int requestCode, THSamsungPurchase reportedPurchase, UserProfileDTO updatedUserPortfolio)
    {
        super.prepareRequestForNextRunAfterPurchaseReportedSuccess(requestCode, reportedPurchase,
                updatedUserPortfolio);
    }

    @Override protected void prepareRequestForNextRunAfterPurchaseReportedFailed(int requestCode, THSamsungPurchase reportedPurchase, SamsungException error)
    {
        super.prepareRequestForNextRunAfterPurchaseReportedFailed(requestCode, reportedPurchase, error);
        THSamsungRequestFull billingRequest = billingRequests.get(requestCode);
        if (billingRequest != null)
        {
            if (billingRequest.restorePurchase)
            {
                Timber.e(error, "Failed to report a purchase to be restored");
                billingRequest.restoreFailedPurchases.add(reportedPurchase);
                billingRequest.restoreFailedErrors.add(error);
                prepareToRestoreOnePurchase(requestCode, billingRequest);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Fetch Inventory">
    @Override protected void handleInventoryFetchedSuccess(int requestCode, List<SamsungSKU> productIdentifiers, Map<SamsungSKU, THSamsungProductDetail> inventory)
    {
        groupItemCache.add(productIdentifiers);
        if (inventory != null)
        {
            groupItemCache.add(inventory.keySet());
        }
        super.handleInventoryFetchedSuccess(requestCode, productIdentifiers, inventory);
    }
    //</editor-fold>

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Nothing to do
    }
}

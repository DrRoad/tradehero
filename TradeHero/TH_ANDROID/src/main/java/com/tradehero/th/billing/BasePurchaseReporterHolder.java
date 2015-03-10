package com.tradehero.th.billing;

import com.tradehero.common.billing.OrderId;
import com.tradehero.common.billing.ProductIdentifier;
import com.tradehero.common.billing.exception.BillingException;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.persistence.portfolio.PortfolioCache;
import com.tradehero.th.persistence.portfolio.PortfolioCompactCache;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

abstract public class BasePurchaseReporterHolder<
        ProductIdentifierType extends ProductIdentifier,
        OrderIdType extends OrderId,
        THProductPurchaseType extends THProductPurchase<ProductIdentifierType, OrderIdType>,
        PurchaseReporterType extends PurchaseReporter<
            ProductIdentifierType,
            OrderIdType,
            THProductPurchaseType,
            BillingExceptionType>,
        BillingExceptionType extends BillingException>
    implements PurchaseReporterHolder<
        ProductIdentifierType,
        OrderIdType,
        THProductPurchaseType,
        BillingExceptionType>
{
    protected Map<Integer /*requestCode*/, PurchaseReporterType> purchaseReporters;
    protected Map<Integer /*requestCode*/, PurchaseReporter.OnPurchaseReportedListener<
            ProductIdentifierType,
            OrderIdType,
            THProductPurchaseType,
            BillingExceptionType>> parentPurchaseReportedHandlers;

    public BasePurchaseReporterHolder()
    {
        super();

        purchaseReporters = new HashMap<>();
        parentPurchaseReportedHandlers = new HashMap<>();
    }

    @Override public boolean isUnusedRequestCode(int requestCode)
    {
        return !purchaseReporters.containsKey(requestCode) &&
                !parentPurchaseReportedHandlers.containsKey(requestCode);
    }

    @Override public void forgetRequestCode(int requestCode)
    {
        parentPurchaseReportedHandlers.remove(requestCode);
        PurchaseReporterType purchaseReporter = purchaseReporters.get(requestCode);
        if (purchaseReporter != null)
        {
            purchaseReporter.setPurchaseReporterListener(null);
        }
        purchaseReporters.remove(requestCode);
    }

    @Override public PurchaseReporter.OnPurchaseReportedListener<
            ProductIdentifierType,
            OrderIdType,
            THProductPurchaseType,
            BillingExceptionType> getPurchaseReportedListener(int requestCode)
    {
        return parentPurchaseReportedHandlers.get(requestCode);
    }

    @Override public void registerPurchaseReportedListener(int requestCode, PurchaseReporter.OnPurchaseReportedListener<
            ProductIdentifierType,
            OrderIdType,
            THProductPurchaseType,
            BillingExceptionType> purchaseReportedHandler)
    {
        parentPurchaseReportedHandlers.put(requestCode, purchaseReportedHandler);
    }

    @Override public void launchReportSequence(int requestCode, THProductPurchaseType purchase)
    {
        PurchaseReporter.OnPurchaseReportedListener<ProductIdentifierType, OrderIdType, THProductPurchaseType, BillingExceptionType> reportedListener = createPurchaseReportedListener();
        PurchaseReporterType purchaseReporter = createPurchaseReporter();
        purchaseReporter.setPurchaseReporterListener(reportedListener);
        purchaseReporters.put(requestCode, purchaseReporter);
        purchaseReporter.reportPurchase(requestCode, purchase);
    }

    protected PurchaseReporter.OnPurchaseReportedListener<ProductIdentifierType, OrderIdType, THProductPurchaseType, BillingExceptionType> createPurchaseReportedListener()
    {
        return new PurchaseReporter.OnPurchaseReportedListener<ProductIdentifierType, OrderIdType, THProductPurchaseType, BillingExceptionType>()
        {
            @Override public void onPurchaseReported(int requestCode, THProductPurchaseType reportedPurchase, UserProfileDTO updatedUserPortfolio)
            {
                handlePurchaseReported(requestCode, reportedPurchase, updatedUserPortfolio);
            }

            @Override public void onPurchaseReportFailed(int requestCode, THProductPurchaseType reportedPurchase, BillingExceptionType error)
            {
                handlePurchaseReportFailed(requestCode, reportedPurchase, error);
            }
        };
    }

    protected void handlePurchaseReported(int requestCode, THProductPurchaseType reportedPurchase, UserProfileDTO updatedUserPortfolio)
    {
        Timber.d("handlePurchaseReported Purchase info " + reportedPurchase);

        if (updatedUserPortfolio != null)
        {
            getUserProfileCache().put(updatedUserPortfolio.getBaseKey(), updatedUserPortfolio);
        }

        OwnedPortfolioId applicablePortfolioId = reportedPurchase.getApplicableOwnedPortfolioId();
        if (applicablePortfolioId != null)
        {
            getPortfolioCompactListCache().invalidate(applicablePortfolioId.getUserBaseKey());
            // TODO put back when #68094144 is fixed
            //getPortfolioCompactCache().invalidate(applicablePortfolioId.getPortfolioIdKey());
            getPortfolioCache().invalidate(applicablePortfolioId);
        }

        PurchaseReporter.OnPurchaseReportedListener<
                ProductIdentifierType,
                OrderIdType,
                THProductPurchaseType,
                BillingExceptionType> handler = getPurchaseReportedListener(requestCode);
        if (handler != null)
        {
            Timber.d("handlePurchaseReported passing on the purchase for requestCode %d", requestCode);
            handler.onPurchaseReported(requestCode, reportedPurchase, updatedUserPortfolio);
        }
        else
        {
            Timber.d("handlePurchaseReported No PurchaseReportedHandler for requestCode %d", requestCode);
        }
    }

    abstract protected UserProfileCache getUserProfileCache();
    abstract protected PortfolioCompactListCache getPortfolioCompactListCache();
    abstract protected PortfolioCompactCache getPortfolioCompactCache();
    abstract protected PortfolioCache getPortfolioCache();

    protected void handlePurchaseReportFailed(int requestCode, THProductPurchaseType reportedPurchase, BillingExceptionType error)
    {
        Timber.e(error, "handlePurchaseReportFailed There was an exception during the report");
        PurchaseReporter.OnPurchaseReportedListener<
                ProductIdentifierType,
                OrderIdType,
                THProductPurchaseType,
                BillingExceptionType> handler = getPurchaseReportedListener(requestCode);
        if (handler != null)
        {
            Timber.d("handlePurchaseReportFailed passing on the exception for requestCode %d", requestCode);
            handler.onPurchaseReportFailed(requestCode, reportedPurchase, error);
        }
        else
        {
            Timber.d("handlePurchaseReportFailed No THIABPurchaseHandler for requestCode %d", requestCode);
        }
    }

    @Override public void onDestroy()
    {
        for (PurchaseReporterType purchaseReporter: purchaseReporters.values())
        {
            if (purchaseReporter != null)
            {
                purchaseReporter.setPurchaseReporterListener(null);
            }
        }
        purchaseReporters.clear();
        parentPurchaseReportedHandlers.clear();
    }

    abstract protected PurchaseReporterType createPurchaseReporter();
}
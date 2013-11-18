package com.tradehero.th.api.purchase;

import com.tradehero.common.billing.googleplay.IABOrderId;
import com.tradehero.common.billing.googleplay.IABProductDetails;
import com.tradehero.common.billing.googleplay.IABPurchase;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exceptions.IABException;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import java.lang.ref.WeakReference;

/** Created with IntelliJ IDEA. User: xavier Date: 11/18/13 Time: 12:06 PM To change this template use File | Settings | File Templates. */
abstract public class BasePurchaseReporter<
        IABOrderIdType extends IABOrderId,
        IABSKUType extends IABSKU,
        IABSKUDetailsType extends IABProductDetails<IABSKUType>,
        IABPurchaseType extends IABPurchase<IABOrderIdType, IABSKUType>>
{
    public static final String TAG = BasePurchaseReporter.class.getSimpleName();

    protected IABPurchaseType purchase;
    protected IABSKUDetailsType skuDetails;
    private WeakReference<PurchaseReporterListener<IABOrderIdType, IABSKUType, IABPurchaseType>> listener = new WeakReference<>(null);

    abstract public void reportPurchase(final IABPurchaseType purchase, final IABSKUDetailsType skuDetails);
    abstract public void reportPurchase(final IABPurchaseType purchase, final IABSKUDetailsType skuDetails, UserBaseKey userBaseKey);
    abstract public void reportPurchase(final IABPurchaseType purchase, final IABSKUDetailsType skuDetails, int portfolioId);
    abstract public void reportPurchase(final IABPurchaseType purchase, final IABSKUDetailsType skuDetails, UserBaseKey userBaseKey, int portfolioId);

    public PurchaseReporterListener<IABOrderIdType, IABSKUType, IABPurchaseType> getListener()
    {
        return this.listener.get();
    }

    /**
     * The listener should be strongly referenced elsewhere
     * @param listener
     */
    public void setListener(final PurchaseReporterListener<IABOrderIdType, IABSKUType, IABPurchaseType> listener)
    {
        this.listener = new WeakReference<>(listener);
    }

    protected void notifyListenerSuccess(final UserProfileDTO updatedUserPortfolio)
    {
        PurchaseReporterListener<IABOrderIdType, IABSKUType, IABPurchaseType> listener1 = getListener();
        if (listener1 != null)
        {
            listener1.onPurchaseReported(this.purchase, updatedUserPortfolio);
        }
    }

    protected void notifyListenerReportFailed(final Throwable error)
    {
        PurchaseReporterListener<IABOrderIdType, IABSKUType, IABPurchaseType> listener1 = getListener();
        if (listener1 != null)
        {
            listener1.onPurchaseReportFailed(this.purchase, error);
        }
    }

    public static interface PurchaseReporterListener<
            IABOrderIdType extends IABOrderId,
            IABSKUType extends IABSKU,
            IABPurchaseType extends IABPurchase<IABOrderIdType, IABSKUType>>
    {
        void onPurchaseReported(IABPurchaseType reportedPurchase, UserProfileDTO updatedUserPortfolio);
        void onPurchaseReportFailed(IABPurchaseType reportedPurchase, Throwable error);
    }
}

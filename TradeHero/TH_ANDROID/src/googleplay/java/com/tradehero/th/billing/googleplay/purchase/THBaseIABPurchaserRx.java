package com.tradehero.th.billing.googleplay.purchase;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.googleplay.BillingServiceBinderObservable;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import com.tradehero.common.billing.googleplay.purchase.BaseIABPurchaserRx;
import com.tradehero.th.billing.googleplay.THIABOrderId;
import com.tradehero.th.billing.googleplay.THIABPurchase;
import com.tradehero.th.billing.googleplay.THIABPurchaseOrder;
import org.json.JSONException;

public class THBaseIABPurchaserRx
        extends BaseIABPurchaserRx<
        IABSKU,
        THIABPurchaseOrder,
        THIABOrderId,
        THIABPurchase>
        implements THIABPurchaserRx
{
    //<editor-fold desc="Constructors">
    public THBaseIABPurchaserRx(
            int requestCode,
            @NonNull THIABPurchaseOrder purchaseOrder,
            @NonNull Activity activity,
            @NonNull IABExceptionFactory iabExceptionFactory,
            @NonNull BillingServiceBinderObservable billingServiceBinderObservable)
    {
        super(requestCode,
                purchaseOrder,
                activity,
                iabExceptionFactory,
                billingServiceBinderObservable);
    }
    //</editor-fold>

    @NonNull @Override protected THIABPurchase createPurchase(
            @NonNull String purchaseData,
            @NonNull String dataSignature)
            throws JSONException
    {
        THIABPurchase purchase = super.createPurchase(purchaseData, dataSignature);
        if (purchaseOrder.getUserToFollow() != null)
        {
            purchase.setUserToFollow(purchaseOrder.getUserToFollow());
        }
        return purchase;
    }

    @Override @NonNull protected THIABPurchase createPurchase(
            @NonNull String itemType,
            @NonNull String purchaseData,
            @NonNull String dataSignature) throws JSONException
    {
        return new THIABPurchase(itemType, purchaseData, dataSignature);
    }
}

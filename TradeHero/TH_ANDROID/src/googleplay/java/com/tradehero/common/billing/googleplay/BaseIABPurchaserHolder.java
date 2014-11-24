package com.tradehero.common.billing.googleplay;

import android.content.Intent;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.BaseBillingPurchaserHolder;
import com.tradehero.common.billing.BillingPurchaser;
import com.tradehero.common.billing.googleplay.exception.IABException;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

abstract public class BaseIABPurchaserHolder<
        IABSKUType extends IABSKU,
        IABPurchaseOrderType extends IABPurchaseOrder<IABSKUType>,
        IABOrderIdType extends IABOrderId,
        IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>,
        IABPurchaserType extends IABPurchaser<
                        IABSKUType,
                        IABPurchaseOrderType,
                        IABOrderIdType,
                        IABPurchaseType,
                        IABExceptionType>,
        IABExceptionType extends IABException>
    extends BaseBillingPurchaserHolder<
        IABSKUType,
        IABPurchaseOrderType,
        IABOrderIdType,
        IABPurchaseType,
        IABExceptionType>
    implements IABPurchaserHolder<
        IABSKUType,
        IABPurchaseOrderType,
        IABOrderIdType,
        IABPurchaseType,
        IABExceptionType>
{
    @NonNull protected final Map<Integer /*requestCode*/, IABPurchaserType> iabPurchasers;

    //<editor-fold desc="Constructors">
    public BaseIABPurchaserHolder()
    {
        super();
        iabPurchasers = new HashMap<>();
    }
    //</editor-fold>

    @Override public boolean isUnusedRequestCode(int requestCode)
    {
        return super.isUnusedRequestCode(requestCode) &&
                !iabPurchasers.containsKey(requestCode);
    }

    @Override public void forgetRequestCode(int requestCode)
    {
        super.forgetRequestCode(requestCode);
        IABPurchaserType purchaser = iabPurchasers.get(requestCode);
        if (purchaser != null)
        {
            purchaser.setListener(null);
            purchaser.setPurchaseFinishedListener(null);
        }
        iabPurchasers.remove(requestCode);
    }

    @Override public void launchPurchaseSequence(int requestCode, IABPurchaseOrderType purchaseOrder)
    {
        BillingPurchaser.OnPurchaseFinishedListener<IABSKUType, IABPurchaseOrderType, IABOrderIdType, IABPurchaseType, IABExceptionType> purchaseListener =
                createPurchaseFinishedListener();
        IABPurchaserType iabPurchaser = createPurchaser();
        iabPurchaser.setPurchaseFinishedListener(purchaseListener);
        iabPurchasers.put(requestCode, iabPurchaser);
        iabPurchaser.purchase(requestCode, purchaseOrder);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Timber.d("onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode);
        IABPurchaser iabPurchaser = iabPurchasers.get(requestCode);
        if (iabPurchaser != null)
        {
            iabPurchaser.handleActivityResult(requestCode, resultCode, data);
        }
        else
        {
            Timber.w("onActivityResult no handler");
        }
    }

    @Override public void onDestroy()
    {
        for (IABPurchaserType iabPurchaser: iabPurchasers.values())
        {
            if (iabPurchaser != null)
            {
                iabPurchaser.onDestroy();
            }
        }
        iabPurchasers.clear();
        super.onDestroy();
    }

    abstract protected IABPurchaserType createPurchaser();
}

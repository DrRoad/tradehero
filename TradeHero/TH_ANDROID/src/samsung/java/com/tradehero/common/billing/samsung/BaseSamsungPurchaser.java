package com.tradehero.common.billing.samsung;

import android.content.Context;
import com.sec.android.iap.lib.helper.SamsungIapHelper;
import com.sec.android.iap.lib.vo.ErrorVo;
import com.sec.android.iap.lib.vo.PurchaseVo;
import com.tradehero.common.billing.samsung.exception.SamsungException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import timber.log.Timber;

abstract public class BaseSamsungPurchaser<
        SamsungSKUType extends SamsungSKU,
        SamsungPurchaseOrderType extends SamsungPurchaseOrder<SamsungSKUType>,
        SamsungOrderIdType extends SamsungOrderId,
        SamsungPurchaseType extends SamsungPurchase<SamsungSKUType, SamsungOrderIdType>,
        SamsungExceptionType extends SamsungException>
    extends BaseSamsungActor
    implements SamsungPurchaser<
        SamsungSKUType,
        SamsungPurchaseOrderType,
        SamsungOrderIdType,
        SamsungPurchaseType,
        SamsungExceptionType>
{
    protected SamsungPurchaseOrderType purchaseOrder;
    @Nullable private OnPurchaseFinishedListener<SamsungSKUType, SamsungPurchaseOrderType, SamsungOrderIdType, SamsungPurchaseType, SamsungExceptionType> purchaseFinishedListener;

    //<editor-fold desc="Constructors">
    public BaseSamsungPurchaser(@NonNull Context context, int mode)
    {
        super(context, mode);
    }
    //</editor-fold>

    @Override @Nullable public OnPurchaseFinishedListener<SamsungSKUType, SamsungPurchaseOrderType, SamsungOrderIdType, SamsungPurchaseType, SamsungExceptionType> getPurchaseFinishedListener()
    {
        return this.purchaseFinishedListener;
    }

    @Override public void setPurchaseFinishedListener(@Nullable OnPurchaseFinishedListener<SamsungSKUType, SamsungPurchaseOrderType, SamsungOrderIdType, SamsungPurchaseType, SamsungExceptionType> purchaseFinishedListener)
    {
        this.purchaseFinishedListener = purchaseFinishedListener;
    }

    @Override public void purchase(int requestCode, @NonNull SamsungPurchaseOrderType purchaseOrder)
    {
        setRequestCode(requestCode);
        this.purchaseOrder = purchaseOrder;
        SamsungSKUType sku = purchaseOrder.getProductIdentifier();
        mIapHelper.startPayment(sku.groupId, sku.itemId, true, this);
    }

    @Override public void onPayment(ErrorVo errorVo, PurchaseVo purchaseVo)
    {
        if (errorVo.getErrorCode() == SamsungIapHelper.IAP_ERROR_NONE)
        {
            handlePurchaseFinished(createSamsungPurchase(purchaseVo));
            notifyPurchaseFinished(createSamsungPurchase(purchaseVo));
        }
        else
        {
            notifyPurchaseFailed(createSamsungException(errorVo));
        }
    }

    abstract protected SamsungPurchaseType createSamsungPurchase(PurchaseVo purchaseVo);
    abstract protected SamsungExceptionType createSamsungException(ErrorVo errorVo);

    protected void handlePurchaseFinished(SamsungPurchaseType purchase)
    {
        // Nothing to do here
    }

    protected void notifyPurchaseFinished(SamsungPurchaseType purchase)
    {
        OnPurchaseFinishedListener<SamsungSKUType, SamsungPurchaseOrderType, SamsungOrderIdType, SamsungPurchaseType, SamsungExceptionType> listenerCopy = purchaseFinishedListener;
        if (listenerCopy != null)
        {
            listenerCopy.onPurchaseFinished(getRequestCode(), purchaseOrder, purchase);
        }
    }

    protected void notifyPurchaseFailed(SamsungExceptionType exception)
    {
        Timber.e(exception, "");
        OnPurchaseFinishedListener<SamsungSKUType, SamsungPurchaseOrderType, SamsungOrderIdType, SamsungPurchaseType, SamsungExceptionType> listenerCopy = purchaseFinishedListener;
        if (listenerCopy != null)
        {
            listenerCopy.onPurchaseFailed(getRequestCode(), purchaseOrder, exception);
        }
    }
}

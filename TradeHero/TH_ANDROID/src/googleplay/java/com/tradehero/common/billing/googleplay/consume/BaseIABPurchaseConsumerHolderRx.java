package com.tradehero.common.billing.googleplay.consume;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.BaseRequestCodeHolder;
import com.tradehero.common.billing.googleplay.IABOrderId;
import com.tradehero.common.billing.googleplay.IABPurchase;
import com.tradehero.common.billing.googleplay.IABSKU;
import rx.Observable;

abstract public class BaseIABPurchaseConsumerHolderRx<
        IABSKUType extends IABSKU,
        IABOrderIdType extends IABOrderId,
        IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>>
        extends BaseRequestCodeHolder<IABPurchaseConsumerRx<
        IABSKUType,
        IABOrderIdType,
        IABPurchaseType>>
        implements IABPurchaseConsumerHolderRx<
        IABSKUType,
        IABOrderIdType,
        IABPurchaseType>
{
    //<editor-fold desc="Constructors">
    public BaseIABPurchaseConsumerHolderRx()
    {
        super();
    }
    //</editor-fold>

    @NonNull @Override public Observable<PurchaseConsumeResult<IABSKUType, IABOrderIdType, IABPurchaseType>> get(
            int requestCode,
            @NonNull IABPurchaseType purchase)
    {
        IABPurchaseConsumerRx<
                IABSKUType,
                IABOrderIdType,
                IABPurchaseType> consumer = actors.get(requestCode);
        if (consumer == null)
        {
            consumer = createPurchaseConsumer(requestCode, purchase);
            actors.put(requestCode, consumer);
        }
        return consumer.get();
    }

    @NonNull abstract protected IABPurchaseConsumerRx<
            IABSKUType,
            IABOrderIdType,
            IABPurchaseType> createPurchaseConsumer(int requestCode, @NonNull IABPurchaseType purchase);

    @Override public void onDestroy()
    {
        for (IABPurchaseConsumerRx<
                IABSKUType,
                IABOrderIdType,
                IABPurchaseType> actor : actors.values())
        {
            actor.onDestroy();
        }
        super.onDestroy();
    }

    @Override public void forgetRequestCode(int requestCode)
    {
        IABPurchaseConsumerRx<
                IABSKUType,
                IABOrderIdType,
                IABPurchaseType> actor = actors.get(requestCode);
        if (actor != null)
        {
            actor.onDestroy();
        }
        super.forgetRequestCode(requestCode);
    }
}

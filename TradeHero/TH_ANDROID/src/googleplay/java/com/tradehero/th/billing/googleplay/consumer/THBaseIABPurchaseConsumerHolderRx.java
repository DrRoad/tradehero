package com.tradehero.th.billing.googleplay.consumer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.googleplay.BillingServiceBinderObservable;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.consume.BaseIABPurchaseConsumerHolderRx;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import com.tradehero.th.billing.googleplay.THIABOrderId;
import com.tradehero.th.billing.googleplay.THIABPurchase;
import javax.inject.Inject;

public class THBaseIABPurchaseConsumerHolderRx
        extends BaseIABPurchaseConsumerHolderRx<
        IABSKU,
        THIABOrderId,
        THIABPurchase>
        implements THIABPurchaseConsumerHolderRx
{
    @NonNull protected final Context context;
    @NonNull protected final IABExceptionFactory iabExceptionFactory;
    @NonNull protected final BillingServiceBinderObservable billingServiceBinderObservable;

    //<editor-fold desc="Constructors">
    @Inject public THBaseIABPurchaseConsumerHolderRx(
            @NonNull Context context,
            @NonNull IABExceptionFactory iabExceptionFactory,
            @NonNull BillingServiceBinderObservable billingServiceBinderObservable)
    {
        this.context = context;
        this.iabExceptionFactory = iabExceptionFactory;
        this.billingServiceBinderObservable = billingServiceBinderObservable;
    }
    //</editor-fold>

    @NonNull @Override protected THBaseIABPurchaseConsumerRx createPurchaseConsumer(
            int requestCode,
            @NonNull THIABPurchase purchase)
    {
        return new THBaseIABPurchaseConsumerRx(requestCode, purchase, context, iabExceptionFactory, billingServiceBinderObservable);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Nothing to do
    }
}

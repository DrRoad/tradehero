package com.tradehero.th.billing.googleplay;

import android.app.Activity;
import com.tradehero.common.billing.googleplay.BaseIABPurchaseConsumer;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import com.tradehero.common.persistence.billing.googleplay.IABPurchaseCache;
import com.tradehero.th.persistence.billing.googleplay.THIABPurchaseCache;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;
import org.jetbrains.annotations.NotNull;

public class THBaseIABPurchaseConsumer
        extends BaseIABPurchaseConsumer<
        IABSKU,
        THIABOrderId,
        THIABPurchase>
    implements THIABPurchaseConsumer
{
    @NotNull protected final THIABPurchaseCache thiabPurchaseCache;

    //<editor-fold desc="Constructors">
    @Inject public THBaseIABPurchaseConsumer(
            @NotNull Provider<Activity> activityProvider,
            @NotNull Lazy<IABExceptionFactory> iabExceptionFactory,
            @NotNull THIABPurchaseCache thiabPurchaseCache)
    {
        super(activityProvider, iabExceptionFactory);
        this.thiabPurchaseCache = thiabPurchaseCache;
    }
    //</editor-fold>

    @Override @NotNull protected IABPurchaseCache<IABSKU, THIABOrderId, THIABPurchase> getPurchaseCache()
    {
        return thiabPurchaseCache;
    }
}

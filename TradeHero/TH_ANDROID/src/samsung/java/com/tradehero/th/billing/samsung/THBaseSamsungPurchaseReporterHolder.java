package com.tradehero.th.billing.samsung;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.exception.SamsungException;
import com.tradehero.th.billing.THBasePurchaseReporterHolder;
import com.tradehero.th.persistence.portfolio.PortfolioCacheRx;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;

public class THBaseSamsungPurchaseReporterHolder
    extends THBasePurchaseReporterHolder<
                SamsungSKU,
                THSamsungOrderId,
                THSamsungPurchase,
                THSamsungPurchaseReporter,
                SamsungException>
    implements THSamsungPurchaseReporterHolder
{
    //<editor-fold desc="Constructors">
    @Inject public THBaseSamsungPurchaseReporterHolder(
            @NonNull Lazy<UserProfileCacheRx> userProfileCache,
            @NonNull Lazy<PortfolioCompactListCacheRx> portfolioCompactListCache,
            @NonNull Lazy<PortfolioCacheRx> portfolioCache,
            @NonNull Provider<THSamsungPurchaseReporter> thSamsungPurchaseReporterProvider)
    {
        super(userProfileCache, portfolioCompactListCache, portfolioCache, thSamsungPurchaseReporterProvider);
    }
    //</editor-fold>
}

package com.tradehero.th.billing.amazon;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.amazon.AmazonSKU;
import com.tradehero.common.billing.amazon.exception.AmazonException;
import com.tradehero.th.billing.THBasePurchaseReporterHolder;
import com.tradehero.th.persistence.portfolio.PortfolioCacheRx;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;

public class THBaseAmazonPurchaseReporterHolder
    extends THBasePurchaseReporterHolder<
                AmazonSKU,
                THAmazonOrderId,
                THAmazonPurchase,
                THAmazonPurchaseReporter,
                AmazonException>
    implements THAmazonPurchaseReporterHolder
{
    //<editor-fold desc="Constructors">
    @Inject public THBaseAmazonPurchaseReporterHolder(
            @NonNull Lazy<UserProfileCacheRx> userProfileCache,
            @NonNull Lazy<PortfolioCompactListCacheRx> portfolioCompactListCache,
            @NonNull Lazy<PortfolioCacheRx> portfolioCache,
            @NonNull Provider<THAmazonPurchaseReporter> thAmazonPurchaseReporterProvider)
    {
        super(userProfileCache, portfolioCompactListCache, portfolioCache, thAmazonPurchaseReporterProvider);
    }
    //</editor-fold>
}

package com.androidth.general.billing.googleplay.report;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.androidth.general.common.billing.googleplay.IABSKU;
import com.androidth.general.api.users.CurrentUserId;
import com.androidth.general.billing.googleplay.THIABOrderId;
import com.androidth.general.billing.googleplay.THIABProductDetail;
import com.androidth.general.billing.googleplay.THIABPurchase;
import com.androidth.general.billing.report.THBasePurchaseReporterHolderRx;
import com.androidth.general.billing.report.THPurchaseReporterRx;
import com.androidth.general.network.service.AlertPlanCheckServiceWrapper;
import com.androidth.general.network.service.AlertPlanServiceWrapper;
import com.androidth.general.network.service.PortfolioServiceWrapper;
import com.androidth.general.network.service.UserServiceWrapper;
import com.androidth.general.persistence.billing.googleplay.THIABProductDetailCacheRx;
import dagger.Lazy;
import javax.inject.Inject;

public class THBaseIABPurchaseReporterHolderRx
        extends THBasePurchaseReporterHolderRx<
        IABSKU,
        THIABProductDetail,
        THIABOrderId,
        THIABPurchase>
        implements THIABPurchaseReporterHolderRx
{
    @NonNull protected final CurrentUserId currentUserId;
    @NonNull protected final Lazy<AlertPlanServiceWrapper> alertPlanServiceWrapper;
    @NonNull protected final Lazy<AlertPlanCheckServiceWrapper> alertPlanCheckServiceWrapper;
    @NonNull protected final Lazy<UserServiceWrapper> userServiceWrapper;
    @NonNull protected final Lazy<PortfolioServiceWrapper> portfolioServiceWrapper;
    @NonNull protected final Lazy<THIABProductDetailCacheRx> skuDetailCache;

    //<editor-fold desc="Constructors">
    @Inject public THBaseIABPurchaseReporterHolderRx(
            @NonNull CurrentUserId currentUserId,
            @NonNull Lazy<AlertPlanServiceWrapper> alertPlanServiceWrapper,
            @NonNull Lazy<AlertPlanCheckServiceWrapper> alertPlanCheckServiceWrapper,
            @NonNull Lazy<UserServiceWrapper> userServiceWrapper,
            @NonNull Lazy<PortfolioServiceWrapper> portfolioServiceWrapper,
            @NonNull Lazy<THIABProductDetailCacheRx> skuDetailCache)
    {
        super();
        this.currentUserId = currentUserId;
        this.alertPlanServiceWrapper = alertPlanServiceWrapper;
        this.alertPlanCheckServiceWrapper = alertPlanCheckServiceWrapper;
        this.userServiceWrapper = userServiceWrapper;
        this.portfolioServiceWrapper = portfolioServiceWrapper;
        this.skuDetailCache = skuDetailCache;
    }
    //</editor-fold>

    @NonNull @Override protected THPurchaseReporterRx<IABSKU, THIABOrderId, THIABPurchase> createReporter(
            int requestCode,
            @NonNull THIABPurchase purchase,
            @NonNull THIABProductDetail productDetail)
    {
        return new THBaseIABPurchaseReporterRx(
                requestCode,
                purchase,
                productDetail,
                alertPlanServiceWrapper,
                alertPlanCheckServiceWrapper,
                userServiceWrapper,
                portfolioServiceWrapper);
    }

    @Override public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data)
    {
        // Nothing to do
    }
}
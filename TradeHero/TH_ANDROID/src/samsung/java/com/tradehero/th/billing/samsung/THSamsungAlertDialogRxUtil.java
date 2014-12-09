package com.tradehero.th.billing.samsung;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.ProductIdentifier;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.THBillingAlertDialogRxUtil;
import com.tradehero.th.billing.samsung.persistence.THSamsungPurchaseCacheRx;
import com.tradehero.th.fragments.billing.THSamsungSKUDetailAdapter;
import com.tradehero.th.fragments.billing.THSamsungStoreProductDetailView;
import com.tradehero.th.utils.ActivityUtil;
import java.util.HashMap;
import javax.inject.Inject;
import timber.log.Timber;

public class THSamsungAlertDialogRxUtil extends THBillingAlertDialogRxUtil<
        SamsungSKU,
        THSamsungProductDetail,
        THSamsungLogicHolderRx,
        THSamsungStoreProductDetailView,
        THSamsungSKUDetailAdapter,
        THSamsungOrderId,
        THSamsungPurchase>
{
    @NonNull protected final THSamsungPurchaseCacheRx thSamsungPurchaseCache;
    @NonNull protected final SamsungStoreUtils samsungStoreUtils;

    //<editor-fold desc="Constructors">
    @Inject public THSamsungAlertDialogRxUtil(
            @NonNull Analytics analytics,
            @NonNull ActivityUtil activityUtil,
            @NonNull THSamsungPurchaseCacheRx thSamsungPurchaseCache,
            @NonNull SamsungStoreUtils samsungStoreUtils)
    {
        super(analytics, activityUtil);
        this.thSamsungPurchaseCache = thSamsungPurchaseCache;
        this.samsungStoreUtils = samsungStoreUtils;
    }
    //</editor-fold>

    //<editor-fold desc="SKU related">
    @Override @NonNull protected THSamsungSKUDetailAdapter createProductDetailAdapter(
            @NonNull Activity activity,
            @NonNull ProductIdentifierDomain skuDomain)
    {
        return new THSamsungSKUDetailAdapter(activity, skuDomain);
    }

    @Override @NonNull public HashMap<ProductIdentifier, Boolean> getEnabledItems()
    {
        HashMap<ProductIdentifier, Boolean> enabledItems = new HashMap<>();
        for (THSamsungPurchase value : thSamsungPurchaseCache.getValues())
        {
            Timber.d("Disabling %s", value);
            enabledItems.put(value.getProductIdentifier(), false);
        }
        return enabledItems;
    }
    //</editor-fold>
}

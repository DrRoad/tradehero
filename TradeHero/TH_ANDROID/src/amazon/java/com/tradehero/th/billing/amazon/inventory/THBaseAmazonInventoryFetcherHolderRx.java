package com.tradehero.th.billing.amazon.inventory;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.amazon.AmazonSKU;
import com.tradehero.common.billing.amazon.inventory.BaseAmazonInventoryFetcherHolderRx;
import com.tradehero.common.billing.amazon.service.AmazonPurchasingService;
import com.tradehero.th.billing.amazon.THAmazonProductDetail;
import java.util.List;
import javax.inject.Inject;

public class THBaseAmazonInventoryFetcherHolderRx
        extends BaseAmazonInventoryFetcherHolderRx<
        AmazonSKU,
        THAmazonProductDetail>
        implements THAmazonInventoryFetcherHolderRx
{
    @NonNull protected final AmazonPurchasingService amazonPurchasingService;

    //<editor-fold desc="Constructors">
    @Inject public THBaseAmazonInventoryFetcherHolderRx(
            @NonNull AmazonPurchasingService amazonPurchasingService)
    {
        super();
        this.amazonPurchasingService = amazonPurchasingService;
    }
    //</editor-fold>

    @NonNull @Override protected THBaseAmazonInventoryFetcherRx createFetcher(int requestCode,
            @NonNull List<AmazonSKU> productIdentifiers)
    {
        return new THBaseAmazonInventoryFetcherRx(requestCode, productIdentifiers, amazonPurchasingService);
    }
}

package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.googleplay.BaseIABInventoryFetcherHolder;
import com.tradehero.common.billing.googleplay.IABSKU;

/**
 * Created by xavier on 2/24/14.
 */
public class THBaseIABInventoryFetcherHolder
    extends BaseIABInventoryFetcherHolder<
        IABSKU,
        THIABProductDetail,
        THIABBillingInventoryFetcher>
    implements THIABInventoryFetcherHolder
{
    public THBaseIABInventoryFetcherHolder()
    {
        super();
    }

    @Override protected THIABBillingInventoryFetcher createInventoryFetcher()
    {
        return new THIABBillingInventoryFetcher();
    }
}

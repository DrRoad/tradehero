package com.tradehero.th.billing.amazon.tester;

import com.tradehero.common.billing.amazon.tester.BaseAmazonBillingAvailableTesterRx;

public class THBaseAmazonBillingAvailableTesterRx
    extends BaseAmazonBillingAvailableTesterRx
    implements THAmazonBillingAvailableTesterRx
{
    //<editor-fold desc="Constructors">
    public THBaseAmazonBillingAvailableTesterRx(int request)
    {
        super(request);
    }
    //</editor-fold>

    @Override public void onDestroy()
    {
    }
}

package com.tradehero.common.billing.googleplay.tester;

import android.support.annotation.NonNull;
import com.tradehero.common.billing.tester.BaseBillingAvailableTesterHolderRx;

abstract public class BaseIABBillingAvailableTesterHolderRx
        extends BaseBillingAvailableTesterHolderRx
{
    //<editor-fold desc="Constructors">
    public BaseIABBillingAvailableTesterHolderRx()
    {
        super();
    }
    //</editor-fold>

    @NonNull @Override abstract protected IABBillingAvailableTesterRx createTester(int requestCode);
}

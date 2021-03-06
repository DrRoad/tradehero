package com.tradehero.common.billing.samsung;

import android.content.Context;
import com.sec.android.iap.lib.helper.SamsungIapHelper;
import com.tradehero.common.billing.RequestCodeActor;
import android.support.annotation.NonNull;

abstract public class BaseSamsungActor
    implements RequestCodeActor
{
    private int activityRequestCode;
    @NonNull protected SamsungIapHelper mIapHelper;
    protected final int mode;

    //<editor-fold desc="Constructors">
    public BaseSamsungActor(@NonNull Context context, int mode)
    {
        super();
        mIapHelper = SamsungIapHelper.getInstance(context, mode);
        this.mode = mode;
    }
    //</editor-fold>

    @Override public int getRequestCode()
    {
        return activityRequestCode;
    }

    protected void setRequestCode(int requestCode)
    {
        this.activityRequestCode = requestCode;
    }
}

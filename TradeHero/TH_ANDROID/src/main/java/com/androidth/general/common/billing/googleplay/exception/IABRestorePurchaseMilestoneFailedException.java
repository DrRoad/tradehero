package com.androidth.general.common.billing.googleplay.exception;

import com.androidth.general.common.billing.googleplay.IABResult;

public class IABRestorePurchaseMilestoneFailedException extends IABException
{
    //<editor-fold desc="Constructors">
    public IABRestorePurchaseMilestoneFailedException(IABResult r)
    {
        super(r);
    }

    public IABRestorePurchaseMilestoneFailedException(int response, String message)
    {
        super(response, message);
    }

    public IABRestorePurchaseMilestoneFailedException(IABResult r, Exception cause)
    {
        super(r, cause);
    }

    public IABRestorePurchaseMilestoneFailedException(int response, String message, Exception cause)
    {
        super(response, message, cause);
    }

    public IABRestorePurchaseMilestoneFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IABRestorePurchaseMilestoneFailedException(Throwable cause)
    {
        super(cause);
    }
    //</editor-fold>
}

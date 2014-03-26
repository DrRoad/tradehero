package com.tradehero.common.billing.samsung.exception;

import com.sec.android.iap.lib.helper.SamsungIapHelper;

/** Created with IntelliJ IDEA. User: xavier Date: 11/5/13 Time: 4:15 PM To change this template use File | Settings | File Templates. */
public class SamsungConfirmInboxException extends SamsungOneCodeException
{
    public static final int VALID_ERROR_CODE = SamsungIapHelper.IAP_ERROR_CONFIRM_INBOX;

    //<editor-fold desc="Constructors">
    public SamsungConfirmInboxException()
    {
        super(VALID_ERROR_CODE);
    }

    public SamsungConfirmInboxException(String message)
    {
        super(VALID_ERROR_CODE, message);
    }

    public SamsungConfirmInboxException(String message, Exception cause)
    {
        super(VALID_ERROR_CODE, message, cause);
    }

    public SamsungConfirmInboxException(Exception cause)
    {
        super(VALID_ERROR_CODE, cause);
    }
    //</editor-fold>

    @Override protected int getOnlyValidErrorCode()
    {
        return VALID_ERROR_CODE;
    }
}

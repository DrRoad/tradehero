package com.androidth.general.common.billing.googleplay.exception;

import com.androidth.general.common.billing.googleplay.IABConstants;
import com.androidth.general.common.billing.googleplay.IABResult;

public class IABRemoteException extends IABOneResponseValueException
{
    public static final int VALID_RESPONSE = IABConstants.IABHELPER_REMOTE_EXCEPTION;

    public IABRemoteException(IABResult r)
    {
        super(r);
    }

    public IABRemoteException(IABResult r, Exception cause)
    {
        super(r, cause);
    }

    public IABRemoteException(String message)
    {
        super(VALID_RESPONSE, message);
    }

    public IABRemoteException(String message, Exception cause)
    {
        super(VALID_RESPONSE, message, cause);
    }

    @Override protected int getOnlyValidResponse()
    {
        return VALID_RESPONSE;
    }
}

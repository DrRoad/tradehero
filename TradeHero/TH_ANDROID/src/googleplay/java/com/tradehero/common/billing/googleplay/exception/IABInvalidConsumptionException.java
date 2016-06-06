package com.androidth.general.common.billing.googleplay.exception;

import com.androidth.general.common.billing.googleplay.IABConstants;
import com.androidth.general.common.billing.googleplay.IABResult;

public class IABInvalidConsumptionException extends IABOneResponseValueException
{
    public static final int VALID_RESPONSE = IABConstants.IABHELPER_INVALID_CONSUMPTION;

    public IABInvalidConsumptionException(IABResult r)
    {
        super(r);
    }

    public IABInvalidConsumptionException(IABResult r, Exception cause)
    {
        super(r, cause);
    }

    public IABInvalidConsumptionException(String message)
    {
        super(VALID_RESPONSE, message);
    }

    public IABInvalidConsumptionException(String message, Exception cause)
    {
        super(VALID_RESPONSE, message, cause);
    }

    @Override protected int getOnlyValidResponse()
    {
        return VALID_RESPONSE;
    }
}

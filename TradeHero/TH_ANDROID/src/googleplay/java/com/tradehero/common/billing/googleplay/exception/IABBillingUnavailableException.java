package com.tradehero.common.billing.googleplay.exception;

import com.tradehero.common.billing.googleplay.IABConstants;
import com.tradehero.common.billing.googleplay.IABResult;

public class IABBillingUnavailableException extends IABOneResponseValueException
{
    public static final int VALID_RESPONSE = IABConstants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE;

    public IABBillingUnavailableException(IABResult r)
    {
        super(r);
    }

    public IABBillingUnavailableException(IABResult r, Exception cause)
    {
        super(r, cause);
    }

    public IABBillingUnavailableException(String message)
    {
        super(VALID_RESPONSE, message);
    }

    public IABBillingUnavailableException(String message, Exception cause)
    {
        super(VALID_RESPONSE, message, cause);
    }

    @Override protected int getOnlyValidResponse()
    {
        return VALID_RESPONSE;
    }
}

package com.androidth.general.common.billing.googleplay.exception;

import com.androidth.general.common.billing.googleplay.IABConstants;
import com.androidth.general.common.billing.googleplay.IABResult;

public class IABItemUnavailableException extends IABOneResponseValueException
{
    public static final int VALID_RESPONSE = IABConstants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE;

    //<editor-fold desc="Constructors">
    public IABItemUnavailableException(IABResult r)
    {
        super(r);
    }

    public IABItemUnavailableException(IABResult r, Exception cause)
    {
        super(r, cause);
    }

    public IABItemUnavailableException(String message)
    {
        super(VALID_RESPONSE, message);
    }

    public IABItemUnavailableException(String message, Exception cause)
    {
        super(VALID_RESPONSE, message, cause);
    }
    //</editor-fold>

    @Override protected int getOnlyValidResponse()
    {
        return VALID_RESPONSE;
    }
}

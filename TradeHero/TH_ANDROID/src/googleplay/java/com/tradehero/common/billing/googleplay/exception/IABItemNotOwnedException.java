package com.androidth.general.common.billing.googleplay.exception;

import com.androidth.general.common.billing.googleplay.IABConstants;
import com.androidth.general.common.billing.googleplay.IABResult;

public class IABItemNotOwnedException extends IABOneResponseValueException
{
    public static final int VALID_RESPONSE = IABConstants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;

    //<editor-fold desc="Constructors">
    public IABItemNotOwnedException(IABResult r)
    {
        super(r);
    }

    public IABItemNotOwnedException(IABResult r, Exception cause)
    {
        super(r, cause);
    }

    public IABItemNotOwnedException(String message)
    {
        super(VALID_RESPONSE, message);
    }

    public IABItemNotOwnedException(String message, Exception cause)
    {
        super(VALID_RESPONSE, message, cause);
    }
    //</editor-fold>

    @Override protected int getOnlyValidResponse()
    {
        return VALID_RESPONSE;
    }
}

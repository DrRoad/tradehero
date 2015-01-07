package com.tradehero.th.api.security;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tradehero.th.R;
import com.tradehero.th.api.security.compact.FxSecurityCompactDTO;
import com.tradehero.th.api.security.key.FxPairSecurityId;
import com.tradehero.th.models.number.THSignedNumber;
import javax.inject.Inject;

public class SecurityCompactDTOUtil
{
    public static final int DEFAULT_RELEVANT_DIGITS = 20;

    //<editor-fold desc="Constructors">
    @Inject public SecurityCompactDTOUtil()
    {
        super();
    }
    //</editor-fold>

    public static int getExpectedPrecision(@NonNull SecurityCompactDTO securityCompactDTO)
    {
        return getExpectedPrecision(securityCompactDTO.askPrice, securityCompactDTO.bidPrice);
    }

    public static int getExpectedPrecision(double ask, double bid)
    {
        String askPrice = THSignedNumber.builder(ask)
                .relevantDigitCount(DEFAULT_RELEVANT_DIGITS)
                .build().toString();
        String bidPrice = THSignedNumber.builder(bid)
                .relevantDigitCount(DEFAULT_RELEVANT_DIGITS)
                .build().toString();
        int askDecimalPlace = askPrice.indexOf('.');
        int bidDecimalPlace = bidPrice.indexOf('.');

        if (askDecimalPlace >= 0 && bidDecimalPlace >= 0)
        {
            int askDecimalCount = askPrice.length() - askDecimalPlace - 1;
            int bidDecimalCount = bidPrice.length() - bidDecimalPlace - 1;

            return Math.max(askDecimalCount, bidDecimalCount);
        }
        else if (askDecimalPlace >= 0)
        {
            return askDecimalPlace;
        }
        else if (bidDecimalPlace >= 0)
        {
            return bidDecimalPlace;
        }
        return 0;
    }

    @NonNull public String getShortSymbol(
            @NonNull Context context,
            @NonNull SecurityCompactDTO securityCompactDTO)
    {
        if (securityCompactDTO instanceof FxSecurityCompactDTO)
        {
            FxPairSecurityId fxPairSecurityId = ((FxSecurityCompactDTO) securityCompactDTO).getFxPair();
            return String.format("%s/%s", fxPairSecurityId.left, fxPairSecurityId.right);
        }

        SecurityId securityId = securityCompactDTO.getSecurityId();
        return context.getString(
                R.string.trade_list_title_with_security,
                securityId.getExchange(),
                securityId.getSecuritySymbol());
    }
}

package com.tradehero.th.utils;

import android.content.Context;
import android.content.res.Resources;
import com.tradehero.th.R;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class NumberDisplayUtils
{
    @NotNull private final Context context;
    protected final int[] SUFFIX_IDS =
    {
        R.string.number_presentation_unit_suffix,
        R.string.number_presentation_thousand_suffix,
        R.string.number_presentation_million_suffix,
        R.string.number_presentation_billion_suffix,
        R.string.number_presentation_trillion_suffix
    };

    // These suffixes are used in tests...
    protected final String[] FALLBACK_SUFFIXES = {"", "k", "M", "B", "Tr"};

    //<editor-fold desc="Constructors">
    @Inject public NumberDisplayUtils(@NotNull Context context)
    {
        this.context = context;
    }
    //</editor-fold>

    public String formatWithRelevantDigits(double number, int relevantDigits)
    {
        return formatWithRelevantDigits(number, relevantDigits, null);
    }

    public String formatWithRelevantDigits(double number, int relevantDigits, String prefix)
    {
        double absVal = Math.abs(number);
        if (absVal > 999999999999999d)
        {
            throw new IllegalArgumentException("We do not accept number larger than 999 trillions");
        }

        if (absVal == 0)
        {
            return "0";
        }

        // Pick suffix
        int tensCategory = (int) Math.floor(Math.log10(absVal));
        int redressDigits = 0;
        if (relevantDigits > 3 && tensCategory >= 3)
        {
            // To change something like 2.592k into 2,592 or something like 2.592M into 2,592k
            redressDigits = relevantDigits - (relevantDigits % 3);
        }
        int suffixIndex = Math.max(0, (int) Math.floor(((float) (tensCategory - redressDigits)) / 3));

        // Get number reduced by suffix
        double reducedNumber = number / Math.pow(10, suffixIndex * 3);

        int desiredDecimal = 2;

        if (absVal >= 1)
        {
            // Number of desired decimals after the decimal separator
            int reducedDigitCount = 1 + tensCategory - suffixIndex * 3;
            desiredDecimal = Math.max(0, relevantDigits - reducedDigitCount);
        }

        String suffix;
        try
        {
            suffix = context.getResources().getString(SUFFIX_IDS[suffixIndex]);
        }
        catch (Resources.NotFoundException e)
        {
            suffix = FALLBACK_SUFFIXES[suffixIndex];
        }

        if (prefix != null)
        {
            return String.format("%s%,." + desiredDecimal + "f%s", prefix, reducedNumber, suffix);
        }
        else
        {
            return String.format("%,." + desiredDecimal + "f%s", reducedNumber, suffix);
        }
    }
}

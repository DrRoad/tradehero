package com.tradehero.th.api.kyc;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tradehero.th.R;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PercentNetWorthForInvestmentRange
{
    LESSTHAN25P(R.string.percent_net_worth_invest_less_than_25_p, 1),
    FROM25PTO50P(R.string.percent_net_worth_invest_from_25_p_to_50_p, 2),
    FROM51PTO75P(R.string.percent_net_worth_invest_from_51_p_to_75_p, 3),
    MORETHAN75P(R.string.percent_net_worth_invest_from_more_than_75p, 4),;

    private static final Map<Integer, PercentNetWorthForInvestmentRange> filedPercentNetWorthRanges;

    @StringRes public final int dropDownText;
    private final int fromServer;

    PercentNetWorthForInvestmentRange(@StringRes int dropDownText, int fromServer)
    {
        this.dropDownText = dropDownText;
        this.fromServer = fromServer;
    }

    @SuppressWarnings("unused")
    @JsonCreator @NonNull static PercentNetWorthForInvestmentRange getPercentNetWorthRange(int fromServer)
    {
        PercentNetWorthForInvestmentRange candidate = filedPercentNetWorthRanges.get(fromServer);
        if (candidate == null)
        {
            throw new IllegalArgumentException(fromServer + " does not match any PercentNetWorthForInvestmentRange");
        }
        return candidate;
    }

    static
    {
        Map<Integer, PercentNetWorthForInvestmentRange> map = new HashMap<>();
        for (PercentNetWorthForInvestmentRange candidate : values())
        {
            if (map.get(candidate.fromServer) != null)
            {
                throw new IllegalArgumentException("You are not allowed to add '" + candidate.fromServer + "' a second time");
            }
            map.put(candidate.fromServer, candidate);
        }
        filedPercentNetWorthRanges = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unused")
    @JsonValue int getFromServerCode()
    {
        return fromServer;
    }
}

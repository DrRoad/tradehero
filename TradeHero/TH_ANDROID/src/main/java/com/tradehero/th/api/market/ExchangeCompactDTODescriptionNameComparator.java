package com.tradehero.th.api.market;

import java.util.Comparator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ExchangeCompactDTODescriptionNameComparator<ExchangeCompactDTOType extends ExchangeCompactDTO>
        implements Comparator<ExchangeCompactDTOType>
{
    @Override public int compare(@Nullable ExchangeCompactDTOType lhs, @Nullable ExchangeCompactDTOType rhs)
    {
        if (lhs == null)
        {
            return rhs == null ? 0 : 1;
        }

        if (rhs == null)
        {
            return -1;
        }

        int descCompare = compareDesc(lhs, rhs);
        if (descCompare != 0)
        {
            return descCompare;
        }

        return compareName(lhs, rhs);
    }

    protected int compareDesc(@NonNull ExchangeCompactDTOType lhs, @NonNull ExchangeCompactDTOType rhs)
    {
        if (lhs.desc == null)
        {
            return rhs.desc == null ? 0 : 1;
        }
        if (rhs.desc == null)
        {
            return -1;
        }
        return lhs.desc.compareTo(rhs.desc);
    }

    protected int compareName(@NonNull ExchangeCompactDTOType lhs, @NonNull ExchangeCompactDTOType rhs)
    {
        return lhs.name.compareTo(rhs.name);
    }
}

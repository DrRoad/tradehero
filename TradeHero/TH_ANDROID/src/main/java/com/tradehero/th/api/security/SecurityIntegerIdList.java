package com.tradehero.th.api.security;

import com.tradehero.common.persistence.DTOKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class SecurityIntegerIdList extends ArrayList<SecurityIntegerId>
    implements DTOKey
{
    //<editor-fold desc="Constructors">
    public SecurityIntegerIdList()
    {
        super();
    }

    public SecurityIntegerIdList(@NotNull Collection<? extends Integer> c, Integer type)
    {
        for (@NotNull Integer id: c)
        {
            add(new SecurityIntegerId(id));
        }
    }
    //</editor-fold>

    public String getCommaSeparated()
    {
        char glue = ',';

        int length = size();
        if (length == 0)
        {
            return null;
        }

        StringBuilder out = new StringBuilder();
        out.append(get(0).key);
        for (int x = 1; x < length; ++x)
        {
            out.append(glue).append(get(x).key);
        }
        return out.toString();
    }
}

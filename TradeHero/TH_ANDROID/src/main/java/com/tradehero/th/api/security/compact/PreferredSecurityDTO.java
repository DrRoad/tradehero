package com.tradehero.th.api.security.compact;

import android.support.annotation.NonNull;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityCompactDTO;

public class PreferredSecurityDTO extends SecurityCompactDTO
{
    public static final String DTO_DESERIALISING_TYPE = "10";

    @NonNull @Override public Integer getSecurityTypeStringResourceId()
    {
        return R.string.security_type_preferred_sec;
    }
}

package com.tradehero.th.fragments.security;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.tradehero.th.R;
import com.tradehero.th.api.security.WarrantType;

public enum WarrantTabType
{
    ALL(null, R.string.warrants_all),
    CALL_ONLY(WarrantType.CALL, R.string.warrant_type_call_only),
    PUT_ONLY(WarrantType.PUT, R.string.warrant_type_put_only),
    ;

    @Nullable public final WarrantType warrantType;
    @StringRes public final int title;

    //<editor-fold desc="Constructors">
    private WarrantTabType(@Nullable WarrantType warrantType, @StringRes int title)
    {
        this.warrantType = warrantType;
        this.title = title;
    }
    //</editor-fold>
}

package com.tradehero.th.api.i18n.lang;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.tradehero.th.R;
import com.tradehero.th.api.i18n.LanguageDTO;

public class LanguageIndonesianDTO extends LanguageDTO
{
    //<editor-fold desc="Constructors">
    public LanguageIndonesianDTO(@NonNull Resources resources)
    {
        super("id",
                resources.getString(R.string.translation_language_known_id),
                resources.getString(R.string.translation_language_known_id_own));
    }
    //</editor-fold>
}

package com.tradehero.th.api.translation.bing;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.tradehero.th.R;
import com.tradehero.th.api.translation.TranslatableLanguageDTOFactory;
import javax.inject.Inject;

public class BingLanguageDTOFactory extends TranslatableLanguageDTOFactory
{
    public static final int BING_LANGUAGE_CODES_RES_ID = R.array.bing_language_codes;

    //<editor-fold desc="Constructors">
    @Inject public BingLanguageDTOFactory()
    {
    }
    //</editor-fold>

    @Override protected String[] getLanguageCodes(@NonNull Resources resources)
    {
        return resources.getStringArray(BING_LANGUAGE_CODES_RES_ID);
    }
}

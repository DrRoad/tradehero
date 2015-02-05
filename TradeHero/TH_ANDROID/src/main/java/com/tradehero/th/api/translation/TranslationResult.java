package com.tradehero.th.api.translation;

import android.support.annotation.DrawableRes;
import com.tradehero.common.persistence.DTO;
import com.tradehero.th.R;

public class TranslationResult implements DTO
{
    public String getContent()
    {
        return null;
    }

    @DrawableRes public int logoResId()
    {
        return R.drawable.default_image;
    }
}

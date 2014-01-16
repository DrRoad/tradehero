package com.tradehero.th.api.competition;

import android.os.Bundle;
import com.tradehero.common.persistence.AbstractIntegerDTOKey;

/**
 * Created by xavier on 1/16/14.
 */
public class HelpVideoId extends AbstractIntegerDTOKey
{
    public static final String TAG = HelpVideoId.class.getSimpleName();
    public static final String BUNDLE_KEY_KEY = HelpVideoId.class.getName() + ".key";

    public HelpVideoId(Integer key)
    {
        super(key);
    }

    public HelpVideoId(Bundle args)
    {
        super(args);
    }

    @Override public String getBundleKey()
    {
        return BUNDLE_KEY_KEY;
    }
}

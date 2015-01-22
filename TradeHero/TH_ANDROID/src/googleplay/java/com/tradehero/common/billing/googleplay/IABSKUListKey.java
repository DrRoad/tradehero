package com.tradehero.common.billing.googleplay;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.ProductIdentifierListKey;
import com.tradehero.common.persistence.AbstractStringDTOKey;

public class IABSKUListKey extends AbstractStringDTOKey implements ProductIdentifierListKey
{
    public static final String BUNDLE_KEY_KEY = IABSKUListKey.class.getName() + ".key";
    public static final String KEY_ALL = "ALL";

    public static IABSKUListKey getInApp()
    {
        return new IABSKUListKey(IABConstants.ITEM_TYPE_INAPP);
    }

    public static IABSKUListKey getSubs()
    {
        return new IABSKUListKey(IABConstants.ITEM_TYPE_SUBS);
    }

    public static IABSKUListKey getAll()
    {
        return new IABSKUListKey(KEY_ALL);
    }

    //<editor-fold desc="Constructors">
    public IABSKUListKey(Bundle args)
    {
        super(args);
    }

    public IABSKUListKey(String key)
    {
        super(key);
    }
    //</editor-fold>

    @NonNull @Override public String getBundleKey()
    {
        return BUNDLE_KEY_KEY;
    }
}

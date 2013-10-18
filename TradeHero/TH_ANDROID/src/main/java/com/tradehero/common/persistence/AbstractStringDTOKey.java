package com.tradehero.common.persistence;

import android.os.Bundle;

/**
 * Created by julien on 14/10/13
 */
public abstract class AbstractStringDTOKey extends AbstractPrimitiveDTOKey<String>
{
    public AbstractStringDTOKey(String key)
    {
        super(key);
    }

    public AbstractStringDTOKey(Bundle args)
    {
        super(args);
    }

    abstract public String getBundleKey();

    public void putParameters(Bundle args)
    {
        args.putString(getBundleKey(), makeKey());
    }
}

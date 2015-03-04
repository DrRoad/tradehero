package com.tradehero.th.fragments.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import com.tradehero.common.persistence.DTO;
import com.tradehero.common.persistence.DTOCacheRx;
import com.tradehero.th.api.security.SecurityId;
import rx.Subscription;

abstract public class AbstractSecurityInfoFragment<InfoType extends DTO>
        extends Fragment
{
    private static final String BUNDLE_KEY_SECURITY_ID = "securityId";

    protected SecurityId securityId;
    protected InfoType value;

    public static void putSecurityId(Bundle args, SecurityId securityId)
    {
        args.putBundle(BUNDLE_KEY_SECURITY_ID, securityId.getArgs());
    }

    public static SecurityId getSecurityId(Bundle args)
    {
        SecurityId extracted = null;
        if (args != null)
        {
            extracted = new SecurityId(args.getBundle(BUNDLE_KEY_SECURITY_ID));
        }
        return extracted;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        linkWith(getSecurityId(getArguments()));
    }

    abstract protected DTOCacheRx<SecurityId, InfoType> getInfoCache();

    protected void unsubscribe(@Nullable Subscription subscription)
    {
        if (subscription != null)
        {
            subscription.unsubscribe();
        }
    }

    public void linkWith(@Nullable SecurityId securityId)
    {
        this.securityId = securityId;
    }

    public void linkWith(InfoType value)
    {
        this.value = value;
        display();
    }

    abstract public void display();
}

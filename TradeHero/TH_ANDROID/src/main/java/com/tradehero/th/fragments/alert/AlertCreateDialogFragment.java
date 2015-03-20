package com.tradehero.th.fragments.alert;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.network.service.AlertServiceWrapper;
import com.tradehero.th.network.service.QuoteServiceWrapper;
import com.tradehero.th.persistence.security.SecurityCompactCacheRx;
import dagger.Lazy;
import javax.inject.Inject;

public class AlertCreateDialogFragment extends BaseAlertEditDialogFragment
{
    private static final String BUNDLE_KEY_SECURITY_ID_BUNDLE = BaseAlertEditFragment.class.getName() + ".securityId";

    @Inject protected SecurityCompactCacheRx securityCompactCache;
    @Inject protected Lazy<AlertServiceWrapper> alertServiceWrapper;
    @Inject protected QuoteServiceWrapper quoteServiceWrapper;

    SecurityId securityId;

    @NonNull public static AlertCreateDialogFragment newInstance(@NonNull SecurityId securityId)
    {
        AlertCreateDialogFragment fragment = new AlertCreateDialogFragment();
        Bundle args = new Bundle();
        putSecurityId(args, securityId);
        fragment.setArguments(args);
        return fragment;
    }

    public static void putSecurityId(@NonNull Bundle args, @NonNull SecurityId securityId)
    {
        args.putBundle(BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
    }

    @NonNull public static SecurityId getSecurityId(@NonNull Bundle args)
    {
        return new SecurityId(args.getBundle(BUNDLE_KEY_SECURITY_ID_BUNDLE));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        securityId = getSecurityId(getArguments());
        viewHolder = new AlertCreateFragmentHolder(
                getActivity(),
                getResources(),
                currentUserId,
                securityAlertCountingHelper,
                quoteServiceWrapper,
                securityCompactCache,
                alertServiceWrapper,
                securityId);
    }
}

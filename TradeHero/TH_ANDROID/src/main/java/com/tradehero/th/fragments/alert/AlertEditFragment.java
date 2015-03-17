package com.tradehero.th.fragments.alert;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import com.tradehero.th.R;
import com.tradehero.th.api.alert.AlertId;
import com.tradehero.th.network.service.AlertServiceWrapper;
import com.tradehero.th.persistence.alert.AlertCacheRx;
import dagger.Lazy;
import javax.inject.Inject;

public class AlertEditFragment extends BaseAlertEditFragment
{
    private static final String BUNDLE_KEY_ALERT_ID_BUNDLE = BaseAlertEditFragment.class.getName() + ".alertId";

    @Inject protected AlertCacheRx alertCache;
    @Inject protected Lazy<AlertServiceWrapper> alertServiceWrapper;

    protected AlertId alertId;

    public static void putAlertId(@NonNull Bundle args, @NonNull AlertId alertId)
    {
        args.putBundle(BUNDLE_KEY_ALERT_ID_BUNDLE, alertId.getArgs());
    }

    @NonNull public static AlertId getAlertId(@NonNull Bundle args)
    {
        return new AlertId(args.getBundle(BUNDLE_KEY_ALERT_ID_BUNDLE));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        alertId = getAlertId(getArguments());
        viewHolder = new AlertEditFragmentHolder(
                getActivity(),
                getResources(),
                currentUserId,
                securityAlertCountingHelper,
                alertCache,
                alertServiceWrapper,
                alertId);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setActionBarTitle(R.string.stock_alert_edit_alert);
    }
}

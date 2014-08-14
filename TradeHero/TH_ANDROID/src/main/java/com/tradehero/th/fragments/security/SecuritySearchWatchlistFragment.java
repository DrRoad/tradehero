package com.tradehero.th.fragments.security;

import android.os.Bundle;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.base.Navigator;
import com.tradehero.th.fragments.watchlist.WatchlistPositionFragment;

public class SecuritySearchWatchlistFragment extends SecuritySearchFragment
{
    @Override protected void pushTradeFragmentIn(SecurityId securityId)
    {
        Bundle args = new Bundle();
        WatchlistEditFragment.putSecurityId(args, securityId);
        args.putString(
                Navigator.BUNDLE_KEY_RETURN_FRAGMENT,
                WatchlistPositionFragment.class.getName());
        getDashboardNavigator().pushFragment(WatchlistEditFragment.class, args);
    }
}

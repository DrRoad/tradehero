package com.tradehero.th.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Menu;
import com.tradehero.th.R;
import com.tradehero.th.fragments.billing.StoreScreenFragment;

public class StoreScreenActivity extends OneFragmentActivity
    implements AchievementAcceptor
{
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        thRouter.registerRoutes(StoreScreenFragment.class);
    }

    @NonNull @Override protected Class<? extends Fragment> getInitialFragment()
    {
        return StoreScreenFragment.class;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.store_screen_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

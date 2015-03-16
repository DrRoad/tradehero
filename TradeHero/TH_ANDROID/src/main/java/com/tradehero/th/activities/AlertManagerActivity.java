package com.tradehero.th.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.UIModule;
import com.tradehero.th.fragments.alert.AlertManagerFragment;
import com.tradehero.th.utils.dagger.AppModule;
import dagger.Module;
import java.util.ArrayList;
import java.util.List;

public class AlertManagerActivity extends BaseActivity
    implements AchievementAcceptor
{
    @InjectView(R.id.my_toolbar) Toolbar toolbar;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.realtabcontent, new AlertManagerFragment());
        transaction.commit();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater();
        return super.onCreateOptionsMenu(menu);
    }

    @Override protected List<Object> getModules()
    {
        List<Object> superModules = new ArrayList<>(super.getModules());
        superModules.add(new AlertManagerActivityModule());
        return superModules;
    }

    @Module(
            addsTo = AppModule.class,
            includes = {
                    UIModule.class
            },
            library = true,
            complete = false,
            overrides = true
    )
    public class AlertManagerActivityModule
    {
    }
}

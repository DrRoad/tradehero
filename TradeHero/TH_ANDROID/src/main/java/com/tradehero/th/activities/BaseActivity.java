package com.tradehero.th.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.base.THApp;
import com.tradehero.th.inject.Injector;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.dialog.OnDialogClickEvent;
import com.tradehero.th.utils.AlertDialogRxUtil;
import com.tradehero.th.utils.Constants;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import rx.functions.Action1;
import timber.log.Timber;

public class BaseActivity extends AppCompatActivity
        implements OnAccountsUpdateListener, Injector
{
    public static final int REQUEST_CODE_ROUTE = "REQUEST_CODE_ROUTE".hashCode() & 0xFF; // 16 bit only
    private static final String INTENT_EXTRA_KEY_ROUTE = BaseActivity.class.getName() + ".route";
    private static final String INTENT_EXTRA_KEY_EXTRAS = BaseActivity.class.getName() + ".extras";

    private AccountManager accountManager;
    private Injector newInjector;

    protected LocalBroadcastManager localBroadcastManager;
    BroadcastReceiver upgradeRequiredBroadcastListener;
    BroadcastReceiver socialTokenBroadcastListener;

    private WeakReference<Toolbar> toolbarRef;

    public static void putRouteParams(@NonNull Intent returnIntent, @NonNull String deepLink, @NonNull Bundle extras)
    {
        returnIntent.putExtra(INTENT_EXTRA_KEY_ROUTE, deepLink);
        returnIntent.putExtra(INTENT_EXTRA_KEY_EXTRAS, extras);
    }

    @Nullable public static RouteParams getRouteParams(@NonNull Intent returnIntent)
    {
        if ((returnIntent != null) && returnIntent.hasExtra(INTENT_EXTRA_KEY_ROUTE))
        {
            return new RouteParams(
                    returnIntent.getStringExtra(INTENT_EXTRA_KEY_ROUTE),
                    returnIntent.getBundleExtra(INTENT_EXTRA_KEY_EXTRAS));
        }
        else
        {
            return null;
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        extendAndInject();

        super.onCreate(savedInstanceState);

        Timber.d("Activity created");

        accountManager = AccountManager.get(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        upgradeRequiredBroadcastListener = new UpgradeRequiredListener();
        socialTokenBroadcastListener = new SocialTokenListener();
    }

    private void extendAndInject()
    {
        THApp thApp = THApp.get(this);
        newInjector = thApp.plus(getModules().toArray());
        newInjector.inject(this);
    }

    @NonNull protected List<Object> getModules()
    {
        return Arrays.<Object>asList(new BaseActivityModule(this));
    }

    @Override protected void onResume()
    {
        super.onResume();

        if (requireLogin())
        {
            accountManager.addOnAccountsUpdatedListener(this, null, true);
        }
        localBroadcastManager.registerReceiver(upgradeRequiredBroadcastListener, ActivityUtil.getIntentFilterUpgrade());
        localBroadcastManager.registerReceiver(socialTokenBroadcastListener, ActivityUtil.getIntentFilterSocialToken());
    }

    @Override protected void onPause()
    {
        super.onPause();

        localBroadcastManager.unregisterReceiver(upgradeRequiredBroadcastListener);
        localBroadcastManager.unregisterReceiver(socialTokenBroadcastListener);
        if (requireLogin())
        {
            accountManager.removeOnAccountsUpdatedListener(this);
        }
    }

    @Override protected void onDestroy()
    {
        socialTokenBroadcastListener = null;
        upgradeRequiredBroadcastListener = null;
        super.onDestroy();
    }

    @Override public void setSupportActionBar(@Nullable Toolbar toolbar)
    {
        toolbarRef = new WeakReference<Toolbar>(toolbar);
        super.setSupportActionBar(toolbar);
    }

    public @Nullable Toolbar getToolbar()
    {
        Toolbar toolbar = toolbarRef.get();
        return toolbar;
    }

    protected boolean requireLogin()
    {
        return true;
    }

    @Override public void onAccountsUpdated(Account[] accounts)
    {
        for (Account account : accounts)
        {
            if (Constants.Auth.PARAM_ACCOUNT_TYPE.equals(account.type))
            {
                return;
            }
        }

        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }

    protected class UpgradeRequiredListener extends BroadcastReceiver
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            showUpgradeDialog();
        }
    }

    protected void showUpgradeDialog()
    {
        AlertDialogRxUtil.popUpgradeRequired(this)
                .subscribe(
                        new Action1<OnDialogClickEvent>()
                        {
                            @Override public void call(OnDialogClickEvent event)
                            {
                                if (event.isPositive())
                                {
                                    THToast.show(R.string.update_guide);
                                    MarketUtil.showAppOnMarket(BaseActivity.this);
                                    finish();
                                }
                            }
                        },
                        new EmptyAction1<Throwable>());
    }

    protected class SocialTokenListener extends BroadcastReceiver
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            ActivityHelper.launchAuthentication(BaseActivity.this, intent.getData());
        }
    }

    @Override public void inject(Object o)
    {
        if (newInjector != null)
        {
            newInjector.inject(o);
        }
    }

    public static class RouteParams
    {
        @NonNull public final String deepLink;
        @NonNull public final Bundle extras;

        public RouteParams(
                @NonNull String deepLink,
                @NonNull Bundle extras)
        {
            this.deepLink = deepLink;
            this.extras = extras;
        }
    }
}

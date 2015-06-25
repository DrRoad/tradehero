package com.tradehero.th.activities;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import com.tradehero.common.persistence.prefs.BooleanPreference;
import com.tradehero.th.R;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.live.LiveCallToActionFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.persistence.prefs.IsLiveTrading;
import com.tradehero.th.widget.LiveSwitcher;
import javax.inject.Inject;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LiveActivityUtil
{
    private DashboardActivity dashboardActivity;
    private CompositeSubscription onDestroyOptionsMenuSubscriptions;

    @Inject @IsLiveTrading BooleanPreference isLiveTrading;
    @Inject DashboardNavigator navigator;
    private PublishSubject<LiveSwitcher.Event> isTradingLivePublishSubject;

    public LiveActivityUtil(DashboardActivity dashboardActivity)
    {
        this.dashboardActivity = dashboardActivity;
        isTradingLivePublishSubject = PublishSubject.create();
        HierarchyInjector.inject(dashboardActivity, this);
    }

    public void onCreateOptionsMenu(Menu menu)
    {
        if (onDestroyOptionsMenuSubscriptions != null)
        {
            onDestroyOptionsMenuSubscriptions.unsubscribe();
            onDestroyOptionsMenuSubscriptions.clear();
            onDestroyOptionsMenuSubscriptions = null;
        }
        onDestroyOptionsMenuSubscriptions = new CompositeSubscription();

        dashboardActivity.getMenuInflater().inflate(R.menu.live_switch_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_live);
        final LiveSwitcher liveSwitcher = (LiveSwitcher) item.getActionView();

        onDestroyOptionsMenuSubscriptions.add(liveSwitcher.getSwitchObservable().subscribe(isTradingLivePublishSubject));
        onDestroyOptionsMenuSubscriptions.add(isTradingLivePublishSubject
                .distinctUntilChanged(
                        new Func1<LiveSwitcher.Event, Boolean>()
                        {
                            @Override public Boolean call(LiveSwitcher.Event event)
                            {
                                return event.isLive;
                            }
                        })
                .startWith(new LiveSwitcher.Event(false, isLiveTrading.get()))
                .doOnNext(new Action1<LiveSwitcher.Event>()
                {
                    @Override public void call(LiveSwitcher.Event event)
                    {
                        //Every-time a change happened.
                        isLiveTrading.set(event.isLive);
                    }
                })
                .subscribe(new Action1<LiveSwitcher.Event>()
                {
                    @Override public void call(LiveSwitcher.Event event)
                    {
                        liveSwitcher.setIsLive(event.isLive, event.isFromUser);
                        onLiveTradingChanged(event);
                    }
                }));

        for (Fragment f : dashboardActivity.getSupportFragmentManager().getFragments())
        {
            if (f instanceof DashboardFragment && f.isVisible() && ((DashboardFragment) f).shouldShowLiveTradingToggle())
            {
                item.setVisible(true);
                break;
            }
        }
    }

    private void onLiveTradingChanged(LiveSwitcher.Event event)
    {
        for (Fragment f : dashboardActivity.getSupportFragmentManager().getFragments())
        {
            if (f instanceof DashboardFragment && f.isVisible())
            {
                ((DashboardFragment) f).onLiveTradingChanged(event.isLive);
            }
        }

        dashboardActivity.getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(dashboardActivity.getResources().getColor(event.isLive ? R.color.tradehero_red : R.color.tradehero_blue)));

        //Specific to this activity?
        dashboardActivity.drawerLayout.setStatusBarBackgroundColor(
                dashboardActivity.getResources().getColor(event.isLive ? R.color.tradehero_red_status_bar : R.color.tradehero_blue_status_bar));

        for (int i = 0; i < dashboardActivity.dashboardTabHost.getTabWidget().getChildCount(); i++)
        {
            dashboardActivity.dashboardTabHost.getTabWidget().getChildAt(i)
                    .setBackgroundResource(
                            event.isLive ? R.drawable.tradehero_bottom_tab_indicator_red : R.drawable.tradehero_bottom_tab_indicator);
        }
        if (event.isFromUser && event.isLive && !(navigator.getCurrentFragment() instanceof LiveCallToActionFragment))
        {
            navigator.pushFragment(LiveCallToActionFragment.class);
        }
    }

    public void onDestroy()
    {
        if (onDestroyOptionsMenuSubscriptions != null)
        {
            onDestroyOptionsMenuSubscriptions.unsubscribe();
            onDestroyOptionsMenuSubscriptions.clear();
        }
        this.isTradingLivePublishSubject = null;
        this.dashboardActivity = null;
    }

    public void switchLive(boolean isLive)
    {
        isTradingLivePublishSubject.onNext(new LiveSwitcher.Event(false, isLive));
    }
}

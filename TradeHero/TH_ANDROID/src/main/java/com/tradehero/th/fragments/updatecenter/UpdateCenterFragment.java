package com.tradehero.th.fragments.updatecenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import com.special.residemenu.ResideMenu;
import com.tradehero.common.utils.THToast;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.MessageType;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.social.AllRelationsFragment;
import com.tradehero.th.fragments.social.follower.SendMessageFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.discussion.RunnableInvalidateMessageList;
import com.tradehero.th.models.notification.RunnableInvalidateNotificationList;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.utils.GraphicUtil;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.utils.route.PreRoutable;
import com.tradehero.th.utils.route.THRouter;
import com.tradehero.th.widget.THTabView;
import java.util.List;
import javax.inject.Inject;
import android.support.annotation.NonNull;
import rx.Observer;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

@PreRoutable(preOpenRunnables = {
        RunnableInvalidateMessageList.class,
        RunnableInvalidateNotificationList.class,
})
@Routable("updatecenter/:pageIndex")
public class UpdateCenterFragment extends DashboardFragment
        implements OnTitleNumberChangeListener,
        ResideMenu.OnMenuListener
{
    static final int FRAGMENT_LAYOUT_ID = 10000;
    public static final String REQUEST_UPDATE_UNREAD_COUNTER = ".updateUnreadCounter";

    @Inject UserProfileCacheRx userProfileCache;
    @Inject CurrentUserId currentUserId;
    @Inject Analytics analytics;
    @Inject GraphicUtil graphicUtil;

    @Inject THRouter thRouter;

    @RouteProperty("pageIndex") int selectedPageIndex = -1;
    private FragmentTabHost mTabHost;
    private BroadcastReceiver broadcastReceiver;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        thRouter.inject(this);
        broadcastReceiver = createBroadcastReceiver();
        Timber.d("onCreate");
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        Timber.d("onCreateView");
        return addTabs();
    }

    @Override public void onResume()
    {
        super.onResume();

        Timber.d("onResume fetchUserProfile");
        fetchUserProfile();

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver,
                        new IntentFilter(REQUEST_UPDATE_UNREAD_COUNTER));

        if (selectedPageIndex > 0)
        {
            mTabHost.setCurrentTab(selectedPageIndex);
        }
    }

    @Override public void onPause()
    {
        super.onPause();

        Timber.d("onPause");
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(broadcastReceiver);
    }

    private void fetchUserProfile()
    {
        fetchUserProfile(false);
    }

    private void fetchUserProfile(boolean forceUpdate)
    {
        AndroidObservable.bindFragment(this, userProfileCache.get(currentUserId.toUserBaseKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createUserProfileCacheObserver());
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        Fragment f = getCurrentFragment();
        if (f != null)
        {
            getCurrentFragment().onCreateOptionsMenu(menu, inflater);
        }

        setActionBarTitle(R.string.message_center_title);
        inflater.inflate(R.menu.notification_center_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_private:
                analytics.addEvent(new SimpleEvent(AnalyticsConstants.Notification_New_Message));
                navigator.get().pushFragment(AllRelationsFragment.class);
                return true;
            case R.id.menu_broadcast:
                jumpToSendBroadcastMessage();
                return true;
        }
        Fragment f = getCurrentFragment();
        if (f != null)
        {
            boolean handled = getCurrentFragment().onOptionsItemSelected(item);
            if (handled)
            {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onPrepareOptionsMenu(Menu menu)
    {
        Fragment f = getCurrentFragment();
        if (f != null)
        {
            getCurrentFragment().onPrepareOptionsMenu(menu);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override public void onOptionsMenuClosed(android.view.Menu menu)
    {
        Fragment f = getCurrentFragment();
        if (f != null)
        {
            getCurrentFragment().onOptionsMenuClosed(menu);
        }
        super.onOptionsMenuClosed(menu);
    }

    @Override public void onDestroyOptionsMenu()
    {
        Fragment f = getCurrentFragment();
        if (f != null)
        {
            f.onDestroyOptionsMenu();
        }
        Timber.d("onDestroyOptionsMenu");

        super.onDestroyOptionsMenu();
    }

    private void jumpToSendBroadcastMessage()
    {
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Notification_New_Broadcast));
        Bundle args = new Bundle();
        args.putInt(SendMessageFragment.KEY_DISCUSSION_TYPE,
                DiscussionType.BROADCAST_MESSAGE.value);
        args.putInt(SendMessageFragment.KEY_MESSAGE_TYPE,
                MessageType.BROADCAST_ALL_FOLLOWERS.typeId);
        navigator.get().pushFragment(SendMessageFragment.class, args);
    }

    @Override public void onStop()
    {
        super.onStop();
        Timber.d("onStop");
    }

    @Override public void onDestroyView()
    {
        // TODO Questionable, as specified by Liang, it should not be needed to clear the tabs here
        Timber.d("onDestroyView");
        //don't have to clear sub fragment to refresh data
        //clearTabs();

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        Timber.d("onDestroy");
        broadcastReceiver = null;
        super.onDestroy();
    }

    private View addTabs()
    {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), ((Fragment) this).getChildFragmentManager(), FRAGMENT_LAYOUT_ID);
        TabWidget tabWidget = mTabHost.getTabWidget();
        if (tabWidget != null)
        // It otherwise fails in Robolectric because it does not have R.id.tabs in the TabHost
        {
            graphicUtil.setBackground(tabWidget, getResources().getDrawable(R.drawable.ab_background));
        }
        Bundle args = getArguments();
        if (args == null)
        {
            args = new Bundle();
        }
        UpdateCenterTabType[] types = UpdateCenterTabType.values();
        for (UpdateCenterTabType tabTitle : types)
        {
            args = new Bundle(args);
            THTabView tabView = THTabView.inflateWith(mTabHost.getTabWidget());
            String title = getString(tabTitle.titleRes, 0);
            tabView.setTitle(title);
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(title).setIndicator(tabView);
            mTabHost.addTab(tabSpec, tabTitle.tabClass, args);
        }

        return mTabHost;
    }

    private void clearTabs()
    {
        if (mTabHost != null)
        {
            android.support.v4.app.FragmentManager fm = ((Fragment) this).getChildFragmentManager();
            List<Fragment> fragmentList = fm.getFragments();
            Timber.d("fragmentList %s", fragmentList);
            if (fragmentList != null && fragmentList.size() > 0)
            {
                FragmentTransaction ft = fm.beginTransaction();
                for (Fragment f : fragmentList)
                {
                    if (f != null)
                    {
                        ft.remove(f);
                    }
                }
                //java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                //TODO this will crash when onDestroy alex
                ft.commitAllowingStateLoss();
                fm.executePendingTransactions();
            }

            mTabHost.clearAllTabs();
            int tabCount = mTabHost.getTabWidget().getTabCount();
            mTabHost = null;
        }
    }

    public Fragment getCurrentFragment()
    {
        if (mTabHost == null)
        {
            return null;
        }
        String tag = mTabHost.getCurrentTabTag();
        android.support.v4.app.FragmentManager fm = ((Fragment) this).getChildFragmentManager();
        return fm.findFragmentByTag(tag);
    }

    private void changeTabTitleNumber(@NonNull UpdateCenterTabType tabType, int number)
    {
        THTabView tabView = (THTabView) mTabHost.getTabWidget().getChildAt(tabType.ordinal());
        tabView.setNumber(number);
    }

    @Override public void onTitleNumberChanged(@NonNull UpdateCenterTabType tabType, int number)
    {
        changeTabTitleNumber(tabType, number);
    }

    @NonNull protected Observer<Pair<UserBaseKey, UserProfileDTO>> createUserProfileCacheObserver()
    {
        return new FetchUserProfileObserver();
    }

    private class FetchUserProfileObserver implements Observer<Pair<UserBaseKey, UserProfileDTO>>
    {
        @Override public void onNext(Pair<UserBaseKey, UserProfileDTO> pair)
        {
            linkWith(pair.second, true);
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            THToast.show(new THException(e));
        }
    }

    private void linkWith(@NonNull UserProfileDTO userProfileDTO, boolean andDisplay)
    {
        if (andDisplay)
        {
            changeTabTitleNumber(
                    UpdateCenterTabType.Messages,
                    userProfileDTO.unreadMessageThreadsCount);
            changeTabTitleNumber(
                    UpdateCenterTabType.Notifications,
                    userProfileDTO.unreadNotificationsCount);
        }
    }

    private BroadcastReceiver createBroadcastReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override public void onReceive(Context context, Intent intent)
            {
                fetchUserProfile(true);
            }
        };
    }

    @Override public void openMenu()
    {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null && currentFragment instanceof ResideMenu.OnMenuListener)
        {
            ((ResideMenu.OnMenuListener) currentFragment).openMenu();
        }
    }

    @Override public void closeMenu()
    {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null && currentFragment instanceof ResideMenu.OnMenuListener)
        {
            ((ResideMenu.OnMenuListener) currentFragment).closeMenu();
        }
    }
}

package com.tradehero.th.fragments.updatecenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.discussion.RunnableInvalidateMessageList;
import com.tradehero.th.models.notification.RunnableInvalidateNotificationList;
import com.tradehero.th.persistence.message.MessageHeaderCache;
import com.tradehero.th.persistence.message.MessageHeaderListCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.route.PreRoutable;
import com.tradehero.th.utils.route.THRouter;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.List;

@PreRoutable(preOpenRunnables = {
        RunnableInvalidateMessageList.class,
        RunnableInvalidateNotificationList.class,
})
@Routable("updatecenter/:pageIndex")
public class UpdateCenterFragment extends DashboardFragment
        implements OnTitleNumberChangeListener
{
    static final int FRAGMENT_LAYOUT_ID = 10000;
    public static final String REQUEST_UPDATE_UNREAD_COUNTER = ".updateUnreadCounter";

    @Inject UserProfileCache userProfileCache;
    @Inject CurrentUserId currentUserId;
    @Inject MessageHeaderListCache messageListCache;
    @Inject MessageHeaderCache messageHeaderCache;
    @Inject THRouter thRouter;

    @RouteProperty("pageIndex") int selectedPageIndex = -1;

    private FragmentTabHost mTabHost;
    private DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> userProfileCacheListener;
    private BroadcastReceiver broadcastReceiver;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        thRouter.inject(this);
        userProfileCacheListener = createUserProfileCacheListener();
        broadcastReceiver = createBroadcastReceiver();
        Timber.d("onCreate");
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        Timber.d("onCreateView");
        return addTabs();
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
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
        detachUserProfileCache();
        userProfileCache.register(currentUserId.toUserBaseKey(), userProfileCacheListener);
        userProfileCache.getOrFetchAsync(currentUserId.toUserBaseKey(),forceUpdate);
    }

    private void detachUserProfileCache()
    {
        userProfileCache.unregister(userProfileCacheListener);
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
        detachUserProfileCache();

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        Timber.d("onDestroy");
        userProfileCacheListener = null;
        broadcastReceiver = null;
        super.onDestroy();
    }

    private View addTabs()
    {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), ((Fragment) this).getChildFragmentManager(), FRAGMENT_LAYOUT_ID);
        //mTabHost.setOnTabChangedListener(new HeroManagerOnTabChangeListener());
        Bundle args = getArguments();
        if (args == null)
        {
            args = new Bundle();
        }
        UpdateCenterTabType[] types = UpdateCenterTabType.values();
        for (UpdateCenterTabType tabTitle : types)
        {
            args = new Bundle(args);
            TitleTabView tabView = (TitleTabView) LayoutInflater.from(getActivity())
                    .inflate(R.layout.message_tab_item, mTabHost.getTabWidget(), false);
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

    private void changeTabTitleNumber(@NotNull UpdateCenterTabType tabType, int number)
    {
        @NotNull TitleTabView tabView = (TitleTabView) mTabHost.getTabWidget().getChildAt(tabType.ordinal());
        tabView.setTitleNumber(number);
    }

    @Override public void onTitleNumberChanged(@NotNull UpdateCenterTabType tabType, int number)
    {
        changeTabTitleNumber(tabType, number);
    }

    @NotNull protected DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> createUserProfileCacheListener()
    {
        return new FetchUserProfileListener();
    }

    private class FetchUserProfileListener implements DTOCacheNew.Listener<UserBaseKey, UserProfileDTO>
    {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull UserProfileDTO value)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            THToast.show(new THException(error));
        }
    }

    private void linkWith(@NotNull UserProfileDTO userProfileDTO, boolean andDisplay)
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
}

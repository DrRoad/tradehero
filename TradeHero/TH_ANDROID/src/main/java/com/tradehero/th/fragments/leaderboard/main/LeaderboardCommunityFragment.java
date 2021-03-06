package com.tradehero.th.fragments.leaderboard.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.SectorContainerLeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.def.DrillDownLeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.def.ExchangeContainerLeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTOList;
import com.tradehero.th.api.leaderboard.key.ExchangeLeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.SectorLeaderboardDefListKey;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.leaderboard.FriendLeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardDefListFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.social.PeopleSearchFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerFragment;
import com.tradehero.th.fragments.social.friend.FriendsInvitationFragment;
import com.tradehero.th.fragments.social.hero.HeroManagerFragment;
import com.tradehero.th.fragments.tutorial.WithTutorial;
import com.tradehero.th.fragments.web.BaseWebViewFragment;
import com.tradehero.th.models.intent.THIntent;
import com.tradehero.th.models.intent.THIntentPassedListener;
import com.tradehero.th.models.intent.competition.ProviderIntent;
import com.tradehero.th.models.intent.competition.ProviderPageIntent;
import com.tradehero.th.models.leaderboard.key.LeaderboardDefKeyKnowledge;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import dagger.Lazy;
import javax.inject.Inject;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;

@Routable("providers")
public class LeaderboardCommunityFragment extends BasePurchaseManagerFragment
        implements WithTutorial, View.OnClickListener
{
    @Inject Lazy<LeaderboardDefListCacheRx> leaderboardDefListCache;
    @Inject CurrentUserId currentUserId;
    @Inject Analytics analytics;
    @Inject CommunityPageDTOFactory communityPageDTOFactory;
    @Inject UserProfileCacheRx userProfileCache;

    @InjectView(R.id.community_screen) BetterViewAnimator communityScreen;
    @InjectView(android.R.id.list) StickyListHeadersListView leaderboardDefListView;

    private BaseWebViewFragment webFragment;
    private LeaderboardCommunityAdapter leaderboardDefListAdapter;
    private int currentDisplayedChildLayoutId;
    @Nullable protected Subscription leaderboardDefListFetchSubscription;
    protected UserProfileDTO currentUserProfileDTO;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        leaderboardDefListAdapter = createAdapter();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.leaderboard_community_screen, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        initViews(view);
    }

    @Override protected void initViews(View view)
    {
        leaderboardDefListView.setOnScrollListener(dashboardBottomTabsListViewScrollListener.get());
        leaderboardDefListView.setAdapter(leaderboardDefListAdapter);
    }

    @Override public void onStart()
    {
        super.onStart();
        leaderboardDefListView.setOnItemClickListener(createItemClickListener());
        // show either progress bar or def list, whichever last seen on this screen
        if (currentDisplayedChildLayoutId != 0)
        {
            communityScreen.setDisplayedChildByLayoutId(currentDisplayedChildLayoutId);
        }
    }

    @Override public void onResume()
    {
        super.onResume();
        fetchCurrentUserProfile();
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.TabBar_Community));

        // We came back into view so we have to forget the web fragment
        detachWebFragment();
    }

    @Override public void onStop()
    {
        unsubscribe(leaderboardDefListFetchSubscription);
        leaderboardDefListFetchSubscription = null;
        currentDisplayedChildLayoutId = communityScreen.getDisplayedChildLayoutId();
        leaderboardDefListView.setOnItemClickListener(null);
        super.onStop();
    }

    @Override public void onDestroy()
    {
        leaderboardDefListAdapter = null;
        detachWebFragment();
        super.onDestroy();
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.social_search_menu, menu);
        setActionBarTitle(R.string.dashboard_community);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO switch sorting type for leaderboard
        switch (item.getItemId())
        {
            case R.id.btn_search:
                pushSearchFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>

    protected LeaderboardCommunityAdapter createAdapter()
    {
        return new LeaderboardCommunityAdapter(
                getActivity(),
                R.layout.leaderboard_definition_item_view);
    }

    private void detachWebFragment()
    {
        if (this.webFragment != null)
        {
            this.webFragment.setThIntentPassedListener(null);
        }
        this.webFragment = null;
    }

    protected void fetchCurrentUserProfile()
    {
        AndroidObservable.bindFragment(this, userProfileCache.get(currentUserId.toUserBaseKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createUserProfileObserver());
    }

    //<editor-fold desc="Data Fetching">
    protected Observer<Pair<UserBaseKey, UserProfileDTO>> createUserProfileObserver()
    {
        return new LeaderboardCommunityUserProfileCacheObserver();
    }

    protected class LeaderboardCommunityUserProfileCacheObserver implements Observer<Pair<UserBaseKey, UserProfileDTO>>
    {
        @Override public void onNext(Pair<UserBaseKey, UserProfileDTO> pair)
        {
            setCurrentUserProfileDTO(pair.second);
            loadLeaderboardData();
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            Timber.e("Failed to download current UserProfile", e);
            THToast.show(R.string.error_fetch_your_user_profile);
        }
    }

    protected void setCurrentUserProfileDTO(@NonNull UserProfileDTO currentUserProfileDTO)
    {
        this.currentUserProfileDTO = currentUserProfileDTO;
    }

    private void loadLeaderboardData()
    {
        fetchLeaderboardDefList();
    }

    private void fetchLeaderboardDefList()
    {
        unsubscribe(leaderboardDefListFetchSubscription);
        leaderboardDefListFetchSubscription = AndroidObservable.bindFragment(
                this,
                leaderboardDefListCache.get().get(new LeaderboardDefListKey()))
                .subscribe(createDefKeyListObserver());
    }

    @NonNull protected Observer<Pair<LeaderboardDefListKey, LeaderboardDefDTOList>> createDefKeyListObserver()
    {
        return new LeaderboardCommunityLeaderboardDefKeyListObserver();
    }

    protected class LeaderboardCommunityLeaderboardDefKeyListObserver implements Observer<Pair<LeaderboardDefListKey, LeaderboardDefDTOList>>
    {
        @Override public void onNext(Pair<LeaderboardDefListKey, LeaderboardDefDTOList> leaderboardDefListKeyLeaderboardDefDTOListPair)
        {
            rePopulateAdapter();
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            THToast.show(getString(R.string.error_fetch_leaderboard_def_list_key));
            Timber.e(e, "Error fetching the leaderboard def key list");
        }
    }
    //</editor-fold>

    protected AdapterView.OnItemClickListener createItemClickListener()
    {
        return new LeaderboardCommunityOnItemClickListener();
    }

    protected class LeaderboardCommunityOnItemClickListener implements AdapterView.OnItemClickListener
    {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
        {
            handleLeaderboardItemClicked((LeaderboardDefDTO) adapterView.getItemAtPosition(position));
        }
    }

    @Override public int getTutorialLayout()
    {
        return R.layout.tutorial_leaderboard_community;
    }

    protected class LeaderboardCommunityTHIntentPassedListener implements THIntentPassedListener
    {
        @Override public void onIntentPassed(THIntent thIntent)
        {
            Timber.d("LeaderboardCommunityTHIntentPassedListener " + thIntent);
            if (thIntent instanceof ProviderIntent)
            {
                // Just in case the user has enrolled
                portfolioCompactListCache.invalidate(currentUserId.toUserBaseKey());
            }

            if (thIntent instanceof ProviderPageIntent)
            {
                Timber.d("Intent is ProviderPageIntent");
                if (webFragment != null)
                {
                    Timber.d("Passing on %s", ((ProviderPageIntent) thIntent).getCompleteForwardUriPath());
                    webFragment.loadUrl(((ProviderPageIntent) thIntent).getCompleteForwardUriPath());
                }
                else
                {
                    Timber.d("WebFragment is null");
                }
            }
            else
            {
                Timber.w("Unhandled intent %s", thIntent);
            }
        }
    }

    protected void rePopulateAdapter()
    {
        communityScreen.setDisplayedChildByLayoutId(android.R.id.list);
        if (leaderboardDefListAdapter != null)
        {
            leaderboardDefListAdapter.clear();
        }
        leaderboardDefListAdapter.addAll(communityPageDTOFactory.collectFromCaches(currentUserProfileDTO.countryCode));
        leaderboardDefListAdapter.notifyDataSetChanged();
    }

    /**
     * TODO to show user detail of the error
     */
    private void handleFailToReceiveLeaderboardDefKeyList()
    {
        communityScreen.setDisplayedChildByLayoutId(R.id.error);
        View displayedChild = communityScreen.getChildAt(communityScreen.getDisplayedChild());
        displayedChild.setOnClickListener(this);
    }

    @Override public void onClick(View v)
    {
        if (v.getId() == R.id.error)
        {
            //if error view is click it means to reload the data
            communityScreen.setDisplayedChildByLayoutId(R.id.progress);
            loadLeaderboardData();
        }
    }

    //<editor-fold desc="Navigation">
    private void handleLeaderboardItemClicked(@NonNull LeaderboardDefDTO leaderboardDefDTO)
    {
        if (leaderboardDefDTO instanceof DrillDownLeaderboardDefDTO)
        {
            DrillDownLeaderboardDefDTO drillDownLeaderboardDefDTO = (DrillDownLeaderboardDefDTO) leaderboardDefDTO;
            analytics.addEvent(new SimpleEvent(AnalyticsConstants.Leaderboards_DrillDown));
            if (drillDownLeaderboardDefDTO instanceof SectorContainerLeaderboardDefDTO)
            {
                pushLeaderboardDefSector(drillDownLeaderboardDefDTO);
            }
            else if (drillDownLeaderboardDefDTO instanceof ExchangeContainerLeaderboardDefDTO)
            {
                pushLeaderboardDefExchange(drillDownLeaderboardDefDTO);
            }
            else
            {
                throw new IllegalArgumentException("Unhandled drillDownLeaderboardDefDTO " + drillDownLeaderboardDefDTO);
            }
        }
        else
        {
            analytics.addEvent(new SimpleEvent(AnalyticsConstants.Leaderboards_ShowLeaderboard));
            pushLeaderboardListViewFragment(leaderboardDefDTO);
        }
    }

    protected void pushLeaderboardListViewFragment(@NonNull LeaderboardDefDTO dto)
    {
        Bundle bundle = new Bundle(getArguments());

        switch (dto.id)
        {
            case LeaderboardDefKeyKnowledge.FRIEND_ID:
                pushFriendsFragment(dto);
                break;
            case LeaderboardDefKeyKnowledge.HERO_ID:
                pushHeroFragment();
                break;
            case LeaderboardDefKeyKnowledge.FOLLOWER_ID:
                pushFollowerFragment();
                break;
            case LeaderboardDefKeyKnowledge.INVITE_FRIENDS_ID:
                pushInvitationFragment();
                break;
            default:
                Timber.d("LeaderboardMarkUserListFragment %s", bundle);
                LeaderboardMarkUserListFragment.putLeaderboardDefKey(bundle, dto.getLeaderboardDefKey());
                navigator.get().pushFragment(LeaderboardMarkUserListFragment.class, bundle);
                break;
        }
    }

    protected void pushFriendsFragment(LeaderboardDefDTO dto)
    {
        Bundle args = new Bundle();
        FriendLeaderboardMarkUserListFragment.putLeaderboardDefKey(args, dto.getLeaderboardDefKey());
        if (navigator != null)
        {
            navigator.get().pushFragment(FriendLeaderboardMarkUserListFragment.class, args);
        }
    }

    protected void pushHeroFragment()
    {
        Bundle bundle = new Bundle();
        HeroManagerFragment.putFollowerId(bundle, currentUserId.toUserBaseKey());
        OwnedPortfolioId applicablePortfolio = getApplicablePortfolioId();
        if (applicablePortfolio != null)
        {
            HeroManagerFragment.putApplicablePortfolioId(bundle, applicablePortfolio);
        }
        navigator.get().pushFragment(HeroManagerFragment.class, bundle);
    }

    protected void pushFollowerFragment()
    {
        Bundle bundle = new Bundle();
        FollowerManagerFragment.putHeroId(bundle, currentUserId.toUserBaseKey());
        OwnedPortfolioId applicablePortfolio = getApplicablePortfolioId();
        if (applicablePortfolio != null)
        {
            //FollowerManagerFragment.putApplicablePortfolioId(bundle, applicablePortfolio);
        }
        navigator.get().pushFragment(FollowerManagerFragment.class, bundle);
    }

    private void pushLeaderboardDefSector(LeaderboardDefDTO leaderboardDefDTOSector)
    {
        Bundle bundle = new Bundle(getArguments());
        (new SectorLeaderboardDefListKey()).putParameters(bundle);
        LeaderboardDefListFragment.putLeaderboardDefKey(bundle, leaderboardDefDTOSector.getLeaderboardDefKey());
        if (navigator != null)
        {
            navigator.get().pushFragment(LeaderboardDefListFragment.class, bundle);
        }
    }

    private void pushLeaderboardDefExchange(LeaderboardDefDTO leaderboardDefDTOExchange)
    {
        Bundle bundle = new Bundle(getArguments());
        (new ExchangeLeaderboardDefListKey()).putParameters(bundle);
        LeaderboardDefListFragment.putLeaderboardDefKey(bundle, leaderboardDefDTOExchange.getLeaderboardDefKey());
        if (navigator != null)
        {
            navigator.get().pushFragment(LeaderboardDefListFragment.class, bundle);
        }
    }

    private void pushSearchFragment()
    {
        if (navigator != null)
        {
            navigator.get().pushFragment(PeopleSearchFragment.class, null);
        }
    }

    private void pushInvitationFragment()
    {
        if (navigator != null)
        {
            navigator.get().pushFragment(FriendsInvitationFragment.class);
        }
    }
    //</editor-fold>
}

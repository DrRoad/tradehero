package com.tradehero.th.fragments.social.hero;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefKey;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.social.HeroDTO;
import com.tradehero.th.api.social.HeroDTOExtWrapper;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.request.THUIBillingRequest;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.social.FragmentUtils;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.models.leaderboard.key.LeaderboardDefKeyKnowledge;
import com.tradehero.th.models.social.follower.HeroTypeResourceDTO;
import com.tradehero.th.models.social.follower.HeroTypeResourceDTOFactory;
import com.tradehero.th.models.user.follow.FollowUserAssistant;
import com.tradehero.th.models.user.follow.SimpleFollowUserAssistant;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefCache;
import com.tradehero.th.persistence.social.HeroType;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

abstract public class HeroesTabContentFragment extends BasePurchaseManagerFragment
        implements PullToRefreshBase.OnRefreshListener2<ListView>
{
    private static final String BUNDLE_KEY_FOLLOWER_ID =
            HeroesTabContentFragment.class.getName() + ".followerId";

    private HeroManagerViewContainer viewContainer;
    private HeroListItemAdapter heroListAdapter;
    // The follower whose heroes we are listing
    @NotNull private UserBaseKey followerId;
    private UserProfileDTO userProfileDTO;
    private List<HeroDTO> heroDTOs;
    protected SimpleFollowUserAssistant simpleFollowUserAssistant;

    @Inject protected HeroManagerInfoFetcher infoFetcher;
    @Inject public HeroAlertDialogUtil heroAlertDialogUtil;
    /** when no heroes */
    @Inject Lazy<LeaderboardDefCache> leaderboardDefCache;
    @Inject HeroTypeResourceDTOFactory heroTypeResourceDTOFactory;
    @Inject CurrentUserId currentUserId;
    @Inject THRouter thRouter;

    //<editor-fold desc="Argument Passing">
    public static void putFollowerId(Bundle args, UserBaseKey followerId)
    {
        args.putBundle(BUNDLE_KEY_FOLLOWER_ID, followerId.getArgs());
    }

    @NotNull public static UserBaseKey getFollowerId(@NotNull Bundle args)
    {
        return new UserBaseKey(args.getBundle(BUNDLE_KEY_FOLLOWER_ID));
    }
    //</editor-fold>

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.followerId = getFollowerId(getArguments());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_store_manage_heroes, container, false);
        initViews(view);
        return view;
    }

    @Override protected void initViews(View view)
    {
        this.viewContainer = new HeroManagerViewContainer(view);
        if (this.viewContainer.btnBuyMore != null)
        {
            this.viewContainer.btnBuyMore.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    handleBuyMoreClicked();
                }
            });
        }

        this.heroListAdapter = new HeroListItemAdapter(
                getActivity(),
                getActivity().getLayoutInflater(),
                /**R.layout.hero_list_item_empty_placeholder*/getEmptyViewLayout(),
                R.layout.hero_list_item,
                R.layout.hero_list_header,
                R.layout.hero_list_header);
        this.heroListAdapter.setHeroStatusButtonClickedListener(createHeroStatusButtonClickedListener());
        this.heroListAdapter.setFollowerId(followerId);
        this.heroListAdapter.setMostSkilledClicked(createHeroListMostSkiledClickedListener());
        if (this.viewContainer.pullToRefreshListView != null)
        {
            this.viewContainer.pullToRefreshListView.setOnRefreshListener(this);
        }
        if (this.viewContainer.heroListView != null)
        {
            this.viewContainer.heroListView.setAdapter(this.heroListAdapter);
            this.viewContainer.heroListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id)
                        {
                            handleHeroClicked(parent, view, position, id);
                        }
                    }
            );
        }
        setListShown(false);
        this.infoFetcher.setUserProfileListener(new HeroManagerUserProfileCacheListener());
        this.infoFetcher.setHeroListListener(new HeroManagerHeroListCacheListener());
    }

    protected HeroListItemView.OnHeroStatusButtonClickedListener createHeroStatusButtonClickedListener()
    {
        return new HeroListItemView.OnHeroStatusButtonClickedListener()
        {
            @Override
            public void onHeroStatusButtonClicked(HeroListItemView heroListItemView,
                    HeroDTO heroDTO)
            {
                handleHeroStatusButtonClicked(heroDTO);
            }
        };
    }

    private void setListShown(boolean shown)
    {
        if (shown)
        {
            this.viewContainer.heroListView.setVisibility(View.VISIBLE);
            this.viewContainer.progressBar.setVisibility(View.INVISIBLE);
        }
        else
        {
            this.viewContainer.heroListView.setVisibility(View.INVISIBLE);
            this.viewContainer.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        setActionBarTitle(getTitle());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onResume()
    {
        super.onResume();
        enablePullToRefresh(false);
        displayProgress(true);
        this.infoFetcher.fetch(this.followerId);
    }

    private boolean isCurrentUser()
    {
        UserBaseKey followerId = getFollowerId(getArguments());
        if (followerId != null && followerId.key != null && currentUserId != null)
        {
            return (followerId.key.intValue() == currentUserId.toUserBaseKey().key.intValue());
        }
        return false;
    }

    private int getEmptyViewLayout()
    {
        if (isCurrentUser())
        {
            return R.layout.hero_list_item_empty_placeholder;
        }
        else
        {
           return R.layout.hero_list_item_empty_placeholder_for_other;
        }
    }

    private int getTitle()
    {
        if (isCurrentUser())
        {
            return R.string.manage_my_heroes_title;
        }
        else
        {
            return R.string.manage_heroes_title;
        }
    }

    private void refreshContent()
    {
        this.infoFetcher.reloadHeroes(this.followerId);
    }

    protected HeroTypeResourceDTO getHeroTypeResource()
    {
        return heroTypeResourceDTOFactory.create(getHeroType());
    }

    abstract protected HeroType getHeroType();

    @Override public void onStop()
    {
        detachFollowAssistant();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        if (this.infoFetcher != null)
        {
            this.infoFetcher.onDestroyView();
        }

        if (this.heroListAdapter != null)
        {
            this.heroListAdapter.setHeroStatusButtonClickedListener(null);
            this.heroListAdapter.setMostSkilledClicked(null);
        }
        this.heroListAdapter = null;
        if (this.viewContainer.heroListView != null)
        {
            this.viewContainer.heroListView.setOnItemClickListener(null);
        }
        this.viewContainer = null;

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        this.infoFetcher = null;
        super.onDestroy();
    }

    @Override protected FollowUserAssistant.OnUserFollowedListener createPremiumUserFollowedListener()
    {
        return new FollowUserAssistant.OnUserFollowedListener()
        {
            @Override
            public void onUserFollowSuccess(
                    @NotNull UserBaseKey userFollowed,
                    @NotNull UserProfileDTO currentUserProfileDTO)
            {
                Timber.d("onUserFollowSuccess");
                THToast.show(getString(R.string.manage_heroes_unfollow_success));
                linkWith(currentUserProfileDTO, true);
                if (infoFetcher != null)
                {
                    infoFetcher.fetchHeroes(followerId);
                }
            }

            @Override public void onUserFollowFailed(@NotNull UserBaseKey userFollowed, @NotNull Throwable error)
            {
                //TODO offical accounts, do not unfollow
                if (userFollowed.isOfficialAccount())
                {
                    THToast.show(getString(R.string.manage_heroes_unfollow_official_accounts_failed));
                }
                else
                {
                    Timber.e(error, "onUserFollowFailed error");
                    THToast.show(getString(R.string.manage_heroes_unfollow_failed));
                }
            }
        };
    }

    protected void detachFollowAssistant()
    {
        SimpleFollowUserAssistant assistantCopy = simpleFollowUserAssistant;
        if (assistantCopy != null)
        {
            assistantCopy.onDestroy();
        }
        simpleFollowUserAssistant = null;
    }

    private void handleBuyMoreClicked()
    {
        OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId();
        if (applicablePortfolioId != null)
        {
            detachRequestCode();
            //noinspection unchecked
            requestCode = userInteractor.run((THUIBillingRequest) uiBillingRequestBuilderProvider.get()
                    .applicablePortfolioId(getApplicablePortfolioId())
                    .domainToPresent(ProductIdentifierDomain.DOMAIN_FOLLOW_CREDITS)
                    .build());
        }
    }

    private void handleHeroStatusButtonClicked(HeroDTO heroDTO)
    {
        handleHeroStatusChangeRequired(heroDTO);
    }

    private void handleHeroClicked(AdapterView<?> parent, View view, int position, long id)
    {
        pushTimelineFragment(((HeroDTO) parent.getItemAtPosition(position)).getBaseKey());
    }

    private void handleHeroStatusChangeRequired(final HeroDTO clickedHeroDTO)
    {
        if (!clickedHeroDTO.active)
        {
            heroAlertDialogUtil.popAlertFollowHero(getActivity(),
                    new DialogInterface.OnClickListener()
                    {
                        @Override public void onClick(DialogInterface dialog, int which)
                        {
                            premiumFollowUser(clickedHeroDTO.getBaseKey());
                        }
                    }
            );
        }
        else
        {
            heroAlertDialogUtil.popAlertUnfollowHero(getActivity(),
                    new DialogInterface.OnClickListener()
                    {
                        @Override public void onClick(DialogInterface dialog, int which)
                        {
                            THToast.show(
                                    getString(R.string.manage_heroes_unfollow_progress_message));
                            unfollow(clickedHeroDTO.getBaseKey());
                        }
                    }
            );
        }
    }

    protected void unfollow(@NotNull UserBaseKey userBaseKey)
    {
        detachFollowAssistant();
        simpleFollowUserAssistant = new SimpleFollowUserAssistant(userBaseKey, createPremiumUserFollowedListener());
        simpleFollowUserAssistant.launchUnFollow();
    }

    private void pushTimelineFragment(UserBaseKey userBaseKey)
    {
        Bundle args = new Bundle();
        thRouter.save(args, userBaseKey);
        getDashboardNavigator().pushFragment(PushableTimelineFragment.class, args);
    }

    private void handleGoMostSkilled()
    {
        // TODO this feels HACKy
        //getDashboardNavigator().popFragment();

        // TODO make it go to most skilled
        //getDashboardNavigator().goToTab(DashboardTabType.COMMUNITY);

        LeaderboardDefKey key =
                new LeaderboardDefKey(LeaderboardDefKeyKnowledge.MOST_SKILLED_ID);
        LeaderboardDefDTO dto = leaderboardDefCache.get().get(key);
        Bundle bundle = new Bundle(getArguments());
        if (dto != null)
        {
            LeaderboardMarkUserListFragment.putLeaderboardDefKey(bundle, dto.getLeaderboardDefKey());
        }
        else
        {
            LeaderboardMarkUserListFragment.putLeaderboardDefKey(bundle, new LeaderboardDefKey(LeaderboardDefKeyKnowledge.MOST_SKILLED_ID));
        }
        getDashboardNavigator().pushFragment(LeaderboardMarkUserListFragment.class, bundle);
    }

    public void display(UserProfileDTO userProfileDTO)
    {
        linkWith(userProfileDTO, true);
    }

    abstract protected void display(HeroDTOExtWrapper heroDTOExtWrapper);

    protected void display(List<HeroDTO> heroDTOs)
    {
        linkWith(heroDTOs, true);
    }

    public void linkWith(UserProfileDTO userProfileDTO, boolean andDisplay)
    {
        this.userProfileDTO = userProfileDTO;
        if (andDisplay)
        {
            viewContainer.displayFollowCount(userProfileDTO);
            viewContainer.displayCoinStack(userProfileDTO);
        }
    }

    public void linkWith(List<HeroDTO> heroDTOs, boolean andDisplay)
    {
        this.heroDTOs = heroDTOs;
        heroListAdapter.setItems(this.heroDTOs);
        if (andDisplay)
        {
            displayHeroList();
        }
    }

    //<editor-fold desc="Display methods">
    public void display()
    {
        viewContainer.displayFollowCount(userProfileDTO);
        viewContainer.displayCoinStack(userProfileDTO);
        displayHeroList();
    }

    private void onRefreshCompleted()
    {
        if (viewContainer.pullToRefreshListView != null)
        {
            viewContainer.pullToRefreshListView.onRefreshComplete();
        }
    }

    private void enablePullToRefresh(boolean enable)
    {
        if (viewContainer.pullToRefreshListView != null)
        {
            if (!enable)
            {
                viewContainer.pullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
            else
            {
                viewContainer.pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            }
        }
    }

    private void displayHeroList()
    {
        if (heroListAdapter != null)
        {
            heroListAdapter.notifyDataSetChanged();
        }
    }

    private void displayProgress(boolean running)
    {
        if (viewContainer.progressBar != null)
        {
            viewContainer.progressBar.setVisibility(running ? View.VISIBLE : View.GONE);
        }
    }

    @Override public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
    {
        Timber.d("onPullDownToRefresh");
        refreshContent();
    }

    @Override public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
    {
        Timber.d("onPullUpToRefresh");
    }
    //</editor-fold>

    private HeroListMostSkilledClickedListener createHeroListMostSkiledClickedListener()
    {
        return new HeroListMostSkilledClickedListener();
    }

    private class HeroManagerUserProfileCacheListener
            implements DTOCacheNew.Listener<UserBaseKey, UserProfileDTO>
    {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull UserProfileDTO value)
        {
            if (key.equals(HeroesTabContentFragment.this.followerId))
            {
                display(value);
            }
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            Timber.e("Could not fetch user profile", error);
            THToast.show(R.string.error_fetch_user_profile);
        }
    }

    private class HeroManagerHeroListCacheListener
            implements DTOCacheNew.Listener<UserBaseKey, HeroDTOExtWrapper>
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull HeroDTOExtWrapper value)
        {
            //displayProgress(false);
            onRefreshCompleted();
            setListShown(true);
            display(value);
            enablePullToRefresh(true);
            notifyHeroesLoaded(value);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            displayProgress(false);
            setListShown(true);
            enablePullToRefresh(true);
            Timber.e(error, "Could not fetch heroes");
            THToast.show(R.string.error_fetch_hero);
        }
    }

    private class HeroListMostSkilledClickedListener implements View.OnClickListener
    {
        @Override public void onClick(View view)
        {
            handleGoMostSkilled();
        }
    }

    private void notifyHeroesLoaded(HeroDTOExtWrapper value)
    {
        OnHeroesLoadedListener listener =
                FragmentUtils.getParent(this, OnHeroesLoadedListener.class);
        if (listener != null && !isDetached())
        {
            listener.onHerosLoaded(getHeroTypeResource(), value);
        }
    }
}
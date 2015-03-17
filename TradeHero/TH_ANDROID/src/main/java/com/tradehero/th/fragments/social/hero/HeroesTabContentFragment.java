package com.tradehero.th.fragments.social.hero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshBase;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.social.HeroDTO;
import com.tradehero.th.api.social.HeroDTOExtWrapper;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.social.FragmentUtils;
import com.tradehero.th.models.social.follower.HeroTypeResourceDTO;
import com.tradehero.th.models.social.follower.HeroTypeResourceDTOFactory;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefCache;
import com.tradehero.th.persistence.social.HeroType;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.List;

abstract public class HeroesTabContentFragment extends DashboardFragment
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

    @Inject protected HeroManagerInfoFetcher infoFetcher;
    @Inject public HeroAlertDialogUtil heroAlertDialogUtil;
    /** when no heroes */
    @Inject Lazy<LeaderboardDefCache> leaderboardDefCache;
    @Inject HeroTypeResourceDTOFactory heroTypeResourceDTOFactory;
    @Inject CurrentUserId currentUserId;
    @Inject THRouter thRouter;

    public static void putFollowerId(Bundle args, UserBaseKey followerId)
    {
        args.putBundle(BUNDLE_KEY_FOLLOWER_ID, followerId.getArgs());
    }

    @NotNull public static UserBaseKey getFollowerId(@NotNull Bundle args)
    {
        return new UserBaseKey(args.getBundle(BUNDLE_KEY_FOLLOWER_ID));
    }

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

    protected void initViews(View view)
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


    private void handleBuyMoreClicked()
    {
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
    }

    private void pushTimelineFragment(UserBaseKey userBaseKey)
    {
    }

    private void handleGoMostSkilled()
    {
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
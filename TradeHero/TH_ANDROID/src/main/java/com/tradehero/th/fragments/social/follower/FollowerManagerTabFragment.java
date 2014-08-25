package com.tradehero.th.fragments.social.follower;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.activities.DashboardActivity;
import com.tradehero.th.api.social.FollowerSummaryDTO;
import com.tradehero.th.api.social.UserFollowerDTO;
import com.tradehero.th.api.social.key.FollowerHeroRelationId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.social.FragmentUtils;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.models.social.follower.HeroTypeResourceDTO;
import com.tradehero.th.models.social.follower.HeroTypeResourceDTOFactory;
import com.tradehero.th.persistence.social.HeroType;
import com.tradehero.th.utils.route.THRouter;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

abstract public class FollowerManagerTabFragment extends BasePurchaseManagerFragment
        implements PullToRefreshBase.OnRefreshListener2<ListView>
{
    public static final int ITEM_ID_REFRESH_MENU = 0;
    private static final String HERO_ID_BUNDLE_KEY =
            FollowerManagerTabFragment.class.getName() + ".heroId";

    @Inject protected CurrentUserId currentUserId;
    @Inject protected HeroTypeResourceDTOFactory heroTypeResourceDTOFactory;
    private FollowerManagerViewContainer viewContainer;
    private FollowerAndPayoutListItemAdapter followerListAdapter;
    private UserBaseKey heroId;
    private FollowerSummaryDTO followerSummaryDTO;
    private FollowerManagerInfoFetcher infoFetcher;
    @Inject THRouter thRouter;

    public static void putHeroId(Bundle args, UserBaseKey followerId)
    {
        args.putBundle(HERO_ID_BUNDLE_KEY, followerId.getArgs());
    }

    public static UserBaseKey getHeroId(Bundle args)
    {
        return new UserBaseKey(args.getBundle(HERO_ID_BUNDLE_KEY));
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view =
                inflater.inflate(R.layout.fragment_store_manage_followers, container, false);
        initViews(view);

        return view;
    }

    @Override protected void initViews(View view)
    {
        viewContainer = new FollowerManagerViewContainer(view);

        if (followerListAdapter == null)
        {
            followerListAdapter = new FollowerAndPayoutListItemAdapter(getActivity(),
                    getActivity().getLayoutInflater(),
                    R.layout.follower_list_header,
                    R.layout.hero_payout_list_item,
                    R.layout.hero_payout_none_list_item,
                    R.layout.follower_list_item,
                    R.layout.follower_none_list_item
            );
        }

        if (viewContainer.pullToRefreshListView != null)
        {
            viewContainer.pullToRefreshListView.setOnRefreshListener(this);
            viewContainer.followerList.setAdapter(followerListAdapter);
        }

        if (viewContainer.followerList != null)
        {
            viewContainer.followerList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id)
                        {
                            ListView listView = (ListView)parent;
                            handleFollowerItemClicked(view, position - listView.getHeaderViewsCount(), id);
                        }
                    }
            );
            viewContainer.followerList.setAdapter(followerListAdapter);
        }
        displayProgress(true);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        setActionBarTitle(getTitle());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        Timber.d("onOptionsItemSelected");
        if (item.getItemId() == ITEM_ID_REFRESH_MENU)
        {
            refreshContent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onResume()
    {
        super.onResume();

        Timber.d("FollowerManagerTabFragment onResume");
        heroId = getHeroId(getArguments());

        fetchFollowers();
    }

    @Override public void onStop()
    {
        detachInfoFetcher();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        if (this.viewContainer.followerList != null)
        {
            this.viewContainer.followerList.setOnItemClickListener(null);
        }
        this.viewContainer = null;
        this.followerListAdapter = null;
        super.onDestroyView();
    }

    protected void detachInfoFetcher()
    {
        if (this.infoFetcher != null)
        {
            this.infoFetcher.onDestroyView();
        }
        this.infoFetcher = null;
    }

    protected void fetchFollowers()
    {
        detachInfoFetcher();
        infoFetcher = new FollowerManagerInfoFetcher(createFollowerSummaryCacheListener());
        infoFetcher.fetch(this.heroId);
    }

    private boolean isCurrentUser()
    {
        UserBaseKey heroId = getHeroId(getArguments());
        if (heroId != null && heroId.key != null && currentUserId != null)
        {
            return (heroId.key.intValue() == currentUserId.toUserBaseKey().key.intValue());
        }
        return false;
    }

    private int getTitle()
    {
        if (isCurrentUser())
        {
            return R.string.manage_my_followers_title;
        }
        else
        {
            return R.string.manage_followers_title;
        }

    }

    protected HeroTypeResourceDTO getHeroTypeResource()
    {
        return heroTypeResourceDTOFactory.create(getFollowerType());
    }

    abstract protected HeroType getFollowerType();

    abstract protected void handleFollowerSummaryDTOReceived(FollowerSummaryDTO fromServer);

    public void display(FollowerSummaryDTO summaryDTO)
    {
        Timber.d("onDTOReceived display followerType:%s,%s", getFollowerType(), summaryDTO);
        linkWith(summaryDTO, true);
    }

    public void linkWith(FollowerSummaryDTO summaryDTO, boolean andDisplay)
    {
        this.followerSummaryDTO = summaryDTO;
        if (andDisplay)
        {
            this.viewContainer.displayTotalRevenue(summaryDTO);
            this.viewContainer.displayTotalAmountPaid(summaryDTO);
            this.viewContainer.displayFollowersCount(summaryDTO);
            displayFollowerList();
        }
    }

    public void display()
    {
        this.viewContainer.displayTotalRevenue(this.followerSummaryDTO);
        this.viewContainer.displayTotalAmountPaid(this.followerSummaryDTO);
        this.viewContainer.displayFollowersCount(this.followerSummaryDTO);
        displayFollowerList();
    }

    public void displayFollowerList()
    {
        if (this.followerListAdapter != null)
        {
            this.followerListAdapter.setFollowerSummaryDTO(this.followerSummaryDTO);
        }
    }

    private void redisplayProgress()
    {
        if (this.viewContainer.progressBar != null)
        {
            this.viewContainer.progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void displayProgress(boolean running)
    {
        if (this.viewContainer.progressBar != null)
        {
            this.viewContainer.progressBar.setVisibility(running ? View.VISIBLE : View.GONE);
        }
        if (this.viewContainer.followerList != null)
        {
            this.viewContainer.followerList.setVisibility(running ? View.GONE : View.VISIBLE);
        }
    }

    @Override public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
    {
        if(followerSummaryDTO == null || followerSummaryDTO.userFollowers == null || followerSummaryDTO.userFollowers.size() == 0)
        {
            displayProgress(true);
        }

        doRefreshContent();
    }

    @Override public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
    {

    }

    private void onRefreshCompleted()
    {
        if (this.viewContainer != null && this.viewContainer.pullToRefreshListView != null)
        {
            viewContainer.pullToRefreshListView.onRefreshComplete();
        }
    }

    private void refreshContent()
    {
        Timber.d("refreshContent");
        redisplayProgress();
        doRefreshContent();
    }

    private void doRefreshContent()
    {
        Timber.d("refreshContent");

        if (heroId == null)
        {
            heroId = getHeroId(getArguments());
        }
        detachInfoFetcher();
        infoFetcher = new FollowerManagerInfoFetcher(createFollowerSummaryCacheRefreshListener());
        infoFetcher.fetch(this.heroId,true);
    }

    private void pushTimelineFragment(int followerId)
    {
        DashboardNavigator navigator = ((DashboardNavigatorActivity) getActivity()).getDashboardNavigator();

        Bundle bundle = new Bundle();
        thRouter.save(bundle, new UserBaseKey(followerId));
        navigator.pushFragment(PushableTimelineFragment.class, bundle);
    }

    private void pushPayoutFragment(UserFollowerDTO followerDTO)
    {
        FollowerHeroRelationId followerHeroRelationId =
                new FollowerHeroRelationId(getApplicablePortfolioId().userId,
                        followerDTO.id, followerDTO.displayName);
        Bundle args = new Bundle();
        args.putBundle(FollowerPayoutManagerFragment.BUNDLE_KEY_FOLLOWER_ID_BUNDLE,
                followerHeroRelationId.getArgs());
        ((DashboardActivity) getActivity()).getDashboardNavigator()
                .pushFragment(FollowerPayoutManagerFragment.class, args);
    }

    private void handleFollowerItemClicked(View view, int position, long id)
    {

        if (followerListAdapter != null
                && followerListAdapter.getItemViewType(position)
                == FollowerAndPayoutListItemAdapter.VIEW_TYPE_ITEM_FOLLOWER)
        {
            UserFollowerDTO followerDTO =
                    (UserFollowerDTO) followerListAdapter.getItem(position);
            if (followerDTO != null)
            {
                if (isCurrentUser())
                {
                    pushPayoutFragment(followerDTO);
                }
                else
                {
                    pushTimelineFragment(followerDTO.id);
                }
            }
            else
            {
                Timber.d("handleFollowerItemClicked: FollowerDTO was null");
            }
        }
        else
        {
            Timber.d("Position clicked ", position);
            //THToast.show("Position clicked " + position);
        }
    }

    protected DTOCacheNew.Listener<UserBaseKey, FollowerSummaryDTO> createFollowerSummaryCacheListener()
    {
        return new FollowerManagerFollowerSummaryListener();
    }

    protected DTOCacheNew.Listener<UserBaseKey, FollowerSummaryDTO> createFollowerSummaryCacheRefreshListener()
    {
        return new RefreshFollowerManagerFollowerSummaryListener();
    }

    protected class FollowerManagerFollowerSummaryListener
            implements DTOCacheNew.Listener<UserBaseKey, FollowerSummaryDTO>
    {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull FollowerSummaryDTO value)
        {
            Timber.d("onDTOReceived");

            displayProgress(false);
            handleFollowerSummaryDTOReceived(value);
            notifyFollowerLoaded(value);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            displayProgress(false);
            THToast.show(R.string.error_fetch_follower);
            Timber.e("Failed to fetch FollowerSummary", error);
        }
    }

    protected class RefreshFollowerManagerFollowerSummaryListener
            implements DTOCacheNew.Listener<UserBaseKey, FollowerSummaryDTO>
    {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull FollowerSummaryDTO value)
        {
            displayProgress(false);
            onRefreshCompleted();
            handleFollowerSummaryDTOReceived(value);
            notifyFollowerLoaded(value);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            displayProgress(false);
            onRefreshCompleted();
            //THToast.show(R.string.error_fetch_follower);
            Timber.e("Failed to fetch FollowerSummary", error);
        }
    }

    private void notifyFollowerLoaded(FollowerSummaryDTO value)
    {
        Timber.d("notifyFollowerLoaded for followerTabIndex:%d",
                getHeroTypeResource().followerTabIndex);
        OnFollowersLoadedListener loadedListener =
                FragmentUtils.getParent(this, OnFollowersLoadedListener.class);
        if (loadedListener != null && !isDetached())
        {
            loadedListener.onFollowerLoaded(getHeroTypeResource().followerTabIndex, value);
        }
    }
}
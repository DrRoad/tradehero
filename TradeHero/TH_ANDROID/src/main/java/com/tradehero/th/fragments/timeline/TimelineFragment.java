package com.tradehero.th.fragments.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import pulltorefresh.PullToRefreshBase;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.route.InjectRoute;
import com.tradehero.th.R;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.discussion.MessageHeaderDTO;
import com.tradehero.th.api.discussion.key.DiscussionKeyFactory;
import com.tradehero.th.api.portfolio.DisplayablePortfolioDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.portfolio.PortfolioCompactDTOList;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.social.FollowerSummaryDTO;
import com.tradehero.th.api.social.UserFollowerDTO;
import com.tradehero.th.api.timeline.TimelineItemDTO;
import com.tradehero.th.api.timeline.key.TimelineItemDTOKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseDTOUtil;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.UserProfileDTOUtil;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.discussion.TimelineDiscussionFragment;
import com.tradehero.th.fragments.position.CompetitionLeaderboardPositionListFragment;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerInfoFetcher;
import com.tradehero.th.fragments.social.hero.HeroAlertDialogUtil;
import com.tradehero.th.fragments.social.hero.HeroManagerFragment;
import com.tradehero.th.fragments.social.message.NewPrivateMessageFragment;
import com.tradehero.th.fragments.social.message.ReplyPrivateMessageFragment;
import com.tradehero.th.fragments.watchlist.WatchlistPositionFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.portfolio.DisplayablePortfolioFetchAssistant;
import com.tradehero.th.models.social.FollowDialogCombo;
import com.tradehero.th.models.social.OnFollowRequestedListener;
import com.tradehero.th.models.user.PremiumFollowUserAssistant;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.message.MessageThreadHeaderCache;
import com.tradehero.th.persistence.portfolio.PortfolioCache;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class TimelineFragment extends BasePurchaseManagerFragment
        implements UserProfileCompactViewHolder.OnProfileClickedListener
{
    private static final String USER_BASE_KEY_BUNDLE_KEY = TimelineFragment.class.getName() + ".userBaseKey";

    public static void putUserBaseKey(Bundle bundle, UserBaseKey userBaseKey)
    {
        bundle.putBundle(USER_BASE_KEY_BUNDLE_KEY, userBaseKey.getArgs());
    }
    @Nullable protected static UserBaseKey getUserBaseKey(@NotNull Bundle args)
    {
        if (args.containsKey(USER_BASE_KEY_BUNDLE_KEY))
        {
            return new UserBaseKey(args.getBundle(USER_BASE_KEY_BUNDLE_KEY));
        }
        return null;
    }

    public static enum TabType
    {
        TIMELINE, PORTFOLIO_LIST, STATS
    }

    @Inject DiscussionKeyFactory discussionKeyFactory;
    @Inject Lazy<HeroAlertDialogUtil> heroAlertDialogUtilLazy;
    @Inject Lazy<CurrentUserId> currentUserIdLazy;
    @Inject Lazy<PortfolioCache> portfolioCache;
    @Inject Lazy<PortfolioCompactListCache> portfolioCompactListCache;
    @Inject Lazy<UserProfileCache> userProfileCache;
    @Inject Lazy<UserServiceWrapper> userServiceWrapperLazy;
    @Inject MessageThreadHeaderCache messageThreadHeaderCache;
    @Inject Provider<DisplayablePortfolioFetchAssistant> displayablePortfolioFetchAssistantProvider;
    @Inject protected THRouter thRouter;
    @Inject UserBaseDTOUtil userBaseDTOUtil;

    @InjectView(R.id.timeline_list_view) TimelineListView timelineListView;
    @InjectView(R.id.timeline_screen) BetterViewAnimator timelineScreen;
    @InjectView(R.id.follow_button) Button mFollowButton;
    @InjectView(R.id.message_button) Button mSendMsgButton;

    @InjectRoute UserBaseKey shownUserBaseKey;

    protected DTOCacheNew.Listener<UserBaseKey, MessageHeaderDTO> messageThreadHeaderFetchListener;
    protected FollowDialogCombo followDialogCombo;
    protected FollowerManagerInfoFetcher infoFetcher;
    protected List<PortfolioCompactDTO> portfolioCompactDTOs;
    protected MessageHeaderDTO messageThreadHeaderDTO;
    protected UserProfileDTO shownProfile;
    private DisplayablePortfolioFetchAssistant displayablePortfolioFetchAssistant;
    private MainTimelineAdapter mainTimelineAdapter;
    private DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> userProfileCacheListener;
    private MiddleCallback<UserProfileDTO> freeFollowMiddleCallback;
    private PullToRefreshBase.OnLastItemVisibleListener lastItemVisibleListener;
    private UserProfileView userProfileView;
    private View loadingView;

    public TabType currentTab = TabType.TIMELINE;
    protected boolean mIsOtherProfile = false;
    private boolean cancelRefreshingOnResume;
    private int displayingProfileHeaderLayoutId;
    //TODO need move to pushableTimelineFragment
    private int mFollowType;//0 not follow, 1 free follow, 2 premium follow
    private boolean mIsHero = false;//whether the showUser follow the user

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        userProfileCacheListener = createUserProfileCacheListener();
        messageThreadHeaderFetchListener = createMessageThreadHeaderCacheListener();
    }

    @Override protected PremiumFollowUserAssistant.OnUserFollowedListener createPremiumUserFollowedListener()
    {
        return new TimelinePremiumUserFollowedListener();
    }

    @Override protected Callback<UserProfileDTO> createFreeUserFollowedCallback()
    {
        return new FreeUserFollowedCallback();
    }

    protected PremiumFollowUserAssistant.OnUserFollowedListener createPremiumUserFollowedForMessageListener()
    {
        return new TimelinePremiumUserForMessageFollowedListener();
    }

    protected OnFollowRequestedListener createFollowRequestedListener()
    {
        return new TimelineFollowRequestedListener();
    }

    protected OnFollowRequestedListener createFollowForMessageRequestedListener()
    {
        return new TimelineFollowForMessageRequestedListener();
    }

    protected Callback<UserProfileDTO> createFreeFollowForMessageCallback()
    {
        return new FreeUserFollowedForMessageCallback();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.timeline_screen, container, false);
        userProfileView = (UserProfileView) inflater.inflate(R.layout.user_profile_view, null);

        loadingView = new ProgressBar(getActivity());

        ButterKnife.inject(this, view);
        initViews(view);
        return view;
    }

    @Override public void onHeroClicked()
    {
        pushHeroFragment();
    }

    @Override public void onFollowerClicked()
    {
        pushFollowerFragment();
    }

    @Override public void onDefaultPortfolioClicked()
    {
        if (portfolioCompactDTOs == null || portfolioCompactDTOs.size() < 1 || portfolioCompactDTOs.get(0) == null)
        {
            // HACK, instead we should test for Default title on PortfolioDTO
            THToast.show("Not enough data, try again");
        }
        else if (shownUserBaseKey == null)
        {
            Timber.e(new NullPointerException("shownUserBaseKey is null"), "");
        }
        else if (portfolioCompactListCache == null)
        {
            Timber.e(new NullPointerException("portfolioCompactListCache is null"), "");
        }
        else if (portfolioCompactListCache.get() == null)
        {
            Timber.e(new NullPointerException("portfolioCompactListCache.get() is null"), "");
        }
        else
        {
            @Nullable PortfolioCompactDTO defaultPortfolio = portfolioCompactListCache.get().getDefaultPortfolio(shownUserBaseKey);
            if (defaultPortfolio != null)
            {
                pushPositionListFragment(new OwnedPortfolioId(
                        shownUserBaseKey.key,
                        defaultPortfolio.id));
            }
        }
    }

    protected void pushHeroFragment()
    {
        Bundle bundle = new Bundle();
        HeroManagerFragment.putFollowerId(
                bundle,
                mIsOtherProfile ? shownUserBaseKey : currentUserId.toUserBaseKey());
        OwnedPortfolioId applicablePortfolio = getApplicablePortfolioId();
        if (applicablePortfolio != null)
        {
            HeroManagerFragment.putApplicablePortfolioId(bundle, applicablePortfolio);
        }
        getDashboardNavigator().pushFragment(HeroManagerFragment.class, bundle);
    }

    protected void pushFollowerFragment()
    {
        Bundle bundle = new Bundle();
        FollowerManagerFragment.putHeroId(
                bundle,
                mIsOtherProfile ? shownUserBaseKey : currentUserId.toUserBaseKey());
        OwnedPortfolioId applicablePortfolio = getApplicablePortfolioId();
        if (applicablePortfolio != null)
        {
            //FollowerManagerFragment.putApplicablePortfolioId(bundle, applicablePortfolio);
        }
        getDashboardNavigator().pushFragment(FollowerManagerFragment.class, bundle);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        displayActionBarTitle();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override protected void initViews(View view)
    {
        if (userProfileView != null)
        {
            //TODO now only one view, userProfileView useless, need cancel, alex

            userProfileView.setProfileClickedListener(this);
            timelineListView.getRefreshableView().addHeaderView(userProfileView);
        }

        if (loadingView != null)
        {
            timelineListView.addFooterView(loadingView);
        }

        displayablePortfolioFetchAssistant = displayablePortfolioFetchAssistantProvider.get();
        displayablePortfolioFetchAssistant.setFetchedListener(
                new DisplayablePortfolioFetchAssistant.OnFetchedListener()
                {
                    @Override public void onFetched()
                    {
                        onLoadFinished();
                        displayPortfolios();
                    }
                });

        lastItemVisibleListener = new TimelineLastItemVisibleListener();
    }

    private class FollowerSummaryListener implements DTOCacheNew.Listener<UserBaseKey, FollowerSummaryDTO>
    {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull FollowerSummaryDTO value)
        {
            updateHeroType(value);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            THToast.show(error.getMessage());
        }
    }

    private void updateHeroType(FollowerSummaryDTO value)
    {
        if (value != null && value.userFollowers.size() > 0)
        {
            for (UserFollowerDTO userFollowerDTO : value.userFollowers)
            {
                if (userFollowerDTO.id == shownUserBaseKey.key)
                {
                    mIsHero = true;
                    return;
                }
            }
        }
        mIsHero = false;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        shownUserBaseKey = getUserBaseKey(getArguments());
        if (shownUserBaseKey == null)
        {
            thRouter.inject(this);
        }
        //create adapter and so on
        linkWith(shownUserBaseKey, true);

        getActivity().getSupportLoaderManager().initLoader(
                mainTimelineAdapter.getTimelineLoaderId(), null,
                mainTimelineAdapter.getLoaderTimelineCallback());
    }

    @Override public void onResume()
    {
        super.onResume();
        if (userProfileView != null && displayingProfileHeaderLayoutId != 0)
        {
            userProfileView.setDisplayedChildByLayoutId(displayingProfileHeaderLayoutId);
        }

        if (cancelRefreshingOnResume)
        {
            timelineListView.onRefreshComplete();
            cancelRefreshingOnResume = false;
        }
        displayablePortfolioFetchAssistant.fetch(getUserBaseKeys());

        fetchMessageThreadHeader();

        displayTab();
    }

    @Override public void onPause()
    {
        if (userProfileView != null)
        {
            displayingProfileHeaderLayoutId = userProfileView.getDisplayedChildLayoutId();
        }
        super.onPause();
    }

    @Override public void onStop()
    {
        detachUserProfileCache();
        detachTimelineAdapter();
        detachTimelineListView();
        detachFreeFollowMiddleCallback();
        detachMessageThreadHeaderFetchTask();
        detachFollowDialogCombo();
        destroyInfoFetcher();

        displayablePortfolioFetchAssistant.setFetchedListener(null);
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        displayablePortfolioFetchAssistant = null;

        if (userProfileView != null)
        {
            userProfileView.setProfileClickedListener(null);
        }
        this.userProfileView = null;
        this.loadingView = null;
        timelineListView = null;
        lastItemVisibleListener = null;
        mainTimelineAdapter = null;

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        messageThreadHeaderFetchListener = null;
        userProfileCacheListener = null;
        super.onDestroy();
    }

    protected void detachTimelineAdapter()
    {
        if (mainTimelineAdapter != null)
        {
            mainTimelineAdapter.setProfileClickListener(null);
            mainTimelineAdapter.setOnLoadFinishedListener(null);
        }
    }

    protected void detachTimelineListView()
    {
        if (timelineListView != null)
        {
            timelineListView.setOnRefreshListener((MainTimelineAdapter) null);
            timelineListView.setOnScrollListener(null);
            timelineListView.setOnLastItemVisibleListener(null);
        }
    }

    protected void detachUserProfileCache()
    {
        userProfileCache.get().unregister(userProfileCacheListener);
    }

    private void detachFreeFollowMiddleCallback()
    {
        if (freeFollowMiddleCallback != null)
        {
            freeFollowMiddleCallback.setPrimaryCallback(null);
        }
        freeFollowMiddleCallback = null;
    }

    private void detachMessageThreadHeaderFetchTask()
    {
        messageThreadHeaderCache.unregister(messageThreadHeaderFetchListener);
    }

    protected void detachFollowDialogCombo()
    {
        FollowDialogCombo followDialogComboCopy = followDialogCombo;
        if (followDialogComboCopy != null)
        {
            followDialogComboCopy.followDialogView.setFollowRequestedListener(null);
        }
        followDialogCombo = null;
    }

    protected void destroyInfoFetcher()
    {
        FollowerManagerInfoFetcher infoFetcherCopy = infoFetcher;
        if (infoFetcherCopy != null)
        {
            infoFetcherCopy.onDestroyView();
        }
        infoFetcher = null;
    }

    protected void fetchMessageThreadHeader()
    {
        detachMessageThreadHeaderFetchTask();
        messageThreadHeaderCache.register(shownUserBaseKey, messageThreadHeaderFetchListener);
        messageThreadHeaderCache.getOrFetchAsync(shownUserBaseKey);
    }

    //<editor-fold desc="Display methods">
    protected void linkWith(@NotNull UserBaseKey userBaseKey, final boolean andDisplay)
    {
        this.shownUserBaseKey = userBaseKey;

        prepareTimelineAdapter(userBaseKey);

        fetchUserProfile(false);

        destroyInfoFetcher();
        infoFetcher = new FollowerManagerInfoFetcher(new FollowerSummaryListener());
        infoFetcher.fetch(currentUserIdLazy.get().toUserBaseKey());

        if (andDisplay)
        {
            displayTab();
        }
    }

    private void refreshPortfolioList()
    {
        portfolioCompactListCache.get().invalidate(shownUserBaseKey);
        displayablePortfolioFetchAssistant.fetch(getUserBaseKeys());
    }

    private AdapterView.OnItemClickListener createTimelineOnClickListener()
    {
        return new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object item = parent.getItemAtPosition(position);

                if (item instanceof TimelineItemDTO)
                {
                    pushDiscussion(((TimelineItemDTO) item).getDiscussionKey());
                }
            }
        };
    }

    private void pushDiscussion(TimelineItemDTOKey timelineItemDTOKey)
    {
        Bundle bundle = new Bundle();
        TimelineDiscussionFragment.putDiscussionKey(bundle, timelineItemDTOKey);
        getDashboardNavigator().pushFragment(TimelineDiscussionFragment.class, bundle);
    }

    protected void linkWith(UserProfileDTO userProfileDTO, boolean andDisplay)
    {
        this.shownProfile = userProfileDTO;
        mainTimelineAdapter.setUserProfileDTO(userProfileDTO);
        if (andDisplay)
        {
            updateView();
        }
    }

    private void linkWith(List<PortfolioCompactDTO> portfolioCompactDTOs, boolean andDisplay)
    {
        this.portfolioCompactDTOs = portfolioCompactDTOs;
        if (portfolioCompactDTOs != null)
        {
            for(PortfolioCompactDTO portfolioCompactDTO: portfolioCompactDTOs)
            {
                portfolioCache.get().getOrFetchAsync(new OwnedPortfolioId(shownUserBaseKey.key, portfolioCompactDTO.id));
            }
        }

        if (andDisplay)
        {
            // Nothing to do
        }
    }

    protected void linkWith(TabType tabType, boolean andDisplay)
    {
        currentTab = tabType;
        if (andDisplay)
        {
            displayTab();
        }
    }

    protected void linkWithMessageThread(MessageHeaderDTO messageHeaderDTO, boolean andDisplay)
    {
        this.messageThreadHeaderDTO = messageHeaderDTO;
        if (andDisplay)
        {
        }
    }

    protected void updateView()
    {
        if (timelineScreen != null)
        {
            timelineScreen.setDisplayedChildByLayoutId(R.id.timeline_list_view_container);
        }
        if (userProfileView != null)
        {
            userProfileView.display(shownProfile);
        }
        displayActionBarTitle();
    }

    public void displayTab()
    {
        mainTimelineAdapter.setCurrentTabType(currentTab);
    }
    //</editor-fold>

    protected void displayActionBarTitle()
    {
        if (shownProfile != null)
        {
            setActionBarTitle(userBaseDTOUtil.getLongDisplayName(getActivity(), shownProfile));
        }
        else
        {
            setActionBarTitle(R.string.loading_loading);
        }
    }

    //<editor-fold desc="Init milestones">
    protected void fetchUserProfile(boolean force)
    {
        detachUserProfileCache();
        userProfileCache.get().register(shownUserBaseKey, userProfileCacheListener);
        userProfileCache.get().getOrFetchAsync(shownUserBaseKey, force);
    }
    //</editor-fold>

    //<editor-fold desc="Initial methods">
    private MainTimelineAdapter createTimelineAdapter(@NotNull UserBaseKey shownUserBaseKey)
    {
        return new MainTimelineAdapter(getActivity(), getActivity().getLayoutInflater(),
                shownUserBaseKey, R.layout.timeline_item_view, R.layout.portfolio_list_item_2_0,
                R.layout.user_profile_stat_view);
        // TODO set the layouts
    }

    private void prepareTimelineAdapter(@NotNull final UserBaseKey shownUserBaseKey)
    {
        mainTimelineAdapter = createTimelineAdapter(shownUserBaseKey);
        mainTimelineAdapter.setProfileClickListener(createTimelineProfileClickListener());
        mainTimelineAdapter.setOnLoadFinishedListener(
                new MainTimelineAdapter.OnLoadFinishedListener()
                {
                    @Override public void onLoadFinished()
                    {
                        TimelineFragment.this.onLoadFinished();
                    }

                    @Override public void onBeginRefresh(TabType tabType)
                    {
                        refreshPortfolioList();
                        fetchUserProfile(true);
                    }
                });
        timelineListView.setOnRefreshListener(mainTimelineAdapter);
        timelineListView.setOnLastItemVisibleListener(lastItemVisibleListener);
        //timelineListView.setRefreshing();
        timelineListView.setAdapter(mainTimelineAdapter);
        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                onMainItemClick(adapterView, view, i, l);
            }
        });
    }
    //</editor-fold>
    /**item of Portfolio tab is clicked*/
    private void onMainItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        Object item = adapterView.getItemAtPosition(i);
        if (item instanceof DisplayablePortfolioDTO)
        {
            DisplayablePortfolioDTO displayablePortfolioDTO = (DisplayablePortfolioDTO) item;
            if (displayablePortfolioDTO.portfolioDTO != null)
            {
                if (displayablePortfolioDTO.portfolioDTO.isWatchlist)
                {
                    pushWatchlistPositionFragment(displayablePortfolioDTO.ownedPortfolioId);
                }
                else
                {
                    pushPositionListFragment(displayablePortfolioDTO.ownedPortfolioId, displayablePortfolioDTO.portfolioDTO);
                }
            }
        }
        else
        {
            Timber.d("TimelineFragment, unhandled view %s", view);
        }
    }

    private void pushPositionListFragment(OwnedPortfolioId ownedPortfolioId)
    {
        pushPositionListFragment(ownedPortfolioId, null);
    }

    /**
     *
     * @param ownedPortfolioId
     * @param portfolioDTO
     */
    private void pushPositionListFragment(OwnedPortfolioId ownedPortfolioId, @Nullable PortfolioDTO portfolioDTO)
    {
        Bundle args = new Bundle();

        PositionListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
        PositionListFragment.putGetPositionsDTOKey(args, ownedPortfolioId);
        PositionListFragment.putShownUser(args, ownedPortfolioId.getUserBaseKey());
        DashboardNavigator navigator =
                ((DashboardNavigatorActivity) getActivity()).getDashboardNavigator();

        if (portfolioDTO != null && portfolioDTO.providerId != null && portfolioDTO.providerId > 0)
        {
            CompetitionLeaderboardPositionListFragment.putProviderId(args, new ProviderId(portfolioDTO.providerId));
            navigator.pushFragment(CompetitionLeaderboardPositionListFragment.class, args);
        }
        else
        {
            navigator.pushFragment(PositionListFragment.class, args);
        }
    }

    /**
     * Go to watchlist
     * @param ownedPortfolioId
     */
    private void pushWatchlistPositionFragment(OwnedPortfolioId ownedPortfolioId)
    {
        Bundle args = new Bundle();
        WatchlistPositionFragment.putOwnedPortfolioId(args, ownedPortfolioId);
        DashboardNavigator navigator =
                ((DashboardNavigatorActivity) getActivity()).getDashboardNavigator();
        navigator.pushFragment(WatchlistPositionFragment.class, args);
    }

    private void onLoadFinished()
    {
        if (timelineListView != null)
        {
            timelineListView.onRefreshComplete();
            loadingView.setVisibility(View.GONE);
            cancelRefreshingOnResume = true;
        }
    }

    protected DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> createUserProfileCacheListener()
    {
        return new TimelineFragmentUserProfileCacheListener();
    }

    protected class TimelineFragmentUserProfileCacheListener implements DTOCacheNew.Listener<UserBaseKey, UserProfileDTO>
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull UserProfileDTO value)
        {
            if (currentTab == TabType.STATS)
            {
                onLoadFinished();
            }
            linkWith(value, true);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            THToast.show(getString(R.string.error_fetch_user_profile));
        }
    }

    @Override protected DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList> createPortfolioCompactListFetchListener()
    {
        return new TimelineFragmentPortfolioCompactListFetchListener();
    }

    protected class TimelineFragmentPortfolioCompactListFetchListener extends BasePurchaseManagementPortfolioCompactListFetchListener
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull PortfolioCompactDTOList value)
        {
            super.onDTOReceived(key, value);
            linkWith(value, true);
        }
    }

    protected List<UserBaseKey> getUserBaseKeys()
    {
        List<UserBaseKey> list = new ArrayList<>();
        list.add(shownUserBaseKey);
        return list;
    }

    public void displayPortfolios()
    {
        this.mainTimelineAdapter.setDisplayablePortfolioItems(getAllPortfolios());
    }

    private List<DisplayablePortfolioDTO> getAllPortfolios()
    {
        if (displayablePortfolioFetchAssistant != null)
        {
            return displayablePortfolioFetchAssistant.getDisplayablePortfolios();
        }
        return null;
    }

    protected void updateBottomButton()
    {
        if (!mIsOtherProfile)
        {
            return;
        }
        mFollowType = getFollowType();
        if (mFollowType == UserProfileDTOUtil.IS_FREE_FOLLOWER)
        {
            mFollowButton.setText(R.string.upgrade_to_premium);
        }
        else if (mFollowType == UserProfileDTOUtil.IS_PREMIUM_FOLLOWER)
        {
            mFollowButton.setText(R.string.following_premium);
        }
        mFollowButton.setVisibility(View.VISIBLE);
        mFollowButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                detachFollowDialogCombo();
                followDialogCombo = heroAlertDialogUtilLazy.get().showFollowDialog(getActivity(), shownProfile,
                        mFollowType, createFollowRequestedListener());
            }
        });
        mSendMsgButton.setVisibility(View.VISIBLE);
        mSendMsgButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                if (!mIsHero && (mFollowType == UserProfileDTOUtil.IS_NOT_FOLLOWER
                        || mFollowType == UserProfileDTOUtil.IS_NOT_FOLLOWER_WANT_MSG))
                {
                    detachFollowDialogCombo();
                    followDialogCombo = heroAlertDialogUtilLazy.get().showFollowDialog(getActivity(), shownProfile,
                            UserProfileDTOUtil.IS_NOT_FOLLOWER_WANT_MSG,
                            createFollowForMessageRequestedListener());
                }
                else
                {
                    pushPrivateMessageFragment();
                }
            }
        });
    }

    protected void pushPrivateMessageFragment()
    {
        DashboardNavigator navigator = getDashboardNavigator();
        if (navigator == null)
        {
            return;
        }
        if (messageThreadHeaderDTO != null)
        {
            Bundle args = new Bundle();
            ReplyPrivateMessageFragment.putCorrespondentUserBaseKey(args, shownUserBaseKey);
            ReplyPrivateMessageFragment.putDiscussionKey(args, discussionKeyFactory.create(messageThreadHeaderDTO));
            navigator.pushFragment(NewPrivateMessageFragment.class, args);
        }
        else
        {
            Bundle args = new Bundle();
            NewPrivateMessageFragment.putCorrespondentUserBaseKey(args, shownUserBaseKey);
            navigator.pushFragment(NewPrivateMessageFragment.class, args);
        }
    }

    /**
     * Null means unsure.
     */
    protected int getFollowType()
    {
        UserProfileDTO userProfileDTO =
                userProfileCache.get().get(currentUserIdLazy.get().toUserBaseKey());
        if (userProfileDTO != null)
        {
            OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId();
            if (applicablePortfolioId != null)
            {
                UserBaseKey purchaserKey = applicablePortfolioId.getUserBaseKey();
                if (purchaserKey != null)
                {
                    UserProfileDTO purchaserProfile = userProfileCache.get().get(purchaserKey);
                    if (purchaserProfile != null)
                    {
                        return purchaserProfile.getFollowType(shownUserBaseKey);
                    }
                }
            }
            else
            {
                return userProfileDTO.getFollowType(shownUserBaseKey);
            }
        }
        return 0;
    }

    protected void freeFollow(@NotNull UserBaseKey heroId, @Nullable Callback<UserProfileDTO> followCallback)
    {
        heroAlertDialogUtilLazy.get().showProgressDialog(getActivity(), getString(R.string.following_this_hero));
        detachFreeFollowMiddleCallback();
        freeFollowMiddleCallback =
                userServiceWrapperLazy.get().freeFollow(heroId, followCallback);
    }

    protected TimelineProfileClickListener createTimelineProfileClickListener()
    {
        return new TimelineProfileClickListener()
        {
            @Override public void onBtnClicked(TabType tabType)
            {
                linkWith(tabType, true);
            }
        };
    }

    public class FreeUserFollowedCallback implements Callback<UserProfileDTO>
    {
        @Override public void success(UserProfileDTO userProfileDTO, Response response)
        {
            userProfileCache.get().put(userProfileDTO.getBaseKey(), userProfileDTO);
            heroAlertDialogUtilLazy.get().dismissProgressDialog();
            updateBottomButton();
            //analytics.addEvent(new ScreenFlowEvent(AnalyticsConstants.FreeFollow_Success, AnalyticsConstants.Profile));
        }

        @Override public void failure(RetrofitError retrofitError)
        {
            THToast.show(new THException(retrofitError));
            heroAlertDialogUtilLazy.get().dismissProgressDialog();
        }
    }

    public class FreeUserFollowedForMessageCallback extends FreeUserFollowedCallback
    {
        @Override public void success(UserProfileDTO userProfileDTO, Response response)
        {
            super.success(userProfileDTO, response);
            pushPrivateMessageFragment();
        }
    }

    public class TimelineFollowRequestedListener implements OnFollowRequestedListener
    {
        @Override public void freeFollowRequested(@NotNull UserBaseKey heroId)
        {
            freeFollow(heroId, createFreeUserFollowedCallback());
        }

        @Override public void premiumFollowRequested(@NotNull UserBaseKey heroId)
        {
            premiumFollowUser(heroId);
        }
    }

    public class TimelineFollowForMessageRequestedListener implements OnFollowRequestedListener
    {
        @Override public void freeFollowRequested(@NotNull UserBaseKey heroId)
        {
            freeFollow(heroId, createFreeFollowForMessageCallback());
        }

        @Override public void premiumFollowRequested(@NotNull UserBaseKey heroId)
        {
            premiumFollowUser(heroId);
        }
    }

    protected class TimelinePremiumUserFollowedListener implements PremiumFollowUserAssistant.OnUserFollowedListener
    {
        @Override public void onUserFollowSuccess(UserBaseKey userFollowed,
                UserProfileDTO currentUserProfileDTO)
        {
            if (!mIsOtherProfile)
            {
                linkWith(currentUserProfileDTO, true);
            }
            updateBottomButton();
            //analytics.addEvent(new ScreenFlowEvent(AnalyticsConstants.PremiumFollow_Success, AnalyticsConstants.Profile));
        }

        @Override public void onUserFollowFailed(UserBaseKey userFollowed, Throwable error)
        {
            // Nothing for now
        }
    }

    protected class TimelinePremiumUserForMessageFollowedListener extends TimelinePremiumUserFollowedListener
    {
        @Override public void onUserFollowSuccess(UserBaseKey userFollowed,
                UserProfileDTO currentUserProfileDTO)
        {
            super.onUserFollowSuccess(userFollowed, currentUserProfileDTO);
            pushPrivateMessageFragment();
            //analytics.addEvent(new ScreenFlowEvent(AnalyticsConstants.PremiumFollow_Success, AnalyticsConstants.Profile));
        }
    }

    protected DTOCacheNew.Listener<UserBaseKey, MessageHeaderDTO> createMessageThreadHeaderCacheListener()
    {
        return new TimelineMessageThreadHeaderCacheListener();
    }

    protected class TimelineMessageThreadHeaderCacheListener implements DTOCacheNew.Listener<UserBaseKey, MessageHeaderDTO>
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull MessageHeaderDTO value)
        {
            linkWithMessageThread(value, true);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            if (!(error instanceof RetrofitError) ||
                    (((RetrofitError) error).getResponse() != null &&
                        ((RetrofitError) error).getResponse().getStatus() != 404))
            {
                THToast.show(R.string.error_fetch_message_thread_header);
                Timber.e(error, "Error while getting message thread");
            }
            else
            {
                // There is just no existing thread
            }
        }
    }

    private class TimelineLastItemVisibleListener implements PullToRefreshBase.OnLastItemVisibleListener
    {
        @Override public void onLastItemVisible()
        {
            mainTimelineAdapter.getTimelineLoader().loadPrevious();
            loadingView.setVisibility(View.VISIBLE);
        }
    }
}

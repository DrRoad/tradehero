package com.tradehero.th.fragments.competition;

import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.SimpleCounterUtils;
import com.tradehero.common.utils.THToast;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.BottomTabs;
import com.tradehero.th.R;
import com.tradehero.th.api.competition.AdDTO;
import com.tradehero.th.api.competition.CompetitionDTOList;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderDisplayCellDTOList;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.competition.ProviderUtil;
import com.tradehero.th.api.competition.key.ProviderDisplayCellListKey;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileCompactDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.DashboardTabHost;
import com.tradehero.th.fragments.competition.zone.CompetitionZoneLegalMentionsView;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneAdvertisementDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDisplayCellDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLeaderboardDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLegalDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZonePortfolioDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneVideoDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneWizardDTO;
import com.tradehero.th.fragments.leaderboard.CompetitionLeaderboardMarkUserListClosedFragment;
import com.tradehero.th.fragments.leaderboard.CompetitionLeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.leaderboard.CompetitionLeaderboardMarkUserListOnGoingFragment;
import com.tradehero.th.fragments.position.CompetitionLeaderboardPositionListFragment;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.fragments.web.BaseWebViewFragment;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.models.intent.THIntentFactory;
import com.tradehero.th.models.intent.THIntentPassedListener;
import com.tradehero.th.persistence.competition.CompetitionListCacheRx;
import com.tradehero.th.persistence.competition.ProviderDisplayCellListCache;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.utils.GraphicUtil;
import dagger.Lazy;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

@Routable({
        "providers/:providerId"
})
public class MainCompetitionFragment extends CompetitionFragment
{
    private static final int EXPECTED_COUNT = 2;

    @InjectView(android.R.id.progress) ProgressBar progressBar;
    @InjectView(R.id.competition_zone_list) AbsListView listView;
    @InjectView(R.id.btn_trade_now) Button btnTradeNow;

    private CompetitionZoneListItemAdapter competitionZoneListItemAdapter;

    private THIntentPassedListener webViewTHIntentPassedListener;
    private BaseWebViewFragment webViewFragment;

    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject CompetitionListCacheRx competitionListCache;
    @Inject ProviderDisplayCellListCache providerDisplayListCellCache;
    @Inject ProviderUtil providerUtil;
    @Inject GraphicUtil graphicUtil;
    @Inject THIntentFactory thIntentFactory;
    @Inject @BottomTabs Lazy<DashboardTabHost> dashboardTabHost;

    @RouteProperty("providerId") Integer routedProviderId;

    protected UserProfileCompactDTO portfolioUserCompactDTO;
    protected CompetitionDTOList competitionDTOs;
    private Subscription competitionListCacheSubscription;
    private ProviderDisplayCellDTOList providerDisplayCellDTOList;
    private DTOCacheNew.Listener<ProviderDisplayCellListKey, ProviderDisplayCellDTOList> displayCellListCacheFetchListener;
    private SimpleCounterUtils simpleCounterUtils;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        thRouter.inject(this);
        if (getArguments() != null && routedProviderId != null)
        {
            putProviderId(getArguments(), new ProviderId(routedProviderId));
        }
        super.onCreate(savedInstanceState);
        this.webViewTHIntentPassedListener = new MainCompetitionWebViewTHIntentPassedListener();
        this.displayCellListCacheFetchListener = createDisplayCellListCacheListener();
        simpleCounterUtils = createSimpleCounterUtils();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_competition, container, false);
        initViews(view);
        return view;
    }

    @Override protected void initViews(View view)
    {
        ButterKnife.inject(this, view);
        if (this.progressBar != null)
        {
            this.progressBar.setVisibility(View.VISIBLE);
        }
        if (this.listView != null)
        {
            this.listView.setOnItemClickListener(createAdapterViewItemClickListener());
            this.listView.setOnScrollListener(dashboardBottomTabsListViewScrollListener.get());
        }
        createAdapter();
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        displayActionBarTitle();
        super.onCreateOptionsMenu(menu, inflater);
    }
    //</editor-fold>

    @Override public void onStart()
    {
        super.onStart();
        AndroidObservable.bindFragment(this, userProfileCache.get(currentUserId.toUserBaseKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createProfileCacheObserver());

        simpleCounterUtils.reset();

        detachCompetitionListCacheTask();
        competitionListCacheSubscription = AndroidObservable.bindFragment(
                this,
                competitionListCache.get(providerId))
                .subscribe(createCompetitionListCacheObserver());

        detachDisplayCellListCacheTask();
        ProviderDisplayCellListKey providerDisplayCellListKey = new ProviderDisplayCellListKey(providerId);
        providerDisplayListCellCache.register(providerDisplayCellListKey, displayCellListCacheFetchListener);
        providerDisplayListCellCache.getOrFetchAsync(providerDisplayCellListKey);
    }

    @Override public void onResume()
    {
        super.onResume();
        // We came back into view so we have to forget the web fragment
        if (this.webViewFragment != null)
        {
            this.webViewFragment.setThIntentPassedListener(null);
        }
        dashboardTabHost.get().setOnTranslate(new DashboardTabHost.OnTranslateListener()
        {
            @Override public void onTranslate(float x, float y)
            {
                btnTradeNow.setTranslationY(y);
            }
        });
        this.webViewFragment = null;
    }

    @Override public void onPause()
    {
        dashboardTabHost.get().setOnTranslate(null);
        super.onPause();
    }

    @Override public void onStop()
    {
        detachCompetitionListCacheTask();
        detachDisplayCellListCacheTask();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        if (this.listView != null)
        {
            this.listView.setOnItemClickListener(null);
            this.listView.setOnScrollListener(null);
        }
        if (this.competitionZoneListItemAdapter != null)
        {
            this.competitionZoneListItemAdapter.clear();
            this.competitionZoneListItemAdapter.setParentOnLegalElementClicked(null);
        }
        this.competitionZoneListItemAdapter = null;
        this.progressBar = null;
        this.listView = null;

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        this.competitionListCacheSubscription = null;
        this.webViewTHIntentPassedListener = null;
        simpleCounterUtils.setListener(null);
        simpleCounterUtils = null;
        super.onDestroy();
    }

    private void detachCompetitionListCacheTask()
    {
        unsubscribe(competitionListCacheSubscription);
        competitionListCacheSubscription = null;
    }

    private void detachDisplayCellListCacheTask()
    {
        providerDisplayListCellCache.unregister(displayCellListCacheFetchListener);
    }

    protected void linkWith(UserProfileCompactDTO userProfileCompactDTO, boolean andDisplay)
    {
        this.portfolioUserCompactDTO = userProfileCompactDTO;
        competitionZoneListItemAdapter.setPortfolioUserProfileCompactDTO(portfolioUserCompactDTO);
        if (andDisplay)
        {
        }
    }

    @Override protected void linkWith(@NotNull ProviderDTO providerDTO, boolean andDisplay)
    {
        super.linkWith(providerDTO, andDisplay);
        competitionZoneListItemAdapter.setProvider(providerDTO);
        if (andDisplay)
        {
            displayActionBarTitle();
            displayTradeNowButton();
        }
    }

    protected void linkWith(CompetitionDTOList competitionIds, boolean andDisplay)
    {
        this.competitionDTOs = competitionIds;
        competitionZoneListItemAdapter.setCompetitionDTOs(competitionIds);
        if (andDisplay)
        {
        }
    }

    protected void linkWith(ProviderDisplayCellDTOList providerDisplayCellDTOList, boolean andDisplay)
    {
        this.providerDisplayCellDTOList = providerDisplayCellDTOList;
        competitionZoneListItemAdapter.setDisplayCellDTOS(providerDisplayCellDTOList);
        if (andDisplay)
        {
        }
    }

    protected void createAdapter()
    {
        this.competitionZoneListItemAdapter = new CompetitionZoneListItemAdapter(
                getActivity(),
                R.layout.competition_zone_item,
                R.layout.competition_zone_ads,
                R.layout.competition_zone_header,
                R.layout.competition_zone_portfolio,
                R.layout.competition_zone_leaderboard_item,
                R.layout.competition_zone_legal_mentions);
        competitionZoneListItemAdapter.setParentOnLegalElementClicked(new MainCompetitionLegalClickedListener());

        if (this.listView != null)
        {
            this.listView.setAdapter(this.competitionZoneListItemAdapter);
        }
    }

    protected void displayListView()
    {
        Timber.d("displayListView %s %s %s %s", portfolioUserCompactDTO, providerDTO, competitionDTOs, providerDisplayCellDTOList);
        if (providerDTO != null)
        {
            if (progressBar != null)
            {
                progressBar.setVisibility(View.GONE);
            }
            competitionZoneListItemAdapter.notifyDataSetChanged();
        }
    }

    private void displayActionBarTitle()
    {
        if (providerDTO != null
                && providerDTO.specificResources != null
                && providerDTO.specificResources.mainCompetitionFragmentTitleResId > 0)
        {
            setActionBarTitle(providerDTO.specificResources.mainCompetitionFragmentTitleResId);
        }
        else if (this.providerDTO == null || this.providerDTO.name == null)
        {
            setActionBarTitle("");
        }
        else
        {
            setActionBarTitle(this.providerDTO.name);
        }
    }

    @OnClick(R.id.btn_trade_now)
    public void handleTradeNowClicked()
    {
        pushTradeNowElement();
    }

    private void displayTradeNowButton()
    {
        if (providerDTO != null)
        {
            btnTradeNow.setVisibility(View.VISIBLE);

            int bgColor = graphicUtil.parseColor(providerDTO.hexColor);
            StateListDrawable stateListDrawable = graphicUtil.createStateListDrawable(getActivity(), bgColor);

            graphicUtil.setBackground(btnTradeNow, stateListDrawable);

            int textColor = graphicUtil.parseColor(providerDTO.textHexColor, graphicUtil.getContrastingColor(bgColor));
            btnTradeNow.setTextColor(textColor);
        }
    }

    //<editor-fold desc="Click Handling">
    private void handleItemClicked(@NotNull CompetitionZoneDTO competitionZoneDTO)
    {
        if (competitionZoneDTO instanceof CompetitionZonePortfolioDTO)
        {
            pushPortfolioElement((CompetitionZonePortfolioDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneVideoDTO)
        {
            pushVideoElement((CompetitionZoneVideoDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneWizardDTO)
        {
            pushWizardElement((CompetitionZoneWizardDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneLeaderboardDTO)
        {
            pushLeaderboardElement((CompetitionZoneLeaderboardDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneLegalDTO)
        {
            pushLegalElement((CompetitionZoneLegalDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneAdvertisementDTO)
        {
            pushAdvertisement((CompetitionZoneAdvertisementDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneDisplayCellDTO)
        {
            handleDisplayCellClicked((CompetitionZoneDisplayCellDTO) competitionZoneDTO);
        }
        // TODO others?
    }

    private void pushAdvertisement(@NotNull CompetitionZoneAdvertisementDTO competitionZoneDTO)
    {
        AdDTO adDTO = competitionZoneDTO.getAdDTO();
        if (adDTO != null && adDTO.redirectUrl != null)
        {
            Bundle args = new Bundle();
            String url = providerUtil.appendUserId(adDTO.redirectUrl, '&');
            CompetitionWebViewFragment.putUrl(args, url);
            navigator.get().pushFragment(CompetitionWebViewFragment.class, args);
        }
    }

    private void pushTradeNowElement()
    {
        Bundle args = new Bundle();
        ProviderSecurityListFragment.putProviderId(args, providerId);
        OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
        if (ownedPortfolioId != null)
        {
            ProviderSecurityListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
        }

        navigator.get().pushFragment(ProviderSecurityListFragment.class, args);
    }

    private void pushPortfolioElement(@NotNull CompetitionZonePortfolioDTO competitionZoneDTO)
    {
        // TODO We need to be able to launch async when the portfolio Id is finally not null
        OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
        if (ownedPortfolioId != null)
        {
            Bundle args = new Bundle();
            PositionListFragment.putGetPositionsDTOKey(args, ownedPortfolioId);
            PositionListFragment.putShownUser(args, ownedPortfolioId.getUserBaseKey());
            PositionListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
            CompetitionLeaderboardPositionListFragment.putProviderId(args, providerId);
            navigator.get().pushFragment(CompetitionLeaderboardPositionListFragment.class, args);
        }
    }

    private void pushVideoElement(@NotNull CompetitionZoneVideoDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        ProviderVideoListFragment.putProviderId(args, providerId);
        OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
        if (ownedPortfolioId != null)
        {
            ProviderVideoListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
        }
        navigator.get().pushFragment(ProviderVideoListFragment.class, args);
    }

    private void pushWizardElement(@NotNull CompetitionZoneWizardDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();

        String competitionUrl = competitionZoneDTO.getWebUrl();
        if (competitionUrl == null)
        {
            competitionUrl = providerUtil.getWizardPage(providerId);
            CompetitionWebViewFragment.putIsOptionMenuVisible(args, false);
        }

        CompetitionWebViewFragment.putUrl(args, competitionUrl);
        this.webViewFragment = navigator.get().pushFragment(CompetitionWebViewFragment.class, args);
        this.webViewFragment.setThIntentPassedListener(this.webViewTHIntentPassedListener);
    }

    private void pushLeaderboardElement(@NotNull CompetitionZoneLeaderboardDTO competitionZoneDTO)
    {
        LeaderboardDefDTO leaderboardDefDTO = competitionZoneDTO.competitionDTO.leaderboard;
        Bundle args = new Bundle();
        CompetitionLeaderboardMarkUserListFragment.putProviderId(args, providerId);
        CompetitionLeaderboardMarkUserListFragment.putCompetition(args, competitionZoneDTO.competitionDTO);

        OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
        if (ownedPortfolioId != null)
        {
            CompetitionLeaderboardMarkUserListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
        }

        if (navigator != null && leaderboardDefDTO.isWithinUtcRestricted())
        {
            navigator.get().pushFragment(CompetitionLeaderboardMarkUserListOnGoingFragment.class, args);
        }
        else if (navigator != null)
        {
            navigator.get().pushFragment(CompetitionLeaderboardMarkUserListClosedFragment.class, args);
        }
    }

    private void pushLegalElement(@NotNull CompetitionZoneLegalDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        if ((competitionZoneDTO).requestedLink.equals(CompetitionZoneLegalDTO.LinkType.RULES))
        {
            CompetitionWebViewFragment.putUrl(args, providerUtil.getRulesPage(providerId));
        }
        else
        {
            CompetitionWebViewFragment.putUrl(args, providerUtil.getTermsPage(providerId));
        }
        if (navigator != null)
        {
            navigator.get().pushFragment(CompetitionWebViewFragment.class, args);
        }
    }

    private void handleDisplayCellClicked(@NotNull CompetitionZoneDisplayCellDTO competitionZoneDisplayCellDTO)
    {
        String redirectUrl = competitionZoneDisplayCellDTO.getRedirectUrl();
        if (redirectUrl != null)
        {
            //thRouter.open(redirectUrl); TODO implement this when router is updated
            Uri uri = Uri.parse(redirectUrl);
            if (thIntentFactory.isHandlableScheme(uri.getScheme()))
            {
                if (uri.getHost().equalsIgnoreCase(getActivity().getString(R.string.intent_host_web)))
                {
                    String url = uri.getQueryParameter("url");
                    if (url != null)
                    {
                        Timber.d("Opening this page: %s", url);
                        Bundle bundle = new Bundle();
                        CompetitionWebViewFragment.putUrl(bundle, url);
                        this.webViewFragment = navigator.get().pushFragment(WebViewFragment.class, bundle);
                        this.webViewFragment.setThIntentPassedListener(this.webViewTHIntentPassedListener);
                    }
                }
                else
                {
                    //TODO Confirm with tho on how this router works
                    try
                    {
                        thIntentFactory.create(getPassedIntent(redirectUrl));
                    } catch (IndexOutOfBoundsException e)
                    {
                        Timber.e(e, "Failed to create intent with string %s", redirectUrl);
                    }
                }
            }
        }
    }

    public SimpleCounterUtils createSimpleCounterUtils()
    {
        return new SimpleCounterUtils(EXPECTED_COUNT, new SimpleCounterUtils.SimpleCounter()
        {
            @Override public void onCountFinished(int count)
            {
                displayListView();
            }
        });
    }

    public Intent getPassedIntent(String url)
    {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
    //</editor-fold>

    private AdapterView.OnItemClickListener createAdapterViewItemClickListener()
    {
        return new MainCompetitionFragmentItemClickListener();
    }

    private Observer<Pair<UserBaseKey, UserProfileDTO>> createProfileCacheObserver()
    {
        return new MainCompetitionUserProfileCacheObserver();
    }

    private class MainCompetitionFragmentItemClickListener
            implements AdapterView.OnItemClickListener
    {
        @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            if (adapterView == null)
            {
                Timber.e(new NullPointerException("adapterView was null"), "onItemClient");
            }
            else
            {
                Object itemClicked = adapterView.getItemAtPosition(i);
                if (itemClicked == null)
                {
                    Timber.e(new NullPointerException("itemClicked was null"), "onItemClient");
                }
                else
                {
                    handleItemClicked((CompetitionZoneDTO) itemClicked);
                }
            }
        }
    }

    private class MainCompetitionWebViewTHIntentPassedListener
            extends CompetitionWebFragmentTHIntentPassedListener
    {
        public MainCompetitionWebViewTHIntentPassedListener()
        {
            super();
        }

        @Override protected BaseWebViewFragment getApplicableWebViewFragment()
        {
            return webViewFragment;
        }

        @Override protected OwnedPortfolioId getApplicablePortfolioId()
        {
            return MainCompetitionFragment.this.getApplicablePortfolioId();
        }

        @Override protected ProviderId getProviderId()
        {
            return providerId;
        }

        @Override protected DashboardNavigator getNavigator()
        {
            return navigator.get();
        }

        @Override protected Class<?> getClassToPop()
        {
            return MainCompetitionFragment.class;
        }
    }

    private class MainCompetitionLegalClickedListener
            implements CompetitionZoneLegalMentionsView.OnElementClickedListener
    {
        @Override public void onElementClicked(CompetitionZoneDTO competitionZoneLegalDTO)
        {
            handleItemClicked(competitionZoneLegalDTO);
        }
    }

    private class MainCompetitionUserProfileCacheObserver
            implements Observer<Pair<UserBaseKey, UserProfileDTO>>
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
            THToast.show(getString(R.string.error_fetch_your_user_profile));
            Timber.e("Error fetching the profile info", e);
        }
    }

    private Observer<Pair<ProviderId, CompetitionDTOList>> createCompetitionListCacheObserver()
    {
        return new MainCompetitionCompetitionListCacheObserver();
    }

    private class MainCompetitionCompetitionListCacheObserver
            implements Observer<Pair<ProviderId, CompetitionDTOList>>
    {
        @Override public void onNext(Pair<ProviderId, CompetitionDTOList> pair)
        {
            linkWith(pair.second, true);
            simpleCounterUtils.increment();
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            THToast.show(getString(R.string.error_fetch_provider_competition_list));
            Timber.e("Error fetching the list of competition info", e);
            simpleCounterUtils.increment();
        }
    }

    private DTOCacheNew.Listener<ProviderDisplayCellListKey, ProviderDisplayCellDTOList> createDisplayCellListCacheListener()
    {
        return new MainCompetitionDisplayCellListCacheListener();
    }

    private class MainCompetitionDisplayCellListCacheListener
            implements DTOCacheNew.Listener<ProviderDisplayCellListKey, ProviderDisplayCellDTOList>
    {
        @Override public void onDTOReceived(@NotNull ProviderDisplayCellListKey providerId, @NotNull ProviderDisplayCellDTOList value)
        {
            linkWith(value, true);
            simpleCounterUtils.increment();
        }

        @Override public void onErrorThrown(@NotNull ProviderDisplayCellListKey key, @NotNull Throwable error)
        {
            THToast.show(getString(R.string.error_fetch_provider_competition_display_cell_list));
            Timber.e("Error fetching the list of competition info cell %s", key, error);
            simpleCounterUtils.increment();
        }
    }
}

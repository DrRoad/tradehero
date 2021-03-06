package com.tradehero.th.fragments.competition;

import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import butterknife.OnItemClick;
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
import com.tradehero.th.persistence.competition.ProviderDisplayCellListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.utils.GraphicUtil;
import dagger.Lazy;
import javax.inject.Inject;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import timber.log.Timber;

@Routable({
        "providers/:providerId"
})
public class MainCompetitionFragment extends CompetitionFragment
{
    @InjectView(android.R.id.progress) ProgressBar progressBar;
    @InjectView(R.id.competition_zone_list) AbsListView listView;
    @InjectView(R.id.btn_trade_now) Button btnTradeNow;

    private CompetitionZoneListItemAdapter competitionZoneListItemAdapter;

    private THIntentPassedListener webViewTHIntentPassedListener;
    private BaseWebViewFragment webViewFragment;

    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject CompetitionListCacheRx competitionListCache;
    @Inject ProviderDisplayCellListCacheRx providerDisplayListCellCache;
    @Inject ProviderUtil providerUtil;
    @Inject GraphicUtil graphicUtil;
    @Inject THIntentFactory thIntentFactory;
    @Inject @BottomTabs Lazy<DashboardTabHost> dashboardTabHost;

    @RouteProperty("providerId") Integer routedProviderId;

    @Nullable private Subscription userProfileCacheSubscription;
    protected UserProfileCompactDTO userProfileCompactDTO;
    @Nullable private Subscription competitionListCacheSubscription;
    protected CompetitionDTOList competitionDTOs;
    @Nullable private Subscription displayCellListCacheFetchSubscription;
    private ProviderDisplayCellDTOList providerDisplayCellDTOList;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        thRouter.inject(this);
        if (getArguments() != null && routedProviderId != null)
        {
            putProviderId(getArguments(), new ProviderId(routedProviderId));
        }
        super.onCreate(savedInstanceState);
        this.webViewTHIntentPassedListener = new MainCompetitionWebViewTHIntentPassedListener();
        createAdapter();
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
        this.progressBar.setVisibility(View.VISIBLE);
        this.listView.setOnScrollListener(dashboardBottomTabsListViewScrollListener.get());
        this.listView.setAdapter(this.competitionZoneListItemAdapter);
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

        unsubscribe(userProfileCacheSubscription);
        userProfileCacheSubscription = AndroidObservable.bindFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey()))
                .subscribe(createProfileCacheObserver());

        unsubscribe(competitionListCacheSubscription);
        competitionListCacheSubscription = AndroidObservable.bindFragment(
                this,
                competitionListCache.get(providerId))
                .subscribe(createCompetitionListCacheObserver());

        unsubscribe(displayCellListCacheFetchSubscription);
        displayCellListCacheFetchSubscription = AndroidObservable.bindFragment(
                this,
                providerDisplayListCellCache.get(new ProviderDisplayCellListKey(providerId)))
                .subscribe(createDisplayCellListCacheObserver());
    }

    @Override public void onResume()
    {
        super.onResume();
        // We came back into view so we have to forget the web fragment
        if (this.webViewFragment != null)
        {
            this.webViewFragment.setThIntentPassedListener(null);
        }
        dashboardTabHost.get().setOnTranslate((x, y) -> btnTradeNow.setTranslationY(y));
        this.webViewFragment = null;
    }

    @Override public void onPause()
    {
        dashboardTabHost.get().setOnTranslate(null);
        super.onPause();
    }

    @Override public void onStop()
    {
        unsubscribe(userProfileCacheSubscription);
        userProfileCacheSubscription = null;
        unsubscribe(competitionListCacheSubscription);
        competitionListCacheSubscription = null;
        unsubscribe(displayCellListCacheFetchSubscription);
        displayCellListCacheFetchSubscription = null;
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        this.listView.setOnScrollListener(null);
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        this.webViewTHIntentPassedListener = null;
        this.competitionZoneListItemAdapter.clear();
        this.competitionZoneListItemAdapter.setParentOnLegalElementClicked(null);
        this.competitionZoneListItemAdapter = null;
        super.onDestroy();
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
        competitionZoneListItemAdapter.setParentOnLegalElementClicked(this::handleItemClicked);
    }

    @Override protected void linkWith(@NonNull ProviderDTO providerDTO, boolean andDisplay)
    {
        super.linkWith(providerDTO, andDisplay);
        competitionZoneListItemAdapter.setProvider(providerDTO);
        if (andDisplay)
        {
            displayActionBarTitle();
            displayTradeNowButton();
            displayListView();
        }
    }

    private Observer<Pair<UserBaseKey, UserProfileDTO>> createProfileCacheObserver()
    {
        return new MainCompetitionUserProfileCacheObserver();
    }

    protected class MainCompetitionUserProfileCacheObserver
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
            if (userProfileCompactDTO == null)
            {
                THToast.show(getString(R.string.error_fetch_your_user_profile));
            }
            Timber.e("Error fetching the profile info", e);
        }
    }

    protected void linkWith(UserProfileCompactDTO userProfileCompactDTO, boolean andDisplay)
    {
        this.userProfileCompactDTO = userProfileCompactDTO;
        competitionZoneListItemAdapter.setPortfolioUserProfileCompactDTO(userProfileCompactDTO);
        if (andDisplay)
        {
            displayListView();
        }
    }

    private Observer<Pair<ProviderId, CompetitionDTOList>> createCompetitionListCacheObserver()
    {
        return new MainCompetitionCompetitionListCacheObserver();
    }

    protected class MainCompetitionCompetitionListCacheObserver
            implements Observer<Pair<ProviderId, CompetitionDTOList>>
    {
        @Override public void onNext(Pair<ProviderId, CompetitionDTOList> pair)
        {
            linkWith(pair.second, true);
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            if (competitionDTOs == null)
            {
                THToast.show(getString(R.string.error_fetch_provider_competition_list));
            }
            Timber.e("Error fetching the list of competition info", e);
        }
    }

    protected void linkWith(CompetitionDTOList competitionDTOs1, boolean andDisplay)
    {
        this.competitionDTOs = competitionDTOs1;
        competitionZoneListItemAdapter.setCompetitionDTOs(competitionDTOs1);
        if (andDisplay)
        {
            displayListView();
        }
    }

    private Observer<Pair<ProviderDisplayCellListKey, ProviderDisplayCellDTOList>> createDisplayCellListCacheObserver()
    {
        return new MainCompetitionDisplayCellListCacheObserver();
    }

    protected class MainCompetitionDisplayCellListCacheObserver
            implements Observer<Pair<ProviderDisplayCellListKey, ProviderDisplayCellDTOList>>
    {
        @Override public void onNext(
                Pair<ProviderDisplayCellListKey, ProviderDisplayCellDTOList> pair)
        {
            linkWith(pair.second, true);
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            if (providerDisplayCellDTOList == null)
            {
                THToast.show(getString(R.string.error_fetch_provider_competition_display_cell_list));
            }
            Timber.e("Error fetching the list of competition info cell", e);
        }
    }

    protected void linkWith(ProviderDisplayCellDTOList providerDisplayCellDTOList, boolean andDisplay)
    {
        this.providerDisplayCellDTOList = providerDisplayCellDTOList;
        competitionZoneListItemAdapter.setDisplayCellDTOS(providerDisplayCellDTOList);
        if (andDisplay)
        {
            displayListView();
        }
    }

    protected void displayListView()
    {
        Timber.d("displayListView %s %s %s %s", userProfileCompactDTO, providerDTO, competitionDTOs, providerDisplayCellDTOList);
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
        if (this.providerDTO == null || this.providerDTO.name == null)
        {
            setActionBarTitle("");
        }
        else
        {
            setActionBarTitle(this.providerDTO.name);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
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
    @OnItemClick(R.id.competition_zone_list)
    protected void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
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

    protected void handleItemClicked(@NonNull CompetitionZoneDTO competitionZoneDTO)
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

    private void pushAdvertisement(@NonNull CompetitionZoneAdvertisementDTO competitionZoneDTO)
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

    private void pushPortfolioElement(@NonNull CompetitionZonePortfolioDTO competitionZoneDTO)
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

    private void pushVideoElement(@NonNull CompetitionZoneVideoDTO competitionZoneDTO)
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

    private void pushWizardElement(@NonNull CompetitionZoneWizardDTO competitionZoneDTO)
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

    private void pushLeaderboardElement(@NonNull CompetitionZoneLeaderboardDTO competitionZoneDTO)
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

    private void pushLegalElement(@NonNull CompetitionZoneLegalDTO competitionZoneDTO)
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

    private void handleDisplayCellClicked(@NonNull CompetitionZoneDisplayCellDTO competitionZoneDisplayCellDTO)
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

    public Intent getPassedIntent(String url)
    {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
    //</editor-fold>

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
}

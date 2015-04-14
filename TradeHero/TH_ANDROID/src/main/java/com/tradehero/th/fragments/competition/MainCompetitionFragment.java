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
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.R;
import com.tradehero.th.api.competition.AdDTO;
import com.tradehero.th.api.competition.CompetitionDTOList;
import com.tradehero.th.api.competition.CompetitionPreSeasonDTO;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderDisplayCellDTOList;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.competition.ProviderPrizePoolDTO;
import com.tradehero.th.api.competition.ProviderUtil;
import com.tradehero.th.api.competition.key.ProviderDisplayCellListKey;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.OnMovableBottomTranslateListener;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.competition.zone.AbstractCompetitionZoneListItemView;
import com.tradehero.th.fragments.competition.zone.CompetitionZoneLegalMentionsView;
import com.tradehero.th.fragments.competition.zone.CompetitionZonePrizePoolView;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneAdvertisementDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDTOUtil;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDisplayCellDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLeaderboardDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLegalDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZonePortfolioDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZonePreSeasonDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneVideoDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneWizardDTO;
import com.tradehero.th.fragments.leaderboard.CompetitionLeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.position.CompetitionLeaderboardPositionListFragment;
import com.tradehero.th.fragments.social.friend.FriendsInvitationFragment;
import com.tradehero.th.fragments.web.BaseWebViewFragment;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.models.intent.THIntentFactory;
import com.tradehero.th.models.intent.THIntentPassedListener;
import com.tradehero.th.models.security.ProviderTradableSecuritiesHelper;
import com.tradehero.th.network.service.ProviderServiceWrapper;
import com.tradehero.th.persistence.competition.CompetitionListCacheRx;
import com.tradehero.th.persistence.competition.CompetitionPreseasonCacheRx;
import com.tradehero.th.persistence.competition.ProviderCacheRx;
import com.tradehero.th.persistence.competition.ProviderDisplayCellListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.ToastAndLogOnErrorAction;
import com.tradehero.th.utils.GraphicUtil;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SingleAttributeEvent;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func6;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Routable({
        "providers/:providerId"
})
public class MainCompetitionFragment extends DashboardFragment
{
    private static final String BUNDLE_KEY_PROVIDER_ID = MainCompetitionFragment.class.getName() + ".providerId";
    private static final String BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE =
            MainCompetitionFragment.class.getName() + ".purchaseApplicablePortfolioId";

    @InjectView(android.R.id.progress) ProgressBar progressBar;
    @InjectView(R.id.competition_zone_list) AbsListView listView;
    @InjectView(R.id.btn_trade_now) Button btnTradeNow;

    private CompetitionZoneListItemAdapter competitionZoneListItemAdapter;

    private THIntentPassedListener webViewTHIntentPassedListener;
    private BaseWebViewFragment webViewFragment;

    @Inject ProviderCacheRx providerCache;
    @Inject THRouter thRouter;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject CompetitionListCacheRx competitionListCache;
    @Inject ProviderDisplayCellListCacheRx providerDisplayListCellCache;
    @Inject ProviderUtil providerUtil;
    @Inject CompetitionZoneDTOUtil competitionZoneDTOUtil;
    @Inject THIntentFactory thIntentFactory;
    @Inject CompetitionPreseasonCacheRx competitionPreSeasonCacheRx;
    @Inject ProviderServiceWrapper providerServiceWrapper;
    @Inject Lazy<ProviderTradableSecuritiesHelper> providerTradableSecuritiesHelperLazy;
    @Inject protected CurrentUserId currentUserId;
    @Inject Analytics analytics;

    @RouteProperty("providerId") Integer routedProviderId;

    protected ProviderId providerId;
    protected ProviderDTO providerDTO;
    protected UserProfileDTO userProfileDTO;
    protected CompetitionDTOList competitionDTOs;
    private ProviderDisplayCellDTOList providerDisplayCellDTOList;
    protected List<ProviderPrizePoolDTO> providerPrizePoolDTOs;
    private List<CompetitionPreSeasonDTO> competitionPreSeasonDTOs;
    private OwnedPortfolioId applicablePortfolioId;

    public static void putProviderId(@NonNull Bundle args, @NonNull ProviderId providerId)
    {
        args.putBundle(BUNDLE_KEY_PROVIDER_ID, providerId.getArgs());
    }

    @NonNull public static ProviderId getProviderId(@NonNull Bundle args)
    {
        return new ProviderId(args.getBundle(BUNDLE_KEY_PROVIDER_ID));
    }

    public static void putApplicablePortfolioId(@NonNull Bundle args, @NonNull OwnedPortfolioId ownedPortfolioId)
    {
        args.putBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE, ownedPortfolioId.getArgs());
    }

    private static OwnedPortfolioId getApplicablePortfolioId(@Nullable Bundle args)
    {
        if (args != null)
        {
            if (args.containsKey(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE))
            {
                return new OwnedPortfolioId(args.getBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE));
            }
        }
        return null;
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        thRouter.inject(this);
        if (getArguments() != null && routedProviderId != null)
        {
            putProviderId(getArguments(), new ProviderId(routedProviderId));
        }
        super.onCreate(savedInstanceState);
        this.providerId = getProviderId(getArguments());
        this.webViewTHIntentPassedListener = new MainCompetitionWebViewTHIntentPassedListener();
        competitionZoneListItemAdapter = createAdapter();
        analytics.fireEvent(
                new SingleAttributeEvent(AnalyticsConstants.Competition_Home, AnalyticsConstants.ProviderId, String.valueOf(providerId.key)));

        applicablePortfolioId = getApplicablePortfolioId(getArguments());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main_competition, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        this.progressBar.setVisibility(View.VISIBLE);
        this.listView.setOnScrollListener(fragmentElements.get().getListViewScrollListener());
        this.listView.setAdapter(this.competitionZoneListItemAdapter);
        competitionZoneDTOUtil.randomiseAd();
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
        fetchAdapterRequisite();
        registerAdapterUserActions();
    }

    @Override public void onResume()
    {
        super.onResume();
        // We came back into view so we have to forget the web fragment
        if (this.webViewFragment != null)
        {
            this.webViewFragment.setThIntentPassedListener(null);
        }
        fragmentElements.get().getMovableBottom().setOnMovableBottomTranslateListener(new OnMovableBottomTranslateListener()
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
        fragmentElements.get().getMovableBottom().setOnMovableBottomTranslateListener(null);
        super.onPause();
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
        this.competitionZoneListItemAdapter = null;
        super.onDestroy();
    }

    protected CompetitionZoneListItemAdapter createAdapter()
    {
        return new CompetitionZoneListItemAdapter(
                getActivity(),
                R.layout.competition_zone_item,
                R.layout.competition_zone_ads,
                R.layout.competition_zone_header,
                R.layout.competition_zone_prize_pool,
                R.layout.competition_zone_portfolio,
                R.layout.competition_zone_leaderboard_item,
                R.layout.competition_zone_legal_mentions);
    }

    private void fetchAdapterRequisite()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(this,
                Observable.combineLatest(
                        userProfileCache.get(currentUserId.toUserBaseKey())
                                .map(new PairGetSecond<UserBaseKey, UserProfileDTO>())
                                .startWith(Observable.just(userProfileDTO))
                                .onErrorReturn(new Func1<Throwable, UserProfileDTO>()
                                {
                                    @Override public UserProfileDTO call(Throwable throwable)
                                    {
                                        if (userProfileDTO == null)
                                        {
                                            THToast.show(R.string.error_fetch_your_user_profile);
                                        }
                                        Timber.e("Error fetching the profile info", throwable);
                                        return userProfileDTO;
                                    }
                                }),
                        providerCache.get(this.providerId)
                                .map(new PairGetSecond<ProviderId, ProviderDTO>())
                                .startWith(Observable.just(providerDTO))
                                .onErrorReturn(new Func1<Throwable, ProviderDTO>()
                                {
                                    @Override public ProviderDTO call(Throwable throwable)
                                    {
                                        if (providerDTO == null)
                                        {
                                            THToast.show(R.string.error_fetch_provider_info);
                                        }
                                        Timber.e("Error fetching the provider info", throwable);
                                        return providerDTO;
                                    }
                                }),
                        competitionListCache.get(providerId)
                                .map(new PairGetSecond<ProviderId, CompetitionDTOList>())
                                .startWith(Observable.just(competitionDTOs))
                                .onErrorReturn(new Func1<Throwable, CompetitionDTOList>()
                                {
                                    @Override public CompetitionDTOList call(Throwable throwable)
                                    {
                                        if (competitionDTOs == null)
                                        {
                                            THToast.show(getString(R.string.error_fetch_provider_competition_list));
                                        }
                                        Timber.e("Error fetching the list of competition info", throwable);
                                        return competitionDTOs;
                                    }
                                }),
                        providerDisplayListCellCache.get(new ProviderDisplayCellListKey(providerId))
                                .map(new PairGetSecond<ProviderDisplayCellListKey, ProviderDisplayCellDTOList>())
                                .startWith(Observable.just(providerDisplayCellDTOList))
                                .onErrorReturn(new Func1<Throwable, ProviderDisplayCellDTOList>()
                                {
                                    @Override public ProviderDisplayCellDTOList call(Throwable throwable)
                                    {
                                        if (providerDisplayCellDTOList == null)
                                        {
                                            THToast.show(getString(R.string.error_fetch_provider_competition_display_cell_list));
                                        }
                                        Timber.e("Error fetching the list of competition info cell", throwable);
                                        return providerDisplayCellDTOList;
                                    }
                                }),
                        competitionPreSeasonCacheRx.get(providerId)
                                .map(new PairGetSecond<ProviderId, CompetitionPreSeasonDTO>())
                                .map(new Func1<CompetitionPreSeasonDTO, List<CompetitionPreSeasonDTO>>()
                                {
                                    @Override public List<CompetitionPreSeasonDTO> call(CompetitionPreSeasonDTO competitionPreSeasonDTO)
                                    {
                                        return Collections.singletonList(competitionPreSeasonDTO);
                                    }
                                })
                                .startWith(Observable.just(competitionPreSeasonDTOs))
                                .onErrorReturn(new Func1<Throwable, List<CompetitionPreSeasonDTO>>()
                                {
                                    @Override public List<CompetitionPreSeasonDTO> call(Throwable throwable)
                                    {
                                        Timber.e(throwable, "Failed fetching preseason for %s", providerId);
                                        return Collections.emptyList();
                                    }
                                }),
                        providerServiceWrapper.getProviderPrizePoolRx(providerId)
                                .map(new Func1<ProviderPrizePoolDTO, List<ProviderPrizePoolDTO>>()
                                {
                                    @Override public List<ProviderPrizePoolDTO> call(ProviderPrizePoolDTO providerPrizePoolDTO)
                                    {
                                        return Collections.singletonList(providerPrizePoolDTO);
                                    }
                                })
                                .startWith(Observable.just(providerPrizePoolDTOs))
                                .onErrorReturn(new Func1<Throwable, List<ProviderPrizePoolDTO>>()
                                {
                                    @Override public List<ProviderPrizePoolDTO> call(Throwable throwable)
                                    {
                                        if (providerPrizePoolDTOs == null)
                                        {
                                            // When there is no prize pool, server returns HTTP404, which is a valid response
                                            boolean is404 = false;
                                            if (throwable instanceof RetrofitError)
                                            {
                                                Response response = ((RetrofitError) throwable).getResponse();
                                                is404 = response != null && response.getStatus() == 404;
                                            }
                                            if (!is404)
                                            {
                                                THToast.show(getString(R.string.error_fetch_provider_prize_pool_info));
                                            }
                                        }
                                        Timber.e(throwable, "Error fetching the provider info");
                                        return Collections.emptyList();
                                    }
                                }),
                        new Func6<UserProfileDTO,
                                ProviderDTO,
                                CompetitionDTOList,
                                ProviderDisplayCellDTOList,
                                List<CompetitionPreSeasonDTO>,
                                List<ProviderPrizePoolDTO>,
                                List<Pair<Integer, CompetitionZoneDTO>>>()
                        {
                            @Override public List<Pair<Integer, CompetitionZoneDTO>> call(
                                    UserProfileDTO userProfileDTO,
                                    ProviderDTO providerDTO,
                                    CompetitionDTOList competitionDTOs,
                                    ProviderDisplayCellDTOList providerDisplayCellDTOs,
                                    List<CompetitionPreSeasonDTO> competitionPreSeasonDTOs,
                                    List<ProviderPrizePoolDTO> providerPrizePoolDTOs)
                            {
                                MainCompetitionFragment.this.userProfileDTO = userProfileDTO;
                                MainCompetitionFragment.this.providerDTO = providerDTO;
                                MainCompetitionFragment.this.competitionDTOs = competitionDTOs;
                                MainCompetitionFragment.this.providerDisplayCellDTOList = providerDisplayCellDTOs;
                                MainCompetitionFragment.this.competitionPreSeasonDTOs = competitionPreSeasonDTOs;
                                MainCompetitionFragment.this.providerPrizePoolDTOs = providerPrizePoolDTOs;
                                return competitionZoneDTOUtil.makeList(
                                        getActivity(),
                                        userProfileDTO,
                                        providerDTO,
                                        competitionDTOs,
                                        providerDisplayCellDTOs,
                                        competitionPreSeasonDTOs,
                                        providerPrizePoolDTOs);
                            }
                        })
                        .subscribeOn(Schedulers.computation()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<Pair<Integer, CompetitionZoneDTO>>>()
                        {
                            @Override public void call(List<Pair<Integer, CompetitionZoneDTO>> elements)
                            {
                                competitionZoneListItemAdapter.setNotifyOnChange(false);
                                competitionZoneListItemAdapter.setElements(elements);
                                competitionZoneListItemAdapter.setNotifyOnChange(true);
                                competitionZoneListItemAdapter.notifyDataSetChanged();
                                displayListView();
                                displayActionBarTitle();
                                displayTradeNowButton();
                            }
                        },
                        new ToastAndLogOnErrorAction("Failed to get requisite")
                ));
    }

    protected void displayListView()
    {
        Timber.d("displayListView %s %s %s %s", userProfileDTO, providerDTO, competitionDTOs, providerDisplayCellDTOList);
        if (providerDTO != null)
        {
            if (progressBar != null)
            {
                progressBar.setVisibility(View.GONE);
            }
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
    public void handleTradeNowClicked(View view)
    {
        Bundle args = new Bundle();
        OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
        providerTradableSecuritiesHelperLazy.get().pushTradableSecuritiesList(args, ownedPortfolioId, providerDTO.associatedPortfolio, providerId);
    }

    private void displayTradeNowButton()
    {
        if (providerDTO != null && btnTradeNow.getVisibility() != View.VISIBLE)
        {
            btnTradeNow.setVisibility(View.VISIBLE);

            int bgColor = GraphicUtil.parseColor(providerDTO.hexColor);
            StateListDrawable stateListDrawable = GraphicUtil.createStateListDrawable(getActivity(), bgColor);

            GraphicUtil.setBackground(btnTradeNow, stateListDrawable);

            int textColor = GraphicUtil.parseColor(providerDTO.textHexColor, GraphicUtil.getContrastingColor(bgColor));
            btnTradeNow.setTextColor(textColor);
        }
    }

    //<editor-fold desc="Click Handling">
    @OnItemClick(R.id.competition_zone_list)
    protected void onItemClick(AdapterView<?> adapterView,
            @SuppressWarnings("UnusedParameters") View view,
            int i,
            @SuppressWarnings("UnusedParameters") long l)
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

    protected void handleItemClicked(@NonNull CompetitionZoneDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        if (competitionZoneDTO instanceof CompetitionZonePortfolioDTO)
        {
            // TODO We need to be able to launch async when the portfolio Id is finally not null
            OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
            if (ownedPortfolioId != null)
            {
                CompetitionLeaderboardPositionListFragment.putGetPositionsDTOKey(args, ownedPortfolioId);
                CompetitionLeaderboardPositionListFragment.putShownUser(args, ownedPortfolioId.getUserBaseKey());
                CompetitionLeaderboardPositionListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
                CompetitionLeaderboardPositionListFragment.putProviderId(args, providerId);
                if (providerDTO != null && providerDTO.associatedPortfolio != null)
                {
                    CompetitionLeaderboardPositionListFragment.putIsFX(args, providerDTO.associatedPortfolio.assetClass);
                }
                navigator.get().pushFragment(CompetitionLeaderboardPositionListFragment.class, args);
            }
        }
        else if (competitionZoneDTO instanceof CompetitionZoneVideoDTO)
        {
            ProviderVideoListFragment.putProviderId(args, providerId);
            navigator.get().pushFragment(ProviderVideoListFragment.class, args);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneWizardDTO)
        {
            String competitionUrl = ((CompetitionZoneWizardDTO) competitionZoneDTO).getWebUrl();
            if (competitionUrl == null)
            {
                competitionUrl = providerUtil.getWizardPage(providerId);
                CompetitionWebViewFragment.putIsOptionMenuVisible(args, false);
            }

            CompetitionWebViewFragment.putUrl(args, competitionUrl);
            this.webViewFragment = navigator.get().pushFragment(CompetitionWebViewFragment.class, args);
            this.webViewFragment.setThIntentPassedListener(this.webViewTHIntentPassedListener);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneLeaderboardDTO)
        {
            LeaderboardDefDTO leaderboardDefDTO = ((CompetitionZoneLeaderboardDTO) competitionZoneDTO).competitionDTO.leaderboard;
            CompetitionLeaderboardMarkUserListFragment.putProviderId(args, providerId);
            CompetitionLeaderboardMarkUserListFragment.putCompetition(args, ((CompetitionZoneLeaderboardDTO) competitionZoneDTO).competitionDTO);

            OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();
            if (ownedPortfolioId != null)
            {
                CompetitionLeaderboardMarkUserListFragment.putApplicablePortfolioId(args, ownedPortfolioId);
            }

            if (navigator != null)
            {
                navigator.get().pushFragment(CompetitionLeaderboardMarkUserListFragment.class, args);
            }
        }
        else if (competitionZoneDTO instanceof CompetitionZoneLegalDTO)
        {
            // Nothing to do, handled by Listener
        }
        else if (competitionZoneDTO instanceof CompetitionZoneAdvertisementDTO)
        {
            AdDTO adDTO = ((CompetitionZoneAdvertisementDTO) competitionZoneDTO).getAdDTO();
            if (adDTO != null && adDTO.redirectUrl != null)
            {
                String url = providerUtil.appendUserId(adDTO.redirectUrl, '&');
                CompetitionWebViewFragment.putUrl(args, url);
                navigator.get().pushFragment(CompetitionWebViewFragment.class, args);
            }
        }
        else if (competitionZoneDTO instanceof CompetitionZoneDisplayCellDTO)
        {
            handleDisplayCellClicked((CompetitionZoneDisplayCellDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZonePreSeasonDTO)
        {
            CompetitionPreseasonDialogFragment dialog = CompetitionPreseasonDialogFragment.newInstance(providerId);
            dialog.show(getActivity().getFragmentManager(), CompetitionPreseasonDialogFragment.TAG);
        }
        // TODO others?
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

    @Nullable public OwnedPortfolioId getApplicablePortfolioId()
    {
        if ((applicablePortfolioId == null) && (providerDTO != null))
        {
            applicablePortfolioId = providerDTO.getAssociatedOwnedPortfolioId();
        }
        return applicablePortfolioId;
    }

    protected void registerAdapterUserActions()
    {
        onStopSubscriptions.add(competitionZoneListItemAdapter.getUserActionObservable()
                .subscribe(
                        new Action1<AbstractCompetitionZoneListItemView.UserAction>()
                        {
                            @Override public void call(AbstractCompetitionZoneListItemView.UserAction userAction)
                            {
                                handleUserAction(userAction);
                            }
                        },
                        new Action1<Throwable>()
                        {
                            @Override public void call(Throwable throwable)
                            {
                                Timber.e(throwable, "When listening to adapter clicks");
                            }
                        }));
    }

    private void handleUserAction(@NonNull AbstractCompetitionZoneListItemView.UserAction userAction)
    {
        if (userAction instanceof CompetitionZonePrizePoolView.UserAction)
        {
            navigator.get().pushFragment(FriendsInvitationFragment.class);
        }
        else if (userAction instanceof CompetitionZoneLegalMentionsView.UserAction)
        {
            CompetitionZoneLegalMentionsView.LinkType linkType =
                    ((CompetitionZoneLegalMentionsView.UserAction) userAction).linkType;
            Bundle args = new Bundle();
            if (linkType.equals(CompetitionZoneLegalMentionsView.LinkType.RULES))
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
        else if (userAction instanceof AdView.UserAction)
        {
            Bundle bundle = new Bundle();
            String url = ((AdView.UserAction) userAction).adDTO.redirectUrl + String.format("&userId=%d", currentUserId.get());
            WebViewFragment.putUrl(bundle, url);
            navigator.get().pushFragment(WebViewFragment.class, bundle);
        }
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

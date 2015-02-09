package com.tradehero.th.fragments.trending;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import butterknife.InjectView;
import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.CollectionUtils;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.adapters.DTOAdapterNew;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderDTOList;
import com.tradehero.th.api.competition.key.ProviderListKey;
import com.tradehero.th.api.market.ExchangeCompactDTODescriptionNameComparator;
import com.tradehero.th.api.market.ExchangeCompactDTOList;
import com.tradehero.th.api.market.ExchangeCompactDTOUtil;
import com.tradehero.th.api.market.ExchangeIntegerId;
import com.tradehero.th.api.market.ExchangeListType;
import com.tradehero.th.api.portfolio.AssetClass;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.key.SecurityListType;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.THBillingInteractorRx;
import com.tradehero.th.fragments.competition.CompetitionEnrollmentWebViewFragment;
import com.tradehero.th.fragments.competition.MainCompetitionFragment;
import com.tradehero.th.fragments.security.SecurityPagedViewDTOAdapter;
import com.tradehero.th.fragments.security.SecuritySearchFragment;
import com.tradehero.th.fragments.social.friend.FriendsInvitationFragment;
import com.tradehero.th.fragments.trade.BuySellStockFragment;
import com.tradehero.th.fragments.trending.filter.TrendingFilterSelectorView;
import com.tradehero.th.fragments.trending.filter.TrendingFilterSpinnerIconAdapterNew;
import com.tradehero.th.fragments.trending.filter.TrendingFilterTypeBasicDTO;
import com.tradehero.th.fragments.trending.filter.TrendingFilterTypeDTO;
import com.tradehero.th.fragments.tutorial.WithTutorial;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTO;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTOList;
import com.tradehero.th.persistence.competition.ProviderListCacheRx;
import com.tradehero.th.persistence.market.ExchangeCompactListCacheRx;
import com.tradehero.th.persistence.market.ExchangeMarketPreference;
import com.tradehero.th.persistence.prefs.PreferredExchangeMarket;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.utils.metrics.events.TrendingStockEvent;
import com.tradehero.th.widget.MultiScrollListener;
import javax.inject.Inject;
import rx.android.app.AppObservable;
import timber.log.Timber;

@Routable("trending-securities")
public class TrendingStockFragment extends TrendingBaseFragment
        implements WithTutorial
{
    @Inject ExchangeCompactListCacheRx exchangeCompactListCache;
    @Inject ProviderListCacheRx providerListCache;
    @Inject Analytics analytics;
    @Inject @PreferredExchangeMarket ExchangeMarketPreference preferredExchangeMarket;

    @InjectView(R.id.trending_filter_selector_view) protected TrendingFilterSelectorView filterSelectorView;
    private DTOAdapterNew<ExchangeCompactSpinnerDTO> exchangeAdapter;

    private UserProfileDTO userProfileDTO;
    private ProviderDTOList providerDTOs;
    private ExchangeCompactSpinnerDTOList exchangeCompactSpinnerDTOs;
    @NonNull private TrendingFilterTypeDTO trendingFilterTypeDTO;

    private ExtraTileAdapterNew wrapperAdapter;
    @Inject protected THBillingInteractorRx userInteractorRx;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        trendingFilterTypeDTO = new TrendingFilterTypeBasicDTO(getResources());
        wrapperAdapter = createSecurityItemViewAdapter();

        exchangeAdapter = new TrendingFilterSpinnerIconAdapterNew(
                getActivity(),
                R.layout.trending_filter_spinner_item);
        exchangeAdapter.setDropDownViewResource(R.layout.trending_filter_spinner_dropdown_item);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_stock_trending, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.filterSelectorView.setExchangeAdapter(exchangeAdapter);
        this.filterSelectorView.apply(this.trendingFilterTypeDTO);
        this.listView.setAdapter(wrapperAdapter);
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchFilter();
        fetchExchangeList();
        fetchUserProfile();
        fetchProviderList();
    }

    @Override public void onResume()
    {
        super.onResume();

        analytics.fireEvent(new SimpleEvent(AnalyticsConstants.TabBar_Trade));
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.search_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.btn_search:
                pushSearchIn();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onDestroy()
    {
        exchangeAdapter = null;
        wrapperAdapter = null;
        super.onDestroy();
    }

    @Override @NonNull protected SecurityPagedViewDTOAdapter createItemViewAdapter()
    {
        return new SecurityPagedViewDTOAdapter(getActivity(), R.layout.trending_security_item);
    }

    @NonNull protected ExtraTileAdapterNew createSecurityItemViewAdapter()
    {
        return new ExtraTileAdapterNew(getActivity(), itemViewAdapter);
    }

    @Override @NonNull protected AbsListView.OnScrollListener createListViewScrollListener()
    {
        int trendingFilterHeight = (int) getResources().getDimension(R.dimen.trending_filter_view_pager_height);
        QuickReturnListViewOnScrollListener filterQuickReturnScrollListener =
                new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER, filterSelectorView,
                        -trendingFilterHeight, null, 0);
        return new MultiScrollListener(super.createListViewScrollListener(), filterQuickReturnScrollListener);
    }

    private void fetchFilter()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                this.filterSelectorView.getObservableFilter())
                .subscribe(
                        this::onNext,
                        this::onErrorFilter));
    }

    protected void onNext(@NonNull TrendingFilterTypeDTO trendingFilterTypeDTO)
    {
        boolean hasChanged = !trendingFilterTypeDTO.equals(this.trendingFilterTypeDTO);
        this.trendingFilterTypeDTO = trendingFilterTypeDTO;
        if (hasChanged)
        {
            preferredExchangeMarket.set(trendingFilterTypeDTO.exchange.getExchangeIntegerId());
            scheduleRequestData();
        }
        else
        {
            if (nearEndScrollListener != null)
            {
                nearEndScrollListener.lowerEndFlag();
                nearEndScrollListener.activateEnd();
            }
            requestDtos();
        }
    }

    protected void onErrorFilter(@NonNull Throwable e)
    {
        Timber.e(e, "Error with filter");
    }

    private void fetchExchangeList()
    {
        ExchangeListType key = new ExchangeListType();
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                exchangeCompactListCache.get(key)
                        .map(new PairGetSecond<>()))
                .subscribe(
                        this::linkWith,
                        e -> {
                            THToast.show(getString(R.string.error_fetch_exchange_list_info));
                            Timber.e("Error fetching the list of exchanges", e);
                        }));
    }

    private void linkWith(@NonNull ExchangeCompactDTOList exchangeDTOs)
    {
        ExchangeCompactSpinnerDTOList spinnerList = new ExchangeCompactSpinnerDTOList(
                getResources(),
                ExchangeCompactDTOUtil.filterAndOrderForTrending(
                        exchangeDTOs,
                        new ExchangeCompactDTODescriptionNameComparator<>()));
        // Adding the "All" choice
        spinnerList.add(0, new ExchangeCompactSpinnerDTO(getResources()));
        linkWith(spinnerList);
    }

    private void linkWith(@NonNull ExchangeCompactSpinnerDTOList exchangeCompactSpinnerDTOs)
    {
        this.exchangeCompactSpinnerDTOs = exchangeCompactSpinnerDTOs;
        setUpFilterSelectorView();
    }

    private void fetchUserProfile()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get().get(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<>()))
                .subscribe(
                        this::linkWith,
                        e -> THToast.show(R.string.error_fetch_user_profile)));
    }

    private void linkWith(UserProfileDTO userProfileDTO)
    {
        this.userProfileDTO = userProfileDTO;
        setUpFilterSelectorView();
        wrapperAdapter.setSurveyEnabled(userProfileDTO.activeSurveyImageURL != null);
    }

    private void fetchProviderList()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                providerListCache.get(new ProviderListKey())
                        .map(new PairGetSecond<>()))
                .subscribe(
                        this::linkWith,
                        e -> THToast.show(R.string.error_fetch_provider_competition_list)));
    }

    protected void linkWith(@NonNull ProviderDTOList providers)
    {
        providerDTOs = providers;
        wrapperAdapter.setProviderEnabled(!providerDTOs.isEmpty());
    }

    @Override public boolean canMakePagedDtoKey()
    {
        return true;
    }

    @NonNull @Override public SecurityListType makePagedDtoKey(int page)
    {
        return trendingFilterTypeDTO.getSecurityListType(page, perPage);
    }

    @Override public int getTutorialLayout()
    {
        return R.layout.tutorial_trending_screen;
    }

    @Override protected void startAnew()
    {
        wrapperAdapter.clearExtraTiles();
        super.startAnew();
    }

    private void setUpFilterSelectorView()
    {
        setDefaultExchange();
        if (exchangeCompactSpinnerDTOs != null)
        {
            exchangeAdapter.clear();
            exchangeAdapter.addAll(exchangeCompactSpinnerDTOs);
            exchangeAdapter.notifyDataSetChanged();
        }
        filterSelectorView.apply(trendingFilterTypeDTO);
    }

    private void setDefaultExchange()
    {
        if (exchangeCompactSpinnerDTOs != null)
        {
            if (userProfileDTO != null)
            {
                preferredExchangeMarket.setDefaultIfUnset(exchangeCompactSpinnerDTOs, userProfileDTO);
            }
            ExchangeIntegerId preferredExchangeId = preferredExchangeMarket.getExchangeIntegerId();
            ExchangeCompactSpinnerDTO initial = exchangeCompactSpinnerDTOs.findFirstWhere(
                    exchange -> exchange.getExchangeIntegerId().equals(preferredExchangeId));
            if (initial != null)
            {
                onNext(trendingFilterTypeDTO.getByExchange(initial));
            }
        }
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object item = parent.getItemAtPosition(position);
        if (isDetached() || item == null)
        {
            return;
        }
        View child = parent.getChildAt(position - parent.getFirstVisiblePosition());
        if (item instanceof SecurityCompactDTO)
        {
            handleSecurityItemOnClick((SecurityCompactDTO) item);
        }
        else if (item instanceof TileType)
        {
            handleExtraTileItemOnClick((TileType) item, child);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled item " + item);
        }
    }

    private void handleExtraTileItemOnClick(@NonNull TileType item, View view)
    {
        switch (item)
        {
            case EarnCredit:
                handleEarnCreditItemOnClick();
                break;
            case ExtraCash:
                handleExtraCashItemOnClick();
                break;
            case ResetPortfolio:
                handleResetPortfolioItemOnClick();
                break;
            case Survey:
                handleSurveyItemOnClick();
                break;
            case FromProvider:
                handleProviderTileOnClick((ProviderTileView) view);
                break;
        }
    }

    private void handleProviderTileOnClick(ProviderTileView view)
    {
        int providerId = view.getProviderId();
        ProviderDTO providerDTO = CollectionUtils.first(providerDTOs, candidate -> candidate.id == providerId);
        handleCompetitionItemClicked(providerDTO);
    }

    private void handleCompetitionItemClicked(@Nullable ProviderDTO providerDTO)
    {
        if (providerDTO != null && providerDTO.isUserEnrolled)
        {
            Bundle args = new Bundle();
            MainCompetitionFragment.putProviderId(args, providerDTO.getProviderId());
            MainCompetitionFragment.putApplicablePortfolioId(args, providerDTO.getAssociatedOwnedPortfolioId());
            navigator.get().pushFragment(MainCompetitionFragment.class, args);
        }
        else if (providerDTO != null)
        {
            navigator.get().pushFragment(CompetitionEnrollmentWebViewFragment.class, providerDTO.getProviderId().getArgs());
        }
    }

    private void handleSurveyItemOnClick()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get().get(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<>())
                        .first())
                .subscribe(
                        profile -> {
                            if (profile.activeSurveyURL != null)
                            {
                                Bundle bundle = new Bundle();
                                WebViewFragment.putUrl(bundle, profile.activeSurveyURL);
                                navigator.get().pushFragment(WebViewFragment.class, bundle, null);
                            }
                        },
                        error -> THToast.show(new THException((Throwable) error))
                ));
    }

    private void handleResetPortfolioItemOnClick()
    {
        //noinspection unchecked
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userInteractorRx.purchaseAndClear(ProductIdentifierDomain.DOMAIN_RESET_PORTFOLIO))
                .subscribe(
                        result -> {
                        },
                        error -> THToast.show(new THException((Throwable) error))
                ));
    }

    protected void handleExtraCashItemOnClick()
    {
        //noinspection unchecked
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userInteractorRx.purchaseAndClear(ProductIdentifierDomain.DOMAIN_VIRTUAL_DOLLAR))
                .subscribe(
                        result -> {
                        },
                        error -> THToast.show(new THException((Throwable) error))
                ));
    }

    private void handleEarnCreditItemOnClick()
    {
        navigator.get().pushFragment(FriendsInvitationFragment.class);
    }

    private void handleSecurityItemOnClick(@NonNull SecurityCompactDTO securityCompactDTO)
    {
        analytics.fireEvent(new TrendingStockEvent(securityCompactDTO.getSecurityId()));

        Bundle args = new Bundle();
        BuySellStockFragment.putSecurityId(args, securityCompactDTO.getSecurityId());

        OwnedPortfolioId ownedPortfolioId = getApplicablePortfolioId();

        if (ownedPortfolioId != null)
        {
            BuySellStockFragment.putApplicablePortfolioId(args, ownedPortfolioId);
        }

        navigator.get().pushFragment(BuySellStockFragment.class, args);
    }

    @Override protected void populateArgumentForSearch(@NonNull Bundle args)
    {
        super.populateArgumentForSearch(args);
        SecuritySearchFragment.putAssetClass(args, AssetClass.STOCKS);
    }
}

package com.tradehero.th.fragments.trending;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.common.SlidingTabLayout;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.R;
import com.tradehero.th.activities.BaseActivity;
import com.tradehero.th.adapters.DTOAdapterNew;
import com.tradehero.th.api.market.ExchangeCompactDTODescriptionNameComparator;
import com.tradehero.th.api.market.ExchangeCompactDTOList;
import com.tradehero.th.api.market.ExchangeCompactDTOUtil;
import com.tradehero.th.api.market.ExchangeIntegerId;
import com.tradehero.th.api.market.ExchangeListType;
import com.tradehero.th.api.portfolio.AssetClass;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.ActionBarOwnerMixin;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.fragments.base.BaseLiveFragmentUtil;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.base.TrendingLiveFragmentUtil;
import com.tradehero.th.fragments.fxonboard.FxOnBoardDialogFragment;
import com.tradehero.th.fragments.market.ExchangeSpinner;
import com.tradehero.th.fragments.position.FXMainPositionListFragment;
import com.tradehero.th.fragments.trending.filter.TrendingFilterSpinnerIconAdapter;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTO;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTOList;
import com.tradehero.th.persistence.market.ExchangeCompactListCacheRx;
import com.tradehero.th.persistence.market.ExchangeMarketPreference;
import com.tradehero.th.persistence.prefs.PreferredExchangeMarket;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.TimberOnErrorAction;
import com.tradehero.th.rx.ToastAndLogOnErrorAction;
import com.tradehero.th.rx.view.DismissDialogAction0;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import com.tradehero.th.utils.route.THRouter;
import com.tradehero.th.widget.OffOnViewSwitcher;
import com.tradehero.th.widget.OffOnViewSwitcherEvent;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@Routable({
        "trending-securities",
        "trending-stocks/tab-index/:stockPageIndex",
        "trending-stocks/exchange/:exchangeId",
        "trending-fx/tab-index/:fxPageIndex",
})
public class TrendingMainFragment extends DashboardFragment
{
    private static final String KEY_ASSET_CLASS = TrendingMainFragment.class.getName() + ".assetClass";
    private static final String KEY_EXCHANGE_ID = TrendingMainFragment.class.getName() + ".exchangeId";

    @Bind(R.id.pager) ViewPager tabViewPager;
    @Bind(R.id.tabs) SlidingTabLayout pagerSlidingTabStrip;
    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject THRouter thRouter;
    @Inject Toolbar toolbar;
    @Inject Analytics analytics;
    @Inject @PreferredExchangeMarket ExchangeMarketPreference preferredExchangeMarket;
    @Inject ExchangeCompactListCacheRx exchangeCompactListCache;

    @RouteProperty("stockPageIndex") Integer selectedStockPageIndex;
    @RouteProperty("fxPageIndex") Integer selectedFxPageIndex;
    @RouteProperty("exchangeId") Integer routedExchangeId;

    @NonNull private static TrendingTabType lastType = TrendingTabType.STOCK;
    @NonNull private static TrendingStockTabType lastStockTab = TrendingStockTabType.getDefault();
    @NonNull private static TrendingFXTabType lastFXTab = TrendingFXTabType.getDefault();

    private TradingStockPagerAdapter tradingStockPagerAdapter;
    private TradingFXPagerAdapter tradingFXPagerAdapter;
    private boolean fetchedFXPortfolio = false;
    private Observable<UserProfileDTO> userProfileObservable;
    @Nullable private OwnedPortfolioId fxPortfolioId;
    public static boolean fxDialogShowed = false;
    private TrendingLiveFragmentUtil trendingLiveFragmentUtil;
    private OffOnViewSwitcher stockFxSwitcher;
    private ExchangeSpinner exchangeSpinner;
    private DTOAdapterNew<ExchangeCompactSpinnerDTO> exchangeAdapter;
    private BehaviorSubject<ExchangeCompactSpinnerDTO> exchangeSpinnerDTOSubject;
    private ExchangeCompactSpinnerDTOList exchangeCompactSpinnerDTOList;

    public static void registerAliases(@NonNull THRouter router)
    {
        router.registerAlias("trending-fx/my-fx", "trending-fx/tab-index/" + TrendingFXTabType.Portfolio.ordinal());
        router.registerAlias("trending-fx/trade-fx", "trending-fx/tab-index/" + TrendingFXTabType.FX.ordinal());
        router.registerAlias("trending-stocks/my-stocks", "trending-stocks/tab-index/" + TrendingStockTabType.StocksMain.ordinal());
        router.registerAlias("trending-stocks/favorites", "trending-stocks/tab-index/" + TrendingStockTabType.Favorites.ordinal());
        router.registerAlias("trending-stocks/trending", "trending-stocks/tab-index/" + TrendingStockTabType.Trending.ordinal());
        router.registerAlias("trending-stocks/price-action", "trending-stocks/tab-index/" + TrendingStockTabType.Price.ordinal());
        router.registerAlias("trending-stocks/unusual-volumes", "trending-stocks/tab-index/" + TrendingStockTabType.Volume.ordinal());
        router.registerAlias("trending-stocks/all-trending", "trending-stocks/tab-index/" + TrendingStockTabType.All.ordinal());
    }

    public static void putAssetClass(@NonNull Bundle args, @NonNull AssetClass assetClass)
    {
        args.putInt(KEY_ASSET_CLASS, assetClass.getValue());
    }

    @Nullable private static AssetClass getAssetClass(@NonNull Bundle args)
    {
        if (!args.containsKey(KEY_ASSET_CLASS))
        {
            return null;
        }
        return AssetClass.create(args.getInt(KEY_ASSET_CLASS));
    }

    public static void putExchangeId(@NonNull Bundle args, @NonNull ExchangeIntegerId exchangeId)
    {
        args.putBundle(KEY_EXCHANGE_ID, exchangeId.getArgs());
    }

    @Nullable private static ExchangeIntegerId getExchangeId(@NonNull Bundle args)
    {
        if (!args.containsKey(KEY_EXCHANGE_ID))
        {
            return null;
        }
        return new ExchangeIntegerId(args.getBundle(KEY_EXCHANGE_ID));
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        initUserProfileObservable();
    }

    private void initUserProfileObservable()
    {
        userProfileObservable = userProfileCache.getOne(currentUserId.toUserBaseKey())
                .subscribeOn(Schedulers.computation())
                .map(new Func1<Pair<UserBaseKey, UserProfileDTO>, UserProfileDTO>()
                {
                    @Override public UserProfileDTO call(Pair<UserBaseKey, UserProfileDTO> pair)
                    {
                        fetchedFXPortfolio = true;
                        if (pair.second.fxPortfolio == null)
                        {
                            fxPortfolioId = null;
                        }
                        else
                        {
                            fxPortfolioId = pair.second.fxPortfolio.getOwnedPortfolioId();
                        }
                        return pair.second;
                    }
                })
                .cache(1);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getArguments().remove(KEY_EXCHANGE_ID);
        tradingStockPagerAdapter = new TradingStockPagerAdapter(getChildFragmentManager());
        tradingFXPagerAdapter = new TradingFXPagerAdapter(getChildFragmentManager());
        AssetClass askedAssetClass = getAssetClass(getArguments());
        if (askedAssetClass != null)
        {
            try
            {
                lastType = TrendingTabType.getForAssetClass(askedAssetClass);
            } catch (IllegalArgumentException e)
            {
                Timber.e(e, "Unhandled assetClass for user " + currentUserId.get());
            }
        }

        exchangeSpinnerDTOSubject = BehaviorSubject.create();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.trending_main_fragment, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        trendingLiveFragmentUtil = new TrendingLiveFragmentUtil(this, view);
        pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override public void onPageScrolled(int i, float v, int i2)
            {
            }

            @Override public void onPageSelected(int i)
            {
                switch (lastType)
                {
                    case STOCK:
                        lastStockTab = TrendingStockTabType.values()[i];
                        break;
                    case FX:
                        lastFXTab = TrendingFXTabType.values()[i];
                        break;
                    default:
                        throw new RuntimeException("Unhandled TrendingTabType." + lastType);
                }
            }

            @Override public void onPageScrollStateChanged(int i)
            {
            }
        });

        initViews();
        analytics.fireEvent(new SimpleEvent(AnalyticsConstants.TabBar_Trade));
    }

    private void initViews()
    {
        if (fxPortfolioId == null)
        {
            lastType = TrendingTabType.STOCK;
        }
        if (tabViewPager == null)
        {
            Timber.e(new NullPointerException("Gotcha TabViewPager is null"), "TabViewPager is null");
        }
        if (lastType == null)
        {
            Timber.e(new NullPointerException("Gotcha lastType is null"), "lastType is null");
        }
        tabViewPager.setAdapter(lastType.equals(TrendingTabType.STOCK) ? tradingStockPagerAdapter : tradingFXPagerAdapter);
        if (!Constants.RELEASE)
        {
            tabViewPager.setOffscreenPageLimit(0);
        }
        pagerSlidingTabStrip.setCustomTabView(R.layout.th_page_indicator, android.R.id.title);
        pagerSlidingTabStrip.setDistributeEvenly(!lastType.equals(TrendingTabType.STOCK));
        pagerSlidingTabStrip.setSelectedIndicatorColors(getResources().getColor(R.color.tradehero_tab_indicator_color));
        pagerSlidingTabStrip.setViewPager(tabViewPager);
    }

    @Override public void onResume()
    {
        super.onResume();
        thRouter.inject(this, getArguments());
    }

    @Override public boolean shouldShowLiveTradingToggle()
    {
        return lastType.equals(TrendingTabType.STOCK);
    }

    @Override public void onLiveTradingChanged(boolean isLive)
    {
        super.onLiveTradingChanged(isLive);
        BaseLiveFragmentUtil.setDarkBackgroundColor(isLive, pagerSlidingTabStrip);
        trendingLiveFragmentUtil.setCallToAction(isLive);
    }

    @Override public void onDestroyOptionsMenu()
    {
        super.onDestroyOptionsMenu();
    }

    @Override public void onPause()
    {
        super.onPause();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.unbind(this);
        trendingLiveFragmentUtil.onDestroyView();
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        this.tradingStockPagerAdapter = null;
        this.tradingFXPagerAdapter = null;
        this.exchangeAdapter = null;
        this.exchangeSpinnerDTOSubject = null;
        this.stockFxSwitcher = null;
        this.exchangeSpinner = null;
        super.onDestroy();
    }

    @Override public void onDetach()
    {
        userProfileObservable = null;
        super.onDetach();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflateCustomToolbarView();
        handlePageRouting();
    }

    private void inflateCustomToolbarView()
    {
        View view = LayoutInflater.from(actionBarOwnerMixin.getActionBar().getThemedContext())
                .inflate(R.layout.trending_custom_actionbar, toolbar, false);
        setActionBarTitle("");
        setupStockFxSwitcher(view);
        setupExchangeSpinner(view);
        actionBarOwnerMixin.setCustomView(view);
    }

    private void setupStockFxSwitcher(View view)
    {
        stockFxSwitcher = (OffOnViewSwitcher) view.findViewById(R.id.switch_stock_fx);
        onDestroyOptionsMenuSubscriptions.add(stockFxSwitcher.getSwitchObservable()
                .subscribe(new Action1<OffOnViewSwitcherEvent>()
                {
                    @Override public void call(final OffOnViewSwitcherEvent offOnViewSwitcherEvent)
                    {
                        final TrendingTabType oldType = lastType;

                        final ProgressDialog progressDialog;
                        if (!fetchedFXPortfolio && userProfileCache.getCachedValue(currentUserId.toUserBaseKey()) == null)
                        {
                            progressDialog =
                                    ProgressDialog.show(getActivity(), getString(R.string.loading_loading),
                                            getString(R.string.alert_dialog_please_wait));
                            progressDialog.setCanceledOnTouchOutside(true);
                        }
                        else
                        {
                            progressDialog = null;
                        }
                        Action0 dismissProgress = new DismissDialogAction0(progressDialog);

                        // We want to identify whether to:
                        // - wait for enough info
                        // - pop for FX enroll
                        // - just change the tab

                        if (userProfileObservable == null)
                        {
                            initUserProfileObservable();
                        }
                        onDestroyOptionsMenuSubscriptions.add(AppObservable.bindFragment(
                                TrendingMainFragment.this,
                                userProfileObservable)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnUnsubscribe(dismissProgress)
                                .finallyDo(dismissProgress)
                                .subscribe(new Subscriber<UserProfileDTO>()
                                {
                                    @Override public void onCompleted()
                                    {

                                    }

                                    @Override public void onError(Throwable e)
                                    {
                                        THToast.show(getString(R.string.error_fetch_your_user_profile));
                                    }

                                    @Override public void onNext(UserProfileDTO userProfileDTO)
                                    {
                                        if (offOnViewSwitcherEvent.isOn && userProfileDTO.fxPortfolio == null && fxPortfolioId == null)
                                        {
                                            if (fxDialogShowed)
                                            {
                                                return;
                                            }
                                            else
                                            {
                                                fxDialogShowed = true;
                                            }
                                            final FxOnBoardDialogFragment onBoardDialogFragment =
                                                    FxOnBoardDialogFragment.showOnBoardDialog(getActivity().getFragmentManager());
                                            onBoardDialogFragment.getUserActionTypeObservable()
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(
                                                            new Action1<FxOnBoardDialogFragment.UserAction>()
                                                            {
                                                                @Override public void call(FxOnBoardDialogFragment.UserAction action)
                                                                {
                                                                    handleUserEnrolledFX(action);
                                                                }
                                                            },
                                                            new TimberOnErrorAction("")
                                                    );
                                        }
                                        else
                                        {
                                            if (!offOnViewSwitcherEvent.isOn)
                                            {
                                                lastType = TrendingTabType.STOCK;
                                            }
                                            else
                                            {
                                                lastType = TrendingTabType.FX;
                                            }
                                            if (!oldType.equals(lastType))
                                            {
                                                clearChildFragmentManager();
                                                initViews();
                                                getActivity().supportInvalidateOptionsMenu();
                                            }
                                        }
                                    }
                                }));
                    }
                }));

        stockFxSwitcher.setIsOn(lastType.equals(TrendingTabType.FX), false);
    }

    private void setupExchangeSpinner(View view)
    {
        exchangeSpinner = (ExchangeSpinner) view.findViewById(R.id.exchange_selection_menu);
        if (lastType == TrendingTabType.FX)
        {
            exchangeSpinner.setVisibility(View.GONE);
            return;
        }
        else if (!TrendingStockTabType.values()[tabViewPager.getCurrentItem()].showExchangeSelection)
        {
            exchangeSpinner.setVisibility(View.GONE);
            return;
        }

        exchangeSpinner.setVisibility(View.VISIBLE);

        exchangeAdapter = new TrendingFilterSpinnerIconAdapter(
                getActivity(),
                R.layout.trending_filter_spinner_item_short);
        exchangeAdapter.setDropDownViewResource(R.layout.trending_filter_spinner_dropdown_item);
        exchangeSpinner.setAdapter(exchangeAdapter);

        exchangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                onExchangeSelected(parent, view, position, id);
            }

            @Override public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        ExchangeListType key = new ExchangeListType();
        onDestroyOptionsMenuSubscriptions.add(AppObservable.bindFragment(
                this,
                exchangeCompactListCache.getOne(key)
                        .map(new PairGetSecond<ExchangeListType, ExchangeCompactDTOList>()))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ExchangeCompactDTOList, ExchangeCompactSpinnerDTOList>()
                {
                    @Override public ExchangeCompactSpinnerDTOList call(ExchangeCompactDTOList exchangeDTOs)
                    {
                        ExchangeCompactSpinnerDTOList spinnerList = new ExchangeCompactSpinnerDTOList(
                                getResources(),
                                ExchangeCompactDTOUtil.filterAndOrderForTrending(
                                        exchangeDTOs,
                                        new ExchangeCompactDTODescriptionNameComparator<>()));
                        // Adding the "All" choice
                        spinnerList.add(0, new ExchangeCompactSpinnerDTO(getResources()));
                        return spinnerList;
                    }
                })
                .startWith(exchangeCompactSpinnerDTOList != null ? Observable.just(exchangeCompactSpinnerDTOList)
                        : Observable.<ExchangeCompactSpinnerDTOList>empty())
                .distinctUntilChanged()
                .doOnNext(new Action1<ExchangeCompactSpinnerDTOList>()
                {
                    @Override public void call(ExchangeCompactSpinnerDTOList exchangeCompactSpinnerDTOs)
                    {
                        exchangeCompactSpinnerDTOList = exchangeCompactSpinnerDTOs;
                    }
                })
                .subscribe(
                        new Action1<ExchangeCompactSpinnerDTOList>()
                        {
                            @Override public void call(ExchangeCompactSpinnerDTOList list)
                            {
                                exchangeAdapter.addAll(list);
                                exchangeAdapter.notifyDataSetChanged();
                                handleExchangeRouting();
                            }
                        },
                        new ToastAndLogOnErrorAction(
                                getString(R.string.error_fetch_exchange_list_info),
                                "Error fetching the list of exchanges")));
    }

    protected void onExchangeSelected(AdapterView<?> parent, View view, int position, long id)
    {
        ExchangeCompactSpinnerDTO dto = (ExchangeCompactSpinnerDTO) parent.getItemAtPosition(position);
        preferredExchangeMarket.set(dto.getExchangeIntegerId());
        exchangeSpinnerDTOSubject.onNext(dto);
    }

    protected void handleUserEnrolledFX(@NonNull FxOnBoardDialogFragment.UserAction userAction)
    {
        if (userAction.type.equals(FxOnBoardDialogFragment.UserActionType.ENROLLED))
        {
            //noinspection ConstantConditions
            fxPortfolioId = userAction.created.getOwnedPortfolioId();
            if (fxPortfolioId != null)
            {
                userProfileCache.invalidate(currentUserId.toUserBaseKey());
            }
            lastType = TrendingTabType.FX;
        }
        else
        {
            lastType = TrendingTabType.STOCK;
        }
        clearChildFragmentManager();
        initViews();
    }

    protected void clearChildFragmentManager()
    {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null)
        {
            fragments.clear();
        }
    }

    protected void handlePageRouting()
    {
        BaseActivity activity = (BaseActivity) getActivity();
        if (selectedStockPageIndex != null)
        {
            if (lastType.equals(TrendingTabType.STOCK))
            {
                if (tabViewPager != null)
                {
                    lastStockTab = TrendingStockTabType.values()[selectedStockPageIndex];
                    tabViewPager.setCurrentItem(selectedStockPageIndex, true);
                    selectedStockPageIndex = null;
                }
            }
            else if (actionBarOwnerMixin != null && activity != null)
            {
                lastType = TrendingTabType.STOCK;
                stockFxSwitcher.setIsOn(false, false);
            }
        }
        else if (selectedFxPageIndex != null)
        {
            if (lastType.equals(TrendingTabType.FX))
            {
                if (tabViewPager != null)
                {
                    lastFXTab = TrendingFXTabType.values()[selectedFxPageIndex];
                    tabViewPager.setCurrentItem(selectedFxPageIndex, true);
                    selectedFxPageIndex = null;
                }
            }
            else if (actionBarOwnerMixin != null && activity != null)
            {
                lastType = TrendingTabType.FX;
                stockFxSwitcher.setIsOn(true, false);
            }
        }
        else if (lastType.equals(TrendingTabType.STOCK))
        {
            tabViewPager.setCurrentItem(lastStockTab.ordinal(), true);
        }
        else if (lastType.equals(TrendingTabType.FX))
        {
            tabViewPager.setCurrentItem(lastFXTab.ordinal(), true);
        }
        else
        {
            throw new RuntimeException("Unhandled TrendingTabType." + lastType);
        }
        clearRoutingParam();
    }

    private void handleExchangeRouting()
    {
        if (routedExchangeId != null
                && lastType.equals(TrendingTabType.STOCK))
        {
            exchangeSpinner.setSelectionById(new ExchangeIntegerId(routedExchangeId));
            routedExchangeId = null;
        }
        else if (lastType.equals(TrendingTabType.STOCK))
        {
            exchangeSpinner.setSelectionById(new ExchangeIntegerId(preferredExchangeMarket.get()));
        }
    }

    private void clearRoutingParam()
    {
        //TODO to static
        getArguments().remove("stockPageIndex");
        getArguments().remove("fxPageIndex");
        getArguments().remove("exchangeId");
    }

    public Observable<ExchangeCompactSpinnerDTO> getExchangeSelectionObservable()
    {
        return exchangeSpinnerDTOSubject.asObservable();
    }

    public static void setLastType(@NonNull AssetClass assetClass)
    {
        if (assetClass.equals(AssetClass.STOCKS))
        {
            lastType = TrendingTabType.STOCK;
            lastStockTab = TrendingStockTabType.getDefault();
        }
        else if (assetClass.equals(AssetClass.FX))
        {
            lastType = TrendingTabType.FX;
            lastFXTab = TrendingFXTabType.getDefault();
        }
    }

    private class TradingStockPagerAdapter extends FragmentPagerAdapter
    {
        @NonNull final SparseArray<Fragment> registeredFragments;

        public TradingStockPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
            registeredFragments = new SparseArray<>();
        }

        @Override public Fragment getItem(int position)
        {
            Bundle args = new Bundle();
            ActionBarOwnerMixin.putKeyShowHomeAsUp(args, false);
            TrendingStockTabType tabType = TrendingStockTabType.values()[position];
            Class fragmentClass = tabType.fragmentClass;
            TrendingStockFragment.putTabType(args, tabType);
            Fragment created = Fragment.instantiate(getActivity(), fragmentClass.getName(), args);
            registeredFragments.put(position, created);
            return created;
        }

        @Override public CharSequence getPageTitle(int position)
        {
            return getString(TrendingStockTabType.values()[position].titleStringResId);
        }

        @Override public int getCount()
        {
            return TrendingStockTabType.values().length;
        }

        @Override public void destroyItem(ViewGroup container, int position, Object object)
        {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }
    }

    private class TradingFXPagerAdapter extends FragmentPagerAdapter
    {
        public TradingFXPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override public Fragment getItem(int position)
        {
            Bundle args = new Bundle();
            ActionBarOwnerMixin.putKeyShowHomeAsUp(args, false);
            Class fragmentClass = TrendingFXTabType.values()[position].fragmentClass;
            if (fragmentClass.equals((Class) FXMainPositionListFragment.class))
            {
                FXMainPositionListFragment.putMainFXPortfolioId(args, fxPortfolioId);
            }
            return Fragment.instantiate(getActivity(), fragmentClass.getName(), args);
        }

        @Override public CharSequence getPageTitle(int position)
        {
            return getString(TrendingFXTabType.values()[position].titleStringResId);
        }

        @Override public int getCount()
        {
            return TrendingFXTabType.values().length;
        }
    }
}

package com.tradehero.th.fragments.trade;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.special.ResideMenu.ResideMenu;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;
import com.tradehero.common.billing.ProductPurchase;
import com.tradehero.common.billing.exception.BillingException;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.alert.AlertId;
import com.tradehero.th.api.market.Exchange;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.portfolio.PortfolioCompactDTOList;
import com.tradehero.th.api.position.PositionDTOCompactList;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.quote.QuoteDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIdList;
import com.tradehero.th.api.security.TransactionFormDTO;
import com.tradehero.th.api.share.wechat.WeChatDTO;
import com.tradehero.th.api.share.wechat.WeChatMessageType;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.PurchaseReporter;
import com.tradehero.th.billing.request.THUIBillingRequest;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.alert.AlertCreateFragment;
import com.tradehero.th.fragments.alert.AlertEditFragment;
import com.tradehero.th.fragments.alert.BaseAlertEditFragment;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.fragments.security.BuySellBottomStockPagerAdapter;
import com.tradehero.th.fragments.security.StockInfoFragment;
import com.tradehero.th.fragments.security.WatchlistEditFragment;
import com.tradehero.th.fragments.social.SocialLinkHelper;
import com.tradehero.th.fragments.social.SocialLinkHelperFactory;
import com.tradehero.th.fragments.trade.view.QuickPriceButtonSet;
import com.tradehero.th.fragments.tutorial.WithTutorial;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.alert.SecurityAlertAssistant;
import com.tradehero.th.models.graphics.ForSecurityItemBackground;
import com.tradehero.th.models.graphics.ForSecurityItemForeground;
import com.tradehero.th.models.portfolio.MenuOwnedPortfolioId;
import com.tradehero.th.models.portfolio.MenuOwnedPortfolioIdFactory;
import com.tradehero.th.models.portfolio.MenuOwnedPortfolioIdList;
import com.tradehero.th.models.share.preference.SocialSharePreferenceHelperNew;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.SecurityServiceWrapper;
import com.tradehero.th.network.service.SocialServiceWrapper;
import com.tradehero.th.network.share.SocialSharer;
import com.tradehero.th.persistence.portfolio.PortfolioCache;
import com.tradehero.th.persistence.portfolio.PortfolioCompactCache;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.utils.AlertDialogUtil;
import com.tradehero.th.utils.DateUtils;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.utils.THSignedNumber;
import com.tradehero.th.utils.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.BuySellEvent;
import com.tradehero.th.utils.metrics.events.ChartTimeEvent;
import com.tradehero.th.utils.metrics.events.SharingOptionsEvent;
import dagger.Lazy;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

@Routable("security/:securityRawInfo")
public class BuySellFragment extends AbstractBuySellFragment
        implements SecurityAlertAssistant.OnPopulatedListener, ViewPager.OnPageChangeListener,
        WithTutorial
{
    public static final String EVENT_CHART_IMAGE_CLICKED = BuySellFragment.class.getName() + ".chartButtonClicked";
    private static final String BUNDLE_KEY_SELECTED_PAGE_INDEX = ".selectedPage";

    public static final int MS_DELAY_FOR_BG_IMAGE = 200;

    private static final boolean DEFAULT_IS_SHARED_TO_WECHAT = false;

    @InjectView(R.id.stock_bg_logo) protected ImageView mStockBgLogo;
    @InjectView(R.id.stock_logo) protected ImageView mStockLogo;
    @InjectView(R.id.portfolio_selector_container) protected View mSelectedPortfolioContainer;
    @InjectView(R.id.portfolio_selected) protected TextView mSelectedPortfolio;
    @InjectView(R.id.market_closed_icon) protected ImageView mMarketClosedIcon;
    @InjectView(R.id.buy_price) protected TextView mBuyPrice;
    @InjectView(R.id.sell_price) protected TextView mSellPrice;
    @InjectView(R.id.vprice_as_of) protected TextView mVpriceAsOf;
    @InjectView(R.id.info) protected TextView mInfoTextView;
    @InjectView(R.id.discussions) protected TextView mDiscussTextView;
    @InjectView(R.id.news) protected TextView mNewsTextView;

    @Inject ResideMenu resideMenu;

    //for dialog
    private AlertDialog mBuySellDialog;
    private TextView mTradeValueTextView;
    private TextView mCashLeftValueTextView;
    private TextView mQuantityTextView;
    private TextView mCashLeftTextView;
    private SeekBar mSlider;
    private QuickPriceButtonSet mQuickPriceButtonSet;
    private EditText mCommentsEditText;
    private ToggleButton mBtnShareFacebook;
    private ToggleButton mBtnShareTwitter;
    private ToggleButton mBtnShareLinkedIn;
    private ToggleButton mBtnShareWeChat;
    private ToggleButton mBtnShareWb;
    private ToggleButton mBtnLocation;
    private ToggleButton mBtnSharePublic;
    private Button mConfirmButton;
    private PushPortfolioFragmentRunnable pushPortfolioFragmentRunnable = null;
    private boolean isBuying = false;
    private boolean isSelling = false;
    private boolean shareLocation = false;
    private boolean sharePublic = false;

    @InjectView(R.id.quote_refresh_countdown) protected ProgressBar mQuoteRefreshProgressBar;
    @InjectView(R.id.chart_frame) protected RelativeLayout mInfoFrame;
    @InjectView(R.id.trade_bottom_pager) protected ViewPager mBottomViewPager;

    @InjectView(R.id.btn_buy) protected Button mBuyBtn;
    @InjectView(R.id.btn_sell) protected Button mSellBtn;
    @InjectView(R.id.btn_add_trigger) protected Button mBtnAddTrigger;
    @InjectView(R.id.btn_add_watch_list) protected Button mBtnAddWatchlist;

    @Inject PortfolioCache portfolioCache;
    @Inject PortfolioCompactCache portfolioCompactCache;
    @Inject MenuOwnedPortfolioIdFactory menuOwnedPortfolioIdFactory;
    @Inject ProgressDialogUtil progressDialogUtil;
    @Inject AlertDialogUtilBuySell alertDialogUtilBuySell;

    @Inject UserWatchlistPositionCache userWatchlistPositionCache;
    @Inject Picasso picasso;
    @Inject Lazy<SocialSharer> socialSharerLazy;
    @Inject @ForSecurityItemForeground protected Transformation foregroundTransformation;
    @Inject @ForSecurityItemBackground protected Transformation backgroundTransformation;

    @Inject AlertDialogUtil alertDialogUtil;
    @Inject SocialLinkHelperFactory socialLinkHelperFactory;
    @Inject SocialSharePreferenceHelperNew socialSharePreferenceHelperNew;

    private PopupMenu mPortfolioSelectorMenu;
    private Set<MenuOwnedPortfolioId> usedMenuOwnedPortfolioIds;

    @Inject protected SecurityAlertAssistant securityAlertAssistant;
    protected DTOCacheNew.Listener<UserBaseKey, SecurityIdList> userWatchlistPositionCacheFetchListener;

    private int mQuantity = 0;
    private Bundle desiredArguments;
    private String mPriceSelectionMethod;

    protected SecurityIdList watchedList;

    private BuySellBottomStockPagerAdapter bottomViewPagerAdapter;
    private int selectedPageIndex;
    @Inject SecurityServiceWrapper securityServiceWrapper;
    private MiddleCallback<SecurityPositionDetailDTO> buySellMiddleCallback;
    private ProgressDialog transactionDialog;
    SocialLinkHelper socialLinkHelper;
    @Inject SocialServiceWrapper socialServiceWrapper;
    private BroadcastReceiver chartImageButtonClickReceiver;

    @Inject Analytics analytics;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        chartImageButtonClickReceiver = createImageButtonClickBroadcastReceiver();
        userWatchlistPositionCacheFetchListener = createUserWatchlistCacheListener();
    }

    @Override protected DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList> createPortfolioCompactListFetchListener()
    {
        return new BuySellPortfolioCompactListFetchListener();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        if (desiredArguments == null)
        {
            desiredArguments = getArguments();
        }
        if (savedInstanceState != null)
        {
            selectedPageIndex = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_PAGE_INDEX);
        }

        View view = inflater.inflate(R.layout.fragment_buy_sell, container, false);
        initViews(view);
        resideMenu.addIgnoredView(mBottomViewPager);
        return view;
    }

    @Override protected void initViews(View view)
    {
        super.initViews(view);
        ButterKnife.inject(this, view);

        if (mQuoteRefreshProgressBar != null)
        {
            mQuoteRefreshProgressBar.setMax(
                    (int) (MILLISEC_QUOTE_REFRESH / MILLISEC_QUOTE_COUNTDOWN_PRECISION));
            mQuoteRefreshProgressBar.setProgress(mQuoteRefreshProgressBar.getMax());
        }

        if (mSelectedPortfolio != null)
        {
            mPortfolioSelectorMenu = new PopupMenu(getActivity(), mSelectedPortfolio);
            mPortfolioSelectorMenu.setOnMenuItemClickListener(
                    new PopupMenu.OnMenuItemClickListener()
                    {
                        @Override public boolean onMenuItemClick(android.view.MenuItem menuItem)
                        {
                            return selectDifferentPortfolio(menuItem);
                        }
                    });
        }

        if (bottomViewPagerAdapter == null)
        {
            bottomViewPagerAdapter =
                    new BuySellBottomStockPagerAdapter(((Fragment) this).getChildFragmentManager());
        }
        if (mBottomViewPager != null)
        {
            mBottomViewPager.setAdapter(bottomViewPagerAdapter);
            mBottomViewPager.setOnPageChangeListener(this);
        }

        selectPage(selectedPageIndex);
    }

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        progressDialogUtil.dismiss(getActivity());
        detachWatchlistFetchTask();
        detachBuySellMiddleCallback();

        outState.putInt(BUNDLE_KEY_SELECTED_PAGE_INDEX, selectedPageIndex);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        securityAlertAssistant.setOnPopulatedListener(this);
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.buy_sell_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        displayActionBarElements();
    }

    @Override public void onDestroyOptionsMenu()
    {
        super.onDestroyOptionsMenu();
    }

    @Override public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>

    @Override public void onStart()
    {
        super.onStart();
        analytics.fireEvent(new ChartTimeEvent(securityId, BuySellBottomStockPagerAdapter.getDefaultChartTimeSpan()));
    }

    @Override public void onResume()
    {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(chartImageButtonClickReceiver,
                        new IntentFilter(EVENT_CHART_IMAGE_CLICKED));

        mBottomViewPager.setCurrentItem(selectedPageIndex);
        securityAlertAssistant.setUserBaseKey(currentUserId.toUserBaseKey());
        securityAlertAssistant.populate();
    }

    @Override public void onPause()
    {
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(chartImageButtonClickReceiver);
        selectedPageIndex = mBottomViewPager.getCurrentItem();
        super.onPause();
    }

    @Override public void onDestroyView()
    {
        detachSocialLinkHelper();
        detachWatchlistFetchTask();
        detachBuySellMiddleCallback();

        securityAlertAssistant.setOnPopulatedListener(null);

        mSelectedPortfolio = null;

        if (mPortfolioSelectorMenu != null)
        {
            mPortfolioSelectorMenu.setOnMenuItemClickListener(null);
        }
        mPortfolioSelectorMenu = null;

        bottomViewPagerAdapter = null;
        mBottomViewPager = null;

        pushPortfolioFragmentRunnable = null;
        if (mBuySellDialog != null)
        {
            mBuySellDialog.dismiss();
            mBuySellDialog = null;
        }
        mTradeValueTextView = null;
        mCashLeftValueTextView = null;
        mQuantityTextView = null;
        mCashLeftTextView = null;
        if (mSlider != null)
        {
            mSlider.setOnSeekBarChangeListener(null);
        }
        mSlider = null;
        if (mQuickPriceButtonSet != null)
        {
            mQuickPriceButtonSet.setListener(null);
        }
        mQuickPriceButtonSet = null;
        mCommentsEditText = null;
        if (mBtnShareFacebook != null)
        {
            mBtnShareFacebook.setOnClickListener(null);
        }
        mBtnShareFacebook = null;
        if (mBtnShareTwitter != null)
        {
            mBtnShareTwitter.setOnClickListener(null);
        }
        mBtnShareTwitter = null;
        if (mBtnShareLinkedIn != null)
        {
            mBtnShareLinkedIn.setOnClickListener(null);
        }
        mBtnShareLinkedIn = null;
        if (mBtnShareWeChat != null)
        {
            mBtnShareWeChat.setOnClickListener(null);
        }
        mBtnShareWeChat = null;
        if (mBtnShareWb != null)
        {
            mBtnShareWb.setOnClickListener(null);
        }
        mBtnShareWb = null;
        if (mBtnLocation != null)
        {
            mBtnLocation.setOnClickListener(null);
        }
        mBtnLocation = null;
        if (mBtnSharePublic != null)
        {
            mBtnSharePublic.setOnCheckedChangeListener(null);
        }
        mBtnSharePublic = null;
        if (mConfirmButton != null)
        {
            mConfirmButton.setOnClickListener(null);
        }
        mConfirmButton = null;

        resideMenu.removeIgnoredView(mBottomViewPager);
        ButterKnife.reset(this);

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        userWatchlistPositionCacheFetchListener = null;
        chartImageButtonClickReceiver = null;
        securityAlertAssistant = null;
        super.onDestroy();
    }

    protected void detachSocialLinkHelper()
    {
        if (socialLinkHelper != null)
        {
            socialLinkHelper.setSocialLinkingCallback(null);
        }
        socialLinkHelper = null;
    }

    protected void detachWatchlistFetchTask()
    {
        userWatchlistPositionCache.unregister(userWatchlistPositionCacheFetchListener);
    }

    @Override public void linkWith(SecurityId securityId, boolean andDisplay)
    {
        super.linkWith(securityId, andDisplay);

        fetchWatchlist();

        if (andDisplay)
        {
            displayWatchlistButton();
        }
    }

    public void fetchWatchlist()
    {
        detachWatchlistFetchTask();
        userWatchlistPositionCache.register(currentUserId.toUserBaseKey(), userWatchlistPositionCacheFetchListener);
        userWatchlistPositionCache.getOrFetchAsync(currentUserId.toUserBaseKey());
    }

    @Override public void linkWith(SecurityCompactDTO securityCompactDTO, boolean andDisplay)
    {
        super.linkWith(securityCompactDTO, andDisplay);
        buildUsedMenuPortfolios();

        if (andDisplay)
        {
            displaySelectedPortfolioContainer();
            displayPortfolioSelectorMenu();
            displaySelectedPortfolio();
            displayStockName();
            displayBottomViewPager();
            loadStockLogo();
            displayBuySellPrice();
            displayAsOf();
        }
    }

    @Override public void linkWith(final SecurityPositionDetailDTO securityPositionDetailDTO,
            boolean andDisplay)
    {
        super.linkWith(securityPositionDetailDTO, andDisplay);

        buildUsedMenuPortfolios();
        setInitialSellQuantityIfCan();

        if (andDisplay)
        {
            displaySelectedPortfolioContainer();
            displayPortfolioSelectorMenu();
            displaySelectedPortfolio();
            displayQuickPriceButtonSet();
            displayBuySellSwitch();
            displayBuySellPrice();
        }
    }

    @Override
    public void linkWith(final PositionDTOCompactList positionDTOCompacts, boolean andDisplay)
    {
        super.linkWith(positionDTOCompacts, andDisplay);
        if (andDisplay)
        {

        }
    }

    @Override public void linkWith(UserProfileDTO userProfileDTO, boolean andDisplay)
    {
        super.linkWith(userProfileDTO, andDisplay);
        setInitialBuyQuantityIfCan();
        setInitialSellQuantityIfCan();
        if (andDisplay)
        {
            displayQuickPriceButtonSet();
        }
    }

    @Override protected void linkWith(QuoteDTO quoteDTO, boolean andDisplay)
    {
        super.linkWith(quoteDTO, andDisplay);
        setInitialBuyQuantityIfCan();
        setInitialSellQuantityIfCan();
        if (andDisplay)
        {
            displayBuySellPrice();
            displayAsOf();
            displayQuickPriceButtonSet();
            displayBuySellSwitch();
        }
    }

    @Override protected void linkWithApplicable(OwnedPortfolioId purchaseApplicablePortfolioId,
            boolean andDisplay)
    {
        super.linkWithApplicable(purchaseApplicablePortfolioId, andDisplay);
        if (purchaseApplicablePortfolioId != null)
        {
            linkWith(portfolioCompactCache.get(purchaseApplicablePortfolioId.getPortfolioIdKey()),
                    andDisplay);
            purchaseApplicableOwnedPortfolioId = purchaseApplicablePortfolioId;
        }
        else
        {
            linkWith((PortfolioCompactDTO) null, andDisplay);
        }
        if (andDisplay)
        {
            displayBuySellSwitch();
            displaySelectedPortfolio();
        }
    }

    @Override protected void linkWith(PortfolioCompactDTO portfolioCompactDTO, boolean andDisplay)
    {
        super.linkWith(portfolioCompactDTO, andDisplay);
        clampBuyQuantity(andDisplay);
        clampSellQuantity(andDisplay);
        if (andDisplay)
        {
            // TODO max purchasable shares
            displayBuySellPrice();
        }
    }

    protected void linkWithWatchlist(SecurityIdList watchedList, boolean andDisplay)
    {
        this.watchedList = watchedList;
        if (andDisplay)
        {
            displayWatchlistButton();
        }
    }

    protected void setInitialBuyQuantityIfCan()
    {
        if (mBuyQuantity == null)
        {
            Integer maxPurchasableShares = getMaxPurchasableShares();
            if (maxPurchasableShares != null)
            {
                linkWithBuyQuantity((int) Math.ceil(((double) maxPurchasableShares) / 2), true);
            }
        }
    }

    protected void setInitialSellQuantityIfCan()
    {
        if (mSellQuantity == null)
        {
            Integer maxSellableShares = getMaxSellableShares();
            if (maxSellableShares != null)
            {
                linkWithSellQuantity(maxSellableShares, true);
                if (maxSellableShares == 0)
                {
                    setTransactionTypeBuy(true);
                }
            }
        }
    }

    //<editor-fold desc="Display Methods"> //hide switch portfolios for temp
    protected void buildUsedMenuPortfolios()
    {
        Set<MenuOwnedPortfolioId> newMenus = new TreeSet<>();

        MenuOwnedPortfolioIdList menus = menuOwnedPortfolioIdFactory.createPortfolioMenus(
                currentUserId.toUserBaseKey(),
                securityPositionDetailDTO);

        newMenus.addAll(menus);
        usedMenuOwnedPortfolioIds = newMenus;
    }

    public void display()
    {
        displayActionBarElements();
        displayPageElements();

        //TODO not know do what, temp remove by alex
        //int avgDailyVolume = 0;
        //if (securityCompactDTO == null || securityCompactDTO.averageDailyVolume == null)
        //{
        //    avgDailyVolume = 0;
        //}
        //else
        //{
        //    avgDailyVolume = (int) Math.ceil(securityCompactDTO.averageDailyVolume);
        //}
        //
        //int volume = 0;
        //if (securityCompactDTO == null || securityCompactDTO.volume == null)
        //{
        //    volume = 0;
        //}
        //else
        //{
        //    volume = (int) Math.ceil(securityCompactDTO.volume);
        //}
    }

    public void displayPageElements()
    {
        displaySelectedPortfolioContainer();
        displayPortfolioSelectorMenu();
        displaySelectedPortfolio();
        displayBuySellPrice();
        displayBottomViewPager();
        displayStockName();
        displayQuickPriceButtonSet();
        displayTriggerButton();
        loadStockLogo();
    }

    public void displaySelectedPortfolioContainer()
    {
        if (mSelectedPortfolioContainer != null)
        {
            mSelectedPortfolioContainer.setVisibility(
                    usedMenuOwnedPortfolioIds != null && usedMenuOwnedPortfolioIds.size() > 1
                            ? View.VISIBLE : View.GONE);
        }
    }

    public void displayPortfolioSelectorMenu()
    {
        if (mPortfolioSelectorMenu != null)
        {
            mPortfolioSelectorMenu.getMenu().clear();
            if (usedMenuOwnedPortfolioIds != null)
            {
                for (MenuOwnedPortfolioId menuOwnedPortfolioId : usedMenuOwnedPortfolioIds)
                {
                    mPortfolioSelectorMenu.getMenu()
                            .add(Menu.NONE, Menu.NONE, Menu.NONE, menuOwnedPortfolioId);
                }
            }
        }
    }

    public void displaySelectedPortfolio()
    {
        TextView selectedPortfolio = mSelectedPortfolio;
        if (selectedPortfolio != null)
        {
            if (usedMenuOwnedPortfolioIds != null
                    && usedMenuOwnedPortfolioIds.size() > 0
                    && purchaseApplicableOwnedPortfolioId != null)
            {
                MenuOwnedPortfolioId chosen = null;

                final Iterator<MenuOwnedPortfolioId> iterator =
                        usedMenuOwnedPortfolioIds.iterator();
                MenuOwnedPortfolioId lastElement = null;
                while (iterator.hasNext())
                {
                    lastElement = iterator.next();
                    //Timber.d("lyl lastElement.title=%s", lastElement.title);
                    if (purchaseApplicableOwnedPortfolioId.equals(lastElement))
                    {
                        chosen = lastElement;
                    }
                }
                if (chosen == null)
                {
                    chosen = lastElement;
                }

                selectedPortfolio.setText(chosen);
            }
        }
    }

    public void displayBottomViewPager()
    {
        BuySellBottomStockPagerAdapter adapter = bottomViewPagerAdapter;
        if (adapter != null)
        {
            adapter.linkWith(securityId);
            adapter.notifyDataSetChanged();
        }
    }

    public void displayStockName()
    {
        updateStockAndSymbol();

        if (mMarketClosedIcon != null)
        {
            boolean marketIsOpen = securityCompactDTO == null
                    || securityCompactDTO.marketOpen == null
                    || securityCompactDTO.marketOpen;
            mMarketClosedIcon.setVisibility(marketIsOpen ? View.GONE : View.VISIBLE);
        }
    }

    public void updateStockAndSymbol()
    {
        String title = "";
        String symbol = "";
        if (securityCompactDTO != null)
        {
            title = securityCompactDTO.name;
            symbol = securityCompactDTO.getExchangeSymbol();
        }
        setActionBarTitle(getString(R.string.security_action_bar_title, title, symbol));
    }

    public void displayBuySellPrice()
    {
        if (mBuyPrice != null)
        {
            String display = securityCompactDTO == null ? "-" : securityCompactDTO.currencyDisplay;
            String bPrice;
            String sPrice;
            THSignedNumber bthSignedNumber;
            THSignedNumber sthSignedNumber;
            if (quoteDTO == null)
            {
                return;
            }
            else
            {
                if (quoteDTO.ask == null)
                {
                    bPrice = getString(R.string.buy_sell_ask_price_not_available);
                }
                else
                {
                    bthSignedNumber =
                            new THSignedNumber(THSignedNumber.TYPE_MONEY, quoteDTO.ask, THSignedNumber.WITHOUT_SIGN, "");
                    bPrice = bthSignedNumber.toString();
                }

                if (quoteDTO.bid == null)
                {
                    sPrice = getString(R.string.buy_sell_bid_price_not_available);
                }
                else
                {
                    sthSignedNumber =
                            new THSignedNumber(THSignedNumber.TYPE_MONEY, quoteDTO.bid, THSignedNumber.WITHOUT_SIGN, "");
                    sPrice = sthSignedNumber.toString();
                }
            }
            String buyPriceText = getString(R.string.buy_sell_button_buy, display, bPrice);
            String sellPriceText = getString(R.string.buy_sell_button_sell, display, sPrice);
            mBuyPrice.setText(buyPriceText);
            mSellPrice.setText(sellPriceText);
        }
    }

    public void displayAsOf()
    {
        if (mVpriceAsOf != null)
        {
            String text;
            if (quoteDTO != null && quoteDTO.asOfUtc != null)
            {
                text = DateUtils.getFormattedDate(getResources(), quoteDTO.asOfUtc);
            }
            else if (securityCompactDTO != null
                    && securityCompactDTO.lastPriceDateAndTimeUtc != null)
            {
                text = DateUtils.getFormattedDate(getResources(), securityCompactDTO.lastPriceDateAndTimeUtc);
            }
            else
            {
                text = "";
            }
            mVpriceAsOf.setText(
                    getResources().getString(R.string.buy_sell_price_as_of) + " " + text);
        }
    }

    public void displayQuickPriceButtonSet()
    {
        QuickPriceButtonSet buttonSetCopy = mQuickPriceButtonSet;
        if (buttonSetCopy != null)
        {
            if (quoteDTO == null)
            {
                buttonSetCopy.setEnabled(false);
            }
            else if (isTransactionTypeBuy && quoteDTO.ask == null)
            {
                buttonSetCopy.setEnabled(false);
            }
            else if (isTransactionTypeBuy)
            {
                buttonSetCopy.setEnabled(true);
                if (this.userProfileDTO != null && userProfileDTO.portfolio != null)
                {
                    buttonSetCopy.setMaxPrice(userProfileDTO.portfolio.cashBalance);
                }
            }
            else if (!isTransactionTypeBuy && (quoteDTO.bid == null || quoteDTO.toUSDRate == null))
            {
                buttonSetCopy.setEnabled(false);
            }
            else if (!isTransactionTypeBuy)
            {
                buttonSetCopy.setEnabled(true);
                Integer maxSellableShares = getMaxSellableShares();
                if (maxSellableShares != null)
                {
                    // TODO see other currencies
                    buttonSetCopy.setMaxPrice(
                            maxSellableShares * quoteDTO.bid * quoteDTO.toUSDRate);
                }
            }
        }
    }

    public void displayActionBarElements()
    {
        displayBuySellSwitch();
    }

    public void displayBuySellSwitch()
    {
        boolean supportSell = false;
        if (positionDTOCompactList == null
                || positionDTOCompactList.size() == 0
                || purchaseApplicableOwnedPortfolioId == null)
        {
            supportSell = false;
        }
        else
        {
            Integer maxSellableShares = getMaxSellableShares();
            supportSell = maxSellableShares != null && maxSellableShares.intValue() > 0;
        }
        if (mSellBtn != null)
        {
            mSellBtn.setVisibility(supportSell ? View.VISIBLE : View.GONE);
        }
    }

    public void displayTriggerButton()
    {
        if (mBtnAddTrigger != null)
        {
            if (securityAlertAssistant.isPopulated())
            {
                mBtnAddTrigger.setEnabled(true);
                if (securityAlertAssistant.getAlertId(securityId) != null)
                {
                    mBtnAddTrigger.setText(R.string.stock_alert_edit_alert);
                }
                else
                {
                    mBtnAddTrigger.setText(R.string.stock_alert_add_alert);
                }
            }
            else // TODO check if failed
            {
                mBtnAddTrigger.setEnabled(false);
            }
        }
    }

    public void displayWatchlistButton()
    {
        if (mBtnAddWatchlist != null)
        {
            if (securityId == null || watchedList == null)
            {
                // TODO show disabled
                mBtnAddWatchlist.setEnabled(false);
            }
            else
            {
                mBtnAddWatchlist.setEnabled(true);
                mBtnAddWatchlist.setText(watchedList.contains(securityId) ?
                        R.string.watchlist_edit_title :
                        R.string.watchlist_add_title);
            }
        }
    }

    public void loadStockLogo()
    {
        if (mStockLogo != null)
        {
            if (mStockBgLogo != null)
            {
                mStockBgLogo.setVisibility(View.INVISIBLE);
            }
            if (isMyUrlOk())
            {
                picasso.load(securityCompactDTO.imageBlobUrl)
                        .transform(foregroundTransformation)
                        .into(mStockLogo, new Callback()
                        {
                            @Override public void onSuccess()
                            {
                                loadStockBgLogoDelayed();
                            }

                            @Override public void onError()
                            {
                                loadStockLogoExchange();
                            }
                        });
            }
            else
            {
                loadStockLogoExchange();
            }
        }
        else
        {
            loadStockBgLogoDelayed();
        }
    }

    public void loadStockLogoExchange()
    {
        if (mStockLogo != null)
        {
            if (securityCompactDTO != null && securityCompactDTO.exchange != null)
            {
                try
                {
                    Exchange exchange = Exchange.valueOf(securityCompactDTO.exchange);
                    mStockLogo.setImageResource(exchange.logoId);
                    loadStockBgLogoDelayed();
                } catch (IllegalArgumentException e)
                {
                    Timber.e("Unknown Exchange %s", securityCompactDTO.exchange, e);
                    loadStockLogoDefault();
                } catch (OutOfMemoryError e)
                {
                    Timber.e(e, securityCompactDTO.exchange);
                    loadStockLogoDefault();
                }
            }
            else
            {
                loadStockLogoDefault();
            }
        }
        else
        {
            loadStockBgLogoDelayed();
        }
    }

    public void loadStockLogoDefault()
    {
        if (mStockLogo != null)
        {
            mStockLogo.setImageResource(R.drawable.default_image);
        }
        loadStockBgLogoDelayed();
    }

    public void loadStockBgLogoDelayed()
    {
        View rootView = getView();
        if (rootView != null)
        {
            rootView.postDelayed(new Runnable()
            {
                @Override public void run()
                {
                    loadStockBgLogo();
                }
            }, MS_DELAY_FOR_BG_IMAGE);
        }
    }

    public void loadStockBgLogo()
    {
        if (mStockBgLogo != null)
        {
            if (isMyUrlOk())
            {
                RequestCreator requestCreator = picasso.load(securityCompactDTO.imageBlobUrl)
                        .transform(backgroundTransformation);
                resizeBackground(requestCreator, mStockBgLogo, new Callback()
                {
                    @Override public void onSuccess()
                    {
                        if (mStockBgLogo != null)
                        {
                            mStockBgLogo.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override public void onError()
                    {
                        loadStockBgLogoExchange();
                    }
                });
            }
            else
            {
                loadStockBgLogoExchange();
            }
        }
    }

    public void loadStockBgLogoExchange()
    {
        if (mStockBgLogo != null)
        {
            if (securityCompactDTO != null && securityCompactDTO.exchange != null)
            {
                try
                {
                    Exchange exchange = Exchange.valueOf(securityCompactDTO.exchange);
                    RequestCreator requestCreator = picasso.load(exchange.logoId)
                            .transform(backgroundTransformation);
                    resizeBackground(requestCreator, mStockBgLogo, null);
                    mStockBgLogo.setVisibility(View.VISIBLE);
                } catch (IllegalArgumentException e)
                {
                    loadStockBgLogoDefault();
                }
            }
            else
            {
                loadStockBgLogoDefault();
            }
        }
    }

    public void loadStockBgLogoDefault()
    {
        if (mStockBgLogo != null)
        {
            mStockBgLogo.setImageResource(R.drawable.default_image);
        }
    }

    protected void resizeBackground(RequestCreator requestCreator, ImageView imageView,
            Callback callback)
    {
        //int width = mInfoFrame.getWidth();
        //int height = mInfoFrame.getHeight();
        int width = mStockBgLogo.getWidth();
        int height = mStockBgLogo.getHeight();
        if (width > 0 && height > 0)
        {
            requestCreator.resize(width, height)
                    .centerCrop()
                    .into(imageView, callback);
        }
    }
    //</editor-fold>

    public boolean isMyUrlOk()
    {
        return (securityCompactDTO != null) && isUrlOk(securityCompactDTO.imageBlobUrl);
    }

    public static boolean isUrlOk(String url)
    {
        return (url != null) && (url.length() > 0);
    }

    @Override public void setTransactionTypeBuy(boolean transactionTypeBuy)
    {
        super.setTransactionTypeBuy(transactionTypeBuy);
        displayQuickPriceButtonSet();
        displayBuySellSwitch();
    }

    @Override protected void setRefreshingQuote(boolean refreshingQuote)
    {
        super.setRefreshingQuote(refreshingQuote);
    }

    @Override protected void prepareFreshQuoteHolder()
    {
        super.prepareFreshQuoteHolder();
        freshQuoteHolder.identifier = "BuySellFragment";
    }

    private void handleBtnAddCashPressed()
    {
        cancelOthersAndShowProductDetailList(ProductIdentifierDomain.DOMAIN_VIRTUAL_DOLLAR);
    }

    @Override public THUIBillingRequest getShowProductDetailRequest(ProductIdentifierDomain domain)
    {
        THUIBillingRequest billingRequest = super.getShowProductDetailRequest(domain);
        billingRequest.purchaseReportedListener = new BuySellPurchaseReportedListener();
        return billingRequest;
    }

    @OnClick(R.id.btn_add_trigger)
    protected void handleBtnAddTriggerClicked()
    {
        if (securityAlertAssistant.isPopulated())
        {
            Bundle args = new Bundle();
            OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId();
            if (applicablePortfolioId != null)
            {
                BaseAlertEditFragment.putApplicablePortfolioId(args, applicablePortfolioId);
            }
            AlertId alertId = securityAlertAssistant.getAlertId(securityId);
            if (alertId != null)
            {
                AlertEditFragment.putAlertId(args, alertId);
                getDashboardNavigator().pushFragment(AlertEditFragment.class, args);
            }
            else
            {
                AlertCreateFragment.putSecurityId(args, securityId);
                getDashboardNavigator().pushFragment(AlertCreateFragment.class, args);
            }
        }
        else if (securityAlertAssistant.isFailed())
        {
            THToast.show("We do not know if you already have an alert on it");
        }
        else
        {
            THToast.show("Try again in a moment");
        }
    }

    @OnClick(R.id.btn_add_watch_list)
    protected void handleBtnWatchlistClicked()
    {
        if (securityId != null)
        {
            Bundle args = new Bundle();
            WatchlistEditFragment.putSecurityId(args, securityId);
            getDashboardNavigator().pushFragment(WatchlistEditFragment.class, args);
        }
        else
        {
            THToast.show(R.string.watchlist_not_enough_info);
        }
    }

    @OnClick(R.id.portfolio_selector_container)
    protected void showPortfolioSelector()
    {
        if (mPortfolioSelectorMenu != null)
        {
            mPortfolioSelectorMenu.show();
        }
    }

    private boolean selectDifferentPortfolio(MenuItem menuItem)
    {
        if (mSelectedPortfolio != null)
        {
            mSelectedPortfolio.setText(menuItem.getTitle());
        }

        linkWithApplicable((MenuOwnedPortfolioId) menuItem.getTitle(), true);
        return true;
    }

    @OnClick({R.id.btn_buy, R.id.btn_sell})
    protected void handleBuySellButtonsClicked(View view)
    {
        trackBuyClickEvent();
        switch (view.getId())
        {
            case R.id.btn_buy:
                isTransactionTypeBuy = true;
                break;
            case R.id.btn_sell:
                isTransactionTypeBuy = false;
                break;
            default:
                throw new IllegalArgumentException("Unhandled button " + view.getId());
        }
        showBuySellDialog();
    }

    @OnClick(R.id.market_closed_icon)
    protected void handleMarketClosedIconClicked()
    {
        notifyMarketClosed();
    }

    @OnClick(R.id.info)
    public void handleTabInfoClicked()
    {
        if (mBottomViewPager != null)
        {
            mBottomViewPager.setCurrentItem(0, true);
        }
    }

    @OnClick(R.id.discussions)
    public void handleDiscussionsTabClicked()
    {
        if (mBottomViewPager != null)
        {
            mBottomViewPager.setCurrentItem(1, true);
        }
    }

    @OnClick(R.id.news)
    public void handleNewsTabClicked()
    {
        if (mBottomViewPager != null)
        {
            mBottomViewPager.setCurrentItem(2, true);
        }
    }

    //<editor-fold desc="Interface Creators">
    private Callback createLogoReadyCallback()
    {
        return new Callback()
        {
            @Override public void onError()
            {
                loadBg();
            }

            @Override public void onSuccess()
            {
                loadBg();
            }

            public void loadBg()
            {
                if (mStockBgLogo != null && BuySellFragment.isUrlOk(
                        (String) mStockBgLogo.getTag(R.string.image_url)))
                {
                    Timber.i("Loading Bg for %s", mStockBgLogo.getTag(R.string.image_url));
                    picasso.load((String) mStockBgLogo.getTag(R.string.image_url))
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image)
                            .resize(mStockBgLogo.getWidth(), mStockBgLogo.getHeight())
                            .centerInside()
                            .transform(foregroundTransformation)
                            .into(mStockBgLogo);
                }
                else if (mStockBgLogo != null && securityCompactDTO != null)
                {
                    int logoId = securityCompactDTO.getExchangeLogoId();
                    if (logoId != 0)
                    {
                        picasso.load(logoId)
                                .resize(mStockBgLogo.getWidth(), mStockBgLogo.getHeight())
                                .centerCrop()
                                .transform(foregroundTransformation)
                                .into(mStockBgLogo);
                    }
                }
            }
        };
    }

    private OnSeekBarChangeListener createSeekBarListener()
    {
        return new OnSeekBarChangeListener()
        {
            @Override public void onStopTrackingTouch(SeekBar seekBar)
            {
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)
                {
                    mQuantity = progress;
                    mQuantityTextView.setText(String.valueOf(progress));
                    updateBuySellDialog();
                    mPriceSelectionMethod = AnalyticsConstants.Slider;
                }
            }
        };
    }

    public void setPublishToShareBySetting()
    {
        socialSharePreferenceHelperNew.load();
    }

    public void setPublishEnable(SocialNetworkEnum socialNetwork)
    {
        socialSharePreferenceHelperNew.updateSocialSharePreference(socialNetwork, true);
        switch (socialNetwork)
        {
            case FB:
                mBtnShareFacebook.setChecked(true);
                break;
            case TW:
                mBtnShareTwitter.setChecked(true);
                break;
            case LN:
                mBtnShareLinkedIn.setChecked(true);
                break;
            case WB:
                mBtnShareWb.setChecked(true);
                break;
        }
    }

    @Nullable public Boolean isSocialLinked(SocialNetworkEnum socialNetwork)
    {
        UserProfileDTO userProfileCopy = userProfileDTO;
        if (userProfileCopy != null)
        {
            switch (socialNetwork)
            {
                case FB:
                    return userProfileCopy.fbLinked;
                case TW:
                    return userProfileCopy.twLinked;
                case LN:
                    return userProfileCopy.liLinked;
                case WB:
                    return userProfileCopy.wbLinked;
                case WECHAT:
                    return null;
                default:
                    Timber.e(new IllegalArgumentException(), "Unhandled socialNetwork.%s", socialNetwork);
                    return false;
            }
        }
        return null;
    }

    public boolean isSocialLinkedOr(SocialNetworkEnum socialNetwork, boolean orElse)
    {
        @Nullable Boolean socialLinked = isSocialLinked(socialNetwork);
        return socialLinked != null ? socialLinked : orElse;
    }

    public void showBuySellDialog()
    {
        // TODO Move to DialogFragment? and pass the values to be shared using bundle/intent
        setPublishToShareBySetting();

        shareLocation = true;
        sharePublic = false;
        pushPortfolioFragmentRunnable = null;
        pushPortfolioFragmentRunnable = new PushPortfolioFragmentRunnable()
        {
            @Override
            public void pushPortfolioFragment(SecurityPositionDetailDTO securityPositionDetailDTO)
            {
                BuySellFragment.this.pushPortfolioFragment(securityPositionDetailDTO);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.security_buy_sell_dialog, null);
        view.setOnClickListener(new OnClickListener()
        {
            @Override public void onClick(View v)
            {
                DeviceUtil.dismissKeyboard(getActivity(), mCommentsEditText);
            }
        });
        builder.setView(view);
        builder.setCancelable(false);
        TextView stockNameTextView = (TextView) view.findViewById(R.id.dialog_stock_name);
        if (stockNameTextView != null && securityCompactDTO != null)
        {
            stockNameTextView.setText(securityCompactDTO.name);
        }
        TextView priceTextView = (TextView) view.findViewById(R.id.dialog_price);
        if (priceTextView != null)
        {
            priceTextView.setText(
                    isTransactionTypeBuy ? mBuyPrice.getText() : mSellPrice.getText());
        }
        ImageButton btnAddCash = (ImageButton) view.findViewById(R.id.dialog_btn_add_cash);
        if (btnAddCash != null)
        {
            btnAddCash.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    DeviceUtil.dismissKeyboard(getActivity(), mCommentsEditText);
                    handleBtnAddCashPressed();
                }
            });
        }

        mTradeValueTextView = null;
        mTradeValueTextView = (TextView) view.findViewById(R.id.vtrade_value);
        mQuantityTextView = null;
        mQuantityTextView = (TextView) view.findViewById(R.id.vquantity);
        if (mQuantityTextView != null)
        {
            mQuantityTextView.setText(String.valueOf(mQuantity));
        }
        mCashLeftValueTextView = null;
        mCashLeftValueTextView = (TextView) view.findViewById(R.id.vcash_left);

        mCashLeftTextView = (TextView) view.findViewById(R.id.dialog_cash_left);
        mCashLeftTextView.setText(
                isTransactionTypeBuy ? R.string.buy_sell_cash_left : R.string.buy_sell_share_left);

        mSlider = null;
        mSlider = (SeekBar) view.findViewById(R.id.seekBar);
        if (mSlider != null)
        {
            mSlider.setOnSeekBarChangeListener(createSeekBarListener());
            if (isTransactionTypeBuy)
            {
                Integer maxPurchasableShares = getMaxPurchasableShares();
                if (maxPurchasableShares != null)
                {
                    mSlider.setMax(maxPurchasableShares);
                    mSlider.setEnabled(maxPurchasableShares > 0);
                }
            }
            else
            {
                Integer maxSellableShares = getMaxSellableShares();
                if (maxSellableShares != null)
                {
                    mSlider.setMax(maxSellableShares);
                    mSlider.setEnabled(maxSellableShares > 0);
                }
            }
        }

        mQuickPriceButtonSet = null;
        mQuickPriceButtonSet = (QuickPriceButtonSet) view.findViewById(R.id.quick_price_button_set);
        if (mQuickPriceButtonSet != null)
        {
            mQuickPriceButtonSet.setListener(createQuickButtonSetListener());
            mQuickPriceButtonSet.addButton(R.id.toggle5k);
            mQuickPriceButtonSet.addButton(R.id.toggle10k);
            mQuickPriceButtonSet.addButton(R.id.toggle25k);
            mQuickPriceButtonSet.addButton(R.id.toggle50k);
        }
        displayQuickPriceButtonSet();

        //comments
        mCommentsEditText = null;
        mCommentsEditText = (EditText) view.findViewById(R.id.comments);

        //share
        mBtnShareFacebook = null;
        mBtnShareFacebook = (ToggleButton) view.findViewById(R.id.btn_share_fb);
        initSocialButton(mBtnShareFacebook, SocialNetworkEnum.FB);

        mBtnShareTwitter = null;
        mBtnShareTwitter = (ToggleButton) view.findViewById(R.id.btn_share_tw);
        initSocialButton(mBtnShareTwitter, SocialNetworkEnum.TW);

        mBtnShareLinkedIn = null;
        mBtnShareLinkedIn = (ToggleButton) view.findViewById(R.id.btn_share_li);
        initSocialButton(mBtnShareLinkedIn, SocialNetworkEnum.LN);

        mBtnShareWeChat = null;
        mBtnShareWeChat = (ToggleButton) view.findViewById(R.id.btn_wechat);
        initSocialButton(mBtnShareWeChat, SocialNetworkEnum.WECHAT, createCheckedChangeListenerForWechat());

        mBtnShareWb = null;
        mBtnShareWb = (ToggleButton) view.findViewById(R.id.btn_share_wb);
        initSocialButton(mBtnShareWb, SocialNetworkEnum.WB);

        mBtnLocation = null;
        mBtnLocation = (ToggleButton) view.findViewById(R.id.btn_location);
        mBtnLocation.setChecked(shareLocation);
        mBtnLocation.setOnClickListener(new OnClickListener()
        {
            @Override public void onClick(View view)
            {
                shareLocation = !shareLocation;
            }
        });
        mBtnLocation.setVisibility(View.GONE);

        mBtnSharePublic = null;
        mBtnSharePublic = (ToggleButton) view.findViewById(R.id.switch_share_public);
        mBtnSharePublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                sharePublic = b;
            }
        });
        mBtnSharePublic.setVisibility(View.GONE);

        //cancel button
        Button cancelButton = (Button) view.findViewById(R.id.dialog_btn_cancel);
        if (cancelButton != null)
        {
            cancelButton.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    if (mBuySellDialog != null)
                    {
                        mBuySellDialog.dismiss();
                    }
                }
            });
        }
        //confirm
        mConfirmButton = null;
        mConfirmButton = (Button) view.findViewById(R.id.dialog_btn_confirm);
        if (mConfirmButton != null)
        {
            mConfirmButton.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    if (mBuySellDialog != null)
                    {
                        mBuySellDialog.dismiss();
                    }
                    socialSharePreferenceHelperNew.save();
                    buySellReport();
                    launchBuySell();
                }
            });
        }

        if (mBuySellDialog != null)
        {
            mBuySellDialog.dismiss();
            mBuySellDialog = null;
        }

        updateBuySellDialog();
        mBuySellDialog = builder.create();
        mBuySellDialog.show();
    }

    private void buySellReport()
    {
        SharingOptionsEvent sharingOptionsEvent = new SharingOptionsEvent.Builder()
                .setBuyEvent(isTransactionTypeBuy)
                .setSecurityId(securityId)
                .setProviderId(portfolioCompactDTO.getProviderIdKey())
                .setPriceSelectionMethod(mPriceSelectionMethod)
                .hasComment(mCommentsEditText != null && !mCommentsEditText.getText().toString().isEmpty())
                .facebookEnabled(mBtnShareFacebook != null && mBtnShareFacebook.isChecked())
                .twitterEnabled(mBtnShareTwitter != null && mBtnShareTwitter.isChecked())
                .linkedInEnabled(mBtnShareLinkedIn != null && mBtnShareLinkedIn.isChecked())
                .wechatEnabled(mBtnShareWeChat != null && mBtnShareWeChat.isChecked())
                .weiboEnabled(mBtnShareWb != null && mBtnShareWb.isChecked())
                .build();

        analytics.fireEvent(sharingOptionsEvent);
    }

    private void initSocialButton(CompoundButton socialButton, SocialNetworkEnum socialNetworkEnum)
    {
        initSocialButton(socialButton, socialNetworkEnum, createCheckedChangeListener());
    }

    private void initSocialButton(CompoundButton socialButton, SocialNetworkEnum socialNetworkEnum,
            CompoundButton.OnCheckedChangeListener onCheckedChangedListener)
    {
        if (socialButton != null)
        {
            socialButton.setTag(socialNetworkEnum);
            socialButton.setChecked(initialShareButtonState(socialNetworkEnum));
            socialButton.setOnCheckedChangeListener(onCheckedChangedListener);
        }
    }

    protected boolean initialShareButtonState(@NotNull SocialNetworkEnum socialNetworkEnum)
    {
        return socialSharePreferenceHelperNew.isShareEnabled(
                socialNetworkEnum,
                isSocialLinkedOr(socialNetworkEnum, false));
    }

    public void askToLinkAccountToSocial(final SocialNetworkEnum socialNetwork)
    {
        alertDialogUtil.popWithOkCancelButton(
                getActivity(),
                getActivity().getApplicationContext().getString(R.string.link, socialNetwork.getName()),
                getActivity().getApplicationContext().getString(R.string.link_description, socialNetwork.getName()),
                R.string.link_now,
                R.string.later,
                new DialogInterface.OnClickListener()//Ok
                {
                    @Override public void onClick(DialogInterface dialogInterface, int i)
                    {
                        linkSocialNetwork(socialNetwork);
                    }
                },
                new DialogInterface.OnClickListener()//Cancel
                {
                    @Override public void onClick(DialogInterface dialogInterface, int i)
                    {
                        alertDialogUtil.dismissProgressDialog();
                    }
                }
        );
    }

    private CompoundButton.OnCheckedChangeListener createCheckedChangeListenerForWechat()
    {
        return new CompoundButton.OnCheckedChangeListener()
        {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if(!compoundButton.isPressed())
                {
                    return;
                }
                SocialNetworkEnum networkEnum = (SocialNetworkEnum) compoundButton.getTag();
                socialSharePreferenceHelperNew.updateSocialSharePreference(networkEnum, isChecked);
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener createCheckedChangeListener()
    {
        return new CompoundButton.OnCheckedChangeListener()
        {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if(!compoundButton.isPressed())
                {
                    return;
                }
                SocialNetworkEnum networkEnum = (SocialNetworkEnum) compoundButton.getTag();
                Boolean socialLinked = isSocialLinked(networkEnum);
                if (isChecked && (socialLinked == null || !socialLinked))
                {
                    if (socialLinked != null)
                    {
                        askToLinkAccountToSocial(networkEnum);
                    }
                    isChecked = false;
                }

                compoundButton.setChecked(isChecked);
                socialSharePreferenceHelperNew.updateSocialSharePreference(networkEnum, isChecked);
            }
        };
    }

    private void linkSocialNetwork(SocialNetworkEnum socialNetworkEnum)
    {
        detachSocialLinkHelper();
        socialLinkHelper = socialLinkHelperFactory.buildSocialLinkerHelper(socialNetworkEnum);
        socialLinkHelper.link(new SocialLinkingCallback(socialNetworkEnum));
    }

    private class SocialLinkingCallback implements retrofit.Callback<UserProfileDTO>
    {
        final SocialNetworkEnum socialNetworkEnum;

        SocialLinkingCallback(final SocialNetworkEnum socialNetworkEnum)
        {
            this.socialNetworkEnum = socialNetworkEnum;
        }

        @Override public void success(UserProfileDTO userProfileDTO, Response response)
        {
            linkWith(userProfileDTO, true);
            setPublishEnable(socialNetworkEnum);
        }

        @Override public void failure(RetrofitError retrofitError)
        {
        }
    }

    public void shareToWeChat()
    {
        if (socialSharePreferenceHelperNew.isShareEnabled(SocialNetworkEnum.WECHAT, DEFAULT_IS_SHARED_TO_WECHAT))
        {
            WeChatDTO weChatDTO = new WeChatDTO();
            weChatDTO.id = securityCompactDTO.id;
            weChatDTO.type = WeChatMessageType.Trade;
            if (isMyUrlOk())
            {
                weChatDTO.imageURL = securityCompactDTO.imageBlobUrl;
            }
            if (isTransactionTypeBuy)
            {
                weChatDTO.title = getString(R.string.buy_sell_switch_buy) + " "
                        + securityCompactDTO.name + " " + mQuantity + getString(
                        R.string.buy_sell_share_count) + " @" + quoteDTO.ask;
            }
            else
            {
                weChatDTO.title = getString(R.string.buy_sell_switch_sell) + " "
                        + securityCompactDTO.name + " " + mQuantity + getString(
                        R.string.buy_sell_share_count) + " @" + quoteDTO.bid;
            }
            socialSharerLazy.get().share(weChatDTO); // TODO proper callback?
        }
    }

    public void updateBuySellDialog()
    {
        String valueText = "-";
        String cashLeftText = getResources().getString(R.string.na);
        if (quoteDTO != null)
        {
            Double priceRefCcy = quoteDTO.getPriceRefCcy(portfolioCompactDTO, true);
            if (priceRefCcy != null && portfolioCompactDTO != null)
            {
                double value = mQuantity * priceRefCcy;
                THSignedNumber thTradeValue =
                        new THSignedNumber(THSignedNumber.TYPE_MONEY, value, THSignedNumber.WITHOUT_SIGN,
                                portfolioCompactDTO.currencyDisplay);
                valueText = thTradeValue.toString();

                if (isTransactionTypeBuy)
                {
                    double cashAvailable = portfolioCompactDTO.cashBalance;
                    THSignedNumber thSignedNumber =
                            new THSignedNumber(THSignedNumber.TYPE_MONEY, cashAvailable - value,
                                    THSignedNumber.WITHOUT_SIGN, portfolioCompactDTO.currencyDisplay);
                    cashLeftText = thSignedNumber.toString();
                }
            }
            if (!isTransactionTypeBuy
                    && positionDTOCompactList != null
                    && portfolioCompactDTO != null)
            {
                Integer maxSellableShares = getMaxSellableShares();
                if (maxSellableShares != null && maxSellableShares != 0)
                {
                    cashLeftText = String.valueOf(maxSellableShares - mQuantity);//share left
                }
            }
        }
        if (mTradeValueTextView != null)
        {
            mTradeValueTextView.setText(valueText);
        }
        if (mQuantityTextView != null)
        {
            mQuantityTextView.setText(String.valueOf(mQuantity));
        }
        if (mCashLeftValueTextView != null)
        {
            mCashLeftValueTextView.setText(cashLeftText);
        }
        if (mSlider != null)
        {
            mSlider.setProgress(mQuantity);
        }
        if (mConfirmButton != null)
        {
            mConfirmButton.setEnabled(mQuantity != 0 && (
                    (!isBuying && isTransactionTypeBuy && hasValidInfoForBuy()) ||
                            (!isSelling && !isTransactionTypeBuy && hasValidInfoForSell())
            ));
        }
    }

    protected boolean hasValidInfoForBuy()
    {
        return securityId != null
                && securityCompactDTO != null
                && quoteDTO != null
                && quoteDTO.ask != null;
    }

    protected boolean hasValidInfoForSell()
    {
        return securityId != null
                && securityCompactDTO != null
                && quoteDTO != null
                && quoteDTO.bid != null;
    }

    private void launchBuySell()
    {
        detachBuySellMiddleCallback();

        if (checkValidToBuyOrSell())
        {
            TransactionFormDTO transactionFormDTO = getBuySellOrder(isTransactionTypeBuy);
            if (transactionFormDTO != null)
            {
                transactionDialog = progressDialogUtil.show(BuySellFragment.this.getActivity(),
                        R.string.processing, R.string.alert_dialog_please_wait);

                buySellMiddleCallback = securityServiceWrapper.doTransaction(
                        securityId, transactionFormDTO, isTransactionTypeBuy,
                        new BuySellCallback(isTransactionTypeBuy));

                if (isTransactionTypeBuy)
                {
                    isBuying = true;
                }
                else
                {
                    isSelling = true;
                }
            }
            else
            {
                alertDialogUtilBuySell.informBuySellOrderWasNull(getActivity());
            }
        }
    }

    private boolean checkValidToBuyOrSell()
    {
        return securityId != null && securityId.getExchange() != null
                && securityId.getSecuritySymbol() != null;
    }

    private void detachBuySellMiddleCallback()
    {
        if (buySellMiddleCallback != null)
        {
            buySellMiddleCallback.setPrimaryCallback(null);
        }
        buySellMiddleCallback = null;
    }

    private TransactionFormDTO getBuySellOrder(boolean isBuy)
    {
        if (quoteDTO == null)
        {
            return null;
        }
        if (getApplicablePortfolioId() == null || getApplicablePortfolioId().portfolioId == null)
        {
            Timber.e("No portfolioId to apply to", new IllegalStateException());
            return null;
        }
        //Timber.d("fb=%b tw=%b li=%b location=%b public=%b quantity=%d", publishToFb,
        //        publishToTw, publishToLi, shareLocation, sharePublic,
        //        isBuy ? mBuyQuantity : mSellQuantity);

        return new TransactionFormDTO(
                shareForTransaction(SocialNetworkEnum.FB),
                shareForTransaction(SocialNetworkEnum.TW),
                shareForTransaction(SocialNetworkEnum.LN),
                shareForTransaction(SocialNetworkEnum.WB),
                shareLocation ? null : null, // TODO implement location
                shareLocation ? null : null,
                shareLocation ? null : null,
                sharePublic,
                mCommentsEditText == null ? null : mCommentsEditText.getText().toString(),
                quoteDTO.rawResponse,
                mQuantity,
                getApplicablePortfolioId().portfolioId
        );
    }

    protected boolean shareForTransaction(@NotNull SocialNetworkEnum socialNetworkEnum)
    {
        return socialSharePreferenceHelperNew.isShareEnabled(socialNetworkEnum, isSocialLinkedOr(socialNetworkEnum, true));
    }

    private void pushPortfolioFragment(SecurityPositionDetailDTO securityPositionDetailDTO)
    {
        if (securityPositionDetailDTO != null && securityPositionDetailDTO.portfolio != null)
        {
            pushPortfolioFragment(new OwnedPortfolioId(
                    currentUserId.get(),
                    securityPositionDetailDTO.portfolio.id));
        }
        else
        {
            pushPortfolioFragment();
        }
    }

    private void pushPortfolioFragment()
    {
        pushPortfolioFragment(getApplicablePortfolioId());
    }

    protected interface PushPortfolioFragmentRunnable
    {
        void pushPortfolioFragment(SecurityPositionDetailDTO securityPositionDetailDTO);
    }

    private void pushPortfolioFragment(OwnedPortfolioId ownedPortfolioId)
    {
        shareToWeChat();
        if (isResumed())
        {
            DashboardNavigator navigator = getDashboardNavigator();
            // TODO find a better way to remove this fragment from the stack
            navigator.popFragment();

            Bundle args = new Bundle();
            OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId();
            if (applicablePortfolioId != null)
            {
                PositionListFragment.putApplicablePortfolioId(args, applicablePortfolioId);
            }
            PositionListFragment.putGetPositionsDTOKey(args, ownedPortfolioId);
            PositionListFragment.putShownUser(args, ownedPortfolioId.getUserBaseKey());
            navigator.pushFragment(PositionListFragment.class, args);
        }
    }

    private void trackBuyClickEvent()
    {
        analytics.fireEvent(new BuySellEvent(isTransactionTypeBuy, securityId));
    }

    private QuickPriceButtonSet.OnQuickPriceButtonSelectedListener createQuickButtonSetListener()
    {
        return new QuickPriceButtonSet.OnQuickPriceButtonSelectedListener()
        {
            @Override public void onQuickPriceButtonSelected(double priceSelected)
            {
                if (quoteDTO == null)
                {
                    // Nothing to do
                }
                else
                {
                    Double priceRefCcy =
                            quoteDTO.getPriceRefCcy(portfolioCompactDTO, isTransactionTypeBuy);
                    if (priceRefCcy == null || priceRefCcy == 0)
                    {
                        // Nothing to do
                    }
                    else
                    {
                        linkWithBuyOrSellQuantity((int) Math.floor(priceSelected / priceRefCcy),
                                true);
                    }
                }

                Integer selectedQuantity = isTransactionTypeBuy ? mBuyQuantity : mSellQuantity;
                mQuantity = selectedQuantity != null ? selectedQuantity : 0;
                updateBuySellDialog();
                mPriceSelectionMethod = AnalyticsConstants.MoneySelection;
            }
        };
    }

    private void pushStockInfoFragmentIn()
    {
        Bundle args = new Bundle();
        args.putBundle(StockInfoFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, this.securityId.getArgs());
        if (providerId != null)
        {
            args.putBundle(StockInfoFragment.BUNDLE_KEY_PROVIDER_ID_BUNDLE,
                    providerId.getArgs());
        }
        getDashboardNavigator().pushFragment(StockInfoFragment.class, args);
    }

    private BroadcastReceiver createImageButtonClickBroadcastReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override public void onReceive(Context context, Intent intent)
            {
                pushStockInfoFragmentIn();
            }
        };
    }
    //</editor-fold>

    //<editor-fold desc="SecurityAlertAssistant.OnPopulatedListener">
    @Override
    public void onPopulateFailed(SecurityAlertAssistant securityAlertAssistant, Throwable error)
    {
        Timber.e("There was an error getting the alert ids", error);
        displayTriggerButton();
    }

    @Override public void onPopulated(SecurityAlertAssistant securityAlertAssistant)
    {
        displayTriggerButton();
    }
    //</editor-fold>

    @Override protected FreshQuoteHolder.FreshQuoteListener createFreshQuoteListener()
    {
        return new BuySellFreshQuoteListener();
    }

    @Override public int getTutorialLayout()
    {
        return R.layout.tutorial_buy_sell;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }

    @Override public void onPageSelected(int position)
    {
        selectPage(position);
    }

    @Override public void onPageScrollStateChanged(int state)
    {
    }

    public void selectPage(int position)
    {
        selectedPageIndex = position;
        mInfoTextView.setEnabled(position != 0);
        mDiscussTextView.setEnabled(position != 1);
        mNewsTextView.setEnabled(position != 2);
        mInfoTextView.setTextColor(getResources().getColor(
                position == 0 ? R.color.white : R.color.btn_twitter_color_end));
        mDiscussTextView.setTextColor(getResources().getColor(
                position == 1 ? R.color.white : R.color.btn_twitter_color_end));
        mNewsTextView.setTextColor(getResources().getColor(
                position == 2 ? R.color.white : R.color.btn_twitter_color_end));

        if (selectedPageIndex == 0)
        {
            resideMenu.clearIgnoredViewList();
        }
        else
        {
            resideMenu.addIgnoredView(mBottomViewPager);
        }
    }

    protected class BuySellFreshQuoteListener extends AbstractBuySellFreshQuoteListener
    {
        @Override public void onMilliSecToRefreshQuote(long milliSecToRefresh)
        {
            if (mQuoteRefreshProgressBar != null)
            {
                mQuoteRefreshProgressBar.setProgress(
                        (int) (milliSecToRefresh / MILLISEC_QUOTE_COUNTDOWN_PRECISION));
            }
        }
    }

    protected class BuySellPortfolioCompactListFetchListener extends BasePurchaseManagementPortfolioCompactListFetchListener
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull PortfolioCompactDTOList value)
        {
            super.onDTOReceived(key, value);
            buildUsedMenuPortfolios();
            setInitialSellQuantityIfCan();
            displayQuickPriceButtonSet();
        }
    }

    protected DTOCacheNew.Listener<UserBaseKey, SecurityIdList> createUserWatchlistCacheListener()
    {
        return new BuySellUserWatchlistCacheListener();
    }

    protected class BuySellUserWatchlistCacheListener
            implements DTOCacheNew.Listener<UserBaseKey, SecurityIdList>
    {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull SecurityIdList value)
        {
            linkWithWatchlist(value, true);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            Timber.e("Failed to fetch list of watch list items", error);
            THToast.show(R.string.error_fetch_portfolio_list_info);
        }
    }

    protected class BuySellPurchaseReportedListener
            implements PurchaseReporter.OnPurchaseReportedListener
    {
        @Override public void onPurchaseReported(int requestCode, ProductPurchase reportedPurchase,
                UserProfileDTO updatedUserProfile)
        {
            linkWith(updatedUserProfile, true);
            updateBuySellDialog();
        }

        @Override
        public void onPurchaseReportFailed(int requestCode, ProductPurchase reportedPurchase,
                BillingException error)
        {
        }
    }

    private class BuySellCallback implements retrofit.Callback<SecurityPositionDetailDTO>
    {
        private final boolean isBuy;

        public BuySellCallback(boolean isBuy)
        {
            this.isBuy = isBuy;
        }

        @Override
        public void success(SecurityPositionDetailDTO securityPositionDetailDTO, Response response)
        {
            onFinish();

            if (securityPositionDetailDTO == null)
            {
                alertDialogUtilBuySell.informBuySellOrderReturnedNull(getActivity());
                return;
            }

            if (isBuy)
            {
                isBuying = false;
            }
            else
            {
                isSelling = false;
            }

            if (pushPortfolioFragmentRunnable != null)
            {
                pushPortfolioFragmentRunnable.pushPortfolioFragment(securityPositionDetailDTO);
            }
        }

        private void onFinish()
        {
            if (transactionDialog != null)
            {
                transactionDialog.hide();
            }
        }

        @Override public void failure(RetrofitError retrofitError)
        {
            onFinish();
            if (retrofitError != null && retrofitError.getResponse() != null)
            {
                Timber.e(retrofitError, "Reporting the error to Crashlytics %s", retrofitError.getBody());
            }
            THToast.show(new THException(retrofitError));
        }
    }
}

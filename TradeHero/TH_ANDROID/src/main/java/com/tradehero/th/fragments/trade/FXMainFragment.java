package com.tradehero.th.fragments.trade;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import butterknife.InjectView;
import com.android.common.SlidingTabLayout;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.portfolio.PortfolioCompactDTOList;
import com.tradehero.th.api.quote.QuoteDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTOUtil;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.compact.FxSecurityCompactDTO;
import com.tradehero.th.api.security.key.FxPairSecurityId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.base.ActionBarOwnerMixin;
import com.tradehero.th.fragments.discussion.stock.SecurityDiscussionFragment;
import com.tradehero.th.fragments.position.SecurityPositionListFragment;
import com.tradehero.th.models.number.THSignedFXRate;
import com.tradehero.th.models.portfolio.MenuOwnedPortfolioId;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.THColorUtils;
import javax.inject.Inject;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import timber.log.Timber;

@Routable({
        "fx-security/:exchange/:symbol"
})
//TODO need refactor by alex
public class FXMainFragment extends AbstractBuySellFragment
{
    private final static long MILLISECOND_FX_QUOTE_REFRESH = 5000;
    @ColorRes private static final int DEFAULT_BUTTON_TEXT_COLOR = R.color.text_primary_inverse;

    @InjectView(R.id.pager) ViewPager tabViewPager;
    @InjectView(R.id.tabs) SlidingTabLayout pagerSlidingTabStrip;

    @Inject CurrentUserId currentUserId;

    private QuoteDTO oldQuoteDTO;
    private BuySellBottomFXPagerAdapter buySellBottomFXPagerAdapter;
    @Nullable private Observer<PortfolioCompactDTO> portfolioCompactDTOObserver;

    @RouteProperty("exchange") String exchange;
    @RouteProperty("symbol") String symbol;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_fx_main, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        pagerSlidingTabStrip.setCustomTabView(R.layout.th_page_indicator, android.R.id.title);
        pagerSlidingTabStrip.setSelectedIndicatorColors(getResources().getColor(R.color.tradehero_tab_indicator_color));
        pagerSlidingTabStrip.setDistributeEvenly(true);
    }

    @Override public void onDestroyView()
    {
        super.onDestroyView();
        this.oldQuoteDTO = null;
        buySellBottomFXPagerAdapter = null;
    }

    @NonNull @Override protected Observable<PortfolioCompactDTO> getDefaultPortfolio()
    {
        return portfolioCompactListCache.get(currentUserId.toUserBaseKey())
                .map(new Func1<Pair<UserBaseKey, PortfolioCompactDTOList>, PortfolioCompactDTO>()
                {
                    @Override public PortfolioCompactDTO call(Pair<UserBaseKey, PortfolioCompactDTOList> userBaseKeyPortfolioCompactDTOListPair)
                    {
                        PortfolioCompactDTO found = userBaseKeyPortfolioCompactDTOListPair.second.getDefaultFxPortfolio();
                        defaultPortfolio = found;
                        return found;
                    }
                })
                .share();
    }

    @NonNull @Override protected Requisite createRequisite()
    {
        if (exchange != null && symbol != null)
        {
            return new Requisite(new SecurityId(exchange, symbol), getArguments());
        }
        return super.createRequisite();
    }

    @Override protected void linkWith(@NonNull OwnedPortfolioId purchaseApplicablePortfolioId)
    {
        if (buySellBottomFXPagerAdapter == null)
        {
            buySellBottomFXPagerAdapter = new BuySellBottomFXPagerAdapter(
                    this.getChildFragmentManager(),
                    getActivity(),
                    purchaseApplicablePortfolioId,
                    requisite.securityId,
                    currentUserId.toUserBaseKey());
            tabViewPager.setAdapter(buySellBottomFXPagerAdapter);
            if (!Constants.RELEASE)
            {
                tabViewPager.setOffscreenPageLimit(0);
            }
            pagerSlidingTabStrip.setViewPager(tabViewPager);
            pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {
                @Override public void onPageScrolled(int i, float v, int i2)
                {
                }

                @Override public void onPageSelected(int i)
                {
                    selectedPortfolioContainer.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
                }

                @Override public void onPageScrollStateChanged(int i)
                {

                }
            });
        }
    }

    @Override protected void linkWith(PortfolioCompactDTO portfolioCompactDTO)
    {
        MenuOwnedPortfolioId currentMenu = selectedPortfolioContainer.getCurrentMenu();
        if (currentMenu != null && portfolioCompactDTO.id == currentMenu.portfolioId)
        {
            this.portfolioCompactDTO = portfolioCompactDTO;
            if (portfolioCompactDTOObserver != null)
            {
                portfolioCompactDTOObserver.onNext(portfolioCompactDTO);
            }
        }
        super.linkWith(portfolioCompactDTO);
    }

    @Override protected long getMillisecondQuoteRefresh()
    {
        return MILLISECOND_FX_QUOTE_REFRESH;
    }

    //<editor-fold desc="Display Methods"> //hide switch portfolios for temp
    @Override public void displayStockName(@NonNull SecurityCompactDTO securityCompactDTO)
    {
        super.displayStockName(securityCompactDTO);
        if (securityCompactDTO instanceof FxSecurityCompactDTO)
        {
            FxPairSecurityId fxPairSecurityId = ((FxSecurityCompactDTO) securityCompactDTO).getFxPair();
            setActionBarTitle(String.format("%s/%s", fxPairSecurityId.left, fxPairSecurityId.right));
            setActionBarSubtitle(null);
        }
    }

    @Override public void displayBuySellPrice(@NonNull SecurityCompactDTO securityCompactDTO, @NonNull QuoteDTO quoteDTO)
    {
        if (buyBtn != null && sellBtn != null)
        {
            int precision = 0;
            if (quoteDTO.ask != null && quoteDTO.bid != null)
            {
                precision = SecurityCompactDTOUtil.getExpectedPrecision(quoteDTO.ask, quoteDTO.bid);
            }

            if (quoteDTO.ask == null)
            {
                buyBtn.setText(R.string.buy_sell_ask_price_not_available);
            }
            else
            {
                double diff = (oldQuoteDTO != null && oldQuoteDTO.ask != null) ? quoteDTO.ask - oldQuoteDTO.ask : 0.0;
                formatButtonText(quoteDTO.ask, diff, precision, buyBtn, getString(R.string.fx_buy));
            }

            if (quoteDTO.bid == null)
            {
                sellBtn.setText(R.string.buy_sell_bid_price_not_available);
            }
            else
            {
                double diff = (oldQuoteDTO != null && oldQuoteDTO.bid != null) ? quoteDTO.bid - oldQuoteDTO.bid : 0.0;
                formatButtonText(quoteDTO.bid, diff, precision, sellBtn, getString(R.string.fx_sell));
            }
        }
    }

    protected void formatButtonText(double value, double diff, int precision, Button btn, String format)
    {
        THSignedFXRate.builder(value)
                .signTypeArrow()
                .withSignValue(diff)
                .enhanceTo((int) (btn.getTextSize() + 15))
                .enhanceWithColor(THColorUtils.getColorResourceIdForNumber(diff, DEFAULT_BUTTON_TEXT_COLOR))
                .withValueColor(DEFAULT_BUTTON_TEXT_COLOR)
                .relevantDigitCount(SecurityCompactDTOUtil.DEFAULT_RELEVANT_DIGITS)
                .expectedPrecision(precision)
                .withDefaultColor()
                .withFallbackColor(R.color.text_primary_inverse)
                .format(format)
                .build()
                .into(btn);
    }

    @Override protected void handleBuySellReady()
    {
        if (buySellBtnContainer.getVisibility() == View.GONE && tabViewPager.getCurrentItem() == BuySellBottomFXPagerAdapter.FRAGMENT_ID_INFO)
        {
            Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
            slideIn.setFillAfter(true);
            buySellBtnContainer.setVisibility(View.VISIBLE);
            buySellBtnContainer.startAnimation(slideIn);
        }
        selectedPortfolioContainer.setVisibility(
                tabViewPager.getCurrentItem() == BuySellBottomFXPagerAdapter.FRAGMENT_ID_INFO ? View.VISIBLE : View.GONE);
    }

    @Override @NonNull protected Observable<Boolean> getSupportSell()
    {
        return Observable.just(true);
    }
    //</editor-fold>

    protected void linkWith(@NonNull PortfolioCompactDTOList portfolioCompactDTOs)
    {
        PortfolioCompactDTO defaultFxPortfolio = portfolioCompactDTOs.getDefaultFxPortfolio();
        if (defaultFxPortfolio != null)
        {
            selectedPortfolioContainer.addMenuOwnedPortfolioId(new MenuOwnedPortfolioId(currentUserId.toUserBaseKey(), defaultFxPortfolio));
            selectedPortfolioContainer.setVisibility(tabViewPager.getCurrentItem() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override protected void linkWith(QuoteDTO quoteDTO)
    {
        this.oldQuoteDTO = this.quoteDTO;
        super.linkWith(quoteDTO);
    }

    private class BuySellBottomFXPagerAdapter extends FragmentPagerAdapter
    {
        public static final int FRAGMENT_ID_INFO = 0;
        public static final int FRAGMENT_ID_DISCUSS = 1;
        public static final int FRAGMENT_ID_HISTORY = 2;

        @NonNull private final Context context;
        @NonNull private final OwnedPortfolioId applicablePortfolioId;
        @NonNull private final SecurityId securityId;
        @NonNull private final UserBaseKey shownUser;

        public BuySellBottomFXPagerAdapter(
                @NonNull FragmentManager fragmentManager,
                @NonNull Context context,
                @NonNull OwnedPortfolioId applicablePortfolioId,
                @NonNull SecurityId securityId,
                @NonNull UserBaseKey shownUser)
        {
            super(fragmentManager);
            this.context = context;
            this.applicablePortfolioId = applicablePortfolioId;
            this.securityId = securityId;
            this.shownUser = shownUser;
        }

        @Override public Fragment getItem(int position)
        {
            Fragment fragment;
            Bundle args = new Bundle();
            ActionBarOwnerMixin.putKeyShowHomeAsUp(args, true);

            switch (position)
            {
                case FRAGMENT_ID_INFO:
                    fragment = new FXInfoFragment();
                    portfolioCompactDTOObserver = ((FXInfoFragment) fragment).getPortfolioObserver();
                    FXInfoFragment.putSecurityId(args, securityId);
                    FXInfoFragment.putApplicablePortfolioId(args, applicablePortfolioId);
                    break;

                case FRAGMENT_ID_DISCUSS:
                    fragment = new SecurityDiscussionFragment();
                    SecurityDiscussionFragment.setHasOptionMenu(args, false);
                    SecurityDiscussionFragment.putSecurityId(args, securityId);
                    break;

                case FRAGMENT_ID_HISTORY:
                    fragment = new SecurityPositionListFragment();
                    SecurityPositionListFragment.setHasOptionMenu(args, false);
                    SecurityPositionListFragment.putShownUser(args, shownUser);
                    SecurityPositionListFragment.putSecurityId(args, securityId);
                    SecurityPositionListFragment.putApplicablePortfolioId(args, applicablePortfolioId);
                    break;

                default:
                    Timber.w("Not supported index " + position);
                    throw new UnsupportedOperationException("Not implemented");
            }
            fragment.setArguments(args);
            fragment.setRetainInstance(false);
            return fragment;
        }

        @Override public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case FRAGMENT_ID_INFO:
                    return context.getString(R.string.security_info);
                case FRAGMENT_ID_DISCUSS:
                    return context.getString(R.string.discovery_discussions);
                case FRAGMENT_ID_HISTORY:
                    return context.getString(R.string.security_history);
            }
            return super.getPageTitle(position);
        }

        @Override public int getCount()
        {
            return 3;
        }
    }
}

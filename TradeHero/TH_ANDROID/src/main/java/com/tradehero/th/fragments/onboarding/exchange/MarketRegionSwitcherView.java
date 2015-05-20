package com.tradehero.th.fragments.onboarding.exchange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.market.ExchangeIntegerId;
import com.tradehero.th.api.market.MarketRegion;
import com.tradehero.th.api.users.UserProfileDTO;
import java.util.Collection;
import java.util.Set;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.internal.util.SubscriptionList;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class MarketRegionSwitcherView extends LinearLayout
{
    private static final int INDEX_CHILD_MAP = 0;
    private static final int INDEX_CHILD_BUTTON = 1;

    @InjectView(R.id.switcher) ViewSwitcher switcher;
    @InjectView(R.id.market_map_selector) MarketRegionMapView mapView;
    @InjectView(R.id.market_button_selector) MarketRegionButtonView buttonView;
    @InjectView(android.R.id.hint) TextView hint;

    private int maxSelectableExchanges;
    private boolean regionsLoaded;
    @NonNull private PublishSubject<MarketRegion> clickedMarketRegionBehavior;
    @NonNull private SubscriptionList childSubscriptions;
    @Nullable private MarketRegion selectedRegion;
    private Set<ExchangeIntegerId> selectedExchanges;

    @Nullable UserProfileDTO currentUserProfile;

    //<editor-fold desc="Constructors">
    public MarketRegionSwitcherView(Context context)
    {
        super(context);
        this.clickedMarketRegionBehavior = PublishSubject.create();
    }

    public MarketRegionSwitcherView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.clickedMarketRegionBehavior = PublishSubject.create();
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
        try
        {
            mapView.loadBackMap();
            switcher.setDisplayedChild(INDEX_CHILD_MAP);
        } catch (OutOfMemoryError e)
        {
            Timber.e(e, "Failed to load map");
            switcher.setDisplayedChild(INDEX_CHILD_BUTTON);
        }
        displayHint();
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        childSubscriptions = new SubscriptionList();
        childSubscriptions.add(mapView.getMarketRegionClickedObservable().subscribe(clickedMarketRegionBehavior));
        childSubscriptions.add(mapView.getSwitchClickedObservable().subscribe(
                new Action1<Boolean>()
                {
                    @Override public void call(Boolean pleaseChange)
                    {
                        if (currentUserProfile != null
                                && currentUserProfile.isAdmin
                                && pleaseChange)
                        {
                            switcher.setDisplayedChild(INDEX_CHILD_BUTTON);
                        }
                    }
                }
        ));
        childSubscriptions.add(buttonView.getMarketRegionClickedObservable().subscribe(clickedMarketRegionBehavior));
    }

    @Override protected void onDetachedFromWindow()
    {
        childSubscriptions.unsubscribe();
        super.onDetachedFromWindow();
    }

    public void enable(@NonNull Collection<? extends MarketRegion> enabledRegions, int maxSelectableExchanges)
    {
        regionsLoaded = true;
        this.maxSelectableExchanges = maxSelectableExchanges;
        displayHint();
        try
        {
            mapView.enable(enabledRegions);
        } catch (OutOfMemoryError e)
        {
            Timber.e(e, "Failed to load hitbox");
            switcher.setDisplayedChild(INDEX_CHILD_BUTTON);
        }
        buttonView.enable(enabledRegions);
    }

    public void setCurrentUserProfile(@Nullable UserProfileDTO currentUserProfile)
    {
        this.currentUserProfile = currentUserProfile;
    }

    public void showClicked(@NonNull MarketRegion clickedRegion)
    {
        setSelectedRegion(clickedRegion);
        mapView.showClicked(clickedRegion);
        buttonView.showClicked(clickedRegion);
    }

    public void setSelectedRegion(@Nullable MarketRegion selectedRegion)
    {
        this.selectedRegion = selectedRegion;
        displayHint();
    }

    public void setSelectedExchanges(Set<ExchangeIntegerId> selectedExchanges)
    {
        this.selectedExchanges = selectedExchanges;
        displayHint();
    }

    protected void displayHint()
    {
        if (!regionsLoaded)
        {
            hint.setText(R.string.on_board_exchange_loading_regions);
        }
        else if (selectedRegion == null)
        {
            hint.setText(R.string.on_board_exchange_tap_map_select_exchanges);
        }
        else if (selectedExchanges == null || selectedExchanges.size() == 0)
        {
            hint.setText(R.string.on_board_exchange_tap_list_select_exchanges);
        }
        else if (selectedExchanges.size() >= maxSelectableExchanges)
        {
            hint.setText(String.format(
                    getResources().getString(R.string.on_board_exchange_tap_list_full),
                    maxSelectableExchanges));
        }
        else
        {
            hint.setText(String.format(
                    getResources().getString(R.string.on_board_exchange_tap_list_select_exchanges_have),
                    maxSelectableExchanges));
        }
    }

    @NonNull public Observable<MarketRegion> getMarketRegionClickedObservable()
    {
        return clickedMarketRegionBehavior.asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<MarketRegion>()
                {
                    @Override public void call(MarketRegion marketRegion)
                    {
                        setSelectedRegion(marketRegion);
                    }
                });
    }
}
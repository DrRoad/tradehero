package com.tradehero.th.fragments.onboarding.exchange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import com.tradehero.th.api.market.MarketRegion;
import com.tradehero.th.rx.view.ViewArrayObservable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.functions.Func1;

public class MarketRegionMapView extends TableLayout
{
    //<editor-fold desc="Constructors">
    public MarketRegionMapView(Context context)
    {
        super(context);
    }

    public MarketRegionMapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    //</editor-fold>

    @NonNull public Observable<MarketRegion> getMarketRegionClickedObservable()
    {
        return ViewArrayObservable.clicks(new ArrayList<View>(getRegionViews()), false)
                .map(new Func1<OnClickEvent, MarketRegion>()
                {
                    @Override public MarketRegion call(OnClickEvent onClickEvent)
                    {
                        return ((MarketRegionView) onClickEvent.view()).region;
                    }
                });
    }

    @NonNull protected List<MarketRegionView> getRegionViews()
    {
        List<MarketRegionView> regionViews = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < getChildCount(); rowIndex++)
        {
            TableRow row = (TableRow) getChildAt(rowIndex);
            for (int regionIndex = 0; regionIndex < row.getChildCount(); regionIndex++)
            {
                regionViews.add((MarketRegionView) row.getChildAt(regionIndex));
            }
        }
        return regionViews;
    }

    public void enable(@NonNull Collection<? extends MarketRegion> enabledRegions)
    {
        for (MarketRegionView view : getRegionViews())
        {
            view.setEnabled(enabledRegions.contains(view.region));
        }
    }
}

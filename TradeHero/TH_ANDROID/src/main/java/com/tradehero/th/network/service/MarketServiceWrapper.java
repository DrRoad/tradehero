package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import com.tradehero.th.api.market.ExchangeCompactDTOList;
import com.tradehero.th.api.market.ExchangeListType;
import com.tradehero.th.api.market.SectorCompactDTOList;
import com.tradehero.th.api.market.SectorDTOList;
import com.tradehero.th.api.market.SectorListType;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class MarketServiceWrapper
{
    @NonNull private final MarketServiceRx marketServiceRx;

    //<editor-fold desc="Constructors">
    @Inject public MarketServiceWrapper(@NonNull MarketServiceRx marketServiceRx)
    {
        this.marketServiceRx = marketServiceRx;
    }
    //</editor-fold>

    //<editor-fold desc="Get Exchanges with Top Securities">
    @NonNull public Observable<ExchangeCompactDTOList> getExchangesRx(@NonNull ExchangeListType exchangeListType)
    {
        return marketServiceRx.getExchanges(exchangeListType.topNStocks);
    }
    //</editor-fold>

    //<editor-fold desc="Get Sectors">
    @NonNull public Observable<SectorCompactDTOList> getSectorCompacts()
    {
        return marketServiceRx.getSectorCompacts();
    }

    @NonNull public Observable<SectorDTOList> getSectors(@NonNull SectorListType sectorListType)
    {
        return marketServiceRx.getSectors(sectorListType.topNStocks);
    }
    //</editor-fold>
}

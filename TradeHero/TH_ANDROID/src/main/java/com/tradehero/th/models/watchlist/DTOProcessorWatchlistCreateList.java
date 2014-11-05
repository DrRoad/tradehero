package com.tradehero.th.models.watchlist;

import android.support.annotation.NonNull;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.models.DTOProcessor;
import com.tradehero.th.persistence.portfolio.PortfolioCacheRx;
import com.tradehero.th.persistence.portfolio.PortfolioCompactCacheRx;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCacheRx;
import com.tradehero.th.persistence.watchlist.WatchlistPositionCacheRx;
import rx.functions.Action1;

public class DTOProcessorWatchlistCreateList implements DTOProcessor<WatchlistPositionDTOList>,
        Action1<WatchlistPositionDTOList>
{
    @NonNull DTOProcessorWatchlistCreate individualProcessor;

    //<editor-fold desc="Constructors">
    public DTOProcessorWatchlistCreateList(
            @NonNull WatchlistPositionCacheRx watchlistPositionCache,
            @NonNull UserBaseKey concernedUser,
            @NonNull PortfolioCompactCacheRx portfolioCompactCache,
            @NonNull PortfolioCacheRx portfolioCache,
            @NonNull UserWatchlistPositionCacheRx userWatchlistPositionCache)
    {
        super();
        individualProcessor = new DTOProcessorWatchlistCreate(
                watchlistPositionCache,
                concernedUser,
                portfolioCompactCache,
                portfolioCache,
                userWatchlistPositionCache);
    }
    //</editor-fold>

    @Override public WatchlistPositionDTOList process(@NonNull WatchlistPositionDTOList value)
    {
        WatchlistPositionDTOList processed = new WatchlistPositionDTOList();
        for (WatchlistPositionDTO position : value)
        {
            processed.add(individualProcessor.process(position));
        }
        return processed;
    }

    @Override public void call(WatchlistPositionDTOList watchlistPositionDTOs)
    {
        process(watchlistPositionDTOs);
    }
}

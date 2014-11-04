package com.tradehero.th.models.watchlist;

import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.models.DTOProcessor;
import com.tradehero.th.persistence.portfolio.PortfolioCacheRx;
import com.tradehero.th.persistence.portfolio.PortfolioCompactCacheRx;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistPositionCache;
import org.jetbrains.annotations.NotNull;
import rx.functions.Action1;
import timber.log.Timber;

public class DTOProcessorWatchlistDelete implements DTOProcessor<WatchlistPositionDTO>,
        Action1<WatchlistPositionDTO>
{
    @NotNull private final UserBaseKey concernedUser;
    @NotNull private final WatchlistPositionCache watchlistPositionCache;
    @NotNull private final PortfolioCompactCacheRx portfolioCompactCache;
    @NotNull private final PortfolioCacheRx portfolioCache;
    @NotNull private final UserWatchlistPositionCache userWatchlistPositionCache;

    //<editor-fold desc="Constructors">
    public DTOProcessorWatchlistDelete(
            @NotNull WatchlistPositionCache watchlistPositionCache,
            @NotNull UserBaseKey concernedUser,
            @NotNull PortfolioCompactCacheRx portfolioCompactCache,
            @NotNull PortfolioCacheRx portfolioCache,
            @NotNull UserWatchlistPositionCache userWatchlistPositionCache)
    {
        super();
        this.concernedUser = concernedUser;
        this.watchlistPositionCache = watchlistPositionCache;
        this.portfolioCompactCache = portfolioCompactCache;
        this.portfolioCache = portfolioCache;
        this.userWatchlistPositionCache = userWatchlistPositionCache;
    }
    //</editor-fold>

    @Override public WatchlistPositionDTO process(@NotNull WatchlistPositionDTO watchlistPositionDTO)
    {
        portfolioCompactCache.invalidate(concernedUser, true);
        portfolioCache.invalidate(concernedUser, true);
        SecurityId deletedSecurityId = null;
        if (watchlistPositionDTO.securityDTO != null)
        {
            deletedSecurityId = watchlistPositionDTO.securityDTO.getSecurityId();
        }
        else
        {
            Timber.e(
                    new NullPointerException("watchlist or security null " + watchlistPositionDTO),
                    null);
        }

        WatchlistPositionDTOList cachedPositions = userWatchlistPositionCache.get(concernedUser);
        if (cachedPositions != null && deletedSecurityId != null)
        {
            cachedPositions.remove(deletedSecurityId);
            userWatchlistPositionCache.put(concernedUser, cachedPositions);
            watchlistPositionCache.invalidate(deletedSecurityId);
        }
        else
        {
            userWatchlistPositionCache.invalidate(concernedUser);
            watchlistPositionCache.invalidate(concernedUser);
        }
        return watchlistPositionDTO;
    }

    @Override public void call(WatchlistPositionDTO watchlistPositionDTO)
    {
        process(watchlistPositionDTO);
    }
}

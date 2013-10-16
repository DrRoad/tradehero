package com.tradehero.th.utils.dagger;

import android.app.Application;
import android.content.Context;
import com.squareup.picasso.Picasso;
import com.tradehero.common.cache.DatabaseCache;
import com.tradehero.common.persistence.CacheHelper;
import com.tradehero.th.api.form.UserAvailabilityRequester;
import com.tradehero.th.base.THUser;
import com.tradehero.th.fragments.leaderboard.LeaderboardFragment;
import com.tradehero.th.fragments.authentication.EmailSignInFragment;
import com.tradehero.th.fragments.portfolio.PortfolioListFragment;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.fragments.timeline.MeTimelineFragment;
import com.tradehero.th.fragments.timeline.TimelineFragment;
import com.tradehero.th.fragments.trade.BuyFragment;
import com.tradehero.th.fragments.trade.ChartFragment;
import com.tradehero.th.fragments.trade.FreshQuoteHolder;
import com.tradehero.th.fragments.trade.YahooNewsFragment;
import com.tradehero.th.fragments.trade.StockInfoFragment;
import com.tradehero.th.fragments.trending.SearchStockPeopleFragment;
import com.tradehero.th.fragments.trade.TradeFragment;
import com.tradehero.th.fragments.trending.TrendingFragment;
import com.tradehero.th.loaders.SearchStockPageItemListLoader;
import com.tradehero.th.loaders.TimelinePagedItemListLoader;
import com.tradehero.th.network.NetworkEngine;
import com.tradehero.th.network.YahooEngine;
import com.tradehero.th.network.service.*;
import com.tradehero.th.persistence.TimelineManager;
import com.tradehero.th.persistence.TimelineStore;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefCache;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefListCache;
import com.tradehero.th.persistence.position.FiledPositionCache;
import com.tradehero.th.persistence.position.PositionCompactIdCache;
import com.tradehero.th.persistence.position.SecurityPositionDetailCache;
import com.tradehero.th.persistence.security.SecurityCompactCache;
import com.tradehero.th.persistence.security.SecurityCompactListCache;
import com.tradehero.th.persistence.security.SecurityIdCache;
import com.tradehero.th.persistence.user.AbstractUserStore;
import com.tradehero.th.persistence.user.UserManager;
import com.tradehero.th.persistence.user.UserStore;
import com.tradehero.th.widget.MarkdownTextView;
import com.tradehero.th.widget.portfolio.PortfolioHeaderItemView;
import com.tradehero.th.widget.timeline.TimelineItemView;
import com.tradehero.th.widget.position.PositionQuickInnerViewHolder;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/** Created with IntelliJ IDEA. User: tho Date: 9/16/13 Time: 5:36 PM Copyright (c) TradeHero */
@Module(
        injects =
        {
                EmailSignInFragment.class,
                TrendingFragment.class,
                SearchStockPeopleFragment.class,
                TradeFragment.class,
                TimelineFragment.class,
                MeTimelineFragment.class,
                MarkdownTextView.class,

                TrendingFragment.class,
                FreshQuoteHolder.class,
                BuyFragment.class,
                YahooNewsFragment.class,
                ChartFragment.class,
                StockInfoFragment.class,
                PortfolioListFragment.class,
                PortfolioHeaderItemView.class,

                PositionListFragment.class,

                UserAvailabilityRequester.class,
                SearchStockPageItemListLoader.class,
                TimelinePagedItemListLoader.class,

                UserManager.class,
                TimelineManager.class,

                UserStore.class,
                TimelineStore.class,
                TimelineStore.Factory.class,
                //SecurityCompactCache.class,
                SecurityCompactListCache.class,
                SecurityPositionDetailCache.class,
                //PositionCompactIdCache.class,
                //FiledPositionCache.class,

                DatabaseCache.class,
                CacheHelper.class,

                TimelineFragment.class,
                TimelineItemView.class,

                LeaderboardFragment.class,
                LeaderboardDefListCache.class,
                LeaderboardDefCache.class,
        },
        staticInjections =
        {
                THUser.class
        }
)
public class TradeHeroModule
{
    private final Application application;
    private final NetworkEngine engine;
    private final YahooEngine yahooEngine;

    public TradeHeroModule(NetworkEngine engine, YahooEngine yahooEngine, Application application)
    {
        this.application = application;
        this.engine = engine;
        this.yahooEngine = yahooEngine;
    }

    @Provides @Singleton UserService provideUserService()
    {
        return engine.createService(UserService.class);
    }

    @Provides @Singleton SecurityService provideSecurityService()
    {
        return engine.createService(SecurityService.class);
    }

    @Provides @Singleton UserTimelineService provideUserTimelineService()
    {
        return engine.createService(UserTimelineService.class);
    }

    @Provides @Singleton QuoteService provideQuoteService()
    {
        return engine.createService(QuoteService.class);
    }

    @Provides @Singleton PortfolioService providePortfolioService()
    {
        return engine.createService(PortfolioService.class);
    }

    @Provides @Singleton PositionService providePositionService()
    {
        return engine.createService(PositionService.class);
    }

    @Provides @Singleton LeaderboardService provideLeaderboardService()
    {
        return engine.createService(LeaderboardService.class);
    }

    @Provides @Singleton ProviderService provideProviderService()
    {
        return engine.createService(ProviderService.class);
    }

    @Provides @Singleton YahooNewsService provideYahooNewsService()
    {
        return yahooEngine.createService(YahooNewsService.class);
    }

    @Provides @Singleton AbstractUserStore provideUserStore(UserStore store)
    {
        return store;
    }

    @Provides @Singleton Picasso providePicasso()
    {
        return Picasso.with(application);
    }


    @Provides Context provideContext()
    {
        return application.getApplicationContext();
    }
}

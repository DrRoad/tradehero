package com.tradehero.th.network.service;

import com.tradehero.common.utils.CustomXmlConverter;
import com.tradehero.th.network.NetworkConstants;
import com.tradehero.th.network.retrofit.RequestHeaders;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import retrofit.RestAdapter;

@Module(
        injects = {
        },
        complete = false,
        library = true
)
public class RetrofitProtectedModule
{
    //<editor-fold desc="API Services">
    @Provides @Singleton AchievementServiceAsync provideAchievementServiceAsync(RestAdapter adapter)
    {
        return adapter.create(AchievementServiceAsync.class);
    }

    @Provides @Singleton AlertPlanServiceAsync provideAlertPlanServiceAsync(RestAdapter adapter)
    {
        return adapter.create(AlertPlanServiceAsync.class);
    }

    @Provides @Singleton AlertServiceAsync provideAlertService(RestAdapter adapter)
    {
        return adapter.create(AlertServiceAsync.class);
    }

    @Provides @Singleton CompetitionServiceAsync provideCompetitionService(RestAdapter adapter)
    {
        return adapter.create(CompetitionServiceAsync.class);
    }

    @Provides @Singleton CurrencyServiceAsync provideCurrencyServiceAsync(RestAdapter adapter)
    {
        return adapter.create(CurrencyServiceAsync.class);
    }

    @Provides @Singleton DiscussionServiceAsync provideDiscussionServiceAsync(RestAdapter adapter)
    {
        return adapter.create(DiscussionServiceAsync.class);
    }

    @Provides @Singleton FollowerServiceAsync provideFollowerService(RestAdapter adapter)
    {
        return adapter.create(FollowerServiceAsync.class);
    }

    @Provides @Singleton LeaderboardServiceAsync provideLeaderboardService(RestAdapter adapter)
    {
        return adapter.create(LeaderboardServiceAsync.class);
    }

    @Provides @Singleton MarketServiceAsync provideMarketServiceAsync(RestAdapter adapter)
    {
        return adapter.create(MarketServiceAsync.class);
    }

    @Provides @Singleton MessageServiceAsync provideMessageServiceAsync(RestAdapter adapter)
    {
        return adapter.create(MessageServiceAsync.class);
    }

    @Provides @Singleton NewsServiceAsync provideNewsServiceAsync(RestAdapter adapter)
    {
        return adapter.create(NewsServiceAsync.class);
    }

    @Provides @Singleton NotificationServiceAsync provideNotificationServiceAsync(RestAdapter adapter)
    {
        return adapter.create(NotificationServiceAsync.class);
    }

    @Provides @Singleton PortfolioServiceAsync providePortfolioServiceAsync(RestAdapter adapter)
    {
        return adapter.create(PortfolioServiceAsync.class);
    }

    @Provides @Singleton PositionServiceAsync providePositionServiceAsync(RestAdapter adapter)
    {
        return adapter.create(PositionServiceAsync.class);
    }

    @Provides @Singleton ProviderServiceAsync provideProviderServiceAsync(RestAdapter adapter)
    {
        return adapter.create(ProviderServiceAsync.class);
    }

    @Provides @Singleton QuoteServiceAsync provideQuoteServiceAsync(RestAdapter adapter)
    {
        return adapter.create(QuoteServiceAsync.class);
    }

    @Provides @Singleton SecurityServiceAsync provideSecurityServiceAsync(RestAdapter adapter)
    {
        return adapter.create(SecurityServiceAsync.class);
    }

    @Provides @Singleton SessionServiceAsync provideSessionServiceAsync(RestAdapter adapter)
    {
        return adapter.create(SessionServiceAsync.class);
    }

    @Provides @Singleton SocialServiceAsync provideSocialServiceAsync(RestAdapter adapter)
    {
        return adapter.create(SocialServiceAsync.class);
    }

    @Provides @Singleton TradeServiceAsync provideTradeServiceAsync(RestAdapter adapter)
    {
        return adapter.create(TradeServiceAsync.class);
    }

    @Provides @Singleton TranslationServiceBingAsync provideBingTranslationServiceAsync(RestAdapter.Builder builder)
    {
        return builder.setEndpoint(NetworkConstants.BING_TRANSLATION_ENDPOINT)
                .setConverter(new CustomXmlConverter())
                .build().create(TranslationServiceBingAsync.class);
    }

    @Provides @Singleton TranslationTokenServiceAsync provideTranslationTokenServiceAsync(RestAdapter adapter)
    {
        return adapter.create(TranslationTokenServiceAsync.class);
    }

    @Provides @Singleton UserServiceAsync provideUserService(RestAdapter adapter)
    {
        return adapter.create(UserServiceAsync.class);
    }

    @Provides @Singleton UserTimelineMarkerServiceAsync provideUserTimelineMarkerServiceAsync(RestAdapter adapter)
    {
        return adapter.create(UserTimelineMarkerServiceAsync.class);
    }

    @Provides @Singleton UserTimelineServiceAsync provideUserTimelineServiceAsync(RestAdapter adapter)
    {
        return adapter.create(UserTimelineServiceAsync.class);
    }

    @Provides @Singleton WatchlistServiceAsync provideWatchlistServiceAsync(RestAdapter adapter)
    {
        return adapter.create(WatchlistServiceAsync.class);
    }

    @Provides @Singleton WeChatServiceAsync provideWeChatServiceAsync(RestAdapter adapter)
    {
        return adapter.create(WeChatServiceAsync.class);
    }

    @Provides @Singleton YahooNewsServiceAsync provideYahooServiceAsync(RestAdapter.Builder builder)
    {
        return builder.setEndpoint(NetworkConstants.YAHOO_FINANCE_ENDPOINT).build().create(YahooNewsServiceAsync.class);
    }

    @Provides @Singleton HomeServiceAsync provideHomeServiceAsync(RestAdapter.Builder builder, RequestHeaders requestHeaders)
    {
        return builder.setEndpoint(NetworkConstants.TRADEHERO_PROD_ENDPOINT)
                .setRequestInterceptor(requestHeaders)
                .build()
                .create(HomeServiceAsync.class);
    }
    //</editor-fold>
}

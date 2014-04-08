package com.tradehero.th.utils.dagger;

import android.content.Context;
import com.tradehero.common.billing.googleplay.BaseIABLogicHolder;
import com.tradehero.common.billing.googleplay.IABBillingInventoryFetcher;
import com.tradehero.common.billing.googleplay.IABServiceConnector;
import com.tradehero.common.cache.DatabaseCache;
import com.tradehero.common.persistence.CacheHelper;
import com.tradehero.th.activities.ActivityModule;
import com.tradehero.th.api.form.AbstractUserAvailabilityRequester;
import com.tradehero.th.base.Application;
import com.tradehero.th.base.THUser;
import com.tradehero.th.billing.googleplay.PurchaseRestorerRequiredMilestone;
import com.tradehero.th.billing.googleplay.THBaseIABInventoryFetcherHolder;
import com.tradehero.th.billing.googleplay.THBaseIABPurchaseReporterHolder;
import com.tradehero.th.billing.googleplay.THIABBillingInventoryFetcher;
import com.tradehero.th.billing.googleplay.THIABInventoryFetchMilestone;
import com.tradehero.th.billing.googleplay.THIABLogicHolderFull;
import com.tradehero.th.billing.googleplay.THIABModule;
import com.tradehero.th.billing.googleplay.THIABPurchaseConsumer;
import com.tradehero.th.billing.googleplay.THIABPurchaseFetchMilestone;
import com.tradehero.th.billing.googleplay.THIABPurchaseFetcher;
import com.tradehero.th.billing.googleplay.THIABPurchaseReporter;
import com.tradehero.th.billing.googleplay.THIABPurchaseRestorer;
import com.tradehero.th.billing.googleplay.THIABPurchaser;
import com.tradehero.th.billing.googleplay.THIABUserInteractor;
import com.tradehero.th.fragments.alert.AlertCreateFragment;
import com.tradehero.th.fragments.alert.AlertEditFragment;
import com.tradehero.th.fragments.alert.AlertManagerFragment;
import com.tradehero.th.fragments.authentication.EmailSignInFragment;
import com.tradehero.th.fragments.billing.StoreScreenFragment;
import com.tradehero.th.fragments.competition.CompetitionWebViewFragment;
import com.tradehero.th.fragments.competition.macquarie.MacquarieWarrantItemViewAdapter;
import com.tradehero.th.fragments.leaderboard.BaseLeaderboardFragment;
import com.tradehero.th.fragments.leaderboard.CompetitionLeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.leaderboard.FriendLeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardCommunityFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardDefListFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardDefView;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserItemView;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserListFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserListView;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserLoader;
import com.tradehero.th.fragments.leaderboard.filter.LeaderboardFilterFragment;
import com.tradehero.th.fragments.news.HeadlineFragment;
import com.tradehero.th.fragments.news.NewsDialogLayout;
import com.tradehero.th.fragments.news.NewsHeadlineView;
import com.tradehero.th.fragments.portfolio.PortfolioListFragment;
import com.tradehero.th.fragments.portfolio.PortfolioListItemAdapter;
import com.tradehero.th.fragments.portfolio.PortfolioListItemView;
import com.tradehero.th.fragments.portfolio.PushablePortfolioListFragment;
import com.tradehero.th.fragments.portfolio.SimpleOwnPortfolioListItemAdapter;
import com.tradehero.th.fragments.portfolio.header.OtherUserPortfolioHeaderView;
import com.tradehero.th.fragments.position.LeaderboardPositionListFragment;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.fragments.position.partial.PositionPartialTopView;
import com.tradehero.th.fragments.security.*;
import com.tradehero.th.fragments.settings.AboutFragment;
import com.tradehero.th.fragments.settings.InviteFriendFragment;
import com.tradehero.th.fragments.settings.SettingsFragment;
import com.tradehero.th.fragments.settings.SettingsProfileFragment;
import com.tradehero.th.fragments.settings.UserFriendDTOView;
import com.tradehero.th.fragments.social.follower.FollowerListItemView;
import com.tradehero.th.fragments.social.follower.FollowerManagerFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerInfoFetcher;
import com.tradehero.th.fragments.social.follower.FollowerPayoutManagerFragment;
import com.tradehero.th.fragments.social.hero.HeroListItemView;
import com.tradehero.th.fragments.social.hero.HeroManagerFragment;
import com.tradehero.th.fragments.social.hero.HeroManagerInfoFetcher;
import com.tradehero.th.fragments.social.message.PrivateMessageFragment;
import com.tradehero.th.fragments.timeline.MeTimelineFragment;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.fragments.timeline.TimelineFragment;
import com.tradehero.th.fragments.timeline.TimelineItemView;
import com.tradehero.th.fragments.timeline.UserProfileCompactViewHolder;
import com.tradehero.th.fragments.timeline.UserProfileDetailViewHolder;
//import com.tradehero.th.fragments.trade.BuySellConfirmFragment;
import com.tradehero.th.fragments.trade.BuySellFragment;
import com.tradehero.th.fragments.trade.FreshQuoteHolder;
import com.tradehero.th.fragments.trade.TradeListFragment;
import com.tradehero.th.fragments.trade.TradeListInPeriodFragment;
import com.tradehero.th.fragments.trade.view.TradeListHeaderView;
import com.tradehero.th.fragments.trade.view.TradeListItemView;
import com.tradehero.th.fragments.trade.view.TradeListOverlayHeaderView;
//import com.tradehero.th.fragments.trade.view.TradeQuantityView;
import com.tradehero.th.fragments.trending.SearchPeopleItemView;
import com.tradehero.th.fragments.trending.SearchStockPeopleFragment;
import com.tradehero.th.fragments.trending.TrendingFragment;
import com.tradehero.th.fragments.trending.filter.TrendingFilterSelectorView;
import com.tradehero.th.fragments.watchlist.WatchlistItemView;
import com.tradehero.th.fragments.watchlist.WatchlistPortfolioHeaderView;
import com.tradehero.th.fragments.watchlist.WatchlistPositionFragment;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.loaders.FriendListLoader;
import com.tradehero.th.loaders.HeadlineListLoader;
import com.tradehero.th.loaders.SearchStockPageListLoader;
import com.tradehero.th.loaders.TimelineListLoader;
import com.tradehero.th.loaders.security.SecurityListPagedLoader;
import com.tradehero.th.loaders.security.macquarie.MacquarieSecurityListPagedLoader;
import com.tradehero.th.models.alert.MiddleCallbackCreateAlertCompact;
import com.tradehero.th.models.alert.MiddleCallbackUpdateAlertCompact;
import com.tradehero.th.models.chart.ChartModule;
import com.tradehero.th.models.intent.competition.ProviderPageIntent;
import com.tradehero.th.models.intent.trending.TrendingIntentFactory;
import com.tradehero.th.models.portfolio.DisplayablePortfolioFetchAssistant;
import com.tradehero.th.models.push.PushNotificationManager;
import com.tradehero.th.models.push.urbanairship.UrbanAirshipPushNotificationManager;
import com.tradehero.th.models.user.MiddleCallbackLogout;
import com.tradehero.th.models.user.MiddleCallbackUpdateUserProfile;
import com.tradehero.th.network.NetworkModule;
import com.tradehero.th.persistence.billing.googleplay.IABSKUListRetrievedAsyncMilestone;
import com.tradehero.th.persistence.leaderboard.LeaderboardManager;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListRetrievedMilestone;
import com.tradehero.th.persistence.prefs.PreferenceModule;
import com.tradehero.th.persistence.timeline.TimelineManager;
import com.tradehero.th.persistence.timeline.TimelineStore;
import com.tradehero.th.persistence.user.UserManager;
import com.tradehero.th.persistence.user.UserProfileRetrievedMilestone;
import com.tradehero.th.persistence.user.UserStore;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistRetrievedMilestone;
import com.tradehero.th.ui.UIModule;
import com.tradehero.th.utils.NumberDisplayUtils;
import com.tradehero.th.widget.MarkdownTextView;
import com.tradehero.th.widget.ServerValidatedUsernameText;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/** Created with IntelliJ IDEA. User: tho Date: 9/16/13 Time: 5:36 PM Copyright (c) TradeHero */
@Module(
        includes = {
                CacheModule.class,
                GraphicModule.class,
                NetworkModule.class,
                SocialNetworkModule.class,
                UIModule.class,
                UxModule.class,
                UserModule.class,
                PreferenceModule.class,
                ChartModule.class,
                ActivityModule.class,
                THIABModule.class,
                NewsModule.class,
        },
        injects =
                {
                        com.tradehero.th.base.Application.class,
                        SettingsProfileFragment.class,
                        MiddleCallbackUpdateUserProfile.class,
                        SettingsFragment.class,
                        MiddleCallbackLogout.class,
                        AboutFragment.class,
                        EmailSignInFragment.class,
                        ServerValidatedUsernameText.UserAvailabilityRequester.class,
                        ServerValidatedUsernameText.class,
                        TrendingFragment.class,
                        TrendingFilterSelectorView.class,
                        SecurityListPagedLoader.class,
                        MacquarieSecurityListPagedLoader.class,
                        SecurityItemViewAdapter.class,
                        MacquarieWarrantItemViewAdapter.class,
                        SecurityItemView.class,
                        WarrantSecurityItemView.class,
                        SearchStockPeopleFragment.class,
                        SearchPeopleItemView.class,
                        FreshQuoteHolder.class,
                        BuySellFragment.class,
                        BuySellFragment.BuySellAsyncTask.class,
                        //BuySellConfirmFragment.class,
                        //BuySellConfirmFragment.BuySellAsyncTask.class,
                        //TradeQuantityView.class,
                        TimelineFragment.class,
                        MeTimelineFragment.class,
                        PushableTimelineFragment.class,
                        PushableTimelineFragment.PushableTimelineTHIABUserInteractor.class,
                        SimpleOwnPortfolioListItemAdapter.class,
                        MarkdownTextView.class,

                        NewsTitleListFragment.class,
                        ChartFragment.class,
                        StockInfoValueFragment.class,
                        WarrantInfoValueFragment.class,
                        StockInfoFragment.class,
                        PortfolioListFragment.class,
                        PushablePortfolioListFragment.class,
                        PortfolioListItemView.class,
                        PortfolioListItemAdapter.class,
                        DisplayablePortfolioFetchAssistant.class,

                        PositionListFragment.class,
                        PositionListFragment.PositionListTHIABUserInteractor.class,
                        LeaderboardPositionListFragment.class,
                        LeaderboardPositionListFragment.LeaderboardPositionListTHIABUserInteractor.class,
                        OtherUserPortfolioHeaderView.class,

                        PositionPartialTopView.class,

                        TradeListFragment.class,
                        TradeListInPeriodFragment.class,
                        TradeListItemView.class,
                        TradeListOverlayHeaderView.class,
                        TradeListHeaderView.class,

                        StoreScreenFragment.class,
                        HeroManagerFragment.class,
                        HeroListItemView.class,
                        FollowerManagerFragment.class,
                        //FollowerManagerFragment.FollowerManagerTabFragment.class,
                        FollowerManagerFragment.AllFollowerFragment.class,
                        FollowerManagerFragment.PrimiumFollowerFragment.class,
                        FollowerManagerFragment.FreeFollowerFragment.class,
                        FollowerManagerInfoFetcher.class,
                        FollowerPayoutManagerFragment.class,
                        FollowerListItemView.class,

                        AbstractUserAvailabilityRequester.class,
                        SearchStockPageListLoader.class,
                        TimelineListLoader.class,

                        UserManager.class,
                        TimelineManager.class,

                        UserStore.class,
                        TimelineStore.class,
                        TimelineStore.Factory.class,

                        DatabaseCache.class,
                        CacheHelper.class,

                        TimelineFragment.class,
                        TimelineItemView.class,
                        UserProfileCompactViewHolder.class,
                        UserProfileDetailViewHolder.class,

                        LeaderboardCommunityFragment.class,
                        LeaderboardDefListFragment.class,

                        LeaderboardDefView.class,
                        LeaderboardManager.class,
                        LeaderboardMarkUserLoader.class,
                        LeaderboardMarkUserListFragment.class,
                        BaseLeaderboardFragment.class,
                        LeaderboardMarkUserItemView.class,
                        LeaderboardMarkUserItemView.LeaderboardMarkUserItemViewTHIABUserInteractor.class,
                        LeaderboardMarkUserListView.class,
                        FriendLeaderboardMarkUserListFragment.class,
                        CompetitionLeaderboardMarkUserListFragment.class,
                        LeaderboardFilterFragment.class,

                        WebViewFragment.class,

                        CompetitionWebViewFragment.class,

                        IABServiceConnector.class,
                        IABBillingInventoryFetcher.class,
                        THIABPurchaseFetcher.class,
                        THIABBillingInventoryFetcher.class,
                        THIABPurchaser.class,
                        THIABPurchaseReporter.class,
                        THIABLogicHolderFull.class,
                        THIABPurchaseConsumer.class,
                        THIABInventoryFetchMilestone.class,
                        THBaseIABInventoryFetcherHolder.class,
                        THBaseIABPurchaseReporterHolder.class,
                        THIABPurchaseRestorer.class,
                        THIABPurchaseFetchMilestone.class,
                        BaseIABLogicHolder.AvailabilityTester.class,
                        IABSKUListRetrievedAsyncMilestone.class,
                        PortfolioCompactListRetrievedMilestone.class,
                        UserProfileRetrievedMilestone.class,
                        PurchaseRestorerRequiredMilestone.class,
                        THIABUserInteractor.class,
                        StoreScreenFragment.StoreScreenTHIABUserInteractor.class,
                        HeroManagerFragment.HeroesTabContentFragment.class,
                        HeroManagerFragment.HeroesTabContentFragment.HeroManagerTHIABUserInteractor.class,
                        HeroManagerFragment.PrimiumHeroFragment.class,
                        HeroManagerFragment.FreeHeroFragment.class,
                        HeroManagerFragment.AllHeroFragment.class,
                        HeroManagerInfoFetcher.class,
                        BuySellFragment.BuySellTHIABUserInteractor.class,

                        WatchlistEditFragment.class,
                        UserWatchlistPositionCache.class,
                        WatchlistRetrievedMilestone.class,
                        WatchlistPositionFragment.class,
                        WatchlistItemView.class,
                        WatchlistPortfolioHeaderView.class,

                        TrendingIntentFactory.class,
                        ProviderPageIntent.class,

                        AlertManagerFragment.class,
                        AlertEditFragment.class,
                        AlertCreateFragment.class,
                        MiddleCallbackUpdateAlertCompact.class,
                        MiddleCallbackCreateAlertCompact.class,

                        InviteFriendFragment.class,

                        UserFriendDTOView.class,
                        FriendListLoader.class,

                        //binding
                        NewsDialogLayout.class,
                        NewsHeadlineView.class,
                        NewsDetailFragment.class,
                        HeadlineFragment.class,
                        HeadlineListLoader.class,

                        PrivateMessageFragment.class,
                },
        staticInjections =
                {
                        THUser.class,
                        NumberDisplayUtils.class,
                },
        complete = false,
        library = true // TODO remove this line
)
public class TradeHeroModule
{
    private final Application application;

    public TradeHeroModule(Application application)
    {
        this.application = application;
    }

    // We should not use like this. Instead use like CurrentActivityHolder
    @Deprecated
    @Provides Context provideContext()
    {
        return application.getApplicationContext();
    }

    @Provides @Singleton Application provideApplication()
    {
        return application;
    }

    @Provides @Singleton PushNotificationManager providePushNotificationManager()
    {
        return new UrbanAirshipPushNotificationManager();
    }
}

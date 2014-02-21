package com.tradehero.th.utils.dagger;

import com.tradehero.th.activities.AuthenticationActivity;
import com.tradehero.th.activities.DashboardActivity;
import com.tradehero.th.auth.operator.ConsumerKey;
import com.tradehero.th.auth.operator.ConsumerSecret;
import com.tradehero.th.auth.operator.FacebookAppId;
import com.tradehero.th.auth.operator.FacebookPermissions;
import dagger.Module;
import dagger.Provides;
import java.util.Collection;
import javax.inject.Singleton;

/**
 * Created with IntelliJ IDEA. User: tho Date: 1/27/14 Time: 11:42 AM Copyright (c) TradeHero
 */
@Module(
        injects = {
                AuthenticationActivity.class,
                DashboardActivity.class,
        },
        complete = false
)
public class SocialNetworkModule
{
    private static final String TWITTER_CONSUMER_KEY = "sJY7n9k29TAhraq4VjDYeg";
    private static final String TWITTER_CONSUMER_SECRET = "gRLhwCd3YgdaKKEH7Gwq9FI75TJuqHfi2TiDRwUHo";
    private static final String LINKEDIN_CONSUMER_KEY = "afed437khxve";
    private static final String LINKEDIN_CONSUMER_SECRET = "hO7VeSyL4y1W2ZiK";
    private static final String FACEBOOK_APP_ID = "431745923529834";

    @Provides @Singleton @ConsumerKey("Twitter") String provideTwitterConsumerKey()
    {
        return TWITTER_CONSUMER_KEY;
    }
    @Provides @Singleton @ConsumerSecret("Twitter") String provideTwitterConsumerSecret()
    {
        return TWITTER_CONSUMER_SECRET;
    }

    @Provides @Singleton @ConsumerKey("LinkedIn") String provideLinkedInConsumerKey()
    {
        return LINKEDIN_CONSUMER_KEY;
    }
    @Provides @Singleton @ConsumerSecret("LinkedIn") String provideLinkedInConsumerSecret()
    {
        return LINKEDIN_CONSUMER_SECRET;
    }

    @Provides @Singleton @FacebookAppId String provideFacebookAppId()
    {
        return FACEBOOK_APP_ID;
    }

    @Provides @Singleton @FacebookPermissions Collection<String> provideFacebookPermissions()
    {
        return null;
    }
}

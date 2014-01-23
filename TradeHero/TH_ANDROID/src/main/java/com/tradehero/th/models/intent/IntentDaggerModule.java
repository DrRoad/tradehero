package com.tradehero.th.models.intent;

import com.tradehero.th.models.intent.competition.ProviderIntentFactory;
import com.tradehero.th.models.intent.portfolio.PortfolioIntentFactory;
import com.tradehero.th.models.intent.security.SecurityIntentFactory;
import com.tradehero.th.models.intent.trending.TrendingIntentFactory;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Created by xavier on 1/13/14.
 */
@Module(
        staticInjections =
                {
                        com.tradehero.th.models.intent.THIntent.class,
                },
        injects =
                {
                        com.tradehero.th.activities.DashboardActivity.class,
                        com.tradehero.th.fragments.web.THWebViewClient.class,
                },
        complete = false,
        library = true
)
public class IntentDaggerModule
{
    public static final String TAG = IntentDaggerModule.class.getSimpleName();

    public IntentDaggerModule()
    {
    }

    @Provides @Singleton THIntentFactory provideTHIntentFactory()
    {
        THIntentFactoryImpl factory = new THIntentFactoryImpl();

        // Dashboard tab factories
        factory.addSubFactory(new TrendingIntentFactory());
        factory.addSubFactory(new PortfolioIntentFactory());
        factory.addSubFactory(new ProviderIntentFactory());

        // Push only factories
        factory.addSubFactory(new SecurityIntentFactory());
        return factory;
    }
}

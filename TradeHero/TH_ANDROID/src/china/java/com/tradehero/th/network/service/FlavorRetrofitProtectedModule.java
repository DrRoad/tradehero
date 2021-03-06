package com.tradehero.th.network.service;

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
public class FlavorRetrofitProtectedModule
{
    //<editor-fold desc="API Services">
    @Provides @Singleton AlertPlanCheckServiceAsync provideAlertPlanCheckServiceAsync(RestAdapter adapter)
    {
        return adapter.create(AlertPlanCheckServiceAsync.class);
    }
    //</editor-fold>
}

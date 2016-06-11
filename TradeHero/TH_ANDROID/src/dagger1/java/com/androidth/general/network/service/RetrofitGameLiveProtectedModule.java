package com.androidth.general.network.service;

import com.androidth.general.network.*;
import com.androidth.general.network.service.ayondo.RetrofitGameLiveAyondoProtectedModule;
import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module(
        includes = {
//                RetrofitGLiveProtectedBuildTypeModule.class,
                RetrofitGameLiveAyondoProtectedModule.class,
        },
        complete = false,
        library = true
)
public class RetrofitGameLiveProtectedModule
{
    //<editor-fold desc="API Services">
    @Provides LiveServiceRx provideLiveServiceRx (@ForLive RestAdapter adapter)
    {
        return adapter.create(LiveServiceRx .class);
    }
    //</editor-fold>
}
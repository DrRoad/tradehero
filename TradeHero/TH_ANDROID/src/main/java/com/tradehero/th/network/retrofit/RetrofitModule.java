package com.tradehero.th.network.retrofit;

import android.content.Context;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.tradehero.common.annotation.ForApp;
import com.tradehero.common.log.RetrofitErrorHandlerLogger;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.common.utils.JacksonConverter;
import com.tradehero.th.api.ObjectMapperWrapper;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderDTODeserialiser;
import com.tradehero.th.api.competition.ProviderDTOJacksonModule;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.position.PositionDTODeserialiser;
import com.tradehero.th.api.position.PositionDTOJacksonModule;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.social.UserFriendsDTODeserialiser;
import com.tradehero.th.api.social.UserFriendsDTOJacksonModule;
import com.tradehero.th.models.intent.competition.ProviderPageIntent;
import com.tradehero.th.network.ApiAuthenticator;
import com.tradehero.th.network.CompetitionUrl;
import com.tradehero.th.network.NetworkConstants;
import com.tradehero.th.network.NullHostNameVerifier;
import com.tradehero.th.network.ServerEndpoint;
import com.tradehero.th.network.service.ProviderService;
import com.tradehero.th.network.service.RetrofitProtectedModule;
import com.tradehero.th.network.service.SecurityService;
import com.tradehero.th.network.service.SocialLinker;
import com.tradehero.th.network.service.SocialServiceWrapper;
import com.tradehero.th.network.service.UserService;
import com.tradehero.th.network.service.UserTimelineService;
import com.tradehero.th.utils.NetworkUtils;
import com.tradehero.th.utils.RetrofitConstants;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import java.io.IOException;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import timber.log.Timber;

@Module(
        includes = {
                FlavorRetrofitModule.class,
                RetrofitProtectedModule.class,
        },
        injects = {
                ProviderPageIntent.class
        },
        complete = false,
        library = true
)
public class RetrofitModule
{
    //<editor-fold desc="API Services">
    @Provides @Singleton ProviderService provideProviderService(RestAdapter adapter)
    {
        return adapter.create(ProviderService.class);
    }

    @Provides @Singleton SecurityService provideSecurityService(RestAdapter adapter)
    {
        return adapter.create(SecurityService.class);
    }

    @Provides @Singleton UserService provideUserService(RestAdapter adapter)
    {
        return adapter.create(UserService.class);
    }

    @Provides @Singleton UserTimelineService provideUserTimelineService(RestAdapter adapter)
    {
        return adapter.create(UserTimelineService.class);
    }
    //</editor-fold>

    @Provides JsonDeserializer<PositionDTO> providesPositionDTODeserialiser(PositionDTODeserialiser deserialiser)
    {
        return deserialiser;
    }

    @Provides JsonDeserializer<ProviderDTO> providesProviderDTODeserialiser(ProviderDTODeserialiser deserialiser)
    {
        return deserialiser;
    }

    @Provides JsonDeserializer<UserFriendsDTO> providersUserFriendsDTODeserialiser(UserFriendsDTODeserialiser deserialiser)
    {
        return deserialiser;
    }

    @Provides ObjectMapper provideCommonObjectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Provides @Singleton @ForApp ObjectMapper provideObjectMapper(
            ObjectMapperWrapper objectMapper,
            UserFriendsDTOJacksonModule userFriendsDTOModule,
            PositionDTOJacksonModule positionDTOModule,
            ProviderDTOJacksonModule providerDTOModule)
    {
        objectMapper.registerModule(userFriendsDTOModule);
        objectMapper.registerModule(positionDTOModule);
        objectMapper.registerModule(providerDTOModule);

        // TODO confirm this is correct here
        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.DEFAULT)
                .withCreatorVisibility(JsonAutoDetect.Visibility.ANY));
        return objectMapper;
    }

    @Provides @Singleton Converter provideConverter(@ForApp ObjectMapper objectMapper)
    {
        return new JacksonConverter(objectMapper);
    }

    @Provides @Singleton Endpoint provideApiServer(@ServerEndpoint StringPreference serverEndpointPreference)
    {
        return Endpoints.newFixedEndpoint(serverEndpointPreference.get());
    }

    @Provides @Singleton @CompetitionUrl String provideCompetitionUrl(Endpoint server)
    {
        return server.getUrl() + NetworkConstants.COMPETITION_PATH;
    }

    @Provides RestAdapter.Builder provideRestAdapterBuilder(
            Client client,
            Converter converter,
            RetrofitSynchronousErrorHandler errorHandler)
    {
        return new RestAdapter.Builder()
                .setConverter(converter)
                .setClient(client)
                .setErrorHandler(errorHandler)
                .setLogLevel(RetrofitConstants.DEFAULT_SERVICE_LOG_LEVEL);
    }

    @Provides @Singleton RestAdapter provideRestAdapter(RestAdapter.Builder builder,
            Endpoint server,
            RequestHeaders requestHeaders,
            RetrofitErrorHandlerLogger errorHandlerLogger)
    {
        return builder
                .setEndpoint(server)
                .setRequestInterceptor(requestHeaders)
                .setErrorHandler(errorHandlerLogger)
                .build();
    }

    @Provides @Singleton Client provideOkClient(OkHttpClient okHttpClient)
    {
        return new OkClient(okHttpClient);
    }

    @Provides @Singleton Cache provideHttpCache(Context context)
    {
        File httpCacheDirectory = new File(context.getCacheDir(), "HttpCache");

        try
        {
            // HttpResponseCache httpResponseCache = HttpResponseCache.install(httpCacheDirectory, );
            return new Cache(httpCacheDirectory, 10 * 1024 * 1024);
        }
        catch (IOException e)
        {
            Timber.e("Could not create http cache", e);
        }
        return null;
    }

    @Provides @Singleton OkHttpClient provideOkHttpClient(Cache cache, Authenticator authenticator, HostnameVerifier hostNameVerifier)
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setCache(cache);
        okHttpClient.setHostnameVerifier(hostNameVerifier);
        okHttpClient.setSslSocketFactory(NetworkUtils.createBadSslSocketFactory());
        okHttpClient.setAuthenticator(authenticator);
        return okHttpClient;
    }

    @Provides @Singleton Authenticator provideAuthenticator(Lazy<ApiAuthenticator> apiAuthenticator)
    {
        return apiAuthenticator.get();
    }

    @Provides @Singleton HostnameVerifier provideHostnameVerifier(NullHostNameVerifier hostNameVerifier)
    {
        return hostNameVerifier;
    }

    @Provides @Singleton SocialLinker provideSocialLinker(SocialServiceWrapper socialServiceWrapper)
    {
        return socialServiceWrapper;
    }
}

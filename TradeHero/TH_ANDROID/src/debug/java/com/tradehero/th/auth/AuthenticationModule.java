package com.tradehero.th.auth;

import android.content.Context;
import com.facebook.TokenCachingStrategy;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.auth.linkedin.LinkedInAuthenticationProvider;
import com.tradehero.th.auth.weibo.WeiboAuthenticationProvider;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import java.util.Collections;
import java.util.Map;
import javax.inject.Singleton;

@Module(
        library = true,
        complete = false
)
public class AuthenticationModule
{
    /** TODO waiting for dagger to have map injection feature, it would make following method nicer */
    @Provides @Singleton @SocialAuth Map<SocialNetworkEnum, AuthenticationProvider> provideSocialAuthTypeToSocialProviderMap(
            Lazy<FacebookAuthenticationProvider> facebookAuthenticationProvider,
            Lazy<TwitterAuthenticationProvider> twitterAuthenticationProvider,
            Lazy<LinkedInAuthenticationProvider> linkedInAuthenticationProvider,
            Lazy<WeiboAuthenticationProvider> weiboAuthenticationProvider)
    {
        Map<SocialNetworkEnum, AuthenticationProvider> enumToUtilMap = AuthenticationModuleBase.provideSocialAuthTypeToSocialProviderMap(
                facebookAuthenticationProvider,
                twitterAuthenticationProvider,
                linkedInAuthenticationProvider,
                weiboAuthenticationProvider);
        return Collections.unmodifiableMap(enumToUtilMap);
    }

    @Provides @Singleton TokenCachingStrategy provideFacebookTokenCachingStrategy(Context context)
    {
        return AuthenticationModuleBase.provideFacebookTokenCachingStrategy(context);
    }
}

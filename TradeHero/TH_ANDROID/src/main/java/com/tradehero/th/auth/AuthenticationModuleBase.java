package com.tradehero.th.auth;

import android.content.Context;
import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.TokenCachingStrategy;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.auth.linkedin.LinkedInAuthenticationProvider;
import com.tradehero.th.auth.weibo.WeiboAuthenticationProvider;
import dagger.Lazy;
import java.util.HashMap;
import java.util.Map;

class AuthenticationModuleBase
{
    static Map<SocialNetworkEnum, AuthenticationProvider> provideSocialAuthTypeToSocialProviderMap(
            Lazy<FacebookAuthenticationProvider> facebookAuthenticationProvider,
            Lazy<TwitterAuthenticationProvider> twitterAuthenticationProvider,
            Lazy<LinkedInAuthenticationProvider> linkedInAuthenticationProvider,
            Lazy<WeiboAuthenticationProvider> weiboAuthenticationProvider)
    {
        Map<SocialNetworkEnum, AuthenticationProvider> enumToUtilMap = new HashMap<>();
        enumToUtilMap.put(SocialNetworkEnum.FB, facebookAuthenticationProvider.get());
        enumToUtilMap.put(SocialNetworkEnum.TW, twitterAuthenticationProvider.get());
        enumToUtilMap.put(SocialNetworkEnum.LN, linkedInAuthenticationProvider.get());
        enumToUtilMap.put(SocialNetworkEnum.WB, weiboAuthenticationProvider.get());
        return enumToUtilMap;
    }

    static TokenCachingStrategy provideFacebookTokenCachingStrategy(Context context)
    {
        return new SharedPreferencesTokenCachingStrategy(context);
    }
}

package com.tradehero.th.auth;

import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.auth.linkedin.LinkedInAuthenticationProvider;
import com.tradehero.th.auth.weibo.WeiboAuthenticationProvider;
import dagger.Module;
import dagger.Provides;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

@Module(
        library = true,
        complete = false
)
public class AuthenticationModule
{
    /** TODO waiting for dagger to have map injection feature, it would make this method a lot nicer */
    @Provides @Singleton @SocialAuth Map<SocialNetworkEnum, AuthenticationProvider> provideSocialAuthTypeMap(
            FacebookAuthenticationProvider facebookAuthenticationProvider,
            TwitterAuthenticationProvider twitterAuthenticationProvider,
            LinkedInAuthenticationProvider linkedInAuthenticationProvider,
            WeiboAuthenticationProvider weiboAuthenticationProvider,
            EmailAuthenticationProvider emailAuthenticationProvider
    )
    {
        Map<SocialNetworkEnum, AuthenticationProvider> enumToUtilMap = new HashMap<>();
        enumToUtilMap.put(SocialNetworkEnum.FB, facebookAuthenticationProvider);
        enumToUtilMap.put(SocialNetworkEnum.TW, twitterAuthenticationProvider);
        enumToUtilMap.put(SocialNetworkEnum.LN, linkedInAuthenticationProvider);
        enumToUtilMap.put(SocialNetworkEnum.WB, weiboAuthenticationProvider);
        enumToUtilMap.put(SocialNetworkEnum.TH, emailAuthenticationProvider);
        return Collections.unmodifiableMap(enumToUtilMap);
    }
}

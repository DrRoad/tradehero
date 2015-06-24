package com.tradehero.th.auth.tencent_qq;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tauth.Tencent;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.auth.AuthData;
import com.tradehero.th.auth.SocialAuthenticationProvider;
import com.tradehero.th.network.service.SocialLinker;
import static com.tradehero.th.utils.Constants.TENCENT_APP_ID;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.functions.Func1;

@Singleton
public class QQAuthenticationProvider extends SocialAuthenticationProvider
{
    private static final String SCOPE = "all";
    @NonNull private final Context context;
    @NonNull private final Tencent tencent;
    @NonNull private final ObjectMapper objectMapper;

    //<editor-fold desc="Constructors">
    @Inject public QQAuthenticationProvider(
            @NonNull SocialLinker socialLinker,
            @NonNull Context context,
            @NonNull ObjectMapper objectMapper)
    {
        super(socialLinker);
        this.context = context;
        this.tencent = Tencent.createInstance(TENCENT_APP_ID, context);
        this.objectMapper = objectMapper;
    }
    //</editor-fold>

    @Override protected Observable<AuthData> createAuthDataObservable(Activity activity)
    {
        tencent.logout(activity);

        return Observable.create(new OperatorTencent(tencent, activity, objectMapper, SCOPE))
            .map(new Func1<QQAppAuthData, AuthData>()
            {
                @Override public AuthData call(@NonNull QQAppAuthData qqAppAuthData)
                {
                    return new AuthData(
                            SocialNetworkEnum.QQ,
                            null, // FIXME TODO expiration
                            qqAppAuthData.openId,
                            qqAppAuthData.accessToken);
                }
            });
    }

    @Override public void logout()
    {
        tencent.logout(context);
    }
}

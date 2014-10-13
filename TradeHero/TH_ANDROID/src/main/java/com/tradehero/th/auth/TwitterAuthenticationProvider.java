package com.tradehero.th.auth;

import android.app.Activity;
import android.content.Context;
import android.webkit.CookieSyncManager;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.auth.operator.ConsumerKey;
import com.tradehero.th.auth.operator.ConsumerSecret;
import com.tradehero.th.auth.operator.OperatorOAuthDialog;
import com.tradehero.th.network.service.SocialLinker;
import javax.inject.Inject;
import javax.inject.Singleton;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import org.jetbrains.annotations.NotNull;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Singleton
public class TwitterAuthenticationProvider extends SocialAuthenticationProvider
{
    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    private static final String CALLBACK_URL = "twitter-oauth://complete";

    private static final String SERVICE_URL_ID = "api.twitter";
    private static final String USER_ID_PARAM = "user_id";
    private static final String SCREEN_NAME_PARAM = "screen_name";

    private final OAuthProvider provider;
    private final CommonsHttpOAuthConsumer consumer;

    @Inject public TwitterAuthenticationProvider(@NotNull SocialLinker socialLinker,
            @ConsumerKey("Twitter") String consumerKey,
            @ConsumerSecret("Twitter") String consumerSecret)
    {
        super(socialLinker);
        provider = new CommonsHttpOAuthProvider(
                REQUEST_TOKEN_URL,
                ACCESS_TOKEN_URL,
                AUTHORIZE_URL);

        consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
    }

    @Override protected Observable<AuthData> createAuthDataObservable(Activity activity)
    {
        return createRequestTokenObservable(activity)
                .flatMap(new Func1<String, Observable<AuthData>>()
                {
                    @Override public Observable<AuthData> call(String s)
                    {
                        return createRetrieveTokenObservable(s);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override public void logout()
    {
        consumer.setTokenWithSecret(null, null);
    }

    private Observable<String> createRequestTokenObservable(final Context context)
    {
        return Observable.create(new Observable.OnSubscribe<String>()
        {

            @Override public void call(Subscriber<? super String> subscriber)
            {
                try
                {
                    String requestToken = provider.retrieveRequestToken(consumer, CALLBACK_URL);
                    CookieSyncManager.createInstance(context);
                    subscriber.onNext(requestToken);
                    subscriber.onCompleted();
                }
                catch (Throwable e)
                {
                    subscriber.onError(e);
                }
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<String, Observable<String>>()
                {
                    @Override public Observable<String> call(final String tokenRequestUrl)
                    {
                        return Observable.create(new OperatorOAuthDialog(context, tokenRequestUrl, CALLBACK_URL, SERVICE_URL_ID));
                    }
                })
                .observeOn(Schedulers.io());
    }

    private Observable<AuthData> createRetrieveTokenObservable(final String verifier)
    {
        return Observable.create(new Observable.OnSubscribe<AuthData>()
        {
            @Override public void call(Subscriber<? super AuthData> subscriber)
            {
                try
                {
                    Timber.d("Verifier: " + verifier);
                    provider.retrieveAccessToken(consumer, verifier);
                    // TODO lot of information can be extracted from response parameters
                    provider.getResponseParameters();
                    subscriber.onNext(new AuthData(SocialNetworkEnum.TW, null, consumer.getToken(), consumer.getTokenSecret()));
                    subscriber.onCompleted();
                }
                catch (Throwable e)
                {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }
}
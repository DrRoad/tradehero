package com.tradehero.th.auth;

import android.app.Activity;
import android.content.Context;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.network.service.SocialLinker;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.NotNull;
import rx.Observable;
import rx.functions.Func1;

public abstract class SocialAuthenticationProvider implements THAuthenticationProvider
{
    public static final String SCREEN_NAME_KEY = "screen_name";
    public static final String ID_KEY = "id";
    public static final String EMAIL_KEY = "email";
    public static final String AUTH_TOKEN_SECRET_KEY = "auth_token_secret";
    public static final String AUTH_TOKEN_KEY = "auth_token";
    public static final String CONSUMER_KEY_KEY = "consumer_key";
    public static final String CONSUMER_SECRET_KEY = "consumer_secret";

    // TODO make it private when the refactor is done
    protected WeakReference<Activity> baseActivity;
    private Map<Activity, Observable<AuthData>> cachedObservables = new WeakHashMap<>();

    protected WeakReference<Context> baseContext;
    protected THAuthenticationProvider.THAuthenticationCallback currentOperationCallback;

    @NotNull protected final SocialLinker socialLinker;

    protected SocialAuthenticationProvider(@NotNull SocialLinker socialLinker)
    {
        this.socialLinker = socialLinker;
    }

    public SocialAuthenticationProvider with(Context context)
    {
        baseContext = new WeakReference<>(context);
        return this;
    }

    @Override public void cancel()
    {
        handleCancel(this.currentOperationCallback);
    }

    protected void handleCancel(THAuthenticationProvider.THAuthenticationCallback callback)
    {
        if ((currentOperationCallback != callback) || (callback == null))
        {
            return;
        }
        try
        {
            callback.onCancel();
        }
        finally
        {
            currentOperationCallback = null;
        }
    }

    @Override
    public final Observable<AuthData> logIn(Activity activity)
    {
        // FIXME use caching
        baseActivity = new WeakReference<>(activity);
        Observable<AuthData> cachedObservable;// = cachedObservables.get(activity);
        //if (cachedObservable != null)
        //{
        //    return cachedObservable;
        //}

        cachedObservable = createAuthDataObservable(activity);
        //cachedObservables.put(activity, cachedObservable);
        return cachedObservable;
    }

    protected abstract Observable<AuthData> createAuthDataObservable(Activity activity);

    protected void clearCachedObservables()
    {
        cachedObservables.clear();
    }

    @Override public Observable<UserProfileDTO> socialLink(
            @NotNull Activity activity)
    {
        return logIn(activity)
                .flatMap(new Func1<AuthData, Observable<UserProfileDTO>>()
                {
                    @Override public Observable<UserProfileDTO> call(AuthData authData)
                    {
                        return socialLinker.link(new AccessTokenForm(authData));
                    }
                });
    }
}

package com.tradehero.th.auth;

/** Created with IntelliJ IDEA. User: tho Date: 8/15/13 Time: 2:44 PM Copyright (c) TradeHero */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.TokenCachingStrategy;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

public class FacebookAuthenticationProvider implements THAuthenticationProvider
{
    private final DateFormat preciseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    public static final String ACCESS_TOKEN_KEY =  "access_token";
    public static final String EXPIRATION_DATE_KEY = "expiration_date";

    private Facebook facebook;
    private Session session;
    private SessionDefaultAudience defaultAudience;
    private String applicationId;
    private int activityCode;
    private WeakReference<Activity> baseActivity;
    private Context applicationContext;
    private Collection<String> permissions;
    private THAuthenticationProvider.THAuthenticationCallback currentOperationCallback;
    private String userId;

    public FacebookAuthenticationProvider(Context context, String applicationId)
    {
        this.preciseDateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));

        this.activityCode = 32665;
        this.baseActivity = new WeakReference<>(null);

        this.applicationId = applicationId;
        if (context != null)
        {
            this.applicationContext = context.getApplicationContext();
        }

        if (applicationId != null)
        {
            this.facebook = new Facebook(applicationId);
        }
    }

    @Deprecated
    public synchronized void extendAccessToken(Context context, THAuthenticationProvider.THAuthenticationCallback callback)
    {
        if (this.currentOperationCallback != null)
        {
            cancel();
        }
        this.currentOperationCallback = callback;
        boolean result = this.facebook.extendAccessToken(context, new Facebook.ServiceListener()
        {
            public void onComplete(Bundle values)
            {
                FacebookAuthenticationProvider.this.handleSuccess(
                        FacebookAuthenticationProvider.this.userId);
            }

            public void onFacebookError(FacebookError e)
            {
                FacebookAuthenticationProvider.this.handleError(e);
            }

            public void onError(Error e)
            {
                FacebookAuthenticationProvider.this.handleError(e);
            }
        });
        if (!result)
        {
            handleCancel();
        }
    }

    public synchronized void authenticate(THAuthenticationProvider.THAuthenticationCallback callback)
    {
        if (this.currentOperationCallback != null)
        {
            cancel();
        }
        this.currentOperationCallback = callback;
        Activity activity = this.baseActivity == null ? null : this.baseActivity.get();
        if (activity == null)
        {
            throw new IllegalStateException(
                    "Activity must be non-null for Facebook authentication to proceed.");
        }
        int activityCode = this.activityCode;
        this.session = new Session.Builder(activity).setApplicationId(this.applicationId)
                .setTokenCachingStrategy(new SharedPreferencesTokenCachingStrategy(activity))
                .build();

        callback.onStart();
        Session.OpenRequest openRequest = new Session.OpenRequest(activity);
        openRequest.setRequestCode(activityCode);
        if (this.defaultAudience != null)
        {
            openRequest.setDefaultAudience(this.defaultAudience);
        }
        if (this.permissions != null)
        {
            openRequest.setPermissions(new ArrayList(this.permissions));
        }
        openRequest.setCallback(new Session.StatusCallback()
        {
            public void call(Session session, SessionState state, Exception exception)
            {
                if (state == SessionState.OPENING)
                {
                    return;
                }
                if (state.isOpened())
                {
                    if (FacebookAuthenticationProvider.this.currentOperationCallback == null)
                    {
                        return;
                    }
                    Request meRequest = Request.newGraphPathRequest(session, "me", new Request.Callback()
                    {
                        public void onCompleted(Response response)
                        {
                            if (response.getError() != null)
                            {
                                if (response.getError().getException() != null)
                                {
                                    FacebookAuthenticationProvider.this.handleError(response.getError().getException());
                                }
                                else
                                {
                                    FacebookAuthenticationProvider.this.handleError(
                                            new Exception("An error occurred while fetching the Facebook user's identity."));
                                }
                            }
                            else
                            {
                                FacebookAuthenticationProvider.this.handleSuccess(
                                        (String) response.getGraphObject()
                                                .getProperty("id"));
                            }
                        }
                    });
                    meRequest.getParameters().putString("fields", "id");
                    meRequest.executeAsync();
                }
                else if (exception != null)
                {
                    FacebookAuthenticationProvider.this.handleError(exception);
                }
                else
                {
                    FacebookAuthenticationProvider.this.handleCancel();
                }
            }
        });
        this.session.openForRead(openRequest);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Activity activity = this.baseActivity.get();
        if (activity != null)
        {
            this.session.onActivityResult(activity, requestCode, resultCode, data);
        }
    }

    public synchronized void cancel()
    {
        handleCancel();
    }

    public int getActivityCode()
    {
        return this.activityCode;
    }

    public String getAuthType()
    {
        return SocialAuthenticationProvider.FACEBOOK_AUTH_TYPE;
    }

    @Override public String getAuthHeader()
    {
        return getAuthType() + " " + getAuthHeaderParameter ();
    }

    @Override public String getAuthHeaderParameter()
    {
        return this.session.getAccessToken();
    }

    public Facebook getFacebook()
    {
        return this.facebook;
    }

    public Session getSession()
    {
        return this.session;
    }

    private void handleCancel()
    {
        if (this.currentOperationCallback == null)
        {
            return;
        }
        try
        {
            this.currentOperationCallback.onCancel();
        }
        finally
        {
            this.currentOperationCallback = null;
        }
    }

    private void handleError(Throwable error)
    {
        if (this.currentOperationCallback == null)
        {
            return;
        }
        try
        {
            this.currentOperationCallback.onError(error);
        }
        finally
        {
            this.currentOperationCallback = null;
        }
    }

    public JSONObject getAuthData(String id, String accessToken, Date expiration) throws JSONException
    {
        JSONObject authData = new JSONObject();
        authData.put(SocialAuthenticationProvider.ID_KEY, id);
        authData.put(ACCESS_TOKEN_KEY, accessToken);
        authData.put(EXPIRATION_DATE_KEY, this.preciseDateFormat.format(expiration));
        return authData;
    }

    private void handleSuccess(String userId)
    {
        if (this.currentOperationCallback == null)
        {
            return;
        }

        this.userId = userId;
        JSONObject authData = null;
        try
        {
            authData = getAuthData(userId, this.session.getAccessToken(), this.session.getExpirationDate());
        }
        catch (JSONException e)
        {
            handleError(e);
            return;
        }
        try
        {
            this.currentOperationCallback.onSuccess(authData);
        }
        finally
        {
            this.currentOperationCallback = null;
        }
    }

    public synchronized void setActivity(Activity activity)
    {
        this.baseActivity = new WeakReference<>(activity);
    }

    public synchronized void setActivityCode(int activityCode)
    {
        this.activityCode = activityCode;
    }

    public synchronized void setPermissions(Collection<String> permissions)
    {
        this.permissions = permissions;
    }

    public boolean restoreAuthentication(JSONObject authData)
    {
        if (authData == null)
        {
            if (this.facebook != null)
            {
                this.facebook.setAccessExpires(0L);
                this.facebook.setAccessToken(null);
            }
            this.session = null;
            return true;
        }
        try
        {
            String accessToken = authData.getString(ACCESS_TOKEN_KEY);
            Date expirationDate =
                    this.preciseDateFormat.parse(authData.getString(EXPIRATION_DATE_KEY));

            if (this.facebook != null)
            {
                this.facebook.setAccessToken(accessToken);
                this.facebook.setAccessExpires(expirationDate.getTime());
            }
            TokenCachingStrategy tcs = new SharedPreferencesTokenCachingStrategy(
                    this.applicationContext);
            Bundle data = tcs.load();
            TokenCachingStrategy.putToken(data, authData.getString(ACCESS_TOKEN_KEY));
            TokenCachingStrategy.putExpirationDate(data, expirationDate);
            tcs.save(data);

            Session newSession = new Session.Builder(this.applicationContext)
                    .setApplicationId(this.applicationId).setTokenCachingStrategy(tcs).build();
            if (newSession.getState() == SessionState.CREATED_TOKEN_LOADED)
            {
                newSession.openForRead(null);
                this.session = newSession;
                Session.setActiveSession(this.session);
            }
            else
            {
                this.session = null;
            }
            return true;
        }
        catch (Exception e)
        {
        }
        return false;
    }

    public void deauthenticate()
    {
        restoreAuthentication(null);
    }

    public String getUserId()
    {
        return this.userId;
    }
}
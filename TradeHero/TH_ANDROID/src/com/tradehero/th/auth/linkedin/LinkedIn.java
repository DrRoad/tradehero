package com.tradehero.th.auth.linkedin;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.CookieSyncManager;
import com.tradehero.th.auth.THAuthenticationProvider;
import com.tradehero.th.auth.twitter.OAuthDialog;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.http.HttpParameters;

/** Created with IntelliJ IDEA. User: tho Date: 8/21/13 Time: 12:48 PM Copyright (c) TradeHero */
public class LinkedIn
{

    private static final String REQUEST_TOKEN_URL = "https://www.linkedin.com/uas/oauth/requestToken";
    private static final String AUTHORIZE_URL = "https://www.linkedin.com/uas/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth/accessToken";
    private static final String PERMISSION_SCOPE = "r_basicprofile r_emailaddress r_network r_contactinfo rw_nus w_messages";
    private static final String CALLBACK_URL = "x-oauthflow-linkedin://callback";
    private String consumerSecret;
    private String consumerKey;
    private static final OAuthProvider PROVIDER =
            new CommonsHttpOAuthProvider(
                    REQUEST_TOKEN_URL + "?scope=" + getScope(),
                    ACCESS_TOKEN_URL,
                    AUTHORIZE_URL);
    private String authToken;
    private String authTokenSecret;

    public LinkedIn(String consumerKey, String consumerSecret)
    {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public static String getScope() {
        try
        {
            return URLEncoder.encode(PERMISSION_SCOPE, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

    public void setConsumerKey(String consumerKey)
    {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret)
    {
        this.consumerSecret = consumerSecret;
    }

    public void authorize(final Context context,
            final THAuthenticationProvider.THAuthenticationCallback callback)
    {
        if ((getConsumerKey() == null) || (getConsumerKey().length() == 0) || (getConsumerSecret()
                == null) ||
                (getConsumerSecret().length() == 0))
        {
            throw new IllegalStateException(
                    "LinkedIn must be initialized with a consumer key and secret before authorization.");
        }
        final OAuthConsumer consumer = new CommonsHttpOAuthConsumer(getConsumerKey(),
                getConsumerSecret());
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage("Connecting ...");
        AsyncTask task = new AsyncTask<Object, Object, String>()
        {
            private Throwable error;

            @Override
            protected void onPostExecute(String result)
            {
                super.onPostExecute(result);
                try
                {
                    if (this.error != null)
                    {
                        callback.onError(this.error);
                        return;
                    }
                    CookieSyncManager.createInstance(context);
                    OAuthDialog dialog = new OAuthDialog(context, result, CALLBACK_URL,
                            "www.linkedin", new OAuthDialog.FlowResultHandler()
                    {
                        @Override
                        public void onError(int errorCode, String description,
                                String failingUrl)
                        {
                            callback.onError(new Exception(
                                    String.format("Error {0}, description: {1}, url: {2}",
                                            errorCode, description, failingUrl)));
                        }

                        @Override
                        public void onComplete(String callbackUrl)
                        {
                            CookieSyncManager.getInstance().sync();
                            Uri uri = Uri.parse(callbackUrl);
                            final String verifier = uri.getQueryParameter("oauth_verifier");
                            if (verifier == null)
                            {
                                callback.onCancel();
                                return;
                            }
                            AsyncTask getTokenTask =
                                    new AsyncTask<Object, Object, HttpParameters>()
                                    {
                                        private Throwable error;

                                        @Override
                                        protected HttpParameters doInBackground(
                                                Object... params)
                                        {
                                            try
                                            {
                                                LinkedIn.PROVIDER
                                                        .retrieveAccessToken(consumer,
                                                                verifier);
                                            }
                                            catch (Throwable e)
                                            {
                                                this.error = e;
                                            }
                                            return LinkedIn.PROVIDER.getResponseParameters();
                                        }

                                        @Override
                                        protected void onPreExecute()
                                        {
                                            super.onPreExecute();
                                            progress.show();
                                        }

                                        @Override
                                        protected void onPostExecute(HttpParameters result)
                                        {
                                            super.onPostExecute(result);
                                            try
                                            {
                                                if (this.error != null)
                                                {
                                                    callback.onError(this.error);
                                                    return;
                                                }
                                                try
                                                {
                                                    LinkedIn.this.setAuthToken(consumer.getToken());
                                                    LinkedIn.this.setAuthTokenSecret(
                                                            consumer.getTokenSecret());
                                                }
                                                catch (Throwable e)
                                                {
                                                    callback.onError(e);
                                                    return;
                                                }
                                                callback.onSuccess(null);
                                            }
                                            finally
                                            {
                                                progress.dismiss();
                                            }
                                        }
                                    };
                            getTokenTask.execute();
                        }

                        @Override
                        public void onCancel()
                        {
                            callback.onCancel();
                        }
                    });
                    dialog.show();
                }
                finally
                {
                    progress.dismiss();
                }
            }

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                progress.show();
            }

            @Override
            protected String doInBackground(Object... params)
            {
                try
                {
                    return LinkedIn.PROVIDER
                            .retrieveRequestToken(consumer, CALLBACK_URL);
                }
                catch (Throwable e)
                {
                    this.error = e;
                }
                return null;
            }
        };
        task.execute();
    }

    public String getConsumerKey()
    {
        return consumerKey;
    }

    public String getConsumerSecret()
    {
        return consumerSecret;
    }

    public void setAuthToken(String authToken)
    {
        this.authToken = authToken;
    }

    public String getAuthToken()
    {
        return authToken;
    }

    public void setAuthTokenSecret(String authTokenSecret)
    {
        this.authTokenSecret = authTokenSecret;
    }

    public String getAuthTokenSecret()
    {
        return authTokenSecret;
    }
}

package com.tradehero.th.base;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.tradehero.common.annotation.ForUser;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.activities.AuthenticationActivity;
import com.tradehero.th.activities.CurrentActivityHolder;
import com.tradehero.th.api.form.FacebookUserFormDTO;
import com.tradehero.th.api.form.LinkedinUserFormDTO;
import com.tradehero.th.api.form.QQUserFormDTO;
import com.tradehero.th.api.form.TwitterUserFormDTO;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.form.UserFormFactory;
import com.tradehero.th.api.form.WeiboUserFormDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.LoginFormDTO;
import com.tradehero.th.api.users.LoginSignUpFormDTO;
import com.tradehero.th.api.users.UserLoginDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.auth.AuthenticationMode;
import com.tradehero.th.auth.THAuthenticationProvider;
import com.tradehero.th.misc.callback.LogInCallback;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.misc.exception.THException.ExceptionCode;
import com.tradehero.th.models.push.DeviceTokenHelper;
import com.tradehero.th.models.user.auth.CredentialsDTO;
import com.tradehero.th.models.user.auth.CredentialsDTOFactory;
import com.tradehero.th.models.user.auth.CredentialsSetPreference;
import com.tradehero.th.models.user.auth.FacebookCredentialsDTO;
import com.tradehero.th.models.user.auth.LinkedinCredentialsDTO;
import com.tradehero.th.models.user.auth.MainCredentialsPreference;
import com.tradehero.th.models.user.auth.QQCredentialsDTO;
import com.tradehero.th.models.user.auth.TwitterCredentialsDTO;
import com.tradehero.th.models.user.auth.WeiboCredentialsDTO;
import com.tradehero.th.network.service.SessionServiceWrapper;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.DTOCacheUtil;
import com.tradehero.th.persistence.social.VisitedFriendListPrefs;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.AlertDialogUtil;
import com.tradehero.th.utils.Constants;
import dagger.Lazy;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import org.json.JSONException;
import timber.log.Timber;

public class THUser
{
    private static AuthenticationMode authenticationMode;
    private static THAuthenticationProvider authenticator;
    private static final Map<String, THAuthenticationProvider> authenticationProviders = new HashMap<>();

    private static HashMap<String, CredentialsDTO> typedCredentials;

    @Inject static MainCredentialsPreference mainCredentialsPreference;
    @Inject static CredentialsSetPreference credentialsSetPreference;
    @Inject static CurrentUserId currentUserId;

    @Inject @ForUser static Lazy<SharedPreferences> sharedPreferences;
    @Inject static Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject static Lazy<SessionServiceWrapper> sessionServiceWrapper;
    @Inject static Lazy<UserProfileCache> userProfileCache;
    @Inject static Lazy<DTOCacheUtil> dtoCacheUtil;
    @Inject static Lazy<AlertDialogUtil> alertDialogUtil;
    @Inject static Lazy<CurrentActivityHolder> currentActivityHolder;
    @Inject static CredentialsDTOFactory credentialsDTOFactory;
    @Inject static Provider<LoginFormDTO> loginFormDTOProvider;

    public static void initialize()
    {
        typedCredentials = new HashMap<>();
        for (CredentialsDTO credentialsDTO : credentialsSetPreference.getCredentials())
        {
            typedCredentials.put(credentialsDTO.getAuthType(), credentialsDTO);
        }
    }

    public static void logInWithAsync(String authType, LogInCallback callback)
    {
        if (!authenticationProviders.containsKey(authType))
        {
            throw new IllegalArgumentException("No authentication provider could be found for the provided authType");
        }
        authenticator = authenticationProviders.get(authType);
        logInWithAsync(authenticationProviders.get(authType), callback);
    }

    private static void logInWithAsync(final THAuthenticationProvider authenticator, final LogInCallback callback)
    {
        CredentialsDTO savedCredentials = typedCredentials.get(authenticator.getAuthType());
        if (savedCredentials != null)
        {
            callback.onStart();
            JSONCredentials jsonCredentials = null;
            try
            {
                jsonCredentials = savedCredentials.createJSON();
                if (authenticator.restoreAuthentication(jsonCredentials))
                {
                    if (callback.onSocialAuthDone(jsonCredentials))
                    {
                        logInAsyncWithJson(savedCredentials, callback);
                    }
                    return;
                }
            }
            catch (JSONException e)
            {
                Timber.e(e, "Failed to convert credentials %s", savedCredentials);
            }
        }
        THAuthenticationProvider.THAuthenticationCallback outerCallback = createCallbackForLogInWithAsync (callback);
        authenticator.authenticate(outerCallback);
    }

    public static void logInAsyncWithJson(final CredentialsDTO credentialsDTO, final LogInCallback callback)
    {
        UserFormDTO userFormDTO = credentialsDTO.createUserFormDTO();
        if (userFormDTO == null)
        {
            // input error, unable to parse as json data
            THToast.show(R.string.authentication_error_creating_signup_form);
            return;
        }
        if (userFormDTO.deviceToken == null)
        {
            userFormDTO.deviceToken = DeviceTokenHelper.getDeviceToken();
        }
        Timber.d("APID: %s,authenticationMode :%s", userFormDTO.deviceToken,/*PushManager.shared().getAPID()*/
                authenticationMode);
        //userFormDTO.deviceToken = DeviceTokenHelper.getDeviceToken();//PushManager.shared().getAPID();

        if (authenticationMode == null)
        {
            authenticationMode = AuthenticationMode.SignIn;
        }

        switch (authenticationMode)
        {
            case SignUpWithEmail:
                Timber.d("SignUpWithEmail Auth Header "+authenticator.getAuthHeader());
                userServiceWrapper.get().signUpWithEmail(
                        authenticator.getAuthHeader(),
                        userFormDTO,
                        createCallbackForSignUpAsyncWithJson(credentialsDTO, callback));
                break;
            case SignUp:
                Timber.d("SignUp Auth Header "+authenticator.getAuthHeader());
                userServiceWrapper.get().signUp(
                        authenticator.getAuthHeader(),
                        userFormDTO,
                        createCallbackForSignUpAsyncWithJson(credentialsDTO, callback));
                break;
            case SignIn:
                LoginFormDTO loginFormDTO = loginFormDTOProvider.get();
                //use new DTO, combine login and social register
                LoginSignUpFormDTO loginSignUpFormDTO = new LoginSignUpFormDTO(loginFormDTO);
                if (userFormDTO instanceof WeiboUserFormDTO)
                {
                    loginSignUpFormDTO.weibo_access_token = ((WeiboUserFormDTO)userFormDTO).accessToken;
                }
                else if (userFormDTO instanceof FacebookUserFormDTO)
                {
                    loginSignUpFormDTO.facebook_access_token = ((FacebookUserFormDTO)userFormDTO).accessToken;
                }
                else if (userFormDTO instanceof TwitterUserFormDTO)
                {
                    loginSignUpFormDTO.twitter_access_token = ((TwitterUserFormDTO)userFormDTO).accessToken;
                    loginSignUpFormDTO.twitter_access_token_secret = ((TwitterUserFormDTO)userFormDTO).accessTokenSecret;
                }
                else if (userFormDTO instanceof LinkedinUserFormDTO)
                {
                    loginSignUpFormDTO.linkedin_access_token = ((LinkedinUserFormDTO)userFormDTO).accessToken;
                    loginSignUpFormDTO.linkedin_access_token_secret = ((LinkedinUserFormDTO)userFormDTO).accessTokenSecret;
                }
                else if (userFormDTO instanceof QQUserFormDTO)
                {
                    loginSignUpFormDTO.qq_access_token = ((QQUserFormDTO)userFormDTO).accessToken;
                    loginSignUpFormDTO.qq_openid = ((QQUserFormDTO)userFormDTO).openid;
                }
                else
                {
                    loginSignUpFormDTO.isEmailLogin = true;
                }

                // TODO save middle callback?
                sessionServiceWrapper.get().signupAndLogin(authenticator.getAuthHeader(),
                        loginSignUpFormDTO,
                        createCallbackForSignInAsyncWithJson(credentialsDTO, callback));
                break;
        }
    }

    private static THAuthenticationProvider.THAuthenticationCallback createCallbackForLogInWithAsync (final LogInCallback callback)
    {
        return new THAuthenticationProvider.THAuthenticationCallback()
        {
            @Override public void onStart()
            {
                callback.onStart();
            }

            @Override public void onSuccess(JSONCredentials json)
            {
                try
                {
                    json.put(UserFormFactory.KEY_TYPE, authenticator.getAuthType());
                    if (callback.onSocialAuthDone(json))
                    {
                        logInAsyncWithJson(credentialsDTOFactory.create(json), callback);
                    }
                }
                catch (JSONException|ParseException ex)
                {
                    Timber.e(ex, "Failed to onsuccess");
                }
            }

            @Override public void onCancel()
            {
                callback.done(null, ExceptionCode.UserCanceled.toException());
            }

            @Override public void onError(Throwable throwable)
            {
                callback.done(null, new THException(throwable));
            }
        };
    }

    private static THCallback<UserProfileDTO> createCallbackForSignUpAsyncWithJson(final CredentialsDTO credentialsDTO, final LogInCallback callback)
    {
        return new THCallback<UserProfileDTO>()
        {
            @Override public void success(UserProfileDTO userProfileDTO, THResponse response)
            {
                saveCredentialsToUserDefaults(credentialsDTO);

                UserLoginDTO userLoginDTO = new UserLoginDTO();
                userLoginDTO.profileDTO = userProfileDTO;
                callback.done(userLoginDTO, null);
            }

            @Override public void failure(THException error)
            {
                checkNeedForUpgrade(error);
                callback.done(null, error);
            }
        };
    }

    private static THCallback<UserLoginDTO> createCallbackForSignInAsyncWithJson(final CredentialsDTO credentialsDTO, final LogInCallback callback)
    {
        return new THCallback<UserLoginDTO>()
        {
            @Override public void success(UserLoginDTO userLoginDTO, THResponse response)
            {
                saveCredentialsToUserDefaults(credentialsDTO);
                callback.done(userLoginDTO, null);
            }

            @Override public void failure(THException error)
            {
                checkNeedForUpgrade(error);
                checkNeedToRenewSocialToken(error, credentialsDTO);
                callback.done(null, error);
            }
        };
    }

    private static void checkNeedForUpgrade(THException error)
    {
        if (error.getCode() == ExceptionCode.DoNotRunBelow)
        {
            final Activity currentActivity = currentActivityHolder.get().getCurrentActivity();
            alertDialogUtil.get().popWithOkCancelButton(
                    currentActivity,
                    R.string.upgrade_needed,
                    R.string.please_update,
                    R.string.update_now,
                    R.string.later,
                    new DialogInterface.OnClickListener()
                    {
                        @Override public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                THToast.show(R.string.update_guide);
                                currentActivity.startActivity(
                                        new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.PLAYSTORE_APP_ID)));
                                currentActivity.finish();
                            }
                            catch (ActivityNotFoundException ex)
                            {

                                currentActivity.startActivity(
                                        new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=" + Constants.PLAYSTORE_APP_ID)));
                                currentActivity.finish();
                            }
                        }
                    });
        }
    }

    private static void checkNeedToRenewSocialToken(THException error, CredentialsDTO credentialsDTO)
    {
        if (error.getCode() == ExceptionCode.RenewSocialToken)
        {
            mainCredentialsPreference.delete();
            final Activity currentActivity = currentActivityHolder.get().getCurrentActivity();

            if (currentActivity instanceof AuthenticationActivity)
            {
                if (credentialsDTO instanceof FacebookCredentialsDTO)
                {
                    ((AuthenticationActivity) currentActivity).authenticateWithFacebook();
                    return;
                }
                if (credentialsDTO instanceof LinkedinCredentialsDTO)
                {
                    ((AuthenticationActivity) currentActivity).authenticateWithLinkedIn();
                    return;
                }
                if (credentialsDTO instanceof QQCredentialsDTO)
                {
                    ((AuthenticationActivity) currentActivity).authenticateWithQQ();
                    return;
                }
                if (credentialsDTO instanceof TwitterCredentialsDTO)
                {
                    ((AuthenticationActivity) currentActivity).authenticateWithTwitter();
                    return;
                }
                if (credentialsDTO instanceof WeiboCredentialsDTO)
                {
                    ((AuthenticationActivity) currentActivity).authenticateWithWeibo();
                    return;
                }
            }

            alertDialogUtil.get().popWithOkCancelButton(currentActivity,
                    R.string.please_update_token_title,
                    R.string.please_update_token_description,
                    R.string.ok,
                    R.string.later,
                    new DialogInterface.OnClickListener()
                    {
                        @Override public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    });
        }
    }

    public static void registerAuthenticationProvider(THAuthenticationProvider provider)
    {
        authenticationProviders.put(provider.getAuthType(), provider);
    }

    /**
     * @param credentialsDTO json data is from social media
     */
    public static void saveCredentialsToUserDefaults(CredentialsDTO credentialsDTO)
    {
        Timber.d("%d authentication tokens loaded", typedCredentials.size());

        mainCredentialsPreference.setCredentials(credentialsDTO);
        mainCredentialsPreference.setCredentials(credentialsDTO);
        typedCredentials.put(credentialsDTO.getAuthType(), credentialsDTO);
        credentialsSetPreference.replaceOrAddCredentials(credentialsDTO);
    }

    public static void clearCurrentUser()
    {
        typedCredentials.clear();
        dtoCacheUtil.get().clearUserRelatedCaches();
        currentUserId.delete();
        VisitedFriendListPrefs.clearVisitedIdList();

        CredentialsDTO currentCredentials = mainCredentialsPreference.getCredentials();
        if (currentCredentials != null)
        {
            THAuthenticationProvider currentProvider = authenticationProviders.get(currentCredentials.getAuthType());
            if (currentProvider != null)
            {
                currentProvider.deauthenticate();
            }
        }

        // clear all preferences
        mainCredentialsPreference.delete();
        credentialsSetPreference.delete();
        SharedPreferences.Editor prefEditor = sharedPreferences.get().edit();
        prefEditor.clear();
        prefEditor.commit();
    }

    public static void setAuthenticationMode(AuthenticationMode authenticationMode)
    {
        THUser.authenticationMode = authenticationMode;
    }

    public static void removeCredential(String authenticationHeader)
    {
        if (typedCredentials == null)
        {
            Timber.d("saveCredentialsToUserDefaults: Credentials were null");
            return;
        }

        Timber.d("%d authentication tokens loaded", typedCredentials.size());

        typedCredentials.remove(authenticationHeader);
        credentialsSetPreference.delete();
    }
}

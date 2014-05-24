package com.tradehero.th.models.user.auth;

import android.text.TextUtils;
import android.util.Base64;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.th.api.form.UserFormFactory;
import java.text.ParseException;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;

public class CredentialsDTOFactory
{
    @Inject public CredentialsDTOFactory()
    {
        super();
    }

    public CredentialsDTO create(String savedToken) throws JSONException, ParseException
    {
        return create(new JSONObject(savedToken));
    }

    public CredentialsDTO create(JSONObject object) throws JSONException, ParseException
    {
        CredentialsDTO created;
        String type = object.getString(UserFormFactory.KEY_TYPE);
        switch(type)
        {
            case EmailCredentialsDTO.EMAIL_AUTH_TYPE:
                created = new EmailCredentialsDTO(object);
                break;

            case FacebookCredentialsDTO.FACEBOOK_AUTH_TYPE:
                created = new FacebookCredentialsDTO(object);
                break;

            case LinkedinCredentialsDTO.LINKEDIN_AUTH_TYPE:
                created = new LinkedinCredentialsDTO(object);
                break;

            case TwitterCredentialsDTO.TWITTER_AUTH_TYPE:
                created = new TwitterCredentialsDTO(object);
                break;

            // TODO WeChat

            case QQCredentialsDTO.QQ_AUTH_TYPE:
                created = new QQCredentialsDTO(object);
                break;

            case WeiboCredentialsDTO.WEIBO_AUTH_TYPE:
                created = new WeiboCredentialsDTO(object);
                break;

            default:
                throw new IllegalArgumentException("Unhandled type " + type);
        }
        return created;
    }

    @Deprecated
    public CredentialsDTO createFromOldSessionToken(StringPreference oldStringPref)
    {
        if (oldStringPref != null)
        {
            String authToken = oldStringPref.get();
            if (authToken != null && !TextUtils.isEmpty(authToken))
            {
                String[] elements = authToken.split(" ");
                if (elements.length == 2)
                {
                    if (elements[0].equals(EmailCredentialsDTO.EMAIL_AUTH_TYPE))
                    {
                        String decoded = new String(
                            Base64.decode(elements[0].getBytes(), Base64.NO_WRAP));
                        if (!TextUtils.isEmpty(decoded))
                        {
                            String[] emailPass = decoded.split(":");
                            if (emailPass.length == 2)
                            {
                                return new EmailCredentialsDTO(emailPass[0], emailPass[1]);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}

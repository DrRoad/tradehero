package com.androidth.general.models.share.preference;

import android.support.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseSocialSharePreferenceDTO implements SocialSharePreferenceDTO
{
    protected static final String KEY_IS_SHARE_ENABLED = BaseSocialSharePreferenceDTO.class.getName() + ".is_share_enabled";
    protected static final String KEY_SOCIAL_NETWORK_ENUM = BaseSocialSharePreferenceDTO.class.getName() + ".social_network_enum";

    private boolean isShareEnabled;

    //<editor-fold desc="Constructors">
    protected BaseSocialSharePreferenceDTO(boolean isShareEnabled)
    {
        this.isShareEnabled = isShareEnabled;
    }
    //</editor-fold>

    @Override public int hashCode()
    {
        return getSocialNetworkEnum().hashCode();
    }

    @Override public boolean equals(Object o)
    {
        return (o instanceof SocialSharePreferenceDTO) && (getSocialNetworkEnum().equals(((SocialSharePreferenceDTO) o).getSocialNetworkEnum()));
    }

    @Override @NonNull public JSONObject toJSONObject() throws JSONException
    {
        JSONObject o = new JSONObject();
        o.put(KEY_SOCIAL_NETWORK_ENUM, getSocialNetworkEnum());
        o.put(KEY_IS_SHARE_ENABLED, isShareEnabled());
        return o;
    }

    @Override public boolean isShareEnabled()
    {
        return isShareEnabled;
    }

    @Override public void setIsShareEnabled(boolean isShareEnabled)
    {
        this.isShareEnabled = isShareEnabled;
    }
}

package com.tradehero.th.models.share.preference;

import com.tradehero.th.api.social.SocialNetworkEnum;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class SocialSharePreferenceDTOFactory
{
    //<editor-fold desc="Constructors">
    @Inject public SocialSharePreferenceDTOFactory()
    {
        super();
    }
    //</editor-fold>

    @NotNull public SocialSharePreferenceDTO create(@NotNull String jsonString) throws JSONException
    {
        return create(new JSONObject(jsonString));
    }

    @NotNull public SocialSharePreferenceDTO create(@NotNull JSONObject jsonObject) throws JSONException
    {
        SocialNetworkEnum networkEnum = SocialNetworkEnum.valueOf(jsonObject.getString(BaseSocialSharePreferenceDTO.KEY_SOCIAL_NETWORK_ENUM));
        boolean isShareEnabled = jsonObject.getBoolean(BaseSocialSharePreferenceDTO.KEY_IS_SHARE_ENABLED);

        return create(networkEnum, isShareEnabled);
    }

    @NotNull public SocialSharePreferenceDTO create(SocialNetworkEnum socialNetworkEnum, boolean isShareEnabled)
    {
        SocialSharePreferenceDTO socialSharePreferenceDTO;

        switch (socialNetworkEnum)
        {
            case WB:
                socialSharePreferenceDTO = new WeiBoSharePreferenceDTO(isShareEnabled);
                break;
            case WECHAT:
                socialSharePreferenceDTO = new WeChatSharePreferenceDTO(isShareEnabled);
                break;
            default:
                throw new IllegalStateException("Unhandled type: " + socialNetworkEnum.getName());
        }

        return socialSharePreferenceDTO;
    }
}

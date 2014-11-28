package com.tradehero.th.api.users.signup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tradehero.th.api.misc.DeviceType;
import com.tradehero.th.api.users.LoginSignUpFormDTO;

public class LoginSignUpFormFacebookDTO extends LoginSignUpFormDTO
{
    @JsonProperty("facebook_access_token")
    public String accessToken;

    //<editor-fold desc="Constructors">
    public LoginSignUpFormFacebookDTO(String deviceToken, DeviceType deviceType, String clientVersion, String device_access_token)
    {
        super(deviceToken, deviceType, clientVersion, device_access_token);
    }
    //</editor-fold>
}
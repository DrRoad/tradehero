package com.tradehero.th.fragments.settings;

import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTOUtil;
import com.tradehero.th.auth.linkedin.LinkedInAuthenticationProvider;
import com.tradehero.th.network.service.SocialServiceWrapper;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.prefs.AuthHeader;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.utils.SocialAlertDialogRxUtil;
import dagger.Lazy;
import javax.inject.Inject;

public class SocialConnectLinkedInSettingViewHolder extends SocialConnectSettingViewHolder
{
    //<editor-fold desc="Constructors">
    @Inject public SocialConnectLinkedInSettingViewHolder(
            @NonNull CurrentUserId currentUserId,
            @NonNull UserProfileCacheRx userProfileCache,
            @NonNull ProgressDialogUtil progressDialogUtil,
            @NonNull UserServiceWrapper userServiceWrapper,
            @NonNull SocialAlertDialogRxUtil socialAlertDialogRxUtil,
            @NonNull SocialServiceWrapper socialServiceWrapper,
            @NonNull Lazy<LinkedInAuthenticationProvider> socialAuthenticationProvider,
            @NonNull UserProfileDTOUtil userProfileDTOUtil,
            @NonNull @AuthHeader String authToken)
    {
        super(currentUserId,
                userProfileCache,
                progressDialogUtil,
                userServiceWrapper,
                socialAlertDialogRxUtil,
                socialServiceWrapper,
                socialAuthenticationProvider,
                userProfileDTOUtil,
                authToken);
    }
    //</editor-fold>

    @StringRes @Override protected int getStringKeyResId()
    {
        return R.string.key_settings_sharing_linked_in;
    }

    @IntegerRes @Override protected int getOrderIntResId()
    {
        return R.integer.key_settings_sharing_linked_in_order;
    }

    @NonNull @Override protected SocialNetworkEnum getSocialNetworkEnum()
    {
        return SocialNetworkEnum.LN;
    }

    @StringRes @Override protected int getLinkingDialogMessage()
    {
        return R.string.authentication_connecting_to_linkedin;
    }

    @StringRes @Override protected int getUnlinkingProgressDialogMessage()
    {
        return R.string.authentication_connecting_tradehero_only;
    }
}

package com.tradehero.th.fragments.social.friend;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.tradehero.th.api.social.SocialNetworkEnum;

public class SocialTypeItem
{
    @NonNull public final SocialNetworkEnum socialNetwork;
    @DrawableRes public final int imageResource;
    @StringRes public final int titleResource;
    @DrawableRes public final int backgroundResource;

    //<editor-fold desc="Constructors">
    public SocialTypeItem(
            @DrawableRes int imageResource,
            @StringRes int titleResource,
            @DrawableRes int backgroundResource,
            @NonNull SocialNetworkEnum socialNetwork)
    {
        this.imageResource = imageResource;
        this.titleResource = titleResource;
        this.socialNetwork = socialNetwork;
        this.backgroundResource = backgroundResource;
    }
    //</editor-fold>
}

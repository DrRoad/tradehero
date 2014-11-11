package com.tradehero.th.models.share;

import android.support.annotation.NonNull;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;

public class LinkedInShareDestination extends BaseShareDestination
    implements ShareDestinationWithEnum
{
    @Override public int getNameResId()
    {
        return R.string.linkedin;
    }

    @Override public int getIdResId()
    {
        return R.integer.social_destination_id_linkedin;
    }

    @Override @NonNull public SocialNetworkEnum getSocialNetworkEnum()
    {
        return SocialNetworkEnum.LN;
    }
}

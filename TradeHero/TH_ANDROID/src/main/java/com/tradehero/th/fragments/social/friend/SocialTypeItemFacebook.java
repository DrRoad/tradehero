package com.tradehero.th.fragments.social.friend;

import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;

public class SocialTypeItemFacebook extends SocialTypeItem
{
    public SocialTypeItemFacebook()
    {
        super(R.drawable.icn_fb_white, R.string.invite_from_facebook, R.drawable.social_item_fb, SocialNetworkEnum.FB);
    }
}

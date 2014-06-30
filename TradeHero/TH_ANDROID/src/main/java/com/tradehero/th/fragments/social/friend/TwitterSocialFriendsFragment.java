package com.tradehero.th.fragments.social.friend;

import com.tradehero.thm.R;
import com.tradehero.th.api.social.SocialNetworkEnum;

public class TwitterSocialFriendsFragment extends SocialFriendsFragment
{
    @Override
    protected SocialNetworkEnum getSocialNetwork()
    {
        return SocialNetworkEnum.TW;
    }

    @Override
    protected String getTitle()
    {
        return getString(R.string.invite_social_friend, getString(R.string.twitter));
    }
}

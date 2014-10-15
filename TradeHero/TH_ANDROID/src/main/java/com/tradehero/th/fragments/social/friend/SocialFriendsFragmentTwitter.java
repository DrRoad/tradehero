package com.tradehero.th.fragments.social.friend;

import android.content.Context;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import javax.inject.Inject;

public class SocialFriendsFragmentTwitter extends SocialFriendsFragment
{
    @SuppressWarnings("UnusedDeclaration") @Inject Context doNotRemoveOrItFails;

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

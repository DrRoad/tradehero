package com.tradehero.th.api.social;

import javax.inject.Inject;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import timber.log.Timber;

public class UserFriendsDTOFactory
{
    public static final String FACEBOOK_SOCIAL_ID = "fb";
    public static final String LINKEDIN_SOCIAL_ID = "li";
    public static final String TWITTER_SOCIAL_ID = "tw";

    //<editor-fold desc="Constructors">
    @Inject public UserFriendsDTOFactory()
    {
        super();
    }
    //</editor-fold>

    @Nullable public UserFriendsDTO createFrom(@NonNull String socialId, @NonNull String socialUserId)
    {
        UserFriendsDTO created = null;
        switch (socialId)
        {
            case FACEBOOK_SOCIAL_ID:
                created = new UserFriendsFacebookDTO(socialUserId);
                break;

            case LINKEDIN_SOCIAL_ID:
                created = new UserFriendsLinkedinDTO(socialUserId);
                break;

            case TWITTER_SOCIAL_ID:
                created = new UserFriendsTwitterDTO(socialUserId);
                break;
        }
        if (created == null)
        {
            Timber.e(new IllegalArgumentException(), "Unhandled socialId:%s", socialId);
        }
        return created;
    }
}

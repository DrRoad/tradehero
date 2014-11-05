package com.tradehero.th.api.social;

import com.tradehero.th.R;
import android.support.annotation.NonNull;

public class UserFriendsWeiboDTO extends UserFriendsDTO
{
    public static final String WEIBO_ID = "wbId";

    @NonNull public String wbId;
    public String wbPicUrl;

    //<editor-fold desc="Constructors">
    public UserFriendsWeiboDTO()
    {
        super();
    }

    public UserFriendsWeiboDTO(@NonNull String wbId)
    {
        this.wbId = wbId;
    }
    //</editor-fold>

    @Override public int getNetworkLabelImage()
    {
        return R.drawable.icn_weibo_round;
    }

    @Override public String getProfilePictureURL()
    {
        return wbPicUrl;
    }

    @Override public InviteDTO createInvite()
    {
        return new InviteWeiboDTO(wbId);
    }

    @Override public int hashCode()
    {
        return super.hashCode() ^ wbId.hashCode();
    }

    @Override protected boolean equals(@NonNull UserFriendsDTO other)
    {
        return super.equals(other) &&
                other instanceof UserFriendsWeiboDTO &&
                wbId.equals(((UserFriendsWeiboDTO) other).wbId);
    }
}

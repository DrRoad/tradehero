package com.tradehero.th.api.users;

import com.tradehero.common.persistence.DTO;
import java.util.Date;

public class UserMessagingRelationshipDTO implements DTO
{
    public int freeSendsRemaining; // -1 signifies unlimited messages 

    // TODO: client to display these on search-lists
    public boolean isHero;
    public boolean isFollower;
    public boolean isFriend;
    public String friendDesc;

    public Date followerSince;
    public Date heroSince;

    public boolean isUnlimited()
    {
        return freeSendsRemaining == -1;
    }

    public boolean canSendPrivate()
    {
        return isUnlimited() || freeSendsRemaining > 0;
    }
}

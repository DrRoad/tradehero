package com.tradehero.th.api.timeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.th.api.discussion.AbstractDiscussionDTO;
import com.tradehero.th.api.security.SecurityMediaDTO;
import com.tradehero.th.api.users.UserProfileCompactDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Created with IntelliJ IDEA. User: tho Date: 9/3/13 Time: 12:57 PM Copyright (c) TradeHero */
public class TimelineItemDTOEnhanced extends AbstractDiscussionDTO
{
    public int type;
    public Date userViewedAtUtc;
    private List<SecurityMediaDTO> medias;
    public Integer pushTypeId;
    public boolean useSysIcon;
    public boolean renderSysStyle;
    public String imageUrl;
    public UserProfileDTO user;

    public TimelineItemDTOEnhanced()
    {
    }

    public List<SecurityMediaDTO> getMedias()
    {
        return Collections.unmodifiableList(medias);
    }

    public void setMedias(List<SecurityMediaDTO> medias)
    {
        this.medias = medias;
    }

    public SecurityMediaDTO getFlavorSecurityForDisplay()
    {
        SecurityMediaDTO securityMediaDTO = null;
        for (SecurityMediaDTO m: medias)
        {
            if (m.securityId != 0)
            {
                securityMediaDTO = m;
            }

            // we prefer the first security with photo
            if (securityMediaDTO != null && securityMediaDTO.url != null)
            {
                return securityMediaDTO;
            }
        }
        return securityMediaDTO;
    }

    @JsonIgnore
    public void setUser(UserProfileCompactDTO user)
    {
        put(UserProfileCompactDTO.TAG, user);
    }

    @JsonIgnore
    public UserProfileCompactDTO getUser()
    {
        return (UserProfileCompactDTO) get(UserProfileCompactDTO.TAG);
    }

    @Override public TimelineItemDTOKey getDiscussionKey()
    {
        return new TimelineItemDTOKey(id);
    }
}
package com.tradehero.th.api.discussion;

import com.tradehero.th.api.ExtendedDTO;
import com.tradehero.th.api.discussion.key.CommentKey;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.users.UserBaseDTO;

/**
 * Created by xavier on 3/7/14.
 */
public class DiscussionDTO extends AbstractDiscussionDTO
{
    public UserBaseDTO user;
    public int inReplyToId;
    public DiscussionType inReplyToType;

    public String url; // to post a link

    public Integer sentToCount;         // only applicable to Messages

    // BEGIN: duplicated from buy/sell
    public Boolean publishToFb;
    public Boolean publishToLi;
    public Boolean publishToTw;
    public String geo_alt;
    public String geo_lat;
    public String geo_long;
    public boolean isPublic;
    // END: duplicated from buy/sell

    //<editor-fold desc="Constructors">
    public DiscussionDTO()
    {
    }

    public <ExtendedDTOType extends ExtendedDTO> DiscussionDTO(ExtendedDTOType other, Class<? extends ExtendedDTO> myClass)
    {
        super(other, myClass);
    }
    //</editor-fold>

    @Override public DiscussionKey getDiscussionKey()
    {
        return new CommentKey(id);
    }
}

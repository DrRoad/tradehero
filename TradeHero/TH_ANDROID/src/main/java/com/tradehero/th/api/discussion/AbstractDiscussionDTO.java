package com.tradehero.th.api.discussion;

import com.tradehero.th.api.ExtendedDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.users.UserBaseKey;
import java.util.Date;

public abstract class AbstractDiscussionDTO extends ExtendedDTO
{
    public int id;
    public Date createdAtUtc;
    public int userId;
    public String text;
    public int upvoteCount;
    public int downvoteCount;
    public int voteDirection; //-1: down, 0: cancel, 1: up
    public int commentCount;
    public String langCode;

    //<editor-fold desc="Constructors">
    public AbstractDiscussionDTO()
    {
        super();
    }

    public <ExtendedDTOType extends ExtendedDTO> AbstractDiscussionDTO(ExtendedDTOType other, Class<? extends ExtendedDTO> myClass)
    {
        super(other, myClass);
    }
    //</editor-fold>

    public void populateVote(AbstractDiscussionDTO target)
    {
        target.upvoteCount = upvoteCount;
        target.downvoteCount = downvoteCount;
        target.voteDirection = voteDirection;
    }

    public UserBaseKey getSenderKey()
    {
        return new UserBaseKey(userId);
    }

    public abstract DiscussionKey getDiscussionKey();
}

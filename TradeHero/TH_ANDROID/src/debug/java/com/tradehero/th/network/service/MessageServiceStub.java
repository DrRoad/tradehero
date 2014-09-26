package com.tradehero.th.network.service;

import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.MessageHeaderDTO;
import com.tradehero.th.api.discussion.ReadablePaginatedMessageHeaderDTO;
import com.tradehero.th.api.discussion.form.MessageCreateFormDTO;
import com.tradehero.th.api.pagination.PaginationInfoDTO;
import com.tradehero.th.api.users.UserMessagingRelationshipDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class MessageServiceStub implements MessageService
{
    //<editor-fold desc="Constructors">
    @Inject public MessageServiceStub()
    {
        super();
    }
    //</editor-fold>

    @Override
    public ReadablePaginatedMessageHeaderDTO getMessageHeaders(Integer page, Integer perPage)
    {
        Timber.d("Returning stub messages");
        ReadablePaginatedMessageHeaderDTO paginatedDTO = new ReadablePaginatedMessageHeaderDTO();
        List<MessageHeaderDTO> messageDTOList = new ArrayList<>();
        Date date = new Date();
        for (int i = 0; i < perPage; i++)
        {
            messageDTOList.add(createMessageHeader(i, page, date));
        }

        paginatedDTO.setData(messageDTOList);

        PaginationInfoDTO paginationInfoDTO = new PaginationInfoDTO();
        paginatedDTO.setPagination(paginationInfoDTO);

        return paginatedDTO;
    }

    @Override public ReadablePaginatedMessageHeaderDTO getMessageHeaders(
            String discussionType,
            Integer senderId,
            Integer page,
            Integer perPage)
    {
        ReadablePaginatedMessageHeaderDTO paginatedDTO = new ReadablePaginatedMessageHeaderDTO();
        List<MessageHeaderDTO> data = new ArrayList<>();
        data.add(createMessageHeaderNeerajToOscarAguilar());
        paginatedDTO.setData(data);
        return paginatedDTO;
    }

    @Override public MessageHeaderDTO getMessageHeader(int commentId, Integer referencedUserId)
    {
        return createMessageHeader(commentId, referencedUserId, new Date());
    }

    @Override public MessageHeaderDTO getMessageThread(int correspondentId)
    {
        return null;
    }

    private MessageHeaderDTO createMessageHeaderNeerajToOscarAguilar()
    {
        MessageHeaderDTO messageHeader = new MessageHeaderDTO();
        messageHeader.id = 1192391;
        messageHeader.discussionType = DiscussionType.PRIVATE_MESSAGE;
        messageHeader.message = "doom";
        messageHeader.senderUserId = 239284;
        messageHeader.recipientUserId = 106711;
        //messageHeader.createdAtUtc = new Date("2014-04-11T12:33:50");
        return messageHeader;
    }

    private MessageHeaderDTO createMessageHeader(int commentId, Integer page, Date date)
    {
        return new MessageHeaderDTO("title-" + commentId + "-" + page, "subtitle-" + commentId, "text-" + commentId, date, true);
    }

    @Override public UserMessagingRelationshipDTO getMessagingRelationgshipStatus(int recipientUserId)
    {
        UserMessagingRelationshipDTO statusDTO = new UserMessagingRelationshipDTO();
        return statusDTO;
    }

    @Override public DiscussionDTO createMessage(MessageCreateFormDTO form)
    {
        throw new IllegalArgumentException("Implement it");
    }

    @Override public BaseResponseDTO deleteMessage(int commentId, int senderUserId,
            int recipientUserId)
    {
        throw new RuntimeException("Not implemented");
    }

    @Override public BaseResponseDTO readMessage(int commentId, int senderUserId,
            int recipientUserId)
    {
        throw new RuntimeException("Not implemented");
    }

    @Override public BaseResponseDTO readAllMessage()
    {
        throw new RuntimeException("Not implemented");
    }
}

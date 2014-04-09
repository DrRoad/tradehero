package com.tradehero.th.network.service;

import com.tradehero.th.api.PaginatedDTO;
import com.tradehero.th.api.PaginationInfoDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.MessageDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import timber.log.Timber;

/**
 * Created by xavier2 on 2014/4/9.
 */
@Singleton public class MessageServiceStub implements MessageService
{
    @Inject public MessageServiceStub()
    {
        super();
    }

    @Override public PaginatedDTO<MessageDTO> getMessages(int page, int perPage)
    {
        Timber.d("Returning stub messages");
        PaginatedDTO<MessageDTO> paginatedDTO = new PaginatedDTO<>();
        List<MessageDTO> messsageDTOList = new ArrayList<>();
        Date date = new Date();
        for (int i = 0; i < perPage; i++)
        {
            messsageDTOList.add(new MessageDTO("title-" + i + "-" + page, "subtitle-" + i, "text-" + i, date));
        }

        paginatedDTO.setData(messsageDTOList);

        PaginationInfoDTO paginationInfoDTO = new PaginationInfoDTO();
        paginatedDTO.setPagination(paginationInfoDTO);

        return paginatedDTO;
    }

    @Override public DiscussionDTO createMessage(MessageDTO form)
    {
        throw new IllegalArgumentException("Implement it");
    }
}

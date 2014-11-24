package com.tradehero.th.persistence.message;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.api.discussion.MessageHeaderDTO;
import com.tradehero.th.api.discussion.key.MessageHeaderId;
import com.tradehero.th.network.service.MessageServiceWrapper;
import com.tradehero.th.persistence.SingleCacheMaxSize;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class MessageHeaderCacheRx extends BaseFetchDTOCacheRx<MessageHeaderId, MessageHeaderDTO>
{
    @NonNull private final MessageServiceWrapper messageServiceWrapper;

    //<editor-fold desc="Constructors">
    @Inject public MessageHeaderCacheRx(
            @SingleCacheMaxSize IntPreference maxSize,
            @NonNull MessageServiceWrapper messageServiceWrapper,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(maxSize.get(), 5, 5, dtoCacheUtil);
        this.messageServiceWrapper = messageServiceWrapper;
    }
    //</editor-fold>

    @Override @NonNull protected Observable<MessageHeaderDTO> fetch(@NonNull MessageHeaderId key)
    {
        return messageServiceWrapper.getMessageHeaderRx(key);
    }

    public void onNext(@NonNull List<? extends MessageHeaderDTO> messageHeaderDTOs)
    {
        for (MessageHeaderDTO messageHeaderDTO : messageHeaderDTOs)
        {
            onNext(messageHeaderDTO.getDTOKey(), messageHeaderDTO);
        }
    }

    public void setUnread(@NonNull MessageHeaderId messageHeaderId, boolean unread)
    {
        MessageHeaderDTO messageHeaderDTO = getValue(messageHeaderId);
        if (messageHeaderDTO != null && messageHeaderDTO.unread != unread)
        {
            messageHeaderDTO.unread = unread;
            onNext(messageHeaderId, messageHeaderDTO);
        }
    }

    public void setUnreadAll(boolean unread)
    {
        for (MessageHeaderId id : snapshot().keySet())
        {
            setUnread(id, unread);
        }
    }
}

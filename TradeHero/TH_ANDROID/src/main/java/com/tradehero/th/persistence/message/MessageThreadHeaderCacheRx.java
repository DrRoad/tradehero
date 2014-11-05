package com.tradehero.th.persistence.message;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.api.discussion.MessageHeaderDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.network.service.MessageServiceWrapper;
import com.tradehero.th.persistence.SingleCacheMaxSize;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class MessageThreadHeaderCacheRx extends BaseFetchDTOCacheRx<UserBaseKey, MessageHeaderDTO>
{
    @NonNull private final MessageServiceWrapper messageServiceWrapper;
    @NonNull private final MessageHeaderCacheRx messageHeaderCache;

    //<editor-fold desc="Constructors">
    @Inject public MessageThreadHeaderCacheRx(
            @SingleCacheMaxSize IntPreference maxSize,
            @NonNull MessageServiceWrapper messageServiceWrapper,
            @NonNull MessageHeaderCacheRx messageHeaderCache,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(maxSize.get(), 5, 5, dtoCacheUtil);
        this.messageServiceWrapper = messageServiceWrapper;
        this.messageHeaderCache = messageHeaderCache;
    }
    //</editor-fold>

    @Override @NonNull protected Observable<MessageHeaderDTO> fetch(@NonNull UserBaseKey key)
    {
        return messageServiceWrapper.getMessageThreadRx(key);
    }

    @Override public void onNext(@NonNull UserBaseKey key, @NonNull MessageHeaderDTO value)
    {
        messageHeaderCache.onNext(value.getDTOKey(), value);
        super.onNext(key, value);
    }
}

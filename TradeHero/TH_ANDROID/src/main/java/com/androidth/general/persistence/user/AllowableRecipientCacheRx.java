package com.androidth.general.persistence.user;

import android.support.annotation.NonNull;
import com.androidth.general.common.persistence.BaseDTOCacheRx;
import com.androidth.general.common.persistence.DTOCacheUtilRx;
import com.androidth.general.common.persistence.UserCache;
import com.androidth.general.api.users.AllowableRecipientDTO;
import com.androidth.general.api.users.UserBaseKey;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton @UserCache
public class AllowableRecipientCacheRx extends BaseDTOCacheRx<UserBaseKey, AllowableRecipientDTO>
{
    public static final int DEFAULT_MAX_VALUE_SIZE = 300;

    @NonNull private final Lazy<UserMessagingRelationshipCacheRx> userMessagingRelationshipCache;

    //<editor-fold desc="Constructors">
    @Inject public AllowableRecipientCacheRx(
            @NonNull Lazy<UserMessagingRelationshipCacheRx> userMessagingRelationshipCache,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(DEFAULT_MAX_VALUE_SIZE, dtoCacheUtil);
        this.userMessagingRelationshipCache = userMessagingRelationshipCache;
    }
    //</editor-fold>

    @Override public void onNext(@NonNull UserBaseKey key, @NonNull AllowableRecipientDTO value)
    {
        userMessagingRelationshipCache.get().onNext(value.user.getBaseKey(), value.relationship);
        super.onNext(key, value);
    }

    public void onNext(@NonNull List<AllowableRecipientDTO> allowableRecipientDTOs)
    {
        for (AllowableRecipientDTO allowableRecipientDTO : allowableRecipientDTOs)
        {
            onNext(allowableRecipientDTO.user.getBaseKey(), allowableRecipientDTO);
        }
    }
}
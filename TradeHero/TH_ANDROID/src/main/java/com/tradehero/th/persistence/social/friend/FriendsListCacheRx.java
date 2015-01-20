package com.tradehero.th.persistence.social.friend;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.BaseFetchDTOCacheRx;
import com.tradehero.common.persistence.DTOCacheUtilRx;
import com.tradehero.common.persistence.UserCache;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.api.social.UserFriendsDTOList;
import com.tradehero.th.api.social.key.FriendsListKey;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.ListCacheMaxSize;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton @UserCache
public class FriendsListCacheRx extends BaseFetchDTOCacheRx<FriendsListKey, UserFriendsDTOList>
{
    @NonNull private final UserServiceWrapper userServiceWrapper;

    //<editor-fold desc="Constructors">
    @Inject public FriendsListCacheRx(
            @NonNull @ListCacheMaxSize IntPreference maxSize,
            @NonNull UserServiceWrapper userServiceWrapper,
            @NonNull DTOCacheUtilRx dtoCacheUtil)
    {
        super(maxSize.get(), dtoCacheUtil);
        this.userServiceWrapper = userServiceWrapper;
    }
    //</editor-fold>

    @Override @NonNull protected Observable<UserFriendsDTOList> fetch(@NonNull FriendsListKey key)
    {
        return userServiceWrapper.getFriendsRx(key);
    }
}

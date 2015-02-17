package com.tradehero.th.persistence.security;

import android.support.annotation.NonNull;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerId;
import com.tradehero.th.api.security.SecurityIntegerIdList;
import com.tradehero.th.network.service.SecurityServiceWrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.functions.Func1;

public class SecurityMultiFetchAssistant
{
    @NonNull private final SecurityIdCache securityIdCache;
    @NonNull private final SecurityCompactCacheRx securityCompactCache;
    @NonNull private final SecurityServiceWrapper securityServiceWrapper;

    //<editor-fold desc="Constructors">
    @Inject public SecurityMultiFetchAssistant(
            @NonNull SecurityIdCache securityIdCache,
            @NonNull SecurityCompactCacheRx securityCompactCache,
            @NonNull SecurityServiceWrapper securityServiceWrapper)
    {
        super();
        this.securityIdCache = securityIdCache;
        this.securityCompactCache = securityCompactCache;
        this.securityServiceWrapper = securityServiceWrapper;
    }
    //</editor-fold>

    @NonNull public Observable<Map<SecurityIntegerId, SecurityCompactDTO>> get(
            @NonNull List<SecurityIntegerId> keysToFetch)
    {
        final Map<SecurityIntegerId, SecurityCompactDTO> returned = new HashMap<>();
        SecurityIntegerIdList remainingKeys = new SecurityIntegerIdList(keysToFetch, null);
        SecurityId found;
        SecurityCompactDTO cached;
        for (SecurityIntegerId id : keysToFetch)
        {
            found = securityIdCache.getCachedValue(id);
            if (found != null)
            {
                cached = securityCompactCache.getCachedValue(found);
                if (cached != null)
                {
                    returned.put(id, cached);
                    remainingKeys.remove(id);
                }
            }
        }
        return securityServiceWrapper.getMultipleSecuritiesRx(remainingKeys)
                .map(new Func1<
                        Map<Integer, SecurityCompactDTO>,
                        Map<SecurityIntegerId, SecurityCompactDTO>>()
                {
                    @Override public Map<SecurityIntegerId, SecurityCompactDTO> call(Map<Integer, SecurityCompactDTO> fetchedMap)
                    {
                        for (Map.Entry<Integer, SecurityCompactDTO> entry : fetchedMap.entrySet())
                        {
                            returned.put(new SecurityIntegerId(entry.getKey()),
                                    entry.getValue());
                        }
                        return returned;
                    }
                });
    }
}

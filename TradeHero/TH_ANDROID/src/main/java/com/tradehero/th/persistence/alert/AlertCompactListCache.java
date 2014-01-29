package com.tradehero.th.persistence.alert;

import com.tradehero.common.persistence.StraightDTOCache;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.alert.AlertId;
import com.tradehero.th.api.alert.AlertIdList;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.network.service.AlertService;
import dagger.Lazy;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Created with IntelliJ IDEA. User: xavier Date: 10/3/13 Time: 5:04 PM To change this template use File | Settings | File Templates. */
@Singleton public class AlertCompactListCache extends StraightDTOCache<UserBaseKey, AlertIdList>
{
    public static final String TAG = AlertCompactListCache.class.getSimpleName();
    public static final int DEFAULT_MAX_SIZE = 50;

    @Inject protected Lazy<AlertService> alertService;
    @Inject protected Lazy<AlertCompactCache> alertCompactCache;

    //<editor-fold desc="Constructors">
    @Inject public AlertCompactListCache()
    {
        super(DEFAULT_MAX_SIZE);
    }
    //</editor-fold>

    @Override protected AlertIdList fetch(UserBaseKey key) throws Throwable
    {
        //THLog.d(TAG, "fetch " + key);
        return putInternal(key, alertService.get().getAlerts(key.key));
    }

    protected AlertIdList putInternal(UserBaseKey key, List<AlertCompactDTO> fleshedValues)
    {
        AlertIdList alertIds = null;
        if (fleshedValues != null)
        {
            alertIds = new AlertIdList();
            alertCompactCache.get().invalidateAll();

            for (AlertCompactDTO alertCompactDTO: fleshedValues)
            {
                AlertId alertId = alertCompactDTO.getAlertId(key.key);
                alertIds.add(alertId);
                alertCompactCache.get().put(alertId, alertCompactDTO);
            }
            put(key, alertIds);
        }
        return alertIds;
    }

    @Override public void invalidateAll()
    {
        super.invalidateAll();
    }
}

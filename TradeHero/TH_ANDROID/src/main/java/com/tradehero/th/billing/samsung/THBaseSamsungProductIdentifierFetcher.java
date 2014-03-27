package com.tradehero.th.billing.samsung;

import android.content.Context;
import com.tradehero.common.billing.samsung.BaseSamsungProductIdentifierFetcher;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.SamsungSKUList;
import com.tradehero.common.billing.samsung.SamsungSKUListKey;
import com.tradehero.common.billing.samsung.exception.SamsungException;
import com.tradehero.common.billing.samsung.exception.SamsungExceptionFactory;
import com.tradehero.th.utils.DaggerUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by xavier on 3/26/14.
 */
public class THBaseSamsungProductIdentifierFetcher
    extends BaseSamsungProductIdentifierFetcher<
            SamsungSKUListKey,
            SamsungSKU,
            SamsungSKUList,
            SamsungException>
    implements THSamsungProductIdentifierFetcher
{
    @Inject protected SamsungExceptionFactory samsungExceptionFactory;

    public THBaseSamsungProductIdentifierFetcher(Context context, int mode)
    {
        super(context, mode);
        DaggerUtils.inject(this);
    }

    @Override protected List<String> getKnownItemGroups()
    {
        List<String> knownGroupIds = new ArrayList<>();
        knownGroupIds.add(THSamsungConstants.IAP_ITEM_GROUP_ID);
        return knownGroupIds;
    }

    @Override protected SamsungSKUListKey createSamsungListKey(String itemType)
    {
        return new SamsungSKUListKey(itemType);
    }

    @Override protected SamsungSKU createSamsungSku(String groupId, String itemId)
    {
        return new SamsungSKU(groupId, itemId);
    }

    @Override protected SamsungException createException(int errorCode)
    {
        return samsungExceptionFactory.create(errorCode);
    }
}

package com.tradehero.th.billing.samsung;

import android.content.Context;
import com.sec.android.iap.lib.vo.ErrorVo;
import com.sec.android.iap.lib.vo.ItemVo;
import com.tradehero.common.billing.samsung.BaseSamsungInventoryFetcher;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.exception.SamsungException;
import com.tradehero.th.billing.samsung.exception.THSamsungExceptionFactory;
import com.tradehero.th.utils.DaggerUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class THBaseSamsungInventoryFetcher
    extends BaseSamsungInventoryFetcher<
        SamsungSKU,
        THSamsungProductDetail,
        SamsungException>
    implements THSamsungInventoryFetcher
{
    @Inject protected THSamsungExceptionFactory samsungExceptionFactory;

    public THBaseSamsungInventoryFetcher(Context context, int mode)
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

    @Override protected SamsungSKU createSamsungSku(String groupId, String itemId)
    {
        return new SamsungSKU(groupId, itemId);
    }

    @Override protected THSamsungProductDetail createSamsungProductDetail(SamsungSKU samsungSKU, ItemVo itemVo)
    {
        return new THSamsungProductDetail(samsungSKU, itemVo);
    }

    @Override protected SamsungException createException(ErrorVo errorVo)
    {
        return samsungExceptionFactory.create(errorVo);
    }
}

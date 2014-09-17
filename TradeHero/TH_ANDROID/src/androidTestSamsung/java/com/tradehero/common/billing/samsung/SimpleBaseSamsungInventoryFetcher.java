package com.tradehero.common.billing.samsung;

import android.content.Context;
import com.sec.android.iap.lib.vo.ErrorVo;
import com.sec.android.iap.lib.vo.ItemVo;
import com.tradehero.common.billing.samsung.exception.SamsungException;
import com.tradehero.common.billing.samsung.exception.SamsungExceptionFactory;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SimpleBaseSamsungInventoryFetcher
    extends BaseSamsungInventoryFetcher<
            SamsungSKU,
            BaseSamsungProductDetail<SamsungSKU>,
            SamsungException>
{
    public SimpleBaseSamsungInventoryFetcher(Context context, int mode)
    {
        super(context, mode);
    }

    @Override protected SamsungSKU createSamsungSku(String groupId, String itemId)
    {
        return new SamsungSKU(groupId, itemId);
    }

    @Override protected BaseSamsungProductDetail<SamsungSKU> createSamsungProductDetail(SamsungItemGroup samsungItemGroup, final ItemVo itemVo)
    {
        return new BaseSamsungProductDetail<SamsungSKU>(samsungItemGroup, itemVo)
        {
            @NotNull @Override public SamsungSKU getProductIdentifier()
            {
                return new SamsungSKU(samsungItemGroup.groupId, itemVo.getItemId());
            }
        };
    }

    @Override protected SamsungException createException(ErrorVo errorVo)
    {
        return new SamsungExceptionFactory().create(errorVo);
    }

    @Override protected List<String> getKnownItemGroups()
    {
        return Arrays.asList("100000104349");
    }
}

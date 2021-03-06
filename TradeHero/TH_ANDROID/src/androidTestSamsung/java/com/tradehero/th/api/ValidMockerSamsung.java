package com.tradehero.th.api;

import com.tradehero.th.api.billing.PurchaseReportDTO;
import com.tradehero.th.api.billing.SamsungPurchaseReportDTO;
import javax.inject.Inject;
import android.support.annotation.NonNull;

public class ValidMockerSamsung extends ValidMocker
{
    @Inject public ValidMockerSamsung()
    {
        super();
    }

    @Override public Object mockValidParameter(@NonNull Class<?> type)
    {
        if (type.equals(PurchaseReportDTO.class))
        {
            return new SamsungPurchaseReportDTO("paymentId", "purchaseId", "productCode");
        }
        return super.mockValidParameter(type);
    }
}

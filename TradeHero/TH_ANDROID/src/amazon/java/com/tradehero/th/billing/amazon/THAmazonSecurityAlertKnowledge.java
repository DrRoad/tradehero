package com.tradehero.th.billing.amazon;

import com.tradehero.common.billing.ProductIdentifier;
import com.tradehero.common.billing.amazon.AmazonSKU;
import com.tradehero.th.api.alert.AlertPlanDTO;
import com.tradehero.th.billing.SecurityAlertKnowledge;
import com.tradehero.th.billing.THBillingConstants;
import javax.inject.Inject;
import javax.inject.Singleton;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Singleton public class THAmazonSecurityAlertKnowledge extends SecurityAlertKnowledge
{
    //<editor-fold desc="Constructors">
    @Inject public THAmazonSecurityAlertKnowledge()
    {
    }
    //</editor-fold>

    @NonNull @Override public AmazonSKU createFrom(@NonNull AlertPlanDTO alertPlanDTO)
    {
        return new AmazonSKU(alertPlanDTO.productIdentifier);
    }

    @Override @Nullable public AmazonSKU getServerEquivalentSKU(@NonNull ProductIdentifier localSKU)
    {
        if (localSKU instanceof AmazonSKU)
        {
            switch (((AmazonSKU) localSKU).skuId)
            {
                case THBillingConstants.SERVER_ALERT_1:
                    return new AmazonSKU(THAmazonConstants.ALERT_1);

                case THBillingConstants.SERVER_ALERT_5:
                    return new AmazonSKU(THAmazonConstants.ALERT_5);

                case THBillingConstants.SERVER_ALERT_UNLIMITED:
                    return new AmazonSKU(THAmazonConstants.ALERT_UNLIMITED);
            }
        }

        return null;
    }
}

package com.tradehero.th.billing.googleplay;

import android.app.Activity;
import com.tradehero.common.billing.googleplay.SKUDetails;
import com.tradehero.th.adapters.billing.THSKUDetailsAdapter;
import java.util.List;

/** Created with IntelliJ IDEA. User: xavier Date: 11/11/13 Time: 10:49 AM To change this template use File | Settings | File Templates. */
public class IABAlertSKUUtils
{
    public static final String TAG = IABAlertSKUUtils.class.getSimpleName();

    public static void popBuyDialog(Activity activity,
                                SKUDomainInformer domainInformer,
                                IABAlertUtils.OnDialogSKUDetailsClickListener<THSKUDetails> clickListener,
                                String skuDomain, int
                                titleResId)
    {
        final THSKUDetailsAdapter detailsAdapter = new THSKUDetailsAdapter(activity, activity.getLayoutInflater(), skuDomain);
        List<THSKUDetails> desiredSkuDetails = domainInformer.getDetailsOfDomain(skuDomain);
        detailsAdapter.setItems(desiredSkuDetails);

        IABAlertUtils.popBuyDialog(activity, detailsAdapter, titleResId, clickListener);
    }
}

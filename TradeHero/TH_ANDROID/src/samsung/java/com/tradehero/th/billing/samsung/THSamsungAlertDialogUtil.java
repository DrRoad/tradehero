package com.tradehero.th.billing.samsung;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.tradehero.common.billing.ProductIdentifier;
import com.tradehero.common.billing.ProductPurchase;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.th.R;
import com.tradehero.th.billing.BillingAlertDialogUtil;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.samsung.persistence.THSamsungPurchaseCacheRx;
import com.tradehero.th.fragments.billing.THSamsungSKUDetailAdapter;
import com.tradehero.th.fragments.billing.THSamsungStoreProductDetailView;
import com.tradehero.th.utils.ActivityUtil;
import com.tradehero.th.utils.metrics.Analytics;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class THSamsungAlertDialogUtil extends BillingAlertDialogUtil<
        SamsungSKU,
        THSamsungProductDetail,
        THSamsungLogicHolder,
        THSamsungStoreProductDetailView,
        THSamsungSKUDetailAdapter>
{
    @NonNull protected final THSamsungPurchaseCacheRx thSamsungPurchaseCache;
    @NonNull protected final SamsungStoreUtils samsungStoreUtils;

    //<editor-fold desc="Constructors">
    @Inject public THSamsungAlertDialogUtil(
            @NonNull Analytics analytics,
            @NonNull ActivityUtil activityUtil,
            @NonNull THSamsungPurchaseCacheRx thSamsungPurchaseCache,
            @NonNull SamsungStoreUtils samsungStoreUtils)
    {
        super(analytics, activityUtil);
        this.thSamsungPurchaseCache = thSamsungPurchaseCache;
        this.samsungStoreUtils = samsungStoreUtils;
    }
    //</editor-fold>

    //<editor-fold desc="SKU related">
    @Override protected THSamsungSKUDetailAdapter createProductDetailAdapter(Activity activity,
            ProductIdentifierDomain skuDomain)
    {
        return new THSamsungSKUDetailAdapter(activity, skuDomain);
    }

    @Override public HashMap<ProductIdentifier, Boolean> getEnabledItems()
    {
        HashMap<ProductIdentifier, Boolean> enabledItems = new HashMap<>();

        for (THSamsungPurchase value : thSamsungPurchaseCache.getValues())
        {
            Timber.d("Disabling %s", value);
            enabledItems.put(value.getProductIdentifier(), false);
        }

        if (enabledItems.size() == 0)
        {
            enabledItems = null;
        }
        return enabledItems;
    }
    //</editor-fold>

    //<editor-fold desc="Restore Related">
    @Deprecated // TODO user list of exceptions
    public AlertDialog handlePurchaseRestoreFinished(final Context context, List<? extends ProductPurchase> restored, List<? extends ProductPurchase> restoreFailed, final DialogInterface.OnClickListener clickListener)
    {
        return handlePurchaseRestoreFinished(context, restored, restoreFailed, clickListener, false);
    }

    @Deprecated // TODO user list of exceptions
    public AlertDialog handlePurchaseRestoreFinished(final Context context, List<? extends ProductPurchase> restored, List<? extends ProductPurchase> restoreFailed, final DialogInterface.OnClickListener clickListener, boolean verbose)
    {
        int countOk = (restored == null ? 0 : restored.size());
        int countRestoreFailed = (restoreFailed == null ? 0 : restoreFailed.size());

        AlertDialog alertDialog = null;
        if (countRestoreFailed == 0 && countOk > 0)
        {
            alertDialog = popPurchasesRestored(context, countOk);
        }
        else if (countRestoreFailed > 0 && countOk == 0)
        {
            // TODO
            alertDialog = popSendEmailSupportRestoreFailed(context, countRestoreFailed, clickListener);
        }
        else if (countRestoreFailed > 0)
        {
            alertDialog = popSendEmailSupportRestorePartiallyFailed(context, clickListener, countOk, countRestoreFailed);
        }
        else if (verbose && countRestoreFailed == 0 && countOk == 0)
        {
            alertDialog = popNoPurchaseToRestore(context);
        }

        if (alertDialog != null)
        {
            alertDialog.setCanceledOnTouchOutside(true);
        }
        Timber.d("Restored purchases: %d, failed restore: %d", countOk, countRestoreFailed);
        return alertDialog;
    }

    public DialogInterface.OnClickListener createFailedRestoreClickListener(final Context context, final Exception exception)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override public void onClick(DialogInterface dialog, int which)
            {
                sendSupportEmailRestoreFailed(context, exception);
            }
        };
    }

    public void sendSupportEmailRestoreFailed(final Context context, Exception exception)
    {
        context.startActivity(Intent.createChooser(
                samsungStoreUtils.getSupportPurchaseRestoreEmailIntent(context, exception),
                context.getString(R.string.iap_send_support_email_chooser_title)));
    }
    //</editor-fold>

}

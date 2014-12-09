package com.tradehero.th.fragments.settings;

import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.support.v4.preference.PreferenceFragment;
import com.tradehero.common.billing.BillingPurchaseRestorer;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.billing.THBillingInteractor;
import com.tradehero.th.billing.THBillingInteractorRx;
import com.tradehero.th.billing.report.PurchaseReportResult;
import com.tradehero.th.billing.request.BaseTHUIBillingRequest;
import com.tradehero.th.billing.request.THUIBillingRequest;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.metrics.MarketSegment;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import rx.Observer;
import timber.log.Timber;

public class RestorePurchaseSettingViewHolder extends OneSettingViewHolder
{
    @NonNull protected final THBillingInteractor billingInteractor;
    @NonNull protected final THBillingInteractorRx billingInteractorRx;
    @NonNull protected final Provider<BaseTHUIBillingRequest.Builder> billingRequestBuilderProvider;

    private Integer restoreRequestCode;
    private BillingPurchaseRestorer.OnPurchaseRestorerListener purchaseRestorerFinishedListener;

    //<editor-fold desc="Constructors">
    @Inject public RestorePurchaseSettingViewHolder(
            @NonNull THBillingInteractor billingInteractor,
            @NonNull THBillingInteractorRx billingInteractorRx,
            @NonNull Provider<BaseTHUIBillingRequest.Builder> billingRequestBuilderProvider)
    {
        this.billingInteractor = billingInteractor;
        this.billingInteractorRx = billingInteractorRx;
        this.billingRequestBuilderProvider = billingRequestBuilderProvider;
    }
    //</editor-fold>

    @Override public void initViews(@NonNull DashboardPreferenceFragment preferenceFragment)
    {
        super.initViews(preferenceFragment);
        purchaseRestorerFinishedListener = new BillingPurchaseRestorer.OnPurchaseRestorerListener()
        {
            @Override public void onPurchaseRestored(
                    int requestCode,
                    List restoredPurchases,
                    List failedRestorePurchases,
                    List failExceptions)
            {
                Timber.d("%d, %d, %d", restoredPurchases.size(), failedRestorePurchases.size(), failExceptions.size());
                if (Integer.valueOf(requestCode).equals(restoreRequestCode))
                {
                    restoreRequestCode = null;
                }
            }
        };
        PreferenceCategory container = (PreferenceCategory) preferenceFragment.findPreference(preferenceFragment.getString(R.string.key_settings_account_group));
        if (Constants.TAP_STREAM_TYPE.marketSegment.equals(MarketSegment.CHINA))
            // TODO this is not so good. It should depend on Billing Module
        {
            if (container != null && clickablePref != null)
            {
                container.removePreference(clickablePref);
            }
        }
    }

    @Override public void destroyViews()
    {
        purchaseRestorerFinishedListener = null;
        super.destroyViews();
    }

    @Override protected int getStringKeyResId()
    {
        return R.string.key_settings_primary_restore_purchases;
    }

    @Override protected void handlePrefClicked()
    {
        PreferenceFragment preferenceFragmentCopy = preferenceFragment;
        if (preferenceFragmentCopy != null)
        {
            if (restoreRequestCode != null)
            {
                billingInteractor.forgetRequestCode(restoreRequestCode);
            }
            //noinspection unchecked
            //restoreRequestCode = billingInteractor.run(createRestoreRequest());
            //noinspection unchecked
            billingInteractorRx.restorePurchasesAndClear()
                    .subscribe(new Observer<PurchaseReportResult>()
                    {
                        @Override public void onNext(PurchaseReportResult o)
                        {
                            THToast.show("restored " + o.reportedPurchase.getProductIdentifier());
                        }

                        @Override public void onCompleted()
                        {
                            THToast.show("restore completed");
                        }

                        @Override public void onError(Throwable e)
                        {
                            THToast.show("restore error " + e);
                        }
                    });
        }
    }

    protected THUIBillingRequest createRestoreRequest()
    {
        BaseTHUIBillingRequest.Builder requestBuilder = billingRequestBuilderProvider.get();
        requestBuilder.restorePurchase(true);
        requestBuilder.startWithProgressDialog(true);
        requestBuilder.popRestorePurchaseOutcome(true);
        requestBuilder.popRestorePurchaseOutcomeVerbose(true);
        //noinspection unchecked
        requestBuilder.purchaseRestorerListener(purchaseRestorerFinishedListener);
        return requestBuilder.build();
    }
}

package com.tradehero.common.billing.googleplay;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import com.android.vending.billing.IInAppBillingService;
import com.tradehero.common.billing.BaseRequestCodeReplayActor;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import com.tradehero.common.rx.ServiceConnectionOperator;
import com.tradehero.th.BuildConfig;
import rx.Observable;
import timber.log.Timber;

public class BaseIABServiceCaller<ResultType> extends BaseRequestCodeReplayActor<ResultType>
{
    public final static String INTENT_VENDING_PACKAGE = "com.android.vending";
    public final static String INTENT_VENDING_SERVICE_BIND = "com.android.vending.billing.InAppBillingService.BIND";
    public final static int TARGET_BILLING_API_VERSION3 = 3;

    @NonNull protected final Context context;
    @NonNull protected final IABExceptionFactory iabExceptionFactory;
    @NonNull protected final Intent serviceIntent ;
    protected final int bindType;

    //<editor-fold desc="Constructors">
    public BaseIABServiceCaller(
            int requestCode,
            @NonNull Context context,
            @NonNull IABExceptionFactory iabExceptionFactory)
    {
        super(requestCode);
        this.context = context;
        this.iabExceptionFactory = iabExceptionFactory;
        this.serviceIntent = getBillingBindIntent();
        this.bindType = Context.BIND_AUTO_CREATE;
    }
    //</editor-fold>

    @NonNull private static Intent getBillingBindIntent()
    {
        Intent serviceIntent = new Intent(INTENT_VENDING_SERVICE_BIND);
        serviceIntent.setPackage(INTENT_VENDING_PACKAGE);
        return serviceIntent;
    }

    @NonNull protected Observable<IABServiceResult> getBillingServiceResult()
    {
        return Observable.create(new ServiceConnectionOperator(context, serviceIntent, bindType))
                .flatMap(this::createResult);
    }

    @NonNull protected Observable<IABServiceResult> createResult(@NonNull IBinder binder)
    {
        IInAppBillingService billingService = IInAppBillingService.Stub.asInterface(binder);
        if (billingService == null)
        {
            return Observable.error(new NullPointerException("Binder returned null for asInterface"));
        }
        try
        {
            return Observable.just(new IABServiceResult(
                    billingService,
                    checkSubscriptionSupported(billingService)));
        } catch (RemoteException e)
        {
            return Observable.error(
                    new IABException(IABConstants.IABHELPER_REMOTE_EXCEPTION, "RemoteException while setting up in-app billing."));
        } catch (IABException e)
        {
            return Observable.error(e);
        }
    }

    protected boolean checkSubscriptionSupported(@NonNull IInAppBillingService billingService) throws RemoteException, IABException
    {
        Timber.d("Checking for in-app billing 3 support.");

        // check for in-app billing v3 support
        int responseStatus = purchaseTypeSupportStatus(billingService, IABConstants.ITEM_TYPE_INAPP);
        if (responseStatus != IABConstants.BILLING_RESPONSE_RESULT_OK)
        {
            // if in-app purchase aren't supported, neither are subscriptions.
            throw iabExceptionFactory.create(responseStatus, "Error checking for billing v3 support.");
        }
        Timber.d("In-app billing version 3 supported for " + BuildConfig.GOOGLE_PLAY_PACKAGE_NAME);

        // check for v3 subscriptions support
        return purchaseTypeSupportStatus(billingService, IABConstants.ITEM_TYPE_SUBS)
                == IABConstants.BILLING_RESPONSE_RESULT_OK;
    }

    /**
     * @param itemType is IABConstants.ITEM_TYPE_INAPP or IABConstants.ITEM_TYPE_SUBS
     * @throws android.os.RemoteException
     */
    protected int purchaseTypeSupportStatus(@NonNull IInAppBillingService billingService, @NonNull String itemType) throws RemoteException
    {
        return billingService.isBillingSupported(
                TARGET_BILLING_API_VERSION3,
                BuildConfig.GOOGLE_PLAY_PACKAGE_NAME,
                itemType);
    }
}

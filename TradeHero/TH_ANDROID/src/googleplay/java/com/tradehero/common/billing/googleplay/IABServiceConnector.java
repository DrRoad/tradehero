package com.tradehero.common.billing.googleplay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.android.vending.billing.IInAppBillingService;
import com.tradehero.common.billing.RequestCodeActor;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class IABServiceConnector implements ServiceConnection, IABServiceListenerHolder, RequestCodeActor
{
    public final static String INTENT_VENDING_PACKAGE = "com.android.vending";
    public final static String INTENT_VENDING_SERVICE_BIND = "com.android.vending.billing.InAppBillingService.BIND";
    public final static int TARGET_BILLING_API_VERSION3 = 3;

    @NonNull protected final Context context;
    @NonNull protected final Lazy<IABExceptionFactory> iabExceptionFactory;

    @Nullable protected IInAppBillingService billingService;

    protected final int requestCode;
    private boolean subscriptionSupported;
    private boolean setupDone = false;
    boolean disposed = false;

    @Nullable protected ConnectorListener listener;

    //<editor-fold desc="Constructors">
    @Inject public IABServiceConnector(
            int request,
            @NonNull Context context,
            @NonNull Lazy<IABExceptionFactory> iabExceptionFactory)
    {
        this.requestCode = request;
        this.context = context;
        this.iabExceptionFactory = iabExceptionFactory;
    }
    //</editor-fold>

    @Override public int getRequestCode()
    {
        return requestCode;
    }

    public void startConnectionSetup()
    {
        checkNotDisposed();
        checkNotSetup();

        Timber.d("Starting in-app billing setup for this %s", ((Object) this).getClass().getSimpleName());

        bindBillingServiceIfAvailable();
    }

    protected void bindBillingServiceIfAvailable()
    {
        Intent serviceIntent = getBillingBindIntent();

        if (isServiceAvailable(serviceIntent))
        {
            // service available to handle that Intent
            ComponentName myService = context.startService(serviceIntent);
            context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }
        else
        {
            // no service available to handle that Intent
            handleSetupFailedInternal(
                    new IABException(IABConstants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE, "Billing service unavailable on device."));
        }
    }

    public Intent getBillingBindIntent()
    {
        Intent serviceIntent = new Intent(INTENT_VENDING_SERVICE_BIND);
        serviceIntent.setPackage(INTENT_VENDING_PACKAGE);
        return serviceIntent;
    }

    protected boolean isServiceAvailable(Intent serviceIntent)
    {
        List<ResolveInfo> intentService = context.getPackageManager().queryIntentServices(serviceIntent, 0);
        return intentService != null && !intentService.isEmpty();
    }

    /**
     * Dispose of object, releasing resources. It's very important to call this method when you are done with this object. It will release any
     * resources used by it such as service connections. Naturally, once the object is disposed of, it can't be used again.
     */
    @Override public void onDestroy()
    {
        Timber.d("Disposing this %s", ((Object) this).getClass().getSimpleName());
        setupDone = false;
        try
        {
            context.unbindService(this);
        }
        catch (IllegalArgumentException e)
        {
            Timber.d("It happened that we had not bound the service yet. Not to worry");
        }
        disposed = true;
        billingService = null;
        listener = null;
    }

    //<editor-fold desc="Service Connection">
    @Override public void onServiceDisconnected(ComponentName name)
    {
        Timber.d("Billing service disconnected.");
        billingService = null;
    }

    @Override public void onServiceConnected(ComponentName name, IBinder binderService)
    {
        Timber.d("Billing service connected.");
        billingService = IInAppBillingService.Stub.asInterface(binderService);
        try
        {
            checkInAppBillingV3Support();
            handleSetupFinishedInternal(new IABResponse(IABConstants.BILLING_RESPONSE_RESULT_OK, "Setup successful."));
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            Timber.e("RemoteException while setting up in-app billing.", e);
            handleSetupFailedInternal(
                    new IABException(IABConstants.IABHELPER_REMOTE_EXCEPTION, "RemoteException while setting up in-app billing."));
        }
        catch (IABException e)
        {
            e.printStackTrace();
            Timber.e("IABException while setting up in-app billing.", e);
            handleSetupFailedInternal(e);
        }
    }
    //</editor-fold>

    protected void checkInAppBillingV3Support() throws RemoteException, IABException
    {
        Timber.d("Checking for in-app billing 3 support.");

        // check for in-app billing v3 support
        int responseStatus = purchaseTypeSupportStatus(IABConstants.ITEM_TYPE_INAPP);
        if (responseStatus != IABConstants.BILLING_RESPONSE_RESULT_OK)
        {
            // if in-app purchase aren't supported, neither are subscriptions.
            subscriptionSupported = false;
            throw iabExceptionFactory.get().create(responseStatus, "Error checking for billing v3 support.");
        }
        Timber.d("In-app billing version 3 supported for " + context.getPackageName());

        // check for v3 subscriptions support
        responseStatus = purchaseTypeSupportStatus(IABConstants.ITEM_TYPE_SUBS);
        if (responseStatus == IABConstants.BILLING_RESPONSE_RESULT_OK)
        {
            Timber.d("Subscriptions AVAILABLE.");
            subscriptionSupported = true;
        }
        else
        {
            // We can proceed if subscriptions are not available
            Timber.d("Subscriptions NOT AVAILABLE. Response: " + responseStatus);
        }

        setupDone = true;
    }

    /**
     *
     * @param itemType is IABConstants.ITEM_TYPE_INAPP or IABConstants.ITEM_TYPE_SUBS
     * @return
     * @throws android.os.RemoteException
     */
    protected int purchaseTypeSupportStatus(String itemType) throws RemoteException
    {
        return billingService.isBillingSupported(TARGET_BILLING_API_VERSION3,
                context.getPackageName(), itemType);
    }

    protected void checkNotDisposed()
    {
        if (disposed)
        {
            throw new IllegalStateException("IabServiceConnector was disposed of, so it cannot be used.");
        }
    }

    private void checkNotSetup()
    {
        if (setupDone)
        {
            throw new IllegalStateException("IAB helper is already set up.");
        }
    }

    // Checks that setup was done; if not, throws an exception.
    protected void checkSetupDone(String operation)
    {
        if (!setupDone)
        {
            throw new IllegalStateException("IAB helper is not set up. Can't perform operation: " + operation);
        }
    }

    //<editor-fold desc="Accessors">
    public boolean areSubscriptionsSupported()
    {
        return this.subscriptionSupported;
    }

    public boolean isSetupDone()
    {
        return setupDone;
    }

    @Nullable public ConnectorListener getListener()
    {
        return listener;
    }

    @Override public void setListener(@Nullable ConnectorListener listener)
    {
        this.listener = listener;
    }
    //</editor-fold>

    private void handleSetupFinishedInternal(IABResponse response)
    {
        if (!disposed)
        {
            handleSetupFinished(response);
            notifyListenerSetupFinished(response);
        }
    }

    private void handleSetupFailedInternal(IABException exception)
    {
        if (!disposed)
        {
            handleSetupFailed(exception);
            notifyListenerSetupFailed(exception);
        }
        onDestroy();
    }

    protected void handleSetupFinished(IABResponse response)
    {
        // Just for children classes
    }

    protected void handleSetupFailed(IABException exception)
    {
        // Just for children classes
    }

    protected void notifyListenerSetupFinished(IABResponse response)
    {
        ConnectorListener listenerCopy = getListener();
        if (listenerCopy != null)
        {
            listenerCopy.onSetupFinished(this, response);
        }
    }

    protected void notifyListenerSetupFailed(IABException exception)
    {
        ConnectorListener listenerCopy = getListener();
        if (listenerCopy != null)
        {
            listenerCopy.onSetupFailed(this, exception);
        }
    }

    public static interface ConnectorListener
    {
        void onSetupFinished(IABServiceConnector connector, IABResponse response);
        void onSetupFailed(IABServiceConnector connector, IABException exception);
    }
}

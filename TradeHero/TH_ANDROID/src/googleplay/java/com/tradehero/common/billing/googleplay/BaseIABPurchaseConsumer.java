package com.tradehero.common.billing.googleplay;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import com.tradehero.common.billing.googleplay.exception.IABMissingTokenException;
import com.tradehero.common.billing.googleplay.exception.IABRemoteException;
import com.tradehero.common.persistence.billing.googleplay.IABPurchaseCache;
import dagger.Lazy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import timber.log.Timber;

abstract public class BaseIABPurchaseConsumer<
            IABSKUType extends IABSKU,
            IABOrderIdType extends IABOrderId,
            IABPurchaseType extends IABPurchase<IABSKUType, IABOrderIdType>>
        extends IABServiceConnector
    implements IABPurchaseConsumer<
        IABSKUType,
        IABOrderIdType,
        IABPurchaseType,
        IABException>
{
    private int requestCode;
    private boolean consuming = false;
    protected IABPurchaseType purchase;
    @Nullable private OnIABConsumptionFinishedListener<IABSKUType, IABOrderIdType, IABPurchaseType, IABException> consumptionFinishedListener;

    //<editor-fold desc="Constructors">
    public BaseIABPurchaseConsumer(
            @NonNull Context context,
            @NonNull Lazy<IABExceptionFactory> iabExceptionFactory)
    {
        super(context, iabExceptionFactory);
    }
    //</editor-fold>

    @Override public int getRequestCode()
    {
        return requestCode;
    }

    @Override public void onDestroy()
    {
        consumptionFinishedListener = null;
        super.onDestroy();
    }

    @NonNull abstract protected IABPurchaseCache<IABSKUType, IABOrderIdType, IABPurchaseType> getPurchaseCache();

    public boolean isConsuming()
    {
        return consuming;
    }

    private void checkNotConsuming()
    {
        if (consuming)
        {
            throw new IllegalStateException("BaseIABPurchaseConsumer is already consuming so it cannot be launched again");
        }
    }

    @Override @Nullable public OnIABConsumptionFinishedListener<IABSKUType, IABOrderIdType, IABPurchaseType, IABException> getConsumptionFinishedListener()
    {
        return consumptionFinishedListener;
    }

    @Override public void setConsumptionFinishedListener(@Nullable OnIABConsumptionFinishedListener<IABSKUType, IABOrderIdType, IABPurchaseType, IABException> consumptionFinishedListener)
    {
        this.consumptionFinishedListener = consumptionFinishedListener;
    }

    @Override public void consume(int requestCode, IABPurchaseType purchase)
    {
        checkNotConsuming();
        this.requestCode = requestCode;

        if (purchase == null)
        {
            throw new IllegalArgumentException("Purchase cannot be null");
        }
        if (purchase.getProductIdentifier() == null)
        {
            throw new IllegalArgumentException("Product Identifier cannot be null");
        }
        if (purchase.getProductIdentifier().identifier == null)
        {
            throw new IllegalArgumentException("Product Identifier's identifier cannot be null");
        }

        if (purchase.getType().equals(IABConstants.ITEM_TYPE_SUBS))
        {
            handleConsumeSkippedInternal(purchase);
        }
        else if (purchase.getToken() == null)
        {
            handleConsumeFailedInternal(new IABMissingTokenException("Token cannot be null"));
        }
        else
        {
            this.purchase = purchase;
            consuming = true;
            startConnectionSetup();
        }
    }

    @Override protected void handleSetupFinished(IABResponse response)
    {
        super.handleSetupFinished(response);
        consumeEffectivelyAsync();
    }

    @Override protected void handleSetupFailed(IABException exception)
    {
        super.handleSetupFailed(exception);
        handleConsumeFailedInternal(exception);
    }

    private void handleConsumeFailedInternal(IABException exception)
    {
        consuming = false;
        handleConsumeFailed(exception);
        notifyListenerConsumeFailed(exception);
    }

    private void handleConsumeFailed(IABException exception)
    {
        // Just for children classes
    }

    private void notifyListenerConsumeFailed(IABException exception)
    {
        OnIABConsumptionFinishedListener<IABSKUType, IABOrderIdType, IABPurchaseType, IABException> listener = getConsumptionFinishedListener();
        if (listener != null)
        {
            listener.onPurchaseConsumeFailed(requestCode, purchase, exception);
        }
    }

    private void handleConsumeFinishedInternal(IABPurchaseType purchase)
    {
        consuming = false;
        getPurchaseCache().invalidate(purchase.getOrderId());
        handleConsumeFinished(purchase);
        notifyListenerConsumeFinished(purchase);
    }

    private void handleConsumeFinished(IABPurchaseType purchase)
    {
        // Just for children classes
    }

    private void handleConsumeSkippedInternal(IABPurchaseType purchase)
    {
        consuming = false;
        handleConsumeSkipped(purchase);
        notifyListenerConsumeFinished(purchase);
    }

    private void handleConsumeSkipped(IABPurchaseType purchase)
    {
        // Just for children classes
    }

    private void notifyListenerConsumeFinished(IABPurchaseType purchase)
    {
        OnIABConsumptionFinishedListener<IABSKUType, IABOrderIdType, IABPurchaseType, IABException> listener = getConsumptionFinishedListener();
        if (listener != null)
        {
            listener.onPurchaseConsumed(requestCode, purchase);
        }
    }

    private void consumeEffectivelyAsync()
    {
        AsyncTask<Void, Void, Void> consumeTask = new AsyncTask<Void, Void, Void>()
        {
            private IABException exception;

            @Override protected Void doInBackground(Void... params)
            {
                if (!disposed)
                {
                    try
                    {
                        consumeEffectively();
                    }
                    catch (RemoteException e)
                    {
                        Timber.e("Remote Exception while fetching inventory.", e);
                        exception = new IABRemoteException("RemoteException while fetching IAB", e);
                    }
                    catch (IABException e)
                    {
                        Timber.e("IAB error.", e);
                        exception = e;
                    }
                }

                return null;
            }

            @Override protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                if (disposed)
                {
                    // Do nothing
                }
                else if (exception != null)
                {
                    handleConsumeFailedInternal(exception);
                }
                else
                {
                    handleConsumeFinishedInternal(purchase);
                }
            }
        };
        consumeTask.execute();
    }

    private void consumeEffectively() throws RemoteException, IABException
    {
        String sku = this.purchase.getProductIdentifier().identifier;
        String token = this.purchase.getToken();
        Timber.d("Consuming sku: %s, token: %s", sku, token);
        int response = this.billingService.consumePurchase(3, context.getPackageName(), token);
        if (response != IABConstants.BILLING_RESPONSE_RESULT_OK)
        {
            throw iabExceptionFactory.get().create(response);
        }
        Timber.d("Consumed successfully sku: %s", sku);
    }
}

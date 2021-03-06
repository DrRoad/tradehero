package com.tradehero.common.billing;

import com.tradehero.common.billing.exception.BillingException;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

abstract public class BaseBillingAvailableTesterHolder<BillingExceptionType extends BillingException>
    implements BillingAvailableTesterHolder<BillingExceptionType>
{
    @NonNull protected final Map<Integer /*requestCode*/, BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType>> parentBillingAvailableListener;

    //<editor-fold desc="Constructors">
    public BaseBillingAvailableTesterHolder()
    {
        super();
        parentBillingAvailableListener = new HashMap<>();
    }
    //</editor-fold>

    @Override public boolean isUnusedRequestCode(int requestCode)
    {
        return !parentBillingAvailableListener.containsKey(requestCode);
    }

    @Override public void forgetRequestCode(int requestCode)
    {
        parentBillingAvailableListener.remove(requestCode);
    }

    /**
     * @param billingAvailableListener
     * @return
     */
    @Override public void registerBillingAvailableListener(int requestCode, @Nullable BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType> billingAvailableListener)
    {
        parentBillingAvailableListener.put(requestCode, billingAvailableListener);
    }

    @Override @Nullable public BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType> getBillingAvailableListener(int requestCode)
    {
        return parentBillingAvailableListener.get(requestCode);
    }

    protected BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType> createBillingAvailableListener()
    {
        return new BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType>()
        {
            @Override public void onBillingAvailable(int requestCode)
            {
                notifyBillingAvailable(requestCode);
            }

            @Override public void onBillingNotAvailable(int requestCode, BillingExceptionType billingException)
            {
                notifyBillingNotAvailable(requestCode, billingException);
            }
        };
    }

    protected void notifyBillingAvailable(int requestCode)
    {
        BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType> handler = getBillingAvailableListener(requestCode);
        if (handler != null)
        {
            handler.onBillingAvailable(requestCode);
        }
        else
        {
            Timber.d("notifyBillingAvailable No OnBillingAvailableListenerListener for %d", requestCode);
        }
    }

    protected void notifyBillingNotAvailable(int requestCode, BillingExceptionType exception)
    {
        Timber.e("notifyBillingNotAvailable There was an exception", exception);
        BillingAvailableTester.OnBillingAvailableListener<BillingExceptionType> handler = getBillingAvailableListener(requestCode);
        if (handler != null)
        {
            handler.onBillingNotAvailable(requestCode, exception);
        }
        else
        {
            Timber.d("notifyBillingNotAvailable No OnBillingAvailableListenerListener for %d", requestCode);
        }
    }

    @Override public void onDestroy()
    {
        parentBillingAvailableListener.clear();
    }
}

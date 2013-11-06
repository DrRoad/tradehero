package com.tradehero.common.billing.googleplay;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import com.tradehero.common.billing.googleplay.exceptions.IABBadResponseException;
import com.tradehero.common.billing.googleplay.exceptions.IABException;
import com.tradehero.common.billing.googleplay.exceptions.IABExceptionFactory;
import com.tradehero.common.billing.googleplay.exceptions.IABRemoteException;
import com.tradehero.common.utils.THLog;
import com.tradehero.th.utils.DaggerUtils;
import dagger.Lazy;
import javax.inject.Inject;
import org.json.JSONException;

import java.util.*;

/**
 * Created by julien on 4/11/13
 */
public class InventoryFetcher extends IABServiceConnector
{
    public static final String TAG = InventoryFetcher.class.getSimpleName();

    protected HashMap<SKU, SKUDetails> inventory;
    private List<SKU> skus;

    private InventoryListener inventoryListener;
    @Inject protected Lazy<IABExceptionFactory> iabExceptionFactory;

    public InventoryFetcher(Context ctx, List<SKU> skus)
    {
        super(ctx);
        this.skus = skus;
        this.inventory = new HashMap<>(skus != null ? skus.size() : 10);
        DaggerUtils.inject(this);
    }

    public void fetchInventory()
    {
        this.startConnectionSetup();
    }

    @Override protected void handleSetupFailed(IABException exception)
    {
        dispose();
        handleInventoryFetchFailure(exception);
    }

    @Override protected void handleSetupFinished(IABResponse response)
    {
        fetchInventoryAsync();
    }

    private void fetchInventoryAsync()
    {
        AsyncTask<Void, Void, HashMap<SKU, SKUDetails>> backgroundTask =  new AsyncTask<Void, Void, HashMap<SKU, SKUDetails>>() {
            private IABException exception;

            @Override protected HashMap<SKU, SKUDetails> doInBackground(Void... params)
            {
                try
                {
                    return internalFetchCompleteInventory();
                }
                catch (RemoteException e)
                {
                    THLog.e(TAG, "Remote Exception while fetching inventory.", e);
                    exception = new IABRemoteException("RemoteException while fetching IAB", e);
                }
                catch (JSONException e)
                {
                    THLog.e(TAG, "Error parsing json.", e);
                    exception = new IABBadResponseException("Unable to parse JSON", e);
                }
                catch (IABException e)
                {
                    THLog.e(TAG, "IAB error.", e);
                    exception = e;
                }
                return null;
            }

            @Override protected void onPostExecute(HashMap<SKU, SKUDetails> skuskuDetailsMap)
            {
                if (exception != null)
                {
                    handleInventoryFetchFailure(exception);
                }
                else
                {
                    inventory = skuskuDetailsMap;
                    notifyListenerFetched();
                }
            }
        };
        backgroundTask.execute();
    }



    private void handleInventoryFetchFailure(IABException e)
    {
        if (this.inventoryListener != null)
        {
            this.inventoryListener.onInventoryFetchFail(this, e);
        }
    }


    protected void notifyListenerFetched()
    {
        InventoryListener listenerCopy = getInventoryListener();
        if (listenerCopy != null)
        {
            listenerCopy.onInventoryFetchSuccess(this, this.getInventory());
        }
    }

    private HashMap<SKU, SKUDetails> internalFetchCompleteInventory() throws IABException, RemoteException, JSONException
    {
        if (skus == null || skus.isEmpty())
        {
            return new HashMap<>();
        }

        HashMap<SKU, SKUDetails> map = internalFetchSKUType(Constants.ITEM_TYPE_INAPP);

        if (areSubscriptionsSupported())
        {
            HashMap<SKU, SKUDetails> subscriptionsMap = internalFetchSKUType(Constants.ITEM_TYPE_SUBS);
            map.putAll(subscriptionsMap);
        }
        return map;
    }

    private Bundle getQuerySKUBundle()
    {
        ArrayList<String> identifiers = new ArrayList<>(this.skus.size());
        for (SKU sku : this.skus)
        {
            identifiers.add(sku.identifier);
        }
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(Constants.GET_SKU_DETAILS_ITEM_LIST, identifiers);
        return querySkus;
    }

    private HashMap<SKU, SKUDetails> internalFetchSKUType(String itemType) throws IABException, RemoteException, JSONException
    {
        Bundle querySkus = getQuerySKUBundle();
        Bundle skuDetails = this.billingService.getSkuDetails(TARGET_BILLING_API_VERSION3, context.getPackageName(), itemType, querySkus);

        if (!skuDetails.containsKey(Constants.RESPONSE_GET_SKU_DETAILS_LIST))
        {
            int statusCode = Constants.getResponseCodeFromBundle(skuDetails);
            if (statusCode != Constants.BILLING_RESPONSE_RESULT_OK)
            {
                THLog.d(TAG, "getSkuDetails() failed: " + Constants.getStatusCodeDescription(statusCode));
                throw iabExceptionFactory.get().create(statusCode);
            }
            else
            {
                THLog.d(TAG, "getSkuDetails() returned a bundle with neither an error nor a detail list.");
                throw new IABBadResponseException(Constants.getStatusCodeDescription(statusCode));
            }
        }

        ArrayList<String> responseList = skuDetails.getStringArrayList(Constants.RESPONSE_GET_SKU_DETAILS_LIST);

        HashMap<SKU, SKUDetails> map = new HashMap<>();
        for (String json : responseList)
        {
            SKUDetails d = new SKUDetails(itemType, json);
            THLog.d(TAG, "Got sku details: " + d);
            map.put(d.sku, d);
        }
        return map;
    }

    public Map<SKU, SKUDetails> getInventory()
    {
        return Collections.unmodifiableMap(inventory);
    }

    public InventoryListener getInventoryListener()
    {
        return inventoryListener;
    }

    public void setInventoryListener(InventoryListener inventoryListener)
    {
        this.inventoryListener = inventoryListener;
    }

    public static interface InventoryListener
    {
        void onInventoryFetchSuccess(InventoryFetcher fetcher, Map<SKU, SKUDetails> inventory);
        void onInventoryFetchFail(InventoryFetcher fetcher, IABException exception);
    }
}

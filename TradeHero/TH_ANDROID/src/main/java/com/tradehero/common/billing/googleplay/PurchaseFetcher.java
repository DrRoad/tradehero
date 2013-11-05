package com.tradehero.common.billing.googleplay;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.tradehero.common.utils.THLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;

/** Created with IntelliJ IDEA. User: xavier Date: 11/5/13 Time: 3:31 PM To change this template use File | Settings | File Templates. */
public class PurchaseFetcher extends IABServiceConnector
{
    public static final String TAG = PurchaseFetcher.class.getSimpleName();

    private Map<SKU, GooglePurchase> purchases;
    private List<SKU> skus;

    public PurchaseFetcher(Context ctx, List<SKU> skus)
    {
        super(ctx);
        this.skus = skus;
        purchases = new HashMap<>();
    }

    @Override protected void handleSetupFailed(IABException exception)
    {
        super.handleSetupFailed(exception);
        // TODO
    }

    @Override protected void handleSetupFinished(IABResponse response)
    {
        super.handleSetupFinished(response);
        // TODO
    }

    int queryPurchases(String itemType) throws JSONException, RemoteException
    {
        // Query purchases
        THLog.d(TAG, "Querying owned items, item type: " + itemType);
        THLog.d(TAG, "Package name: " + context.getPackageName());
        boolean verificationFailed = false;
        String continueToken = null;

        do
        {
            Bundle ownedItems = getPurchases(itemType, continueToken);

            int response = Constants.getResponseCodeFromBundle(ownedItems);
            THLog.d(TAG, "Owned items response: " + String.valueOf(response));
            if (response != Constants.BILLING_RESPONSE_RESULT_OK)
            {
                THLog.d(TAG, "getPurchases() failed: " + Constants.getStatusCodeDescription(response));
                return response;
            }
            if (!ownedItems.containsKey(Constants.RESPONSE_INAPP_ITEM_LIST)
                    || !ownedItems.containsKey(Constants.RESPONSE_INAPP_PURCHASE_DATA_LIST)
                    || !ownedItems.containsKey(Constants.RESPONSE_INAPP_SIGNATURE_LIST))
            {
                THLog.w(TAG, "Bundle returned from getPurchases() doesn't contain required fields.");
                return Constants.IABHELPER_BAD_RESPONSE;
            }

            ArrayList<String> ownedSkus = ownedItems.getStringArrayList(
                    Constants.RESPONSE_INAPP_ITEM_LIST);
            ArrayList<String> purchaseDataList = ownedItems.getStringArrayList(
                    Constants.RESPONSE_INAPP_PURCHASE_DATA_LIST);
            ArrayList<String> signatureList = ownedItems.getStringArrayList(
                    Constants.RESPONSE_INAPP_SIGNATURE_LIST);

            for (int i = 0; i < purchaseDataList.size(); ++i)
            {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);
                String sku = ownedSkus.get(i);
                if (Security.verifyPurchase(Constants.BASE_64_PUBLIC_KEY, purchaseData, signature))
                {
                    THLog.d(TAG, "Sku is owned: " + sku);
                    GooglePurchase purchase = new GooglePurchase(itemType, purchaseData, signature);

                    if (TextUtils.isEmpty(purchase.token))
                    {
                        THLog.w(TAG, "BUG: empty/null token!");
                        THLog.d(TAG, "Purchase data: " + purchaseData);
                    }

                    // Record ownership and token
                    purchases.put(purchase.getProductIdentifier(), purchase);
                }
                else
                {
                    THLog.w(TAG, "Purchase signature verification **FAILED**. Not adding item.");
                    THLog.d(TAG, "   Purchase data: " + purchaseData);
                    THLog.d(TAG, "   Signature: " + signature);
                    verificationFailed = true;
                }
            }

            continueToken = ownedItems.getString(Constants.INAPP_CONTINUATION_TOKEN);
            THLog.d(TAG, "Continuation token: " + continueToken);
        } while (!TextUtils.isEmpty(continueToken));

        return verificationFailed ? Constants.IABHELPER_VERIFICATION_FAILED : Constants.BILLING_RESPONSE_RESULT_OK;
    }

    protected Bundle getPurchases(String itemType, String continueToken) throws RemoteException
    {
        THLog.d(TAG, "Calling getPurchases with continuation token: " + continueToken);
        return billingService.getPurchases(TARGET_BILLING_API_VERSION3, context.getPackageName(),
                itemType, continueToken);
    }
}

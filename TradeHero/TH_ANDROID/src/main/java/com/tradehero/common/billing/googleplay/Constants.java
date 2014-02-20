package com.tradehero.common.billing.googleplay;

import android.content.Intent;
import android.os.Bundle;
import timber.log.Timber;

/**
 * Created by julien on 4/11/13
 */
public class Constants
{
    public static final String BASE_64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhVgfcepa4NXGyS5kSGD1TmksVWhZcyMrqqJVBsuQgi+Io0+vFmboFN5n/nYPWWFOPjpvo8ht/11bglW+V+LtPOauk3/lCyFYGMVxuzv55J+YPimNBBnpECIqr6wfHyk0k6h2XPDJeEG2fPV3CIgAWiyNlH3JZVPrmVUIoU4537GACssREjFi7DERyv0JPg9n+0qlBb/NKhpbh00uniDXbNcb9KAb3e+kWI3+qpextDn0k6nt6/nqEFNZMD4JFlbqdrbc9Lfd+Zj2XUO983oVBbuRoIW11UUL5nY6qnjdh+FHG6254mbqoPtWeMnYrMPp3d733WOQdXhsfxwC0Fx99QIDAQAB";

    // Billing response codes
    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    // IAB Helper error codes
    public static final int IABHELPER_ERROR_BASE = -1000;
    public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
    public static final int IABHELPER_BAD_RESPONSE = -1002;
    public static final int IABHELPER_VERIFICATION_FAILED = -1003;
    public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
    public static final int IABHELPER_USER_CANCELLED = -1005;
    public static final int IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006;
    public static final int IABHELPER_MISSING_TOKEN = -1007;
    public static final int IABHELPER_UNKNOWN_ERROR = -1008;
    public static final int IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
    public static final int IABHELPER_INVALID_CONSUMPTION = -1010;

    // Keys for the responses from InAppBillingService
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    // Item types
    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String ITEM_TYPE_SUBS = "subs";

    // some fields on the getSkuDetails response bundle
    public static final String GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
    public static final String GET_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";
    public static final int ALERT_PLAN_UNLIMITED = 10000;

    public static final String GOOGLE_PLAY_ACCOUNT_URL = "https://play.google.com/store/account";

    /**
     * Returns a human-readable description for the given response code.
     *
     * @param code The response code
     * @return A human-readable string explaining the result code.
     *     It also includes the result code numerically.
     */
    public static String getStatusCodeDescription(int code)
    {
        String[] iab_msgs = ("0:OK/1:User Canceled/2:Unknown/" +
                "3:Billing Unavailable/4:Item unavailable/" +
                "5:Developer Error/6:Error/7:Item Already Owned/" +
                "8:Item not owned").split("/");
        String[] iabhelper_msgs = ("0:OK/-1001:Remote exception during initialization/" +
                "-1002:Bad response received/" +
                "-1003:Purchase signature verification failed/" +
                "-1004:Send intent failed/" +
                "-1005:User cancelled/" +
                "-1006:Unknown purchase response/" +
                "-1007:Missing token/" +
                "-1008:Unknown error/" +
                "-1009:Subscriptions not available/" +
                "-1010:Invalid consumption attempt").split("/");

        if (code <= IABHELPER_ERROR_BASE)
        {
            int index = IABHELPER_ERROR_BASE - code;
            if (index >= 0 && index < iabhelper_msgs.length)
            {
                return iabhelper_msgs[index];
            }
            else
            {
                return String.valueOf(code) + ":Unknown IAB Helper Error";
            }
        }
        else if (code < 0 || code >= iab_msgs.length)
        {
            return String.valueOf(code) + ":Unknown";
        }
        else
        {
            return iab_msgs[code];
        }
    }

    // Workaround to bug where sometimes response codes come as Long instead of Integer
    public static int getResponseCodeFromBundle(Bundle b)
    {
        Object o = b.get(RESPONSE_CODE);
        if (o == null)
        {
            Timber.d("Bundle with null response code, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        }
        else if (o instanceof Integer)
        {
            return ((Integer) o).intValue();
        }
        else if (o instanceof Long)
        {
            return (int) ((Long) o).longValue();
        }
        else
        {
            Timber.w("Unexpected type for bundle response code.");
            Timber.w(o.getClass().getName());
            throw new RuntimeException("Unexpected type for bundle response code: " + o.getClass().getName());
        }
    }

    // Workaround to bug where sometimes response codes come as Long instead of Integer
    public static int getResponseCodeFromIntent(Intent i)
    {
        Object o = i.getExtras().get(Constants.RESPONSE_CODE);
        if (o == null)
        {
            Timber.d("Intent with no response code, assuming OK (known issue)");
            return Constants.BILLING_RESPONSE_RESULT_OK;
        }
        else if (o instanceof Integer)
        {
            return ((Integer) o).intValue();
        }
        else if (o instanceof Long)
        {
            return (int) ((Long) o).longValue();
        }
        else
        {
            Timber.d("Unexpected type for intent response code.");
            Timber.d(o.getClass().getName());
            throw new RuntimeException("Unexpected type for intent response code: " + o.getClass().getName());
        }
    }
}

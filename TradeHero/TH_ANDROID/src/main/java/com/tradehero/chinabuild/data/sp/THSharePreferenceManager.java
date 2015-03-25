package com.tradehero.chinabuild.data.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.tradehero.chinabuild.data.AppInfoDTO;
import com.tradehero.th.utils.Constants;

/**
 * Created by palmer on 14-10-21.
 */
public class THSharePreferenceManager {

    //SharePreference Name
    private final static String TH_SP_NAME = "th_sp_name_app_version";
    private final static String TH_SP_GUIDE_NAME = "th_sp_guide_name";
    private final static String TH_SP_SHARE_ENDPOINT = "th_sp_share_endpoint";

    //The latest version
    public final static String KEY_APP_NEW_VERSION_DOWNLOAD_URL = "key_app_new_version_download_url";
    public final static String KEY_APP_SUGGEST_UPDATE = "key_app_suggest_update";
    public final static String KEY_APP_FORCE_UPDATE = "key_app_force_update";

    //Notifications
    public final static String KEY_APP_NOTIFICATION_ON_OFF = "key_app_notification_on_off";
    public final static String KEY_SIGN_IN_ACCOUNT = "key_sign_in_account";

    //Dialog record
    public final static String PROPERTY_MORE_THAN_FIFTEEN = "property_more_than_fifteen";
    public final static String PROPERTY_MORE_THAN_TWENTY_FIVE = "property_more_than_twenty_five";
    public static boolean isMoreThanFifteenShowed = false;
    public static boolean isMoreThanTwentyShowed = false;
    public final static String FANS_MORE_THAN_NINE = "fans_more_than_nine";
    public static boolean FansMoreThanNineShowed = false;
    public final static String LOGIN_CONTINUALLY = "login_continually";
    public static boolean isLoginContinuallyShowed = false;
    public static int Login_Continuous_Time = 0;

    //只显示一次 交易直接跳转热门持有
    public final static String KEY_APP_SHOW_TRADE_HOLD_ONCE = "key_app_show_trade_hold_once";

    //Novice
    public final static String RECOMMEND_STOCK_GOD = "recommend_stock_god";

    public static AppInfoDTO getAppVersionInfo(Context context) {
        if (context == null) {
            return null;
        }
        String url = "";
        boolean suggestUpdate = false;
        boolean forceUpdate = false;
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        url = sp.getString(KEY_APP_NEW_VERSION_DOWNLOAD_URL, "");
        suggestUpdate = sp.getBoolean(KEY_APP_SUGGEST_UPDATE, false);
        forceUpdate = sp.getBoolean(KEY_APP_FORCE_UPDATE, false);
        AppInfoDTO dto = new AppInfoDTO();
        dto.setForceUpgrade(forceUpdate);
        dto.setSuggestUpgrade(suggestUpdate);
        dto.setLatestVersionDownloadUrl(url);
        return dto;
    }

    public static void saveUpdateAppUrlLastestVersionCode(Context context, String url, boolean suggestUpdate, boolean forceUpdate) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_APP_NEW_VERSION_DOWNLOAD_URL, url).commit();
        sp.edit().putBoolean(KEY_APP_FORCE_UPDATE, forceUpdate).commit();
        sp.edit().putBoolean(KEY_APP_SUGGEST_UPDATE, suggestUpdate).commit();
    }

    public static boolean isNotificationsOn(Context context) {
        if (context == null) {
            return true;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        boolean isOn = sp.getBoolean(KEY_APP_NOTIFICATION_ON_OFF, true);
        return isOn;
    }

    public static void setNotificaitonsStatus(Context context, boolean notificationsStatus) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_APP_NOTIFICATION_ON_OFF, notificationsStatus).commit();
    }

    public static void saveAccount(Context context, String account) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SIGN_IN_ACCOUNT, account).commit();
    }

    public static String getAccount(Context context) {
        if (context == null) {
            return "";
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SIGN_IN_ACCOUNT, "");
    }

    public static void recordShareDialogMoreThanFifteen(int userId, boolean isConfirm, Context context) {
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        if (isConfirm) {
            sp.edit().putInt(userId + "true" + PROPERTY_MORE_THAN_FIFTEEN, 1).commit();
        } else {
            int cancelRecord = sp.getInt(userId + "false" + PROPERTY_MORE_THAN_FIFTEEN, 0);
            cancelRecord++;
            sp.edit().putInt(userId + "false" + PROPERTY_MORE_THAN_FIFTEEN, cancelRecord).commit();
        }
    }

    public static boolean isShareDialogMoreThanFifteenAvailable(int userId, Context context) {
        if(isMoreThanFifteenShowed){
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        int confirmRecord = sp.getInt(userId + "true" + PROPERTY_MORE_THAN_FIFTEEN, 0);
        if (confirmRecord > 0) {
            return false;
        }
        int cancelRecord = sp.getInt(userId + "false" + PROPERTY_MORE_THAN_FIFTEEN, 0);
        if (cancelRecord >= 3) {
            return false;
        }
        return true;
    }

    public static void recordShareDialogMoreThanTwentyFive(int userId, boolean isConfirm, Context context) {
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        if (isConfirm) {
            sp.edit().putInt(userId + "true" + PROPERTY_MORE_THAN_TWENTY_FIVE, 1).commit();
        } else {
            int cancelRecord = sp.getInt(userId + "false" + PROPERTY_MORE_THAN_TWENTY_FIVE, 0);
            cancelRecord++;
            sp.edit().putInt(userId + "false" + PROPERTY_MORE_THAN_TWENTY_FIVE, cancelRecord).commit();
        }
    }

    public static boolean isShareDialogMoreThanTwentyFiveAvailable(int userId, Context context) {
        if(isMoreThanTwentyShowed){
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        int confirmRecord = sp.getInt(userId + "true" + PROPERTY_MORE_THAN_TWENTY_FIVE, 0);
        if (confirmRecord > 0) {
            return false;
        }
        int cancelRecord = sp.getInt(userId + "false" + PROPERTY_MORE_THAN_TWENTY_FIVE, 0);
        if (cancelRecord >= 3) {
            return false;
        }
        return true;
    }


    public static void recordShareDialogFANSMoreThanNine(int userId, boolean isConfirm, Context context) {
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        if (isConfirm) {
            sp.edit().putInt(userId + "true" + FANS_MORE_THAN_NINE, 1).commit();
        } else {
            int cancelRecord = sp.getInt(userId + "false" + FANS_MORE_THAN_NINE, 0);
            cancelRecord++;
            sp.edit().putInt(userId + "false" + FANS_MORE_THAN_NINE, cancelRecord).commit();
        }
    }

    public static boolean isShareDialogFANSMoreThanNineAvailable(int userId, Context context) {
        if(FansMoreThanNineShowed){
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        int confirmRecord = sp.getInt(userId + "true" + FANS_MORE_THAN_NINE, 0);
        if (confirmRecord > 0) {
            return false;
        }
        int cancelRecord = sp.getInt(userId + "false" + FANS_MORE_THAN_NINE, 0);
        if (cancelRecord >= 3) {
            return false;
        }
        return true;
    }

    public static void recordShareDialogLoginContinually(int userId, boolean isConfirm, Context context){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        if (isConfirm) {
            sp.edit().putInt(userId + "true" + LOGIN_CONTINUALLY, 1).commit();
        } else {
            int cancelRecord = sp.getInt(userId + "false" + LOGIN_CONTINUALLY, 0);
            cancelRecord++;
            sp.edit().putInt(userId + "false" + LOGIN_CONTINUALLY, cancelRecord).commit();
        }
    }

    public static boolean isShareDialogLoginContinually(int userId, Context context){
        if(isLoginContinuallyShowed){
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        int confirmRecord = sp.getInt(userId + "true" + LOGIN_CONTINUALLY, 0);
        if (confirmRecord > 0) {
            return false;
        }
        int cancelRecord = sp.getInt(userId + "false" + LOGIN_CONTINUALLY, 0);
        if (cancelRecord >= 3) {
            return false;
        }
        return true;
    }

    public static boolean isRecommendedStock(int userId, Context context){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        boolean result = sp.getBoolean(userId + RECOMMEND_STOCK_GOD, false);
        return result;
    }

    public static void setRecommendedStock(int userId, Context context){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(userId + RECOMMEND_STOCK_GOD, true).commit();
    }

    public static void clearDialogShowedRecord(){
        THSharePreferenceManager.isMoreThanFifteenShowed = false;
        THSharePreferenceManager.isMoreThanTwentyShowed = false;
        THSharePreferenceManager.FansMoreThanNineShowed = false;
        THSharePreferenceManager.isLoginContinuallyShowed = false;
        THSharePreferenceManager.Login_Continuous_Time = 0;
    }

    //Guide View
    public final static String GUIDE_STOCK_DETAIL = "guide_stock_detail";
    public final static String GUIDE_COMPETITION = "guide_competition";
    public final static String GUIDE_COMPETITION_INTRO_EDIT = "guide_competition_intro_edit";
    public final static String GUIDE_STOCK_BUY = "guide_stock_buy";
    public final static String GUIDE_MAIN_TAB_ZERO= "guide_main_tab_zero";
    public final static String GUIDE_MAIN_TAB_TWO = "guide_main_tab_two";
    public final static String GUIDE_MAIN_TAB_THREE = "guide_main_tab_three";
    public final static String GUIDE_MAIN_TAB_FOUR = "guide_main_tab_four";

    public static boolean isGuideAvailable(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_GUIDE_NAME, Context.MODE_PRIVATE);
        boolean result = sp.getBoolean(key, true);
        return result;
    }

    public static void setGuideShowed(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_GUIDE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, false).commit();
    }

    public static boolean isShowTradeHoldOnce(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(TH_SP_GUIDE_NAME, Context.MODE_PRIVATE);
        boolean isOn = sp.getBoolean(KEY_APP_SHOW_TRADE_HOLD_ONCE, false);
        if(!isOn)
        {
            sp.edit().putBoolean(KEY_APP_SHOW_TRADE_HOLD_ONCE, true).commit();
        }
        return isOn;
    }

    //Share End Point
    public final static String KEY_SHARE_ENDPOINT = "key_share_end_point";

    public static String getShareEndPoint(Context context){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_SHARE_ENDPOINT, Context.MODE_PRIVATE);
        String endPoint = sp.getString(KEY_SHARE_ENDPOINT, Constants.DEFAULT_SHARE_ENDPOINT);
        return endPoint;
    }

    public static void setShareEndpoint(Context context, String endPoint){
        SharedPreferences sp = context.getSharedPreferences(TH_SP_SHARE_ENDPOINT, Context.MODE_PRIVATE);
        sp.edit().putString(endPoint, Constants.DEFAULT_SHARE_ENDPOINT).commit();
    }

}

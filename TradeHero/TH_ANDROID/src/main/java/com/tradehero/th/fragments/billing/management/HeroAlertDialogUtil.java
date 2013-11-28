package com.tradehero.th.fragments.billing.management;

import android.content.Context;
import android.content.DialogInterface;
import com.tradehero.th.R;
import com.tradehero.th.utils.AlertDialogUtil;

/** Created with IntelliJ IDEA. User: xavier Date: 11/28/13 Time: 4:34 PM To change this template use File | Settings | File Templates. */
public class HeroAlertDialogUtil
{
    public static final String TAG = HeroAlertDialogUtil.class.getSimpleName();

    public static void popAlertFollowHero(Context context, DialogInterface.OnClickListener okClickListener)
    {
        AlertDialogUtil.popWithOkCancelButton(
                context,
                R.string.manage_heroes_alert_follow_title,
                R.string.manage_heroes_alert_follow_message,
                R.string.manage_heroes_alert_follow_ok,
                R.string.manage_heroes_alert_follow_cancel,
                okClickListener);
    }

    public static void popAlertUnfollowHero(Context context, DialogInterface.OnClickListener okClickListener)
    {
        AlertDialogUtil.popWithOkCancelButton(
                context,
                R.string.manage_heroes_alert_unfollow_title,
                R.string.manage_heroes_alert_unfollow_message,
                R.string.manage_heroes_alert_unfollow_ok,
                R.string.manage_heroes_alert_unfollow_cancel,
                okClickListener);
    }
}

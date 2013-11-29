package com.tradehero.th.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.tradehero.th.R;

/** Created with IntelliJ IDEA. User: xavier Date: 11/19/13 Time: 4:38 PM To change this template use File | Settings | File Templates. */
public class AlertDialogUtil
{
    public static final String TAG = AlertDialogUtil.class.getSimpleName();

    public static DialogInterface.OnClickListener createDefaultCancelListener()
    {
        return new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        };
    }

    public static AlertDialog popWithNegativeButton(final Context context, int titleResId, int descriptionResId, int cancelResId)
    {
        return popWithNegativeButton(context, titleResId, descriptionResId, cancelResId, createDefaultCancelListener());
    }

    public static AlertDialog popWithNegativeButton(final Context context, int titleResId, int descriptionResId, int cancelResId,
            DialogInterface.OnClickListener cancelListener)
    {
        return popWithNegativeButton(context,
                context.getString(titleResId),
                context.getString(descriptionResId),
                context.getString(cancelResId),
                cancelListener);
    }

    public static AlertDialog popWithNegativeButton(final Context context, String titleRes, String descriptionRes, String cancelRes)
    {
        return popWithNegativeButton(context, titleRes, descriptionRes, cancelRes, createDefaultCancelListener());
    }

    public static AlertDialog popWithNegativeButton(final Context context, String titleRes, String descriptionRes, String cancelRes,
            DialogInterface.OnClickListener cancelListener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(titleRes)
                .setMessage(descriptionRes)
                .setIcon(R.drawable.th_app_logo)
                .setCancelable(true)
                .setNegativeButton(cancelRes, cancelListener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return alertDialog;
    }

    public static AlertDialog popWithOkCancelButton(final Context context, int titleResId, int descriptionResId, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener)
    {
        return popWithOkCancelButton(context, titleResId, descriptionResId, okResId, cancelResId,
                okClickListener, createDefaultCancelListener());
    }

    public static AlertDialog popWithOkCancelButton(final Context context, int titleResId, int descriptionResId, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener, final DialogInterface.OnClickListener cancelClickListener)
    {
        return popWithOkCancelButton(context,
                context.getString(titleResId),
                context.getString(descriptionResId),
                okResId,
                cancelResId,
                okClickListener,
                cancelClickListener);
    }

    public static AlertDialog popWithOkCancelButton(final Context context, String title, String description, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener)
    {
        return popWithOkCancelButton(context, title, description, okResId, cancelResId,
                okClickListener, createDefaultCancelListener());
    }

    public static AlertDialog popWithOkCancelButton(final Context context, String title, String description, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener, final DialogInterface.OnClickListener cancelClickListener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setMessage(description)
                .setIcon(R.drawable.th_app_logo)
                .setCancelable(true)
                .setNegativeButton(cancelResId, cancelClickListener)
                .setPositiveButton(okResId, okClickListener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return alertDialog;
    }
}

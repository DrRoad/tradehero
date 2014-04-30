package com.tradehero.th.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.UserBaseDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTOUtil;
import com.tradehero.th.fragments.social.FollowDialogView;
import com.tradehero.th.models.social.OnFollowRequestedListener;
import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA. User: xavier Date: 11/19/13 Time: 4:38 PM To change this template use
 * File | Settings | File Templates.
 */
public class AlertDialogUtil
{
    private ProgressDialog mProgressDialog;

    @Inject public AlertDialogUtil()
    {
        super();
    }

    public DialogInterface.OnClickListener createDefaultCancelListener()
    {
        return new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        };
    }

    public AlertDialog popWithNegativeButton(final Context context, int titleResId,
            int descriptionResId, int cancelResId)
    {
        return popWithNegativeButton(context, titleResId, descriptionResId, cancelResId,
                createDefaultCancelListener());
    }

    public AlertDialog popWithNegativeButton(final Context context, int titleResId,
            int descriptionResId, int cancelResId,
            DialogInterface.OnClickListener cancelListener)
    {
        return popWithNegativeButton(context,
                context.getString(titleResId),
                context.getString(descriptionResId),
                context.getString(cancelResId),
                cancelListener);
    }

    public AlertDialog popWithNegativeButton(final Context context, String titleRes,
            String descriptionRes, String cancelRes)
    {
        return popWithNegativeButton(context, titleRes, descriptionRes, cancelRes,
                createDefaultCancelListener());
    }

    public AlertDialog popWithNegativeButton(final Context context, String titleRes,
            String descriptionRes, String cancelRes,
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
        alertDialog.setCanceledOnTouchOutside(true);
        return alertDialog;
    }

    public AlertDialog popWithOkCancelButton(final Context context, int titleResId,
            int descriptionResId, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener)
    {
        return popWithOkCancelButton(context, titleResId, descriptionResId, okResId, cancelResId,
                okClickListener, createDefaultCancelListener());
    }

    public AlertDialog popWithOkCancelButton(final Context context, int titleResId,
            int descriptionResId, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener,
            final DialogInterface.OnClickListener cancelClickListener)
    {
        return popWithOkCancelButton(context,
                context.getString(titleResId),
                context.getString(descriptionResId),
                okResId,
                cancelResId,
                okClickListener,
                cancelClickListener);
    }

    public AlertDialog popWithOkCancelButton(final Context context, String title,
            String description, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener)
    {
        return popWithOkCancelButton(context, title, description, okResId, cancelResId,
                okClickListener, createDefaultCancelListener());
    }

    public AlertDialog popWithOkCancelButton(final Context context, String title,
            String description, int okResId, int cancelResId,
            final DialogInterface.OnClickListener okClickListener,
            final DialogInterface.OnClickListener cancelClickListener)
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
        alertDialog.setCanceledOnTouchOutside(true);
        return alertDialog;
    }

    public Dialog popTutorialContent(final Context context, int layoutResourceId)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.tutorial_master_layout);
        ViewGroup tutorialContentView = (ViewGroup) dialog.findViewById(R.id.tutorial_content);
        LayoutInflater.from(context).inflate(layoutResourceId, tutorialContentView, true);
        ((View) tutorialContentView.getParent()).setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.95f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
        return dialog;
    }

    public AlertDialog popMarketClosed(final Context context, SecurityId securityId)
    {
        if (securityId == null)
        {
            return popWithNegativeButton(context,
                    R.string.alert_dialog_market_close_title,
                    R.string.alert_dialog_market_close_message_basic,
                    R.string.alert_dialog_market_close_cancel);
        }
        else
        {
            return popWithNegativeButton(context,
                    context.getString(R.string.alert_dialog_market_close_title),
                    String.format(context.getString(R.string.alert_dialog_market_close_message),
                            securityId.exchange, securityId.securitySymbol),
                    context.getString(R.string.alert_dialog_market_close_cancel));
        }
    }

    public void showFollowDialog(Context context, UserBaseDTO userBaseDTO, int followType, final OnFollowRequestedListener followRequestedListener)
    {
        if (followType == UserProfileDTOUtil.IS_PREMIUM_FOLLOWER)
        {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        FollowDialogView followDialogView = (FollowDialogView) inflater.inflate(R.layout.follow_dialog, null);
        followDialogView.setFollowType(followType);
        followDialogView.display(userBaseDTO);

        builder.setView(followDialogView);
        builder.setCancelable(true);

        final AlertDialog mFollowDialog = builder.create();
        mFollowDialog.show();

        followDialogView.setFollowRequestedListener(new OnFollowRequestedListener()
        {
            @Override public void freeFollowRequested(UserBaseKey heroId)
            {
                onFinish();
                followRequestedListener.freeFollowRequested(heroId);
            }

            @Override public void premiumFollowRequested(UserBaseKey heroId)
            {
                onFinish();
                followRequestedListener.premiumFollowRequested(heroId);
            }

            private void onFinish()
            {
                mFollowDialog.dismiss();
            }
        });
    }

    public void showProgressDialog(Context context)
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    public void showProgressDialog(Context context, String content)
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(content);
        mProgressDialog.show();
    }

    public void dismissProgressDialog()
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
    }

    public void showDefaultDialog(Context context, int resId)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(R.string.app_name)
                .setMessage(context.getString(resId))
                .setIcon(R.drawable.th_app_logo)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

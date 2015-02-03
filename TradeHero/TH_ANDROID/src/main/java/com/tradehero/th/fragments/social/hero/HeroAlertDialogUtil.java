package com.tradehero.th.fragments.social.hero;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import com.tradehero.th.R;
import com.tradehero.th.api.users.UserBaseDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTOUtil;
import com.tradehero.th.fragments.social.FollowDialogView;
import com.tradehero.th.models.social.FollowDialogCombo;
import com.tradehero.th.models.social.OnFollowRequestedListener;
import com.tradehero.th.utils.AlertDialogUtil;

public class HeroAlertDialogUtil extends AlertDialogUtil
{
    @Deprecated // Use HeroAlertDialogRxUtil
    @Nullable public static FollowDialogCombo showFollowDialog(
            @NonNull final Context context,
            @Nullable UserBaseDTO userBaseDTO,
            final int followType,
            @NonNull final OnFollowRequestedListener followRequestedListener)
    {
        if (followType == UserProfileDTOUtil.IS_PREMIUM_FOLLOWER)
        {
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        FollowDialogView followDialogView = (FollowDialogView) inflater.inflate(R.layout.follow_dialog, null);
        followDialogView.display(userBaseDTO);
        followDialogView.setFollowType(followType);

        final AlertDialog mFollowDialog = new AlertDialog.Builder(context)
                .setView(followDialogView)
                .setCancelable(true)
                .create();
        mFollowDialog.setCancelable(true);
        mFollowDialog.setCanceledOnTouchOutside(true);
        mFollowDialog.show();

        followDialogView.setFollowRequestedListener(new MiddleFollowRequestedListener(
                followRequestedListener,
                followType,
                mFollowDialog
        ));

        return new FollowDialogCombo(mFollowDialog, followDialogView);
    }

    private static class MiddleFollowRequestedListener implements OnFollowRequestedListener
    {
        @NonNull private final OnFollowRequestedListener followRequestedListener;
        private final int followType;
        @NonNull private final AlertDialog mFollowDialog;

        //<editor-fold desc="Constructors">
        private MiddleFollowRequestedListener(
                @NonNull OnFollowRequestedListener followRequestedListener,
                int followType,
                @NonNull AlertDialog mFollowDialog)
        {
            this.followRequestedListener = followRequestedListener;
            this.followType = followType;
            this.mFollowDialog = mFollowDialog;
        }
        //</editor-fold>

        @Override public void freeFollowRequested(@NonNull UserBaseKey heroId)
        {
            onFinish();
            if (followType != UserProfileDTOUtil.IS_FREE_FOLLOWER)
            {
                followRequestedListener.freeFollowRequested(heroId);
            }
        }

        @Override public void premiumFollowRequested(@NonNull UserBaseKey heroId)
        {
            onFinish();
            followRequestedListener.premiumFollowRequested(heroId);
        }

        private void onFinish()
        {
            mFollowDialog.dismiss();
        }
    }
}

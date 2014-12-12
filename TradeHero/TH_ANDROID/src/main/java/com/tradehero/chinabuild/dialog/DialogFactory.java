package com.tradehero.chinabuild.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import com.tradehero.common.widget.dialog.THDialog;
import com.tradehero.th.R;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public class DialogFactory {
    //<editor-fold desc="Constructors">
    @Inject
    public DialogFactory() {
        super();
    }
    //</editor-fold>


    public Dialog createSecurityDetailDialog(@NotNull Context context,
                                             @Nullable SecurityDetailDialogLayout.OnMenuClickedListener menuClickedListener) {
        SecurityDetailDialogLayout contentView = (SecurityDetailDialogLayout) LayoutInflater.from(context)
                .inflate(R.layout.security_detail_dialog_layout, null);
        contentView.setMenuClickedListener(menuClickedListener);
        return THDialog.showUpDialog(context, contentView);
    }

    public Dialog createTimeLineDetailDialog(@NotNull Context context, @NotNull TimeLineDetailDialogLayout.TimeLineDetailMenuClickListener menuClickListener,
        boolean isDeleteAllowed, boolean isReportAllowed) {
        TimeLineDetailDialogLayout contentView = (TimeLineDetailDialogLayout)LayoutInflater.from(context).inflate(R.layout.timeline_detail_dialog_layout, null);
        contentView.setMenuClickListener(menuClickListener);
        contentView.setBtnStatus(isDeleteAllowed, isReportAllowed);
        return THDialog.showUpDialog(context, contentView);
    }

}

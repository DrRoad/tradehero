package com.androidth.general.fragments.updatecenter;

import android.support.v4.app.Fragment;
import com.androidth.general.R;
import com.androidth.general.fragments.updatecenter.messageNew.MessagesCenterNewFragment;
import com.androidth.general.fragments.updatecenter.notifications.NotificationsCenterFragment;

enum UpdateCenterTabType
{
    Messages(R.string.message_center_private_message_menu, MessagesCenterNewFragment.class),
    Notifications(R.string.message_center_tab_notification, NotificationsCenterFragment.class);

    public final int titleRes;
    public final Class<? extends Fragment> tabClass;

    UpdateCenterTabType(int titleRes, Class<? extends Fragment> tabClass)
    {
        this.titleRes = titleRes;
        this.tabClass = tabClass;
    }
}

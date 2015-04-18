package com.tradehero.th.fragments.updatecenter;

import android.support.v4.app.Fragment;
import com.tradehero.th.R;
import com.tradehero.th.fragments.updatecenter.messageNew.MessagesCenterNewFragment;
import com.tradehero.th.fragments.updatecenter.notifications.NotificationsCenterFragment;

public enum UpdateCenterTabType
{
    Messages(R.string.message_center_private_message_menu, MessagesCenterNewFragment.class),
    Notifications(R.string.message_center_tab_notification, NotificationsCenterFragment.class);

    public final int titleRes;
    public final Class<? extends Fragment> tabClass;

    private UpdateCenterTabType(int titleRes, Class<? extends Fragment> tabClass)
    {
        this.titleRes = titleRes;
        this.tabClass = tabClass;
    }

    public static UpdateCenterTabType fromOrdinal(int tabTypeOrdinal)
    {
        if (values().length > tabTypeOrdinal)
        {
            return values()[tabTypeOrdinal];
        }

        return null;
    }
}


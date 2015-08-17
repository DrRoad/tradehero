package com.tradehero.th.fragments.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.Bind;
import com.tradehero.th.R;
import com.tradehero.th.inject.HierarchyInjector;
import javax.inject.Inject;

public class SettingsDrawerMenuItem extends LinearLayout
{
    @Inject UnreadSettingPreferenceHolder unreadSettingPreferenceHolder;
    @Bind(R.id.unread_icon) View unreadIcon;

    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public SettingsDrawerMenuItem(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public SettingsDrawerMenuItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public SettingsDrawerMenuItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();

        if(!isInEditMode())
        {
            ButterKnife.bind(this);
            HierarchyInjector.inject(this);
        }
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        unreadIcon.setVisibility(hasUnVisitedSetting() ? View.VISIBLE : View.INVISIBLE);
    }

    public boolean hasUnVisitedSetting()
    {
        return unreadSettingPreferenceHolder.hasUnread();
    }
}
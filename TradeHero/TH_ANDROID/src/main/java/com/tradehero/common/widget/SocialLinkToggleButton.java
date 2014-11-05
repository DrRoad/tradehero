package com.tradehero.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ToggleButton;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import android.support.annotation.NonNull;

public class SocialLinkToggleButton extends ToggleButton
{
    @NonNull private SocialNetworkEnum socialNetworkEnum;

    //<editor-fold desc="Constructors">
    public SocialLinkToggleButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }
    //</editor-fold>

    private void init(@NonNull Context context, @NonNull AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SocialLinkToggleButton);
        socialNetworkEnum = SocialNetworkEnum.fromIndex(a.getInt(R.styleable.SocialLinkToggleButton_networkType, -1));
        a.recycle();
    }

    @NonNull public SocialNetworkEnum getSocialNetworkEnum()
    {
        return socialNetworkEnum;
    }
}

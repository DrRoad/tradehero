package com.tradehero.th.fragments.translation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.i18n.LanguageDTO;
import com.tradehero.th.api.translation.UserTranslationSettingDTO;

public class TranslatableLanguageItemLinearView extends LinearLayout
    implements DTOView<LanguageDTO>
{
    @NonNull protected TranslatableLanguageItemViewHolder viewHolder;

    //<editor-fold desc="Constructors">
    public TranslatableLanguageItemLinearView(Context context)
    {
        super(context);
        init();
    }

    public TranslatableLanguageItemLinearView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public TranslatableLanguageItemLinearView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    protected void init()
    {
        viewHolder = createViewHolder();
    }
    //</editor-fold>

    @NonNull protected TranslatableLanguageItemViewHolder createViewHolder()
    {
        return new TranslatableLanguageItemViewHolder();
    }

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        viewHolder.initViews(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        viewHolder.initViews(this);
    }

    @Override protected void onDetachedFromWindow()
    {
        viewHolder.resetViews();
        super.onDetachedFromWindow();
    }

    public void setCurrentTranslationSetting(@Nullable UserTranslationSettingDTO currentTranslationSetting)
    {
        viewHolder.setCurrentTranslationSetting(currentTranslationSetting);
    }

    @Override public void display(LanguageDTO dto)
    {
        viewHolder.display(dto);
    }
}

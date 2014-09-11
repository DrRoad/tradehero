package com.tradehero.th.fragments.trending;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.th.R;
import com.tradehero.th.models.graphics.ForExtraTileBackground;
import com.tradehero.th.inject.HierarchyInjector;
import dagger.Lazy;
import javax.inject.Inject;

public class EarnCreditTileView extends ImageView
{
    @Inject protected Lazy<Picasso> picasso;
    @Inject @ForExtraTileBackground Transformation backgroundTransformation;

    //<editor-fold desc="Constructors">
    public EarnCreditTileView(Context context)
    {
        super(context);
    }

    public EarnCreditTileView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public EarnCreditTileView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();

        HierarchyInjector.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        // no nid to clean on detach, coz this view's content will never change
        picasso.get()
                .load(R.drawable.tile_trending_refer)
                .placeholder(R.drawable.white_rounded_background_xml)
                .transform(backgroundTransformation)
                .fit()
                .into(this);
    }
}

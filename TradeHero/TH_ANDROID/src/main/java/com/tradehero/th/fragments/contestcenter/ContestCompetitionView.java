package com.tradehero.th.fragments.contestcenter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.widgets.AspectRatioImageView;
import com.squareup.widgets.AspectRatioImageViewCallback;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.inject.HierarchyInjector;
import dagger.Lazy;
import javax.inject.Inject;

public class ContestCompetitionView extends AspectRatioImageView
        implements DTOView<ContestPageDTO>
{
    @DrawableRes private static final int PLACE_HOLDER = R.drawable.lb_competitions_bg;

    @Inject protected Lazy<Picasso> picasso;
    @Nullable private ContestPageDTO contestPageDTO;
    @Nullable private ProviderDTO providerDTO;

    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public ContestCompetitionView(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public ContestCompetitionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
        HierarchyInjector.inject(this);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        displayImageView();
    }

    @Override protected void onDetachedFromWindow()
    {
        setImageDrawable(null);
        super.onDetachedFromWindow();
    }

    @Override public void display(@Nullable ContestPageDTO dto)
    {
        this.contestPageDTO = dto;
        if (contestPageDTO != null)
        {
            linkWith(((ProviderContestPageDTO) contestPageDTO).providerDTO, true);
        }
    }

    private void linkWith(@Nullable ProviderDTO providerDTO, boolean andDisplay)
    {
        this.providerDTO = providerDTO;
        if (andDisplay)
        {
            displayImageView();
        }
    }

    protected void displayImageView()
    {
        RequestCreator request;
        if (providerDTO != null)
        {
            setVisibility(View.VISIBLE);

            String url = providerDTO.getStatusSingleImageUrl();
            if (url != null)
            {
                request = picasso.get()
                        .load(url)
                        .placeholder(PLACE_HOLDER);
            }
            else
            {
                request = picasso.get().load(PLACE_HOLDER);
            }
        }
        else
        {
            request = picasso.get().load(PLACE_HOLDER);
        }
        request.into(this, new AspectRatioImageViewCallback(this));
    }
}

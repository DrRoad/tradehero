package com.tradehero.th.fragments.onboarding.sector;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.api.SelectableDTO;
import com.tradehero.th.R;
import com.tradehero.th.api.market.SectorDTO;
import com.tradehero.th.fragments.onboarding.OnBoardSelectableViewLinear;
import com.tradehero.th.fragments.onboarding.exchange.TopStockListView;
import javax.inject.Inject;

public class OnBoardSectorItemView extends OnBoardSelectableViewLinear<SectorDTO, SelectableDTO<SectorDTO>>
{
    @DrawableRes private static final int DEFAULT_SECTOR_LOGO = R.drawable.accounts_glyph_name_default;

    @Inject Picasso picasso;

    @InjectView(android.R.id.icon1) ImageView logoImage;
    @InjectView(android.R.id.text1) TextView shortNameView;
    @InjectView(R.id.top_stock_list) TopStockListView topStockListView;

    //<editor-fold desc="Constructors">
    public OnBoardSectorItemView(Context context)
    {
        super(context);
    }

    public OnBoardSectorItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public OnBoardSectorItemView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
    //</editor-fold>

    @Override protected void onDetachedFromWindow()
    {
        if (logoImage != null)
        {
            picasso.cancelRequest(logoImage);
        }
        super.onDetachedFromWindow();
    }

    @Override public void display(@NonNull SelectableDTO<SectorDTO> dto)
    {
        super.display(dto);
        display(dto.value);
    }

    protected void display(@Nullable SectorDTO dto)
    {
        if (logoImage != null)
        {
            picasso.cancelRequest(logoImage);
            if (dto == null || dto.imageUrl == null)
            {
                logoImage.setImageResource(DEFAULT_SECTOR_LOGO);
            }
            else
            {
                picasso.load(dto.imageUrl)
                        .into(logoImage);
            }
        }

        if (shortNameView != null)
        {
            if (dto == null)
            {
                shortNameView.setText(R.string.na);
            }
            else
            {
                shortNameView.setText(dto.name);
            }
        }
        if (topStockListView != null)
        {
            if (dto != null)
            {
                topStockListView.setVisibility(VISIBLE);
                topStockListView.display(dto.getTopSecurities());
            }
            else
            {
                topStockListView.setVisibility(GONE);
            }
        }
    }
}

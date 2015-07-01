package com.tradehero.th.fragments.trending.filter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTO;

public class TrendingFilterSpinnerItemShortView extends LinearLayout
        implements DTOView<ExchangeCompactSpinnerDTO>
{
    @InjectView(R.id.trending_filter_spinner_item_label) TextView label;

    @Nullable private ExchangeCompactSpinnerDTO exchangeCompactSpinnerDTO;

    //<editor-fold desc="Constructors">
    public TrendingFilterSpinnerItemShortView(Context context)
    {
        super(context);
    }

    public TrendingFilterSpinnerItemShortView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public TrendingFilterSpinnerItemShortView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override public void display(ExchangeCompactSpinnerDTO dto)
    {
        linkWith(dto, true);
    }

    public void linkWith(@Nullable ExchangeCompactSpinnerDTO dto, boolean andDisplay)
    {
        this.exchangeCompactSpinnerDTO = dto;
        if (andDisplay)
        {
            displayText();
        }
    }

    public void displayText()
    {
        if (label != null)
        {
            if (exchangeCompactSpinnerDTO != null)
            {
                if (exchangeCompactSpinnerDTO.name != null)
                {
                    label.setText(exchangeCompactSpinnerDTO.name);
                }
                else
                {
                    label.setText(exchangeCompactSpinnerDTO.toString());
                }
            }
            else
            {
                label.setText(R.string.na);
            }
        }
    }
}

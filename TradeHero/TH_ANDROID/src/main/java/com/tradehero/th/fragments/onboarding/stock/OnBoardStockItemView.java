package com.tradehero.th.fragments.onboarding.stock;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import butterknife.InjectView;
import com.tradehero.common.api.SelectableDTO;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.fragments.onboarding.OnBoardSelectableViewLinear;
import com.tradehero.th.fragments.security.SecurityItemView;

public class OnBoardStockItemView extends OnBoardSelectableViewLinear<SecurityCompactDTO, SelectableDTO<SecurityCompactDTO>>
{
    @InjectView(R.id.security_view) SecurityItemView securityItemView;

    //<editor-fold desc="Constructors">
    public OnBoardStockItemView(Context context)
    {
        super(context);
    }

    public OnBoardStockItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public OnBoardStockItemView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        if (!isInEditMode())
        {
            securityItemView.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
    }

    @Override public void display(@NonNull SelectableDTO<SecurityCompactDTO> dto)
    {
        super.display(dto);
        securityItemView.display(dto.value);
    }
}

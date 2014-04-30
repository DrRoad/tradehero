package com.tradehero.th.fragments.position.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tradehero.th.adapters.ExpandableListItem;
import com.tradehero.th.api.position.PositionDTO;

public class PositionClosedView extends AbstractPositionView<
        PositionDTO,
        ExpandableListItem<PositionDTO>>
{
    //<editor-fold desc="Constructors">
    public PositionClosedView(Context context)
    {
        super(context);
    }

    public PositionClosedView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PositionClosedView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>
}

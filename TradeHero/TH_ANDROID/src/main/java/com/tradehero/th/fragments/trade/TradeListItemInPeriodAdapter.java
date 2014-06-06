package com.tradehero.th.fragments.trade;

import android.content.Context;
import android.view.LayoutInflater;
import com.tradehero.th.R;
import com.tradehero.th.api.position.PositionDTO;

public class TradeListItemInPeriodAdapter extends AbstractTradeListItemAdapter<PositionDTO>
{
    public static final int LAYOUT_RES_ID_POSITION_OPEN = R.layout.position_open_in_period;
    public static final int LAYOUT_RES_ID_POSITION_CLOSED = R.layout.position_closed_in_period;

    //<editor-fold desc="Constructors">
    public TradeListItemInPeriodAdapter(Context context, LayoutInflater inflater)
    {
        super(context, inflater);
    }
    //</editor-fold>

    @Override public int getOpenPositionLayoutResId()
    {
        return LAYOUT_RES_ID_POSITION_OPEN;
    }

    @Override public int getClosedPositionLayoutResId()
    {
        return LAYOUT_RES_ID_POSITION_CLOSED;
    }
}

package com.tradehero.th.fragments.position.partial;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.adapters.ExpandableListItem;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedNumber;

abstract public class AbstractPositionPartialBottomOpenView<
        PositionDTOType extends PositionDTO,
        ExpandableListItemType extends ExpandableListItem<PositionDTOType>
        >
        extends AbstractPartialBottomView<PositionDTOType, ExpandableListItemType>
{
    @InjectView(R.id.unrealised_pl_value_header) protected TextView unrealisedPLValueHeader;
    @InjectView(R.id.unrealised_pl_value) protected TextView unrealisedPLValue;
    @InjectView(R.id.realised_pl_value_header) protected TextView realisedPLValueHeader;
    @InjectView(R.id.realised_pl_value) protected TextView realisedPLValue;
    @InjectView(R.id.total_invested_value) protected TextView totalInvestedValue;
    @InjectView(R.id.market_value_value) protected TextView marketValueValue;
    @InjectView(R.id.quantity_value) protected TextView quantityValue;
    @InjectView(R.id.average_price_value) protected TextView averagePriceValue;

    //<editor-fold desc="Constructors">
    public AbstractPositionPartialBottomOpenView(Context context)
    {
        super(context);
    }

    public AbstractPositionPartialBottomOpenView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AbstractPositionPartialBottomOpenView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override public void linkWith(PositionDTOType positionDTO, boolean andDisplay)
    {
        super.linkWith(positionDTO, andDisplay);
        if (andDisplay)
        {
            displayUnrealisedPLValueHeader();
            displayUnrealisedPLValue();
            displayRealisedPLValueHeader();
            displayRealisedPLValue();
            displayTotalInvested();
            displayMarketValue();
            displayQuantityValue();
            displayAveragePriceValue();
        }
    }

    @Override public void displayModelPart()
    {
        super.displayModelPart();
        displayUnrealisedPLValueHeader();
        displayUnrealisedPLValue();
        displayRealisedPLValueHeader();
        displayRealisedPLValue();
        displayTotalInvested();
        displayMarketValue();
        displayQuantityValue();
        displayAveragePriceValue();
    }

    public void displayUnrealisedPLValueHeader()
    {
        if (unrealisedPLValueHeader != null)
        {
            if (positionDTO != null && positionDTO.unrealizedPLRefCcy != null && positionDTO.unrealizedPLRefCcy < 0)
            {
                unrealisedPLValueHeader.setText(R.string.position_unrealised_loss_header);
            }
            else
            {
                unrealisedPLValueHeader.setText(R.string.position_unrealised_profit_header);
            }
        }
    }

    public void displayUnrealisedPLValue()
    {
        if (unrealisedPLValue != null)
        {
            positionDTOUtils.setUnrealizedPLLook(unrealisedPLValue, positionDTO);
        }
    }

    public void displayRealisedPLValueHeader()
    {
        if (realisedPLValueHeader != null)
        {
            if (positionDTO != null && positionDTO.unrealizedPLRefCcy != null && positionDTO.realizedPLRefCcy < 0)
            {
                realisedPLValueHeader.setText(R.string.position_realised_loss_header);
            }
            else
            {
                realisedPLValueHeader.setText(R.string.position_realised_profit_header);
            }
        }
    }

    public void displayRealisedPLValue()
    {
        if (realisedPLValue != null)
        {
            positionDTOUtils.setRealizedPLLook(realisedPLValue, positionDTO);
        }
    }

    public void displayTotalInvested()
    {
        if (totalInvestedValue != null)
        {
            if (positionDTO != null)
            {
                totalInvestedValue.setText(positionDTOUtils.getSumInvested(getResources(), positionDTO));
            }
        }
    }

    public void displayMarketValue()
    {
        if (marketValueValue != null)
        {
            if (positionDTO != null)
            {
                marketValueValue.setText(positionDTOUtils.getMarketValue(getResources(), positionDTO));
            }
        }
    }

    public void displayQuantityValue()
    {
        if (quantityValue != null)
        {
            if (positionDTO != null && positionDTO.shares != null)
            {
                quantityValue.setText(String.format("%,d", positionDTO.shares));
            }
            else
            {
                quantityValue.setText(R.string.na);
            }
        }
    }

    public void displayAveragePriceValue()
    {
        if (averagePriceValue != null)
        {
            if (positionDTO != null && positionDTO.averagePriceRefCcy != null)
            {
                THSignedNumber ThAveragePriceRefCcy = THSignedMoney.builder(positionDTO.averagePriceRefCcy)
                        .withOutSign()
                        .currency(positionDTO.getNiceCurrency())
                        .build();
                averagePriceValue.setText(ThAveragePriceRefCcy.toString());
            }
            else
            {
                averagePriceValue.setText(R.string.na);
            }
        }
    }
}

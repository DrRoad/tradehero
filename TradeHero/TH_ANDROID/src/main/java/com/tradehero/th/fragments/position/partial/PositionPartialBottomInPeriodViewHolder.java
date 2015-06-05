package com.tradehero.th.fragments.position.partial;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import com.tradehero.common.annotation.ViewVisibilityValue;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.position.PositionInPeriodDTO;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.utils.DateUtils;

public class PositionPartialBottomInPeriodViewHolder implements DTOView<PositionPartialBottomInPeriodViewHolder.DTO>
{
    @InjectView(R.id.position_list_bottom_in_period_container) @Optional protected View inPeriodPositionContainer;
    @InjectView(R.id.position_list_in_period_title) @Optional protected View inPeriodTitle;
    @InjectView(R.id.position_list_overall_title) @Optional protected View overallTitle;
    @InjectView(R.id.in_period_pl_value_header) @Optional protected TextView inPeriodPLHeader;
    @InjectView(R.id.in_period_pl_value) @Optional protected TextView inPeriodPL;
    @InjectView(R.id.in_period_additional_invested) @Optional protected TextView inPeriodAdditionalInvested;
    @InjectView(R.id.in_period_start_value) @Optional protected TextView inPeriodValueAtStart;
    @InjectView(R.id.in_period_start_value_date) @Optional protected TextView inPeriodStartValueDate;
    @InjectView(R.id.in_period_roi_value) @Optional protected TextView inPeriodRoiValue;

    //<editor-fold desc="Constructors">
    public PositionPartialBottomInPeriodViewHolder(@NonNull View container)
    {
        super();
        ButterKnife.inject(this, container);
    }
    //</editor-fold>

    @Override public void display(DTO dto)
    {
        if (inPeriodPositionContainer != null)
        {
            inPeriodPositionContainer.setVisibility(dto.inPeriodVisibility);
        }
        if (inPeriodTitle != null)
        {
            inPeriodTitle.setVisibility(dto.inPeriodVisibility);
        }
        if (overallTitle != null)
        {
            overallTitle.setVisibility(dto.inPeriodVisibility);
        }
        if (inPeriodPLHeader != null)
        {
            inPeriodPLHeader.setText(dto.inPeriodPLHeader);
        }
        if (inPeriodPL != null)
        {
            inPeriodPL.setText(dto.inPeriodPL);
        }
        if (inPeriodAdditionalInvested != null)
        {
            inPeriodAdditionalInvested.setText(dto.inPeriodAdditionalInvested);
        }
        if (inPeriodValueAtStart != null)
        {
            inPeriodValueAtStart.setText(dto.inPeriodValueAtStart);
        }
        if (inPeriodStartValueDate != null)
        {
            inPeriodStartValueDate.setText(dto.inPeriodValueStartDate);
        }
        if (inPeriodRoiValue != null)
        {
            inPeriodRoiValue.setText(dto.inPeriodRoiValue);
        }
    }

    public static class DTO
    {
        @NonNull public final PositionDTO positionDTO;

        @ViewVisibilityValue public final int inPeriodVisibility;
        @NonNull public final CharSequence inPeriodPLHeader;
        @NonNull public final CharSequence inPeriodPL;
        @NonNull public final CharSequence inPeriodAdditionalInvested;
        @NonNull public final CharSequence inPeriodValueAtStart;
        @NonNull public final CharSequence inPeriodValueStartDate;
        @NonNull public final CharSequence inPeriodRoiValue;

        public DTO(@NonNull Resources resources, @NonNull PositionDTO positionDTO)
        {
            this.positionDTO = positionDTO;
            String na = resources.getString(R.string.na);

            inPeriodVisibility = positionDTO instanceof PositionInPeriodDTO ? View.VISIBLE : View.GONE;

            //<editor-fold desc="In Period PL">
            Double totalPLInPeriodRefCcy = positionDTO instanceof PositionInPeriodDTO
                    ? ((PositionInPeriodDTO) positionDTO).totalPLInPeriodRefCcy
                    : null;
            inPeriodPLHeader = totalPLInPeriodRefCcy == null
                    ? na
                    : resources.getString(
                            totalPLInPeriodRefCcy >= 0 ?
                                    R.string.position_in_period_profit :
                                    R.string.position_in_period_loss);
            inPeriodPL = totalPLInPeriodRefCcy == null
                    ? na
                    : THSignedMoney.builder(totalPLInPeriodRefCcy)
                            .withOutSign()
                            .currency(positionDTO.getNiceCurrency())
                            .build()
                            .createSpanned();
            //</editor-fold>

            //<editor-fold desc="In Period ROI Value">
            Double roiInPeriod = positionDTO instanceof PositionInPeriodDTO
                    ? ((PositionInPeriodDTO) positionDTO).getROIInPeriod()
                    : null;
            inPeriodRoiValue = roiInPeriod == null
                    ? na
                    : THSignedPercentage.builder(roiInPeriod * 100.0)
                            .signTypePlusMinusAlways()
                            .withDefaultColor()
                            .relevantDigitCount(3)
                            .build()
                            .createSpanned();
            //</editor-fold>

            //<editor-fold desc="In Period Additional Invested">
            Double sumPurchasesInPeriodRefCcy = positionDTO instanceof PositionInPeriodDTO
                    ? ((PositionInPeriodDTO) positionDTO).sumPurchasesInPeriodRefCcy
                    : null;
            inPeriodAdditionalInvested = sumPurchasesInPeriodRefCcy == null
                    ? na
                    : THSignedMoney.builder(sumPurchasesInPeriodRefCcy)
                            .withOutSign()
                            .currency(positionDTO.getNiceCurrency())
                            .build()
                            .createSpanned();
            //</editor-fold>

            //<editor-fold desc="In Period Value At Start">
            Double marketValueStartPeriodRefCcy = positionDTO instanceof PositionInPeriodDTO
                    ? ((PositionInPeriodDTO) positionDTO).marketValueStartPeriodRefCcy
                    : null;
            inPeriodValueAtStart = marketValueStartPeriodRefCcy == null || /* It appears iOS version does that */ marketValueStartPeriodRefCcy <= 0
                    ? na
                    : THSignedMoney.builder(marketValueStartPeriodRefCcy)
                            .withOutSign()
                            .currency(positionDTO.getNiceCurrency())
                            .build()
                            .createSpanned();
            //</editor-fold>

            inPeriodValueStartDate = resources.getString(
                    R.string.position_in_period_as_of,
                    DateUtils.getDisplayableDate(resources, positionDTO.latestTradeUtc));
        }
    }
}

package com.tradehero.th.fragments.alert;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tradehero.common.graphics.WhiteToTransparentTransformation;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.trade.BuySellFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.utils.DateUtils;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.Lazy;

public class AlertItemView extends RelativeLayout
        implements DTOView<AlertCompactDTO>
{
    @InjectView(R.id.logo) ImageView stockLogo;
    @InjectView(R.id.stock_symbol) TextView stockSymbol;
    @InjectView(R.id.alert_description) TextView alertDescription;
    @InjectView(R.id.alert_status) TextView alertStatus;

    @Inject protected Lazy<Picasso> picasso;
    @Inject DashboardNavigator navigator;

    private AlertCompactDTO alertCompactDTO;

    //<editor-fold desc="Constructors">
    public AlertItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        HierarchyInjector.inject(this);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ButterKnife.inject(this);
    }

    @Override protected void onDetachedFromWindow()
    {
        ButterKnife.reset(this);
        super.onDetachedFromWindow();
    }

    @Override public void display(AlertCompactDTO alertCompactDTO)
    {
        this.alertCompactDTO = alertCompactDTO;
        if (alertCompactDTO != null)
        {
            displayStockSymbol();
            displayStockLogo();
            displayAlertDescription();
            displayAlertStatus();
            displayTrigger();
        }
    }

    private void displayAlertDescription()
    {
        if (alertCompactDTO.priceMovement != null)
        {
            alertDescription.setText(getPriceMovementDescription(alertCompactDTO.priceMovement * 100));
        }
        else if (alertCompactDTO.upOrDown) // up
        {
            alertDescription.setText(getPriceRaiseDescription(alertCompactDTO.targetPrice));
        }
        else
        {
            alertDescription.setText(getPriceFallDescription(alertCompactDTO.targetPrice));
        }
    }

    private Spanned getPriceFallDescription(double targetPrice)
    {
        THSignedNumber thPriceRaise = THSignedMoney.builder(targetPrice)
                .withOutSign()
                .build();
        return Html.fromHtml(String.format(
                getContext().getString(R.string.stock_alert_when_price_falls),
                thPriceRaise.toString()
        ));
    }

    private Spanned getPriceRaiseDescription(double targetPrice)
    {
        THSignedNumber thPriceRaise = THSignedMoney.builder(targetPrice)
                .withOutSign()
                .build();
        return Html.fromHtml(String.format(
                getContext().getString(R.string.stock_alert_when_price_raises),
                thPriceRaise.toString()
        ));
    }

    private Spanned getPriceMovementDescription(double percentage)
    {
        THSignedNumber thPercentageChange = THSignedPercentage.builder(percentage).build();
        return Html.fromHtml(String.format(
                getContext().getString(R.string.stock_alert_when_price_move),
                thPercentageChange.toString()
        ));
    }

    private void displayTrigger()
    {
        if (alertCompactDTO.activeUntilDate != null)
        {
            //alertDescription.setText(getFormattedTriggerDescription(alertCompactDTO.activeUntilDate));
        }
    }

    private Spanned getFormattedTriggerDescription(Date activeUntilDate)
    {
        return null;
    }

    private void displayAlertStatus()
    {
        if (alertCompactDTO.active)
        {
            if (alertCompactDTO.activeUntilDate != null)
            {
                alertStatus.setText(getFormattedActiveUntilString(alertCompactDTO.activeUntilDate));
            }
            alertStatus.setTextColor(getResources().getColor(R.color.black));
        }
        else
        {
            alertStatus.setText(R.string.stock_alert_inactive);
            alertStatus.setTextColor(getResources().getColor(R.color.text_gray_normal));
        }
    }

    private Spanned getFormattedActiveUntilString(@NonNull Date activeUntilDate)
    {
        return Html.fromHtml(getContext().getString(R.string.stock_alert_active_until_date, DateUtils.getFormattedDate(getResources(), activeUntilDate)));
    }

    private void displayStockSymbol()
    {
        if (alertCompactDTO.security != null)
        {
            stockSymbol.setText(alertCompactDTO.security.getExchangeSymbol());
        }
    }

    private void displayStockLogo()
    {
        SecurityCompactDTO securityCompactDTO = alertCompactDTO.security;
        if (securityCompactDTO != null && securityCompactDTO.imageBlobUrl != null)
        {
            picasso.get()
                    .load(securityCompactDTO.imageBlobUrl)
                    .transform(new WhiteToTransparentTransformation())
                    .into(stockLogo);
        }
        else if (securityCompactDTO != null)
        {
            picasso.get()
                    .load(securityCompactDTO.getExchangeLogoId())
                    .into(stockLogo);
        }
        else
        {
            stockLogo.setImageResource(R.drawable.default_image);
        }
    }

    @OnClick({R.id.trade})
    public void onTradeClick()
    {
        handleBuyAndSellButtonClick();
    }

    private void handleBuyAndSellButtonClick()
    {
        if (alertCompactDTO != null && alertCompactDTO.security != null &&
                alertCompactDTO.security.getSecurityId() != null)
        {
            Bundle args = new Bundle();
            BuySellFragment.putSecurityId(args, alertCompactDTO.security.getSecurityId());
            navigator.pushFragment(BuySellFragment.class, args);
        }
    }
}

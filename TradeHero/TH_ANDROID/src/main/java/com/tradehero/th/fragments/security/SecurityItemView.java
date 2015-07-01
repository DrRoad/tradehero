package com.tradehero.th.fragments.security;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.market.Exchange;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.utils.DateUtils;
import java.util.Map;
import timber.log.Timber;

public class SecurityItemView extends RelativeLayout
        implements DTOView<SecurityCompactDTO>
{
    @InjectView(R.id.stock_logo) ImageView stockLogo;
    @InjectView(R.id.ic_market_close) @Optional ImageView marketCloseIcon;
    @InjectView(R.id.stock_name) TextView stockName;
    @InjectView(R.id.exchange_symbol) TextView exchangeSymbol;
    @InjectView(R.id.last_price) TextView lastPrice;
    @InjectView(R.id.tv_stock_roi) @Optional TextView stockRoi;
    @InjectView(R.id.country_logo) @Optional ImageView countryLogo;
    @InjectView(R.id.date) @Optional TextView date;
    @InjectView(R.id.sec_type) @Optional TextView securityType;

    protected SecurityCompactDTO securityCompactDTO;
    protected Map<SecurityId, AlertCompactDTO> alerts;
    protected WatchlistPositionDTOList watchlist;

    //<editor-fold desc="Constructors">
    public SecurityItemView(Context context)
    {
        super(context);
    }

    public SecurityItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SecurityItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        init();
    }

    protected void init()
    {
        HierarchyInjector.inject(this);
        ButterKnife.inject(this);
        stockLogo.setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        loadImage();
    }

    @Override protected void onDetachedFromWindow()
    {
        clearHandler();

        if (stockLogo != null)
        {
            stockLogo.setImageDrawable(null);
        }
        if (countryLogo != null)
        {
            countryLogo.setImageDrawable(null);
        }

        super.onDetachedFromWindow();
    }

    protected void clearHandler()
    {
        Handler handler = getHandler();
        if (handler != null)
        {
            handler.removeCallbacks(null);
        }
    }

    public boolean isMyUrlOk()
    {
        return (securityCompactDTO != null) && isUrlOk(securityCompactDTO.imageBlobUrl);
    }

    public static boolean isUrlOk(String url)
    {
        return (url != null) && (!url.isEmpty());
    }

    @Override public void display(final SecurityCompactDTO securityCompactDTO)
    {
        this.securityCompactDTO = securityCompactDTO;

        displayStockName();
        displayExchangeSymbol();
        displayDate();
        displayLastPrice();
        displayStockRoi();
        displayIcon();
        displaySecurityType();
        displayCountryLogo();
        loadImage();
    }

    public void display(@Nullable Map<SecurityId, AlertCompactDTO> alerts,
            @Nullable WatchlistPositionDTOList watchlist)
    {
        this.alerts = alerts;
        this.watchlist = watchlist;
        displayIcon();
    }

    //<editor-fold desc="Display Methods">
    public void display()
    {
        displayStockName();
        displayExchangeSymbol();
        displayDate();
        displayLastPrice();
        displayStockRoi();
        displayIcon();
        displaySecurityType();
        displayCountryLogo();
        loadImage();
    }

    public void displayStockName()
    {
        if (stockName != null)
        {
            if (securityCompactDTO != null)
            {
                stockName.setText(securityCompactDTO.name);
            }
            else
            {
                stockName.setText(R.string.na);
            }
        }
    }

    public void displayExchangeSymbol()
    {
        if (exchangeSymbol != null)
        {
            if (securityCompactDTO != null)
            {
                exchangeSymbol.setText(securityCompactDTO.getExchangeSymbol());
            }
            else
            {
                exchangeSymbol.setText(R.string.na);
            }
            exchangeSymbol.setTextColor(getResources().getColor(R.color.text_primary));
        }
    }

    public void displayDate()
    {
        if (date != null)
        {
            if (securityCompactDTO != null)
            {
                if (securityCompactDTO.lastPriceDateAndTimeUtc != null)
                {
                    date.setText(DateUtils.getFormattedUtcDate(getResources(),
                            securityCompactDTO.lastPriceDateAndTimeUtc));
                }
                if (securityCompactDTO.marketOpen != null)
                {
                    date.setTextColor(getResources().getColor(
                            securityCompactDTO.marketOpen ? R.color.text_primary : R.color.text_secondary));
                }
            }
            else
            {
                date.setText(R.string.na);
            }
        }
    }

    public void displayLastPrice()
    {
        if (lastPrice != null)
        {
            if (securityCompactDTO != null && securityCompactDTO.lastPrice != null && !Double.isNaN(
                    securityCompactDTO.lastPrice))
            {
                THSignedMoney.builder(securityCompactDTO.lastPrice)
                        .signTypeArrow()
                        .withValueColor(R.color.text_primary)
                        .withCurrencyColor(R.color.text_primary)
                        .currency(securityCompactDTO.currencyDisplay)
                        .withDefaultColor()
                        .withOutSign()
                        .boldValue()
                        .build()
                        .into(lastPrice);
            }
            else
            {
                lastPrice.setText(R.string.na);
            }
        }
    }

    public void displayIcon()
    {
        if (marketCloseIcon != null && securityCompactDTO != null && securityCompactDTO.marketOpen != null)
        {
            marketCloseIcon.setImageResource(
                    securityCompactDTO.marketOpen
                            ? R.drawable.icn_market_open
                            : R.drawable.icn_market_closed);
        }
    }

    public void displaySecurityType()
    {
        if (securityType != null)
        {
            if (this.securityCompactDTO != null && this.securityCompactDTO.getSecurityTypeStringResourceId() != null)
            {
                securityType.setText(securityCompactDTO.getSecurityTypeStringResourceId());
            }
            else
            {
                securityType.setText(R.string.na);
            }
        }
    }

    public void displayCountryLogo()
    {
        if (countryLogo != null)
        {
            try
            {
                if (securityCompactDTO != null)
                {
                    countryLogo.setImageResource(securityCompactDTO.getExchangeLogoId());
                }
                else
                {
                    countryLogo.setImageResource(R.drawable.default_image);
                }
            } catch (OutOfMemoryError e)
            {
                Timber.e(e, "");
            }
        }
    }

    private void displayStockRoi()
    {
        if (stockRoi != null)
        {
            if (securityCompactDTO != null && securityCompactDTO.risePercent != null)
            {
                double roi = securityCompactDTO.risePercent;
                THSignedPercentage
                        .builder(roi * 100)
                        .withSign()
                        .relevantDigitCount(3)
                        .withDefaultColor()
                        .defaultColorForBackground()
                        .signTypeArrow()
                        .build()
                        .into(stockRoi);
            }
            else
            {
                stockRoi.setVisibility(View.GONE);
            }
        }
    }
    //</editor-fold>

    private void resetImage()
    {
        if (stockLogo != null)
        {
            stockLogo.setImageBitmap(null);
        }
    }

    public void loadImage()
    {
        resetImage();

        if (isMyUrlOk())
        {
            ImageLoader.getInstance().displayImage(securityCompactDTO.imageBlobUrl, stockLogo);
        }
        else
        {
            loadExchangeImage();
        }
    }

    public void loadExchangeImage()
    {
        if (securityCompactDTO != null && securityCompactDTO.exchange != null)
        {
            try
            {
                Exchange exchange = Exchange.valueOf(securityCompactDTO.exchange);
                stockLogo.setImageResource(exchange.logoId);
            } catch (IllegalArgumentException e)
            {
                Timber.e("Unknown Exchange %s", securityCompactDTO.exchange, e);
                loadDefaultImage();
            }
        }
        else
        {
            loadDefaultImage();
        }
    }

    public void loadDefaultImage()
    {
        if (stockLogo != null)
        {
            stockLogo.setVisibility(View.VISIBLE);
            stockLogo.setImageResource(R.drawable.default_image);
        }
    }
}

package com.tradehero.th.fragments.security;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.compact.WarrantDTO;
import com.tradehero.th.models.security.WarrantDTOFormatter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.inject.Inject;
import timber.log.Timber;

public class WarrantSecurityItemView extends SecurityItemView
{
    @InjectView(R.id.combined_strike_price_type) TextView combinedStrikePriceType;
    @InjectView(R.id.warrant_type) TextView warrantType;
    @InjectView(R.id.expiry_date) TextView expiryDate;

    @Inject protected WarrantDTOFormatter warrantDTOFormatter;

    //<editor-fold desc="Constructors">
    public WarrantSecurityItemView(Context context)
    {
        super(context);
    }

    public WarrantSecurityItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public WarrantSecurityItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        if (combinedStrikePriceType != null)
        {
            combinedStrikePriceType.setSelected(true);
        }
    }

    @Override public void linkWith(SecurityCompactDTO securityCompactDTO, boolean andDisplay)
    {
        super.linkWith(securityCompactDTO, andDisplay);

        if (andDisplay)
        {
            displayCombinedStrikePriceType();
            displayWarrantType();
            displayExpiryDate();
        }
    }

    @Override public void display()
    {
        super.display();
        displayCombinedStrikePriceType();
        displayWarrantType();
        displayExpiryDate();
    }

    public void displayCombinedStrikePriceType()
    {
        if (combinedStrikePriceType != null)
        {
            if (securityCompactDTO instanceof WarrantDTO)
            {
                combinedStrikePriceType.setText(warrantDTOFormatter.getCombinedStrikePriceType(getContext(), (WarrantDTO) securityCompactDTO));
            }
            else
            {
                combinedStrikePriceType.setText(R.string.na);
            }
        }
    }

    public void displayWarrantType()
    {
        if (warrantType != null)
        {
            if (securityCompactDTO instanceof WarrantDTO)
            {
                warrantType.setText(((WarrantDTO) securityCompactDTO).getWarrantType().stringResId);
            }
            else
            {
                warrantType.setText(R.string.na);
            }
        }
    }

    public void displayExpiryDate()
    {
        if (expiryDate != null)
        {
            if (securityCompactDTO instanceof WarrantDTO && ((WarrantDTO) securityCompactDTO).expiryDate != null)
            {
                SimpleDateFormat df = new SimpleDateFormat("d MMM yy", Locale.US);
                expiryDate.setText(df.format(((WarrantDTO) securityCompactDTO).expiryDate));
            }
            else
            {
                expiryDate.setText(R.string.na);
            }
        }
    }

    @Override public void displayLastPrice()
    {
        if (securityCompactDTO == null)
        {
            // Nothing to do
        }
        else if (securityCompactDTO.marketOpen == null)
        {
            Timber.w("displayMarketClose marketOpen is null");
        }
        else if (securityCompactDTO.marketOpen)
        {
            if (marketCloseIcon != null)
            {
                marketCloseIcon.setVisibility(View.GONE);
            }
        }
        else
        {
            if (marketCloseIcon != null)
            {
                marketCloseIcon.setVisibility(View.VISIBLE);
            }
        }
    }
}

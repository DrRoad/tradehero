package com.tradehero.th.models.trade;

import android.content.res.Resources;
import android.widget.TextView;
import com.tradehero.th.R;
import com.tradehero.th.api.trade.TradeDTO;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.utils.THColorUtils;
import com.tradehero.th.models.number.THSignedNumber;
import javax.inject.Inject;

public class TradeDTOUtils
{
    @Inject public TradeDTOUtils()
    {
        super();
    }

    public void setRealizedPLLook(TextView textView, TradeDTO tradeDTO, String refCurrency)
    {
        textView.setText(getRealizedPL(textView.getResources(), tradeDTO, refCurrency));
        textView.setTextColor(textView.getResources().getColor(THColorUtils.getColorResourceIdForNumber(tradeDTO.realizedPLAfterTradeRefCcy)));
    }

    private String getRealizedPL(Resources resources, TradeDTO tradeDTO, String refCurrency)
    {
        if (tradeDTO != null)
        {
            THSignedNumber formattedNumber = THSignedMoney
                    .builder(tradeDTO.realizedPLAfterTradeRefCcy)
                    .withOutSign()
                    .currency(refCurrency)
                    .build();
            return formattedNumber.toString();
        }
        else
        {
            return resources.getString(R.string.na);
        }
    }

    public void setUnrealizedPLLook(TextView textView, TradeDTO tradeDTO, String refCurrency)
    {
        textView.setText(getUnrealizedPL(textView.getResources(), tradeDTO, refCurrency));
        textView.setTextColor(textView.getResources().getColor(THColorUtils.getColorResourceIdForNumber(tradeDTO.realizedPLAfterTradeRefCcy)));
    }

    private String getUnrealizedPL(Resources resources, TradeDTO tradeDTO, String refCurrency)
    {
        if (tradeDTO != null)
        {
            THSignedNumber formattedNumber = THSignedMoney
                    .builder(tradeDTO.realizedPLAfterTradeRefCcy)
                    .withOutSign()
                    .currency(refCurrency)
                    .build();
            return formattedNumber.toString();
        }
        else
        {
            return resources.getString(R.string.na);
        }
    }
}

package com.tradehero.th.fragments.trending.filter;

import android.os.Bundle;
import com.tradehero.th.R;
import com.tradehero.th.api.market.ExchangeDTO;
import com.tradehero.th.api.security.key.TrendingAllSecurityListType;
import com.tradehero.th.api.security.key.TrendingSecurityListType;

/**
 * Created by xavier on 1/15/14.
 */
public class TrendingFilterTypeGenericDTO extends TrendingFilterTypeDTO
{
    public static final String TAG = TrendingFilterTypeGenericDTO.class.getSimpleName();
    public static final int DEFAULT_TITLE_RES_ID = R.string.trending_filter_all_title;
    public static final int DEFAULT_ICON_RES_ID = 0;
    public static final int DEFAULT_DESCRIPTION_RES_ID = R.string.trending_filter_all_description;
    public static final String TRACK_EVENT_SYMBOL = "All Securities";

    //<editor-fold desc="Constructors">
    public TrendingFilterTypeGenericDTO()
    {
        super(
                DEFAULT_TITLE_RES_ID,
                DEFAULT_ICON_RES_ID,
                DEFAULT_DESCRIPTION_RES_ID);
    }

    public TrendingFilterTypeGenericDTO(ExchangeDTO exchangeDTO)
    {
        super(
                DEFAULT_TITLE_RES_ID,
                DEFAULT_ICON_RES_ID,
                DEFAULT_DESCRIPTION_RES_ID,
                exchangeDTO);
    }

    public TrendingFilterTypeGenericDTO(Bundle bundle)
    {
        super(bundle);
    }
    //</editor-fold>

    @Override public TrendingFilterTypeDTO getPrevious()
    {
        return new TrendingFilterTypePriceDTO(exchange);
    }

    @Override public TrendingFilterTypeDTO getNext()
    {
        return new TrendingFilterTypeBasicDTO(exchange);
    }

    @Override public TrendingSecurityListType getSecurityListType(String usableExchangeName, Integer page, Integer perPage)
    {
        return new TrendingAllSecurityListType(usableExchangeName, page, perPage);
    }

    @Override public String getTrackEventCategory()
    {
        return TRACK_EVENT_SYMBOL;
    }

    @Override protected void putParameters(Bundle args)
    {
        super.putParameters(args);
        args.putString(BUNDLE_KEY_CLASS_TYPE, TrendingFilterTypeGenericDTO.class.getName());
    }
}

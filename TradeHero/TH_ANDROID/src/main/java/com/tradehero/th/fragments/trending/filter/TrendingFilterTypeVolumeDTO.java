package com.tradehero.th.fragments.trending.filter;

import android.content.res.Resources;
import com.tradehero.th.R;
import com.tradehero.th.api.security.key.TrendingSecurityListType;
import com.tradehero.th.api.security.key.TrendingVolumeSecurityListType;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTO;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TrendingFilterTypeVolumeDTO extends TrendingFilterTypeDTO
{
    public static final int DEFAULT_TITLE_RES_ID = R.string.trending_filter_volume_title;
    public static final int DEFAULT_ICON_RES_ID = R.drawable.ic_trending_volume;
    public static final int DEFAULT_DESCRIPTION_RES_ID = R.string.trending_filter_volume_description;
    public static final String TRACK_EVENT_SYMBOL = "Unusual Volumes";

    //<editor-fold desc="Constructors">
    public TrendingFilterTypeVolumeDTO(@NonNull Resources resources)
    {
        super(resources,
                DEFAULT_TITLE_RES_ID,
                DEFAULT_ICON_RES_ID,
                DEFAULT_DESCRIPTION_RES_ID);
    }

    public TrendingFilterTypeVolumeDTO(@NonNull ExchangeCompactSpinnerDTO exchangeCompactSpinnerDTO)
    {
        super(
                DEFAULT_TITLE_RES_ID,
                DEFAULT_ICON_RES_ID,
                DEFAULT_DESCRIPTION_RES_ID,
                exchangeCompactSpinnerDTO);
    }
    //</editor-fold>

    @Override @NonNull public TrendingFilterTypeDTO getPrevious()
    {
        return new TrendingFilterTypeBasicDTO(exchange);
    }

    @Override @NonNull public TrendingFilterTypeDTO getNext()
    {
        return new TrendingFilterTypePriceDTO(exchange);
    }

    @Override @NonNull public TrendingSecurityListType getSecurityListType(
            @Nullable String usableExchangeName,
            @Nullable Integer page,
            @Nullable Integer perPage)
    {
        return new TrendingVolumeSecurityListType(usableExchangeName, page, perPage);
    }

    @Override @NonNull public String getTrackEventCategory()
    {
        return TRACK_EVENT_SYMBOL;
    }
}

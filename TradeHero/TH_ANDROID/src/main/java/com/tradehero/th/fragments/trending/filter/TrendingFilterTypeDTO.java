package com.tradehero.th.fragments.trending.filter;

import android.content.res.Resources;
import android.os.Bundle;
import com.tradehero.th.api.security.key.TrendingSecurityListType;
import com.tradehero.th.models.market.ExchangeCompactSpinnerDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class TrendingFilterTypeDTO
{
    public static final String BUNDLE_KEY_CLASS_TYPE = TrendingFilterTypeDTO.class.getName() + ".classType";
    public static final String BUNDLE_KEY_TITLE_RES_ID = TrendingFilterTypeDTO.class.getName() + ".titleResId";
    public static final String BUNDLE_KEY_TITLE_ICON_RES_ID = TrendingFilterTypeDTO.class.getName() + ".iconResId";
    public static final String BUNDLE_KEY_DESCRIPTION_RES_ID = TrendingFilterTypeDTO.class.getName() + ".descriptionResId";
    public static final String BUNDLE_KEY_EXCHANGE = TrendingFilterTypeDTO.class.getName() + ".exchange";

    public final int titleResId;
    public final int titleIconResId;
    public final int descriptionResId;

    @NotNull public ExchangeCompactSpinnerDTO exchange;

    //<editor-fold desc="Constructors">
    public TrendingFilterTypeDTO(@NotNull Resources resources, int titleResId, int titleIconResId, int descriptionResId)
    {
        this.titleResId = titleResId;
        this.titleIconResId = titleIconResId;
        this.descriptionResId = descriptionResId;
        this.exchange = new ExchangeCompactSpinnerDTO(resources);
    }

    public TrendingFilterTypeDTO(int titleResId, int titleIconResId, int descriptionResId,
            @NotNull ExchangeCompactSpinnerDTO exchangeCompactSpinnerDTO)
    {
        this.titleResId = titleResId;
        this.titleIconResId = titleIconResId;
        this.descriptionResId = descriptionResId;
        this.exchange = exchangeCompactSpinnerDTO;
    }

    public TrendingFilterTypeDTO(@NotNull Resources resources, @NotNull Bundle bundle)
    {
        this.titleResId = bundle.getInt(BUNDLE_KEY_TITLE_RES_ID);
        this.titleIconResId = bundle.getInt(BUNDLE_KEY_TITLE_ICON_RES_ID);
        this.descriptionResId = bundle.getInt(BUNDLE_KEY_DESCRIPTION_RES_ID);
        this.exchange = new ExchangeCompactSpinnerDTO(resources, bundle.getBundle(BUNDLE_KEY_EXCHANGE));
    }
    //</editor-fold>

    @NotNull public TrendingSecurityListType getSecurityListType(@Nullable Integer page, @Nullable Integer perPage)
    {
        return getSecurityListType(exchange.getApiName(), page, perPage);
    }

    @NotNull abstract public TrendingFilterTypeDTO getPrevious();
    @NotNull abstract public TrendingFilterTypeDTO getNext();
    @NotNull abstract public TrendingSecurityListType getSecurityListType(@Nullable String usableExchangeName, @Nullable Integer page, @Nullable Integer perPage);
    @NotNull abstract public String getTrackEventCategory();
}

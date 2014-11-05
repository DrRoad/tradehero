package com.tradehero.th.models.market;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.th.R;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.api.market.Exchange;
import com.tradehero.th.api.market.ExchangeCompactDTO;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import timber.log.Timber;

public class ExchangeCompactSpinnerDTO extends ExchangeCompactDTO implements CharSequence
{
    public static final String ALL_EXCHANGES = "allExchanges";

    @NonNull private final Resources resources;
    @Nullable private Drawable flagDrawable;

    public static String getName(@NonNull Resources resources, @NonNull Bundle args)
    {
        return args.getString(BUNDLE_KEY_NAME, resources.getString(R.string.trending_filter_exchange_all));
    }

    //<editor-fold desc="Constructors">
    public ExchangeCompactSpinnerDTO(@NonNull Resources resources)
    {
        super(-1,
                ALL_EXCHANGES,
                Country.NONE.name(),
                0,
                null,
                false,
                true,
                false);
        this.resources = resources;
    }

    public ExchangeCompactSpinnerDTO(@NonNull Resources resources, @NonNull ExchangeCompactDTO exchangeDTO)
    {
        super(exchangeDTO);
        this.resources = resources;
    }

    public ExchangeCompactSpinnerDTO(@NonNull Resources resources, @NonNull Bundle bundle)
    {
        super(bundle);
        this.resources = resources;
        this.name = getName(resources, bundle);
    }
    //</editor-fold>

    @Nullable @JsonIgnore public String getApiName()
    {
        return name.equals(ALL_EXCHANGES) ? null : name;
    }

    @NonNull @JsonIgnore public String getUsableDisplayName()
    {
        return name.equals(ALL_EXCHANGES) ? resources.getString(R.string.trending_filter_exchange_all) : name;
    }

    @Nullable @Override public Exchange getExchangeByName()
    {
        if (name.equals(ALL_EXCHANGES))
        {
            return null;
        }
        return super.getExchangeByName();
    }

    @Override @NonNull public String toString()
    {
        String usableName = getUsableDisplayName();
        if (desc == null)
        {
            return usableName;
        }
        return resources.getString(R.string.trending_filter_exchange_drop_down, usableName, desc);
    }

    //<editor-fold desc="CharSequence">
    @Override public CharSequence subSequence(int start, int end)
    {
        return toString().subSequence(start, end);
    }

    @Override public char charAt(int index)
    {
        return toString().charAt(index);
    }

    @Override public int length()
    {
        return toString().length();
    }
    //</editor-fold>

    @Nullable public Drawable getFlagDrawable()
    {
        if (flagDrawable == null)
        {
            Integer flagResId = getFlagResId();
            if (flagResId != null)
            {
                try
                {
                    flagDrawable = resources.getDrawable(flagResId);
                }
                catch (OutOfMemoryError e)
                {
                    Timber.e(e, "Inflating flag for %s", name);
                }
            }
        }
        return flagDrawable;
    }

    @Override public int hashCode()
    {
        return name.hashCode();
    }

    @Override public boolean equals(Object other)
    {
        return other instanceof ExchangeCompactSpinnerDTO &&
                equals((ExchangeCompactSpinnerDTO) other);
    }

    protected boolean equals(@NonNull ExchangeCompactSpinnerDTO other)
    {
        return name.equals(other.name);
    }
}

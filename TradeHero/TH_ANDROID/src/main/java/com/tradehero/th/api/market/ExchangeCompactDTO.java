package com.tradehero.th.api.market;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.common.persistence.DTO;
import timber.log.Timber;

public class ExchangeCompactDTO implements DTO
{
    private static final String BUNDLE_KEY_ID = ExchangeDTO.class.getName() + ".id";
    public static final String BUNDLE_KEY_NAME = ExchangeDTO.class.getName() + ".name";
    private static final String BUNDLE_KEY_DESC = ExchangeDTO.class.getName() + ".desc";
    private static final String BUNDLE_KEY_IS_INCLUDED_IN_TRENDING = ExchangeDTO.class.getName() + ".isIncludedInTrending";

    public int id;
    @NonNull public String name;
    @NonNull public String countryCode;
    public double sumMarketCap;
    @Nullable public String desc;
    public boolean isInternal;
    public boolean isIncludedInTrending;
    public boolean chartDataSource;

    //<editor-fold desc="Constructors">
    protected ExchangeCompactDTO()
    {
    }

    public ExchangeCompactDTO(
            int id,
            @NonNull String name,
            @NonNull String countryCode,
            double sumMarketCap,
            String desc,
            boolean isInternal,
            boolean isIncludedInTrending,
            boolean chartDataSource)
    {
        super();
        this.id = id;
        this.name = name;
        this.countryCode = countryCode;
        this.sumMarketCap = sumMarketCap;
        this.desc = desc;
        this.isInternal = isInternal;
        this.isIncludedInTrending = isIncludedInTrending;
        this.chartDataSource = chartDataSource;
    }

    public ExchangeCompactDTO(@NonNull ExchangeCompactDTO other)
    {
        super();
        this.id = other.id;
        this.name = other.name;
        this.countryCode = other.countryCode;
        this.sumMarketCap = other.sumMarketCap;
        this.desc = other.desc;
        this.isInternal = other.isInternal;
        this.isIncludedInTrending = other.isIncludedInTrending;
        this.chartDataSource = other.chartDataSource;
    }

    public ExchangeCompactDTO(@NonNull Bundle bundle)
    {
        super();
        this.id = bundle.getInt(BUNDLE_KEY_ID);
        this.name = bundle.getString(BUNDLE_KEY_NAME);
        this.desc = bundle.getString(BUNDLE_KEY_DESC);
        this.isIncludedInTrending = bundle.getBoolean(BUNDLE_KEY_IS_INCLUDED_IN_TRENDING);
    }
    //</editor-fold>

    @JsonIgnore public ExchangeIntegerId getExchangeIntegerId()
    {
        return new ExchangeIntegerId(id);
    }

    @JsonIgnore public ExchangeStringId getExchangeStringId()
    {
        return new ExchangeStringId(name);
    }

    @Nullable @JsonIgnore public Integer getFlagResId()
    {
        Integer fromName = getNameFlagResId();
        if (fromName != null)
        {
            return fromName;
        }
        return getCountryCodeFlagResId();
    }

    @Nullable @JsonIgnore public Integer getNameFlagResId()
    {
        Exchange exchange = getExchangeByName();
        if (exchange != null)
        {
            return exchange.logoId;
        }
        return null;
    }

    @Nullable @JsonIgnore public Exchange getExchangeByName()
    {
        try
        {
            return Exchange.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            Timber.e(e, "Exchange logo does not exist for name %s", name);
        }
        return null;
    }

    @Nullable @JsonIgnore public Integer getCountryCodeFlagResId()
    {
        Country country = getCountry();
        if (country != null)
        {
            return country.logoId;
        }
        return null;
    }

    @Nullable @JsonIgnore public Country getCountry()
    {
        try
        {
            return Country.valueOf(countryCode);
        }
        catch (IllegalArgumentException e)
        {
            Timber.e(e, "No Country for countryCode %s", countryCode);
        }
        return null;
    }

    protected void putParameters(@NonNull Bundle args)
    {
        args.putInt(BUNDLE_KEY_ID, this.id);
        args.putString(BUNDLE_KEY_NAME, this.name);
        args.putString(BUNDLE_KEY_DESC, this.desc);
        args.putBoolean(BUNDLE_KEY_IS_INCLUDED_IN_TRENDING, this.isIncludedInTrending);
    }

    @NonNull public Bundle getArgs()
    {
        Bundle args = new Bundle();
        putParameters(args);
        return args;
    }

    @Override public boolean equals(Object other)
    {
        if (other == null || !(other instanceof ExchangeDTO))
        {
            return false;
        }
        return name.equals(((ExchangeDTO) other).name);
    }

    /** Since equals methods does not use hashCode (rule: 2 objects that equal --> same hashCode) */
    @Override public int hashCode()
    {
        assert false: "hashCode not designed";
        return 42;
    }
}

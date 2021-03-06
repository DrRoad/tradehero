package com.tradehero.th.api.security.key;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TrendingBasicSecurityListType extends TrendingSecurityListType
{
    //<editor-fold desc="Constructors">
    public TrendingBasicSecurityListType(@NonNull TrendingSecurityListType other)
    {
        super(other);
    }

    public TrendingBasicSecurityListType(@Nullable String exchange, @Nullable Integer page, @Nullable Integer perPage)
    {
        super(exchange, page, perPage);
    }

    public TrendingBasicSecurityListType(@Nullable String exchange, @Nullable Integer page)
    {
        super(exchange, page);
    }

    public TrendingBasicSecurityListType(@Nullable String exchange)
    {
        super(exchange);
    }

    public TrendingBasicSecurityListType(@Nullable Integer page, @Nullable Integer perPage)
    {
        super(page, perPage);
    }

    public TrendingBasicSecurityListType(@Nullable Integer page)
    {
        super(page);
    }

    public TrendingBasicSecurityListType()
    {
        super();
    }
    //</editor-fold>

    @Override protected boolean equals(@NonNull TrendingSecurityListType other)
    {
        return super.equals(other)
                && other instanceof TrendingBasicSecurityListType;
    }

    @Override public int compareTo(TrendingSecurityListType another)
    {
        if (another == null)
        {
            return 1;
        }

        if (!TrendingBasicSecurityListType.class.isInstance(another))
        {
            // TODO is it very expensive?
            return TrendingBasicSecurityListType.class.getName().compareTo(((Object) another).getClass().getName());
        }

        return compareTo(TrendingBasicSecurityListType.class.cast(another));
    }

    public int compareTo(TrendingBasicSecurityListType another)
    {
        return super.compareTo(another);
    }

    @Override public String toString()
    {
        return "TrendingBasicSecurityListType{" +
                "exchange='" + exchange + "'" +
                ", page=" + getPage() +
                ", perPage=" + perPage +
                '}';
    }
}

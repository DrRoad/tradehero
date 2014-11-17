package com.tradehero.th.api.position;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.th.api.ExtendedDTO;
import com.tradehero.th.utils.SecurityUtils;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PositionDTOCompact extends ExtendedDTO implements Serializable
{

    public int id;
    public Integer shares;
    public int portfolioId;
    public Double fxRate;

    // This price is always in USD
    public Double averagePriceRefCcy;
    @Nullable public String currencyDisplay;
    @Nullable public String currencyISO;

    //<editor-fold desc="Constructors">
    public PositionDTOCompact()
    {
        super();
    }

    public <ExtendedDTOType extends ExtendedDTO> PositionDTOCompact(ExtendedDTOType other, Class<? extends ExtendedDTO> myClass)
    {
        super(other, myClass);
    }

    public<PositionDTOCompactType extends PositionDTOCompact> PositionDTOCompact(PositionDTOCompactType other,
            Class<? extends PositionDTOCompact> myClass)
    {
        super(other, myClass);
    }
    //</editor-fold>

    @JsonIgnore
    public Boolean isClosed()
    {
        if (shares == null)
        {
            return null;
        }
        return shares == 0;
    }

    @JsonIgnore
    public Boolean isOpen()
    {
        if (shares == null)
        {
            return null;
        }
        return shares != 0;
    }

    @JsonIgnore @NotNull
    public PositionCompactId getPositionCompactId()
    {
        return new PositionCompactId(id);
    }

    @JsonIgnore @NotNull
    public String getNiceCurrency()
    {
        if (currencyDisplay != null && !currencyDisplay.isEmpty())
        {
            return currencyDisplay;
        }
        return SecurityUtils.DEFAULT_VIRTUAL_CASH_CURRENCY_DISPLAY;
    }

    @Contract("null -> null; !null -> !null") @Nullable
    public static List<PositionCompactId> getPositionCompactIds(
            @Nullable List<PositionDTOCompact> positionDTOCompacts)
    {
        if (positionDTOCompacts == null)
        {
            return null;
        }

        List<PositionCompactId> positionCompactIds = new ArrayList<>();
        for (PositionDTOCompact positionDTOCompact: positionDTOCompacts)
        {
            positionCompactIds.add(positionDTOCompact.getPositionCompactId());
        }
        return positionCompactIds;
    }

    @Override public String toString()
    {
        return "PositionDTOCompact{" +
                "id=" + id +
                ", shares=" + shares +
                ", portfolioId=" + portfolioId +
                ", averagePriceRefCcy=" + averagePriceRefCcy +
                ", currencyDisplay=" + currencyDisplay +
                ", currencyISO=" + currencyISO +
                ", extras={" + formatExtras(", ").toString() + "}" +
                '}';
    }

    public static String getShortDouble(double d)
    {
        DecimalFormat df = new DecimalFormat("0.00");
        String db = df.format(d);
        return db;
    }
}

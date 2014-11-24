package com.tradehero.th.api.trade;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.tradehero.common.persistence.DTOKey;
import com.tradehero.th.api.position.OwnedPositionId;

public class OwnedTradeId extends OwnedPositionId implements DTOKey
{
    public final static String BUNDLE_KEY_TRADE_ID = OwnedTradeId.class.getName() + ".tradeId";

    @NonNull public final Integer tradeId;

    //<editor-fold desc="Constructors">
    public OwnedTradeId(int userId, int portfolioId, int positionId, int tradeId)
    {
        super(userId, portfolioId, positionId);
        this.tradeId = tradeId;
    }

    public OwnedTradeId(Bundle args)
    {
        super(args);
        this.tradeId = args.getInt(BUNDLE_KEY_TRADE_ID);
    }
    //</editor-fold>

    public static boolean isOwnedTradeId(@NonNull Bundle args)
    {
        return isOwnedPositionId(args)
                && args.containsKey(BUNDLE_KEY_TRADE_ID);
    }
    
    @Override public int hashCode()
    {
        return super.hashCode() ^ tradeId.hashCode();
    }

    public boolean equals(OwnedTradeId other)
    {
        return (other != null) &&
                super.equals(other) &&
                tradeId.equals(other.tradeId);
    }

    public int compareTo(OwnedTradeId other)
    {
        if (this == other)
        {
            return 0;
        }

        if (other == null)
        {
            return 1;
        }

        int parentComp = super.compareTo(other);
        if (parentComp != 0)
        {
            return parentComp;
        }

        return tradeId.compareTo(other.tradeId);
    }

    @Override protected void putParameters(@NonNull Bundle args)
    {
        super.putParameters(args);
        args.putInt(BUNDLE_KEY_TRADE_ID, tradeId);
    }

    @Override @NonNull public String toString()
    {
        return String.format("[userId=%d; portfolioId=%d; positionId=%d; tradeId=%d]", userId, portfolioId, positionId, tradeId);
    }
}

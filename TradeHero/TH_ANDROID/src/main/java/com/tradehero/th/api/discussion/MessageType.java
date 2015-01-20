package com.tradehero.th.api.discussion;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tradehero.th.R;
import com.tradehero.th.utils.metrics.AnalyticsConstants;

public enum MessageType
{
    PRIVATE(1, AnalyticsConstants.PrivateMessage),
    BROADCAST_FREE_FOLLOWERS(2, R.string.follower_type_free, AnalyticsConstants.BroadcastFreeFollowers),
    BROADCAST_PAID_FOLLOWERS(3, R.string.follower_type_premium, AnalyticsConstants.BroadcastPremiumFollowers),
    BROADCAST_ALL_FOLLOWERS(4, R.string.follower_type_all, AnalyticsConstants.BroadcastAllFollowers);

    public final int typeId;
    @StringRes public final int titleResource;
    public final String localyticsResource;

    //<editor-fold desc="Constructors">
    private MessageType(int typeId, String localyticsResource)
    {
        this.typeId = typeId;
        this.titleResource = 0;
        this.localyticsResource = localyticsResource;
    }

    private MessageType(int typeId, @StringRes int titleResource, String localyticsResource)
    {
        this.typeId = typeId;
        this.titleResource = titleResource;
        this.localyticsResource = localyticsResource;
    }
    //</editor-fold>

    @JsonCreator @NonNull public static MessageType fromId(int id)
    {
        MessageType[] arr = MessageType.values();
        for (MessageType type : arr)
        {
            if (type.typeId == id)
            {
                return type;
            }
        }
        throw new IllegalArgumentException("Unrecognised id " + id);
    }

    public static MessageType[] getShowingTypes()
    {
        MessageType[] r = new MessageType[3];
        r[0] = BROADCAST_PAID_FOLLOWERS;
        r[1] = BROADCAST_FREE_FOLLOWERS;
        r[2] = BROADCAST_ALL_FOLLOWERS;
        return r;
    }

    @Override public String toString()
    {
        switch (this)
        {
            case BROADCAST_PAID_FOLLOWERS:
                return "Premium";
            case BROADCAST_FREE_FOLLOWERS:
                return "Free";
            case BROADCAST_ALL_FOLLOWERS:
                return "All";
        }
        return "";
    }

    @SuppressWarnings("UnusedDeclaration")
    @JsonValue
    final int value()
    {
        return typeId;
    }

}
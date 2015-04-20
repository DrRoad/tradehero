package com.tradehero.th.api.misc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeviceType
{
    Unknown(0),
    IOS(1),
    Android(2),
    ChineseVersion(3),
    ChineseVersionAndroid(6);

    private final int serverValue;

    DeviceType(int serverValue)
    {
        this.serverValue = serverValue;
    }

    public int getServerValue()
    {
        return serverValue;
    }

    @JsonCreator public static DeviceType fromValue(int value)
    {
        for (DeviceType deviceType : values())
        {
            if (deviceType.serverValue == value)
            {
                return deviceType;
            }
        }
        throw new IllegalArgumentException("Value " + value + " does not map to a DeviceType");
    }

    @JsonValue final int value()
    {
        return serverValue;
    }
}

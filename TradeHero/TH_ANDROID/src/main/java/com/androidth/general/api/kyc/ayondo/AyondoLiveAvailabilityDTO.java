package com.androidth.general.api.kyc.ayondo;

import android.support.annotation.NonNull;

import com.androidth.general.api.kyc.LiveAvailabilityDTO;
import com.androidth.general.api.market.Country;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AyondoLiveAvailabilityDTO implements LiveAvailabilityDTO
{
    @JsonProperty("requestorIp") private String requestorIp;
    @JsonProperty("requestorCountry") private Country requestorCountry;
    @JsonProperty("isAvailable") private boolean isAvailable;

    public AyondoLiveAvailabilityDTO()
    {
        super();
    }

    @Override @NonNull public String getRequestorIp()
    {
        return requestorIp;
    }

    @Override @NonNull public Country getRequestorCountry()
    {
        return requestorCountry;
    }

    @Override public boolean isAvailable()
    {
        return isAvailable;
    }
}
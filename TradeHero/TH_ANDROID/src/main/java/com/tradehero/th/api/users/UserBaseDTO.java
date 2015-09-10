package com.tradehero.th.api.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.common.utils.THJsonAdapter;
import com.tradehero.th.api.ExtendedDTO;
import com.tradehero.th.api.market.Country;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

import java.io.IOException;
import java.util.Date;

public class UserBaseDTO extends ExtendedDTO
{
    public int id;
    @Nullable public String picture;
    public String displayName;
    public String displayNamePinYinFirstChar;
    public String school;
    public String firstName;
    public String lastName;
    public Date memberSince;
    public boolean isAdmin;
    public Double roiSinceInception;
    @Nullable public String countryCode;

    public UserBaseDTO()
    {
    }

    @JsonIgnore @NotNull public UserBaseKey getBaseKey()
    {
        return new UserBaseKey(id);
    }

    @JsonIgnore @Nullable public Country getCountry()
    {
        if (countryCode != null)
        {
            try
            {
                return Country.valueOf(countryCode);
            } catch (IllegalArgumentException e)
            {
                Timber.e(e, "Failed to get Country.%s", countryCode);
            }
        }
        return null;
    }

    @Override public int hashCode()
    {
        return Integer.valueOf(id).hashCode();
    }

    @Override public boolean equals(Object other)
    {
        return (other instanceof UserBaseDTO) && Integer.valueOf(id).equals(((UserBaseDTO) other).id);
    }

    @Override public String toString()
    {
        try
        {
            return THJsonAdapter.getInstance().toStringBody(this);
        } catch (IOException e)
        {
            e.printStackTrace();
            return "Failed to json";
        }
    }

    public String getDisplayName()
    {
        if (displayName != null)
        {
            return displayName.replace("　", "").replace(" ","");
        }
        return "";
    }

    public String getShortDisplayName(int length)
    {
        String name = getDisplayName();
        if (name.length() > length) {
            name = name.substring(0, length - 1);
            name = name + "...";
        }

        return name;
    }
}

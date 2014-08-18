package com.tradehero.th.api.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.common.utils.THJsonAdapter;
import com.tradehero.th.api.ExtendedDTO;
import com.tradehero.th.api.market.Country;
import java.io.IOException;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

// TODO remove ExtendedDTO
public class UserBaseDTO extends ExtendedDTO
{
    public int id;
    @Nullable public String picture;
    public String displayName;
    public String firstName;
    public String lastName;
    public Date memberSince;
    public boolean isAdmin;
    public String activeSurveyURL;
    public String activeSurveyImageURL;
    public Double roiSinceInception;
    @Nullable public String countryCode;

    public UserBaseDTO()
    {
    }

    @JsonIgnore @NotNull public UserBaseKey getBaseKey()
    {
        return new UserBaseKey(id);
    }

    @JsonIgnore public boolean isOfficialAccount()
    {
        return getBaseKey().isOfficialAccount();
    }

    @JsonIgnore @Nullable public Country getCountry()
    {
        if (countryCode != null)
        {
            try
            {
                return Country.valueOf(countryCode);
            }
            catch (IllegalArgumentException e)
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "Failed to json";
        }
    }
}

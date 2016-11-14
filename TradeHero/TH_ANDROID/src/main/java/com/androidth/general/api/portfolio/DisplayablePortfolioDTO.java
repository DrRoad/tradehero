package com.androidth.general.api.portfolio;

import android.support.annotation.Nullable;
import com.androidth.general.common.utils.THJsonAdapter;
import com.androidth.general.api.users.UserBaseDTO;
import com.androidth.general.persistence.user.UserProfileCacheRx;
import java.io.IOException;

public class DisplayablePortfolioDTO implements Comparable
{
    @Nullable public OwnedPortfolioId ownedPortfolioId;
    @Nullable public UserBaseDTO userBaseDTO;
    @Nullable public PortfolioDTO portfolioDTO;

    public enum PORTFOLIO_TYPE{
        PORTFOLIOS, COMPETITIONS, WATCHLISTS, OTHERS
    }

    //<editor-fold desc="Constructors">
    public DisplayablePortfolioDTO()
    {
        super();
    }

    public DisplayablePortfolioDTO(@Nullable OwnedPortfolioId ownedPortfolioId)
    {
        this.ownedPortfolioId = ownedPortfolioId;
    }

    public DisplayablePortfolioDTO(
            @Nullable UserBaseDTO userBaseDTO,
            @Nullable PortfolioDTO portfolioDTO)
    {
        if (portfolioDTO != null)
        {
            this.ownedPortfolioId = portfolioDTO.getOwnedPortfolioId();
        }
        this.userBaseDTO = userBaseDTO;
        this.portfolioDTO = portfolioDTO;
    }

    public DisplayablePortfolioDTO(
            @Nullable OwnedPortfolioId ownedPortfolioId,
            @Nullable UserBaseDTO userBaseDTO,
            @Nullable PortfolioDTO portfolioDTO)
    {
        this.ownedPortfolioId = ownedPortfolioId;
        this.userBaseDTO = userBaseDTO;
        this.portfolioDTO = portfolioDTO;
    }
    //</editor-fold>

    public void populate(UserProfileCacheRx userProfileCache)
    {
        this.userBaseDTO = userProfileCache.getCachedValue(this.ownedPortfolioId.getUserBaseKey());
    }

    public boolean isPopulated()
    {
        return this.ownedPortfolioId != null && this.userBaseDTO != null && this.portfolioDTO != null;
    }

    public boolean isValid()
    {
        return isPopulated() &&
                this.userBaseDTO.id == this.ownedPortfolioId.userId &&
                this.portfolioDTO.id == this.ownedPortfolioId.portfolioId;
    }

    @Override public boolean equals(Object other)
    {
        return (other instanceof DisplayablePortfolioDTO) && equals((DisplayablePortfolioDTO) other);
    }

    public boolean equals(DisplayablePortfolioDTO other)
    {
        return (other != null) &&
                (this.ownedPortfolioId == null ? other.ownedPortfolioId == null : this.ownedPortfolioId.equalFields(other.ownedPortfolioId));
    }

    @Override public int hashCode()
    {
        return this.ownedPortfolioId == null ? 0 : this.ownedPortfolioId.hashCode();
    }

    /**
     * In this implementation, the natural order is:
     * a- null first
     * b- all those with null userBaseDTO
     * c- all those with null userBaseDTO.getBaseKey()
     * d- all those with userBaseDTO as current user
     *    da- first the default portfolio
     *    db- then ordered by creation date:
     *       dba- null first
     *       dbb- then older
     *       dbc- then newer
     * e- all those with userBaseDTO as not the current user
     *    ea- ordered by user name natural order
     *       eaa- for each user, ordered by creation date, older first
     * @param other
     * @return
     */
    @Override public int compareTo(@Nullable Object other)
    {
        if (other == null)
        {
            return 1;
        }

        if (other instanceof DisplayablePortfolioDTO)
        {
            return compareTo((DisplayablePortfolioDTO) other);
        }

        return other.getClass().getName().compareTo(DisplayablePortfolioDTO.class.getName());
    }

    public int compareTo(DisplayablePortfolioDTO other)
    {

        if (other == null)
        {
            return 1; // a-
        }

        if (this.ownedPortfolioId == null)
        {
            return other.ownedPortfolioId == null ? 0 : -1;
        }
        if (other.ownedPortfolioId == null)
        {
            return 1;
        }

        return this.ownedPortfolioId.compareTo(other.ownedPortfolioId);
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
        }
        return "Failed to StringBody";
    }
}

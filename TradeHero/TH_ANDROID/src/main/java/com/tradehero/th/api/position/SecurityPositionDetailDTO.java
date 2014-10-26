package com.tradehero.th.api.position;

import com.tradehero.common.persistence.DTO;
import com.tradehero.th.api.competition.ProviderCompactDTOList;
import com.tradehero.th.api.portfolio.OwnedPortfolioIdList;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.UserBaseKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecurityPositionDetailDTO implements DTO
{
    public SecurityCompactDTO security;
    public PositionDTOCompactList positions;
    //public PositionDTOCompact position; // This is a backward compatible element. Do not add back
    @Deprecated public PortfolioDTO portfolio; // Does it always comes back as null
    public ProviderCompactDTOList providers;
    public int firstTradeAllTime;

    public int tradeId;
    public int positionId;

    //<editor-fold desc="Constructors">
    public SecurityPositionDetailDTO()
    {
    }

    public SecurityPositionDetailDTO(SecurityCompactDTO security, PositionDTOCompactList positions,
            PortfolioDTO portfolio, ProviderCompactDTOList providers, int firstTradeAllTime)
    {
        this.security = security;
        this.positions = positions;
        this.portfolio = portfolio;
        this.providers = providers;
        this.firstTradeAllTime = firstTradeAllTime;
    }
    //</editor-fold>

    @Nullable public SecurityId getSecurityId()
    {
        if (security == null)
        {
            return null;
        }
        return security.getSecurityId();
    }

    @Nullable public OwnedPortfolioIdList getProviderAssociatedOwnedPortfolioIds()
    {
        if (providers == null)
        {
            return null;
        }
        return providers.getAssociatedOwnedPortfolioIds();
    }
}

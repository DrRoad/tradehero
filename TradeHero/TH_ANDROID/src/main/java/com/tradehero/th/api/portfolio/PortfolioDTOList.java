package com.tradehero.th.api.portfolio;

import com.tradehero.common.api.BaseArrayList;
import com.tradehero.common.persistence.DTO;
import java.util.Collection;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PortfolioDTOList extends BaseArrayList<PortfolioDTO>
    implements DTO
{
    //<editor-fold desc="Constructors">
    public PortfolioDTOList()
    {
        super();
    }

    public PortfolioDTOList(Collection<? extends PortfolioDTO> c)
    {
        super(c);
    }
    //</editor-fold>

    @Nullable public PortfolioDTO getDefaultPortfolio()
    {
        for (PortfolioDTO portfolioDTO : this)
        {
            if (portfolioDTO.isDefault())
            {
                return portfolioDTO;
            }
        }
        return null;
    }

}

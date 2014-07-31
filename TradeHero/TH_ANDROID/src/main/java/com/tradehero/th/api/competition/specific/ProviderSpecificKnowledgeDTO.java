package com.tradehero.th.api.competition.specific;

import org.jetbrains.annotations.Nullable;

public class ProviderSpecificKnowledgeDTO
{
    @Nullable public Boolean includeProviderPortfolioOnWarrants;
    @Nullable public Boolean hasWizard; //TODO Remove this when SGX is finished

    //<editor-fold desc="Constructors">
    public ProviderSpecificKnowledgeDTO()
    {
        super();
    }
    //</editor-fold>
}

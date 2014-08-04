package com.tradehero.th.models.security;

import com.tradehero.th.api.competition.ProviderCompactDTO;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton public class WarrantSpecificKnowledgeFactory
{
    @NotNull private final Map<ProviderId, OwnedPortfolioId> warrantUsingProviders;
    @NotNull protected final CurrentUserId currentUserId;

    //<editor-fold desc="Constructors">
    @Inject public WarrantSpecificKnowledgeFactory(
            @NotNull CurrentUserId currentUserId)
    {
        super();
        this.currentUserId = currentUserId;
        warrantUsingProviders = new HashMap<>();
    }
    //</editor-fold>

    public void add(@NotNull ProviderCompactDTO providerCompactDTO)
    {
        if (providerCompactDTO.specificKnowledge != null &&
                providerCompactDTO.specificKnowledge.includeProviderPortfolioOnWarrants != null &&
                providerCompactDTO.specificKnowledge.includeProviderPortfolioOnWarrants)
        {
            warrantUsingProviders.put(
                    providerCompactDTO.getProviderId(),
                    providerCompactDTO.getAssociatedOwnedPortfolioId(currentUserId.toUserBaseKey()));
        }
    }

    public void clear()
    {
        warrantUsingProviders.clear();
    }
}

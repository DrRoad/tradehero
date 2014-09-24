package com.tradehero.th.models.provider;

import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderDTOList;
import com.tradehero.th.models.DTOProcessor;
import org.jetbrains.annotations.NotNull;

public class DTOProcessorProviderCompactListReceived
    extends DTOProcessorProviderCompactListReceivedBase<ProviderDTO, ProviderDTOList>
{
    public DTOProcessorProviderCompactListReceived(
            @NotNull DTOProcessor<ProviderDTO> providerCompactProcessor)
    {
        super(providerCompactProcessor);
    }
}

package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.competition.CompetitionPreSeasonDTO;
import com.tradehero.th.api.competition.CompetitionPreseasonShareFormDTO;
import com.tradehero.th.api.competition.HelpVideoDTOList;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderDTOList;
import com.tradehero.th.api.competition.ProviderDisplayCellDTOList;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.competition.ProviderPrizePoolDTO;
import com.tradehero.th.api.competition.key.BasicProviderSecurityListType;
import com.tradehero.th.api.competition.key.HelpVideoListKey;
import com.tradehero.th.api.competition.key.ProviderDisplayCellListKey;
import com.tradehero.th.api.competition.key.ProviderSecurityListType;
import com.tradehero.th.api.competition.key.SearchProviderSecurityListType;
import com.tradehero.th.api.competition.key.WarrantProviderSecurityListType;
import com.tradehero.th.api.competition.key.WarrantUnderlyersProviderSecurityListType;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import com.tradehero.th.api.security.WarrantType;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.models.portfolio.DTOProcessorPortfolioReceived;
import com.tradehero.th.models.provider.DTOProcessorProviderCompactListReceived;
import com.tradehero.th.models.provider.DTOProcessorProviderReceived;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class ProviderServiceWrapper
{
    @NonNull private final ProviderServiceRx providerServiceRx;
    @NonNull private final CurrentUserId currentUserId;

    //<editor-fold desc="Constructors">
    @Inject public ProviderServiceWrapper(
            @NonNull ProviderServiceRx providerServiceRx,
            @NonNull CurrentUserId currentUserId)
    {
        super();
        this.providerServiceRx = providerServiceRx;
        this.currentUserId = currentUserId;
    }
    //</editor-fold>

    //<editor-fold desc="Get Providers">
    @NonNull public Observable<ProviderDTOList> getProvidersRx()
    {
        return this.providerServiceRx.getProviders()
                .map(new DTOProcessorProviderCompactListReceived(
                        new DTOProcessorProviderReceived(currentUserId)));
    }
    //</editor-fold>

    //<editor-fold desc="Get Provider">
    @NonNull public Observable<ProviderPrizePoolDTO> getProviderPrizePoolRx(@NonNull ProviderId providerId)
    {
        return this.providerServiceRx.getProviderPrizePool(providerId.key);
    }

    @NonNull public Observable<ProviderDTO> getProviderRx(@NonNull ProviderId providerId)
    {
        return this.providerServiceRx.getProvider(providerId.key)
                .map(new DTOProcessorProviderReceived(currentUserId));
    }
    //</editor-fold>

    //<editor-fold desc="Get Provider Portfolio">
    @NonNull public Observable<PortfolioDTO> getPortfolio(@NonNull ProviderId providerId)
    {
        return this.providerServiceRx.getPortfolio(providerId.key)
                .map(new DTOProcessorPortfolioReceived<>(currentUserId.toUserBaseKey()));
    }
    //</editor-fold>

    //<editor-fold desc="Get Provider Securities">
    @NonNull public Observable<SecurityCompactDTOList> getProviderSecuritiesRx(@NonNull ProviderSecurityListType key)
    {
        Observable<SecurityCompactDTOList> received;
        if (key instanceof SearchProviderSecurityListType)
        {
            SearchProviderSecurityListType searchKey = (SearchProviderSecurityListType) key;
            received = this.providerServiceRx.searchSecurities(
                    searchKey.providerId.key,
                    searchKey.searchString,
                    searchKey.getPage(),
                    searchKey.perPage);
        }
        else if (key instanceof BasicProviderSecurityListType)
        {
            received = this.providerServiceRx.getSecurities(
                    key.providerId.key,
                    key.getPage(),
                    key.perPage);
        }
        else if (key instanceof WarrantUnderlyersProviderSecurityListType)
        {
            received = this.providerServiceRx.getWarrantUnderlyers(
                    key.providerId.key,
                    key.getPage(),
                    key.perPage);
        }
        else if (key instanceof WarrantProviderSecurityListType)
        {
            WarrantType warrantType = ((WarrantProviderSecurityListType) key).warrantType;
            received = this.providerServiceRx.getProviderWarrants(
                    key.providerId.key,
                    key.getPage(),
                    key.perPage,
                    warrantType != null ? warrantType.shortCode : null);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type " + ((Object) key).getClass().getName());
        }
        return received;
    }
    //</editor-fold>

    //<editor-fold desc="Get Help Videos">
    @NonNull public Observable<HelpVideoDTOList> getHelpVideosRx(@NonNull HelpVideoListKey helpVideoListKey)
    {
        return this.getHelpVideosRx(helpVideoListKey.getProviderId());
    }

    @NonNull public Observable<HelpVideoDTOList> getHelpVideosRx(@NonNull ProviderId providerId)
    {
        return this.providerServiceRx.getHelpVideos(providerId.key);
    }
    //</editor-fold>

    //<editor-fold desc="Get Display Cells">
    @NonNull public Observable<ProviderDisplayCellDTOList> getDisplayCellsRx(@NonNull ProviderDisplayCellListKey providerDisplayCellListKey)
    {
        return this.getDisplayCellsRx(providerDisplayCellListKey.getProviderId());
    }

    @NonNull public Observable<ProviderDisplayCellDTOList> getDisplayCellsRx(@NonNull ProviderId providerId)
    {
        return this.providerServiceRx.getDisplayCells(providerId.key);
    }

    @NonNull public Observable<CompetitionPreSeasonDTO> getPreseasonDetails(@NonNull ProviderId providerId)
    {
        return this.providerServiceRx.getPreseasonDetails(providerId.key);
    }

    @NonNull public Observable<BaseResponseDTO> sharePreSeason(@NonNull CompetitionPreseasonShareFormDTO competitionPreseasonShareFormDTO)
    {
        return this.providerServiceRx.sharePreseason(competitionPreseasonShareFormDTO);
    }
    //</editor-fold>
}

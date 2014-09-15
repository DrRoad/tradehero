package com.tradehero.th.network.service;

import com.tradehero.th.api.competition.key.ProviderSecurityListType;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import com.tradehero.th.api.security.SecurityCompactExtraDTO;
import com.tradehero.th.api.security.SecurityCompactExtraDTOList;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerIdList;
import com.tradehero.th.api.security.TransactionFormDTO;
import com.tradehero.th.api.security.key.SearchSecurityListType;
import com.tradehero.th.api.security.key.SecurityListType;
import com.tradehero.th.api.security.key.TrendingAllSecurityListType;
import com.tradehero.th.api.security.key.TrendingBasicSecurityListType;
import com.tradehero.th.api.security.key.TrendingPriceSecurityListType;
import com.tradehero.th.api.security.key.TrendingSecurityListType;
import com.tradehero.th.api.security.key.TrendingVolumeSecurityListType;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.models.DTOProcessor;
import com.tradehero.th.models.security.DTOProcessorMultiSecurities;
import com.tradehero.th.models.security.DTOProcessorSecurityPositionReceived;
import com.tradehero.th.models.security.DTOProcessorSecurityPositionUpdated;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.persistence.position.SecurityPositionDetailCache;
import com.tradehero.th.persistence.security.SecurityCompactCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import java.util.Map;
import java.util.Timer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.Callback;
import timber.log.Timber;

@Singleton public class SecurityServiceWrapper
{
    @NotNull private final SecurityService securityService;
    @NotNull private final SecurityServiceAsync securityServiceAsync;
    @NotNull private final ProviderServiceWrapper providerServiceWrapper;
    @NotNull private final SecurityPositionDetailCache securityPositionDetailCache;
    @NotNull private final SecurityCompactCache securityCompactCache;
    @NotNull private final UserProfileCache userProfileCache;
    @NotNull private final CurrentUserId currentUserId;

    //<editor-fold desc="Constructors">
    @Inject public SecurityServiceWrapper(
            @NotNull SecurityService securityService,
            @NotNull SecurityServiceAsync securityServiceAsync,
            @NotNull ProviderServiceWrapper providerServiceWrapper,
            @NotNull SecurityPositionDetailCache securityPositionDetailCache,
            @NotNull SecurityCompactCache securityCompactCache,
            @NotNull UserProfileCache userProfileCache,
            @NotNull CurrentUserId currentUserId)
    {
        super();
        this.securityService = securityService;
        this.securityServiceAsync = securityServiceAsync;
        this.providerServiceWrapper = providerServiceWrapper;
        this.securityPositionDetailCache = securityPositionDetailCache;
        this.securityCompactCache = securityCompactCache;
        this.userProfileCache = userProfileCache;
        this.currentUserId = currentUserId;
    }
    //</editor-fold>

    //<editor-fold desc="Get Multiple Securities">
    @NotNull private DTOProcessor<Map<Integer, SecurityCompactDTO>> createMultipleSecurityProcessor()
    {
        return new DTOProcessorMultiSecurities(securityCompactCache);
    }

    public Map<Integer, SecurityCompactDTO> getMultipleSecurities(@NotNull SecurityIntegerIdList ids)
    {
        return createMultipleSecurityProcessor().process(
                securityService.getMultipleSecurities(ids.getCommaSeparated()));
    }

    @NotNull public MiddleCallback<Map<Integer, SecurityCompactDTO>> getMultipleSecurities(
            @NotNull SecurityIntegerIdList ids,
            @Nullable Callback<Map<Integer, SecurityCompactDTO>> callback)
    {
        MiddleCallback<Map<Integer, SecurityCompactDTO>> middleCallback = new
                BaseMiddleCallback<>(callback, createMultipleSecurityProcessor());
        securityServiceAsync.getMultipleSecurities(ids.getCommaSeparated(), middleCallback);

        return middleCallback;
    }
    //</editor-fold>

    public SecurityCompactDTOList processFromExtraData(SecurityCompactExtraDTOList received)
    {
        SecurityCompactDTOList retList = new SecurityCompactDTOList();
        if (received != null)
        {
            for (int i = 0; i < received.size(); i++)
            {
                SecurityCompactDTO security = received.get(i).security;
                security.marketCapRefUSD = received.get(i).marketCapRefUSD;
                security.watchCount = received.get(i).watchCount;
                security.holdCount = received.get(i).holdCount;
                security.searchCount = received.get(i).searchCount;
                retList.add(security);
            }
        }
        return retList;
    }

    //<editor-fold desc="Get Securities">
    public SecurityCompactDTOList getSecurities(@NotNull SecurityListType key)
    {
        SecurityCompactDTOList received;
        if (key instanceof TrendingSecurityListType)
        {
            TrendingSecurityListType trendingKey = (TrendingSecurityListType) key;
            if (trendingKey instanceof TrendingBasicSecurityListType)
            {
                received = this.securityService.getTrendingSecurities(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingPriceSecurityListType)
            {
                received = this.securityService.getTrendingSecuritiesByPrice(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingVolumeSecurityListType)
            {
                received = this.securityService.getTrendingSecuritiesByVolume(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingAllSecurityListType)
            {
                SecurityCompactExtraDTOList data = null;
                if (((TrendingAllSecurityListType) trendingKey).type == TrendingAllSecurityListType.ALL_SECURITY_LIST_TYPE_WATCH)
                {
                    data = this.securityService.getTrendingSecuritiesAllInExchangeWatch(
                            trendingKey.exchange,
                            trendingKey.getPage(),
                            trendingKey.perPage);
                }
                else if (((TrendingAllSecurityListType) trendingKey).type == TrendingAllSecurityListType.ALL_SECURITY_LIST_TYPE_HOLD)
                {
                    data = this.securityService.getTrendingSecuritiesAllInExchangeHold(
                            trendingKey.exchange,
                            trendingKey.getPage(),
                            trendingKey.perPage);
                }
                else if (((TrendingAllSecurityListType) trendingKey).type == TrendingAllSecurityListType.ALL_SECURITY_LIST_TYPE_CHINA_CONCEPT)
                {
                    data = this.securityService.getTrendingSecuritiesAllInExchangeChinaConcept(
                            trendingKey.exchange,
                            trendingKey.getPage(),
                            trendingKey.perPage);
                }
                else if (((TrendingAllSecurityListType) trendingKey).type == TrendingAllSecurityListType.ALL_SECURITY_LIST_TYPE_COMPETITION)
                {
                    SecurityCompactDTOList dataCompetition = this.securityService.getTrendingSecuritiesAllInCompetition(
                            ((TrendingAllSecurityListType) trendingKey).competitionId,
                            trendingKey.getPage(),
                            trendingKey.perPage);
                    return dataCompetition;
                }

                else if (((TrendingAllSecurityListType) trendingKey).type == TrendingAllSecurityListType.ALL_SECURITY_LIST_TYPE_SEARCH)
                {
                    SecurityCompactDTOList dataCompetition = this.securityService.getTrendingSecuritiesAllInCompetitionSearch(
                            ((TrendingAllSecurityListType) trendingKey).competitionId,
                            ((TrendingAllSecurityListType) trendingKey).q,
                            trendingKey.getPage(),
                            trendingKey.perPage);
                    return dataCompetition;
                }
                received = processFromExtraData(data);
            }
            else
            {
                throw new IllegalArgumentException("Unhandled type " + trendingKey.getClass().getName());
            }
        }
        else if (key instanceof SearchSecurityListType)
        {
            SearchSecurityListType searchKey = (SearchSecurityListType) key;
            received = this.securityService.searchSecurities(
                    searchKey.searchString,
                    searchKey.getPage(),
                    searchKey.perPage);
        }
        else if (key instanceof ProviderSecurityListType)
        {
            received = providerServiceWrapper.getProviderSecurities((ProviderSecurityListType) key);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type " + key.getClass().getName());
        }
        return received;
    }

    @NotNull public MiddleCallback<SecurityCompactDTOList> getSecurities(
            @NotNull SecurityListType key,
            @Nullable Callback<SecurityCompactDTOList> callback)
    {
        MiddleCallback<SecurityCompactDTOList> middleCallback = new BaseMiddleCallback<>(callback);
        if (key instanceof TrendingSecurityListType)
        {
            TrendingSecurityListType trendingKey = (TrendingSecurityListType) key;
            if (trendingKey instanceof TrendingBasicSecurityListType)
            {
                this.securityServiceAsync.getTrendingSecurities(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage,
                        middleCallback);
            }
            else if (trendingKey instanceof TrendingPriceSecurityListType)
            {
                this.securityServiceAsync.getTrendingSecuritiesByPrice(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage,
                        middleCallback);
            }
            else if (trendingKey instanceof TrendingVolumeSecurityListType)
            {
                this.securityServiceAsync.getTrendingSecuritiesByVolume(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage,
                        middleCallback);
            }
            else if (trendingKey instanceof TrendingAllSecurityListType)
            {
                this.securityServiceAsync.getTrendingSecuritiesAllInExchange(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage,
                        middleCallback);
            }
            else
            {
                throw new IllegalArgumentException("Unhandled type " + trendingKey.getClass().getName());
            }
        }
        else if (key instanceof SearchSecurityListType)
        {
            SearchSecurityListType searchKey = (SearchSecurityListType) key;
            this.securityServiceAsync.searchSecurities(
                    searchKey.searchString,
                    searchKey.getPage(),
                    searchKey.perPage,
                    middleCallback);
        }
        else if (key instanceof ProviderSecurityListType)
        {
            return providerServiceWrapper.getProviderSecurities((ProviderSecurityListType) key, callback);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type " + key.getClass().getName());
        }
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Get Security">
    @NotNull protected DTOProcessor<SecurityPositionDetailDTO> createSecurityPositionDetailDTOProcessor(@NotNull SecurityId securityId)
    {
        return new DTOProcessorSecurityPositionReceived(securityId, currentUserId);
    }

    public SecurityPositionDetailDTO getSecurity(@NotNull SecurityId securityId)
    {
        return createSecurityPositionDetailDTOProcessor(securityId).process(
                this.securityService.getSecurity(securityId.getExchange(), securityId.getPathSafeSymbol()));
    }

    @NotNull public MiddleCallback<SecurityPositionDetailDTO> getSecurity(
            @NotNull SecurityId securityId,
            @Nullable Callback<SecurityPositionDetailDTO> callback)
    {
        MiddleCallback<SecurityPositionDetailDTO> middleCallback = new BaseMiddleCallback<>(
                callback,
                createSecurityPositionDetailDTOProcessor(securityId));
        this.securityServiceAsync.getSecurity(securityId.getExchange(), securityId.getPathSafeSymbol(), middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Buy Security">
    @NotNull private DTOProcessor<SecurityPositionDetailDTO> createSecurityPositionUpdatedProcessor(@NotNull SecurityId securityId)
    {
        return new DTOProcessorSecurityPositionUpdated(
                securityPositionDetailCache,
                userProfileCache,
                currentUserId,
                securityId);
    }

    public SecurityPositionDetailDTO buy(
            @NotNull SecurityId securityId,
            @NotNull TransactionFormDTO transactionFormDTO)
    {
        return createSecurityPositionUpdatedProcessor(securityId).process(
                this.securityService.buy(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO));
    }

    @NotNull public MiddleCallback<SecurityPositionDetailDTO> buy(
            @NotNull SecurityId securityId,
            @NotNull TransactionFormDTO transactionFormDTO,
            @Nullable Callback<SecurityPositionDetailDTO> callback)
    {
        MiddleCallback<SecurityPositionDetailDTO> middleCallback = new BaseMiddleCallback<>(callback, createSecurityPositionUpdatedProcessor(
                securityId));
        this.securityServiceAsync.buy(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Sell Security">
    public SecurityPositionDetailDTO sell(
            @NotNull SecurityId securityId,
            @NotNull TransactionFormDTO transactionFormDTO)
    {
        return createSecurityPositionUpdatedProcessor(securityId).process(
                this.securityService.sell(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO));
    }

    @NotNull public MiddleCallback<SecurityPositionDetailDTO> sell(
            @NotNull SecurityId securityId,
            @NotNull TransactionFormDTO transactionFormDTO,
            @Nullable Callback<SecurityPositionDetailDTO> callback)
    {
        MiddleCallback<SecurityPositionDetailDTO> middleCallback = new BaseMiddleCallback<>(callback, createSecurityPositionUpdatedProcessor(
                securityId));
        this.securityServiceAsync.sell(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Buy or Sell Security">
    public SecurityPositionDetailDTO doTransaction(
            @NotNull SecurityId securityId,
            @NotNull TransactionFormDTO transactionFormDTO,
            boolean isBuy)
    {
        if (isBuy)
        {
            return buy(securityId, transactionFormDTO);
        }
        return sell(securityId, transactionFormDTO);
    }

    @NotNull public MiddleCallback<SecurityPositionDetailDTO> doTransaction(
            @NotNull SecurityId securityId,
            @NotNull TransactionFormDTO transactionFormDTO,
            boolean isBuy,
            @Nullable Callback<SecurityPositionDetailDTO> callback)
    {
        if (isBuy)
        {
            return buy(securityId, transactionFormDTO, callback);
        }
        return sell(securityId, transactionFormDTO, callback);
    }
    //</editor-fold>
}

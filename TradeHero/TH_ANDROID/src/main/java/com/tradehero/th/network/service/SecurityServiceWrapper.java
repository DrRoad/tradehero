package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.key.ProviderSecurityListType;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.position.SecurityPositionTransactionDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerId;
import com.tradehero.th.api.security.SecurityIntegerIdList;
import com.tradehero.th.api.security.TransactionFormDTO;
import com.tradehero.th.api.security.key.ExchangeSectorSecurityListType;
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
import com.tradehero.th.models.security.DTOProcessorSecurityPositionDetailReceived;
import com.tradehero.th.models.security.DTOProcessorSecurityPositionTransactionUpdated;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.persistence.portfolio.PortfolioCacheRx;
import com.tradehero.th.persistence.position.SecurityPositionDetailCacheRx;
import com.tradehero.th.persistence.security.SecurityCompactCacheRx;
import dagger.Lazy;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit.Callback;
import rx.Observable;

@Singleton public class SecurityServiceWrapper
{
    @NonNull private final SecurityService securityService;
    @NonNull private final SecurityServiceAsync securityServiceAsync;
    @NonNull private final SecurityServiceRx securityServiceRx;
    @NonNull private final ProviderServiceWrapper providerServiceWrapper;
    @NonNull private final Lazy<SecurityCompactCacheRx> securityCompactCache;
    @NonNull private final Lazy<SecurityPositionDetailCacheRx> securityPositionDetailCache;
    @NonNull private final Lazy<PortfolioCacheRx> portfolioCache;
    @NonNull private final CurrentUserId currentUserId;

    //<editor-fold desc="Constructors">
    @Inject public SecurityServiceWrapper(
            @NonNull SecurityService securityService,
            @NonNull SecurityServiceAsync securityServiceAsync,
            @NonNull SecurityServiceRx securityServiceRx,
            @NonNull ProviderServiceWrapper providerServiceWrapper,
            @NonNull Lazy<SecurityCompactCacheRx> securityCompactCache,
            @NonNull Lazy<SecurityPositionDetailCacheRx> securityPositionDetailCache,
            @NonNull Lazy<PortfolioCacheRx> portfolioCache,
            @NonNull CurrentUserId currentUserId)
    {
        super();
        this.securityService = securityService;
        this.securityServiceAsync = securityServiceAsync;
        this.securityServiceRx = securityServiceRx;
        this.providerServiceWrapper = providerServiceWrapper;
        this.securityCompactCache = securityCompactCache;
        this.securityPositionDetailCache = securityPositionDetailCache;
        this.portfolioCache = portfolioCache;
        this.currentUserId = currentUserId;
    }
    //</editor-fold>

    //<editor-fold desc="Get Multiple Securities">
    @NonNull private DTOProcessorMultiSecurities createMultipleSecurityProcessor()
    {
        return new DTOProcessorMultiSecurities(securityCompactCache.get());
    }

    @NonNull public Observable<Map<Integer, SecurityCompactDTO>> getMultipleSecuritiesRx(@NonNull SecurityIntegerIdList ids)
    {
        return securityServiceRx.getMultipleSecurities(ids.getCommaSeparated())
                .map(createMultipleSecurityProcessor());
    }
    //</editor-fold>

    //<editor-fold desc="Get Securities">
    @Deprecated
    public SecurityCompactDTOList getSecurities(@NonNull SecurityListType key)
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
                received =  this.securityService.getTrendingSecuritiesByPrice(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingVolumeSecurityListType)
            {
                received =  this.securityService.getTrendingSecuritiesByVolume(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingAllSecurityListType)
            {
                received =  this.securityService.getTrendingSecuritiesAllInExchange(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else
            {
                throw new IllegalArgumentException("Unhandled type " + ((Object) trendingKey).getClass().getName());
            }
        }
        else if (key instanceof SearchSecurityListType)
        {
            SearchSecurityListType searchKey = (SearchSecurityListType) key;
            received =  this.securityService.searchSecurities(
                    searchKey.searchString,
                    searchKey.getPage(),
                    searchKey.perPage);
        }
        else if (key instanceof ProviderSecurityListType)
        {
            received =  providerServiceWrapper.getProviderSecurities((ProviderSecurityListType) key);
        }
        else if (key instanceof ExchangeSectorSecurityListType)
        {
            ExchangeSectorSecurityListType exchangeKey = (ExchangeSectorSecurityListType) key;
            received = this.securityService.getBySectorAndExchange(
                    exchangeKey.exchangeId == null ? null: exchangeKey.exchangeId.key,
                    exchangeKey.sectorId == null ? null : exchangeKey.sectorId.key,
                    key.page,
                    key.perPage);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type " + ((Object) key).getClass().getName());
        }
        return received;
    }

    @NonNull public Observable<SecurityCompactDTOList> getSecuritiesRx(@NonNull SecurityListType key)
    {
        Observable<SecurityCompactDTOList> received;
        if (key instanceof TrendingSecurityListType)
        {
            TrendingSecurityListType trendingKey = (TrendingSecurityListType) key;
            if (trendingKey instanceof TrendingBasicSecurityListType)
            {
                received = this.securityServiceRx.getTrendingSecurities(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingPriceSecurityListType)
            {
                received =  this.securityServiceRx.getTrendingSecuritiesByPrice(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingVolumeSecurityListType)
            {
                received =  this.securityServiceRx.getTrendingSecuritiesByVolume(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else if (trendingKey instanceof TrendingAllSecurityListType)
            {
                received =  this.securityServiceRx.getTrendingSecuritiesAllInExchange(
                        trendingKey.exchange,
                        trendingKey.getPage(),
                        trendingKey.perPage);
            }
            else
            {
                throw new IllegalArgumentException("Unhandled type " + ((Object) trendingKey).getClass().getName());
            }
        }
        else if (key instanceof SearchSecurityListType)
        {
            SearchSecurityListType searchKey = (SearchSecurityListType) key;
            received =  this.securityServiceRx.searchSecurities(
                    searchKey.searchString,
                    searchKey.getPage(),
                    searchKey.perPage);
        }
        else if (key instanceof ProviderSecurityListType)
        {
            received =  providerServiceWrapper.getProviderSecuritiesRx((ProviderSecurityListType) key);
        }
        else if (key instanceof ExchangeSectorSecurityListType)
        {
            ExchangeSectorSecurityListType exchangeKey = (ExchangeSectorSecurityListType) key;
            received = this.securityServiceRx.getBySectorAndExchange(
                    exchangeKey.exchangeId == null ? null: exchangeKey.exchangeId.key,
                    exchangeKey.sectorId == null ? null : exchangeKey.sectorId.key,
                    key.page,
                    key.perPage);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type " + ((Object) key).getClass().getName());
        }
        return received;
    }
    //</editor-fold>

    //<editor-fold desc="Get Security">
    @NonNull protected DTOProcessor<SecurityPositionDetailDTO> createSecurityPositionDetailDTOProcessor(@NonNull SecurityId securityId)
    {
        return new DTOProcessorSecurityPositionDetailReceived(securityId, currentUserId.toUserBaseKey());
    }

    @NonNull public Observable<SecurityPositionDetailDTO> getSecurityPositionDetailRx(
            @NonNull SecurityId securityId)
    {
        return securityServiceRx.getSecurity(
                securityId.getExchange(),
                securityId.getPathSafeSymbol())
                .map(securityPositionDetailDTO -> {
                    if (securityPositionDetailDTO.providers != null)
                    {
                        for (ProviderDTO providerDTO : securityPositionDetailDTO.providers)
                        {
                            if (providerDTO.associatedPortfolio != null)
                            {
                                providerDTO.associatedPortfolio.userId = currentUserId.get();
                            }
                        }
                    }

                    return securityPositionDetailDTO;
                });
    }

    @NonNull public Observable<SecurityCompactDTO> getSecurityRx(@NonNull SecurityIntegerId securityIntegerId)
    {
        SecurityIntegerIdList list = new SecurityIntegerIdList();
        list.add(securityIntegerId);
        return securityServiceRx.getMultipleSecurities(list.getCommaSeparated())
                .map(map -> map.get(securityIntegerId.key));
    }
    //</editor-fold>

    //<editor-fold desc="Buy Security">
    @NonNull private DTOProcessorSecurityPositionTransactionUpdated createSecurityPositionTransactionUpdatedProcessor(@NonNull SecurityId securityId)
    {
        return new DTOProcessorSecurityPositionTransactionUpdated(
                securityId,
                currentUserId.toUserBaseKey(),
                portfolioCache.get());
    }

    @Deprecated
    public SecurityPositionTransactionDTO buy(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO)
    {
        return createSecurityPositionTransactionUpdatedProcessor(securityId).process(
                this.securityService.buy(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO));
    }

    @Deprecated
    @NonNull public MiddleCallback<SecurityPositionTransactionDTO> buy(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO,
            @Nullable Callback<SecurityPositionTransactionDTO> callback)
    {
        MiddleCallback<SecurityPositionTransactionDTO> middleCallback = new BaseMiddleCallback<>(callback, createSecurityPositionTransactionUpdatedProcessor(
                securityId));
        this.securityServiceAsync.buy(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO, middleCallback);
        return middleCallback;
    }

    @NonNull public Observable<SecurityPositionTransactionDTO> buyRx(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO)
    {
        return this.securityServiceRx.buy(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO)
            .map(createSecurityPositionTransactionUpdatedProcessor(securityId));
    }
    //</editor-fold>

    //<editor-fold desc="Sell Security">
    @Deprecated
    public SecurityPositionTransactionDTO sell(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO)
    {
        return createSecurityPositionTransactionUpdatedProcessor(securityId).process(
                this.securityService.sell(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO));
    }

    @Deprecated
    @NonNull public MiddleCallback<SecurityPositionTransactionDTO> sell(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO,
            @Nullable Callback<SecurityPositionTransactionDTO> callback)
    {
        MiddleCallback<SecurityPositionTransactionDTO> middleCallback = new BaseMiddleCallback<>(callback, createSecurityPositionTransactionUpdatedProcessor(
                securityId));
        this.securityServiceAsync.sell(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO, middleCallback);
        return middleCallback;
    }

    @NonNull public Observable<SecurityPositionTransactionDTO> sellRx(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO)
    {
        return this.securityServiceRx.sell(securityId.getExchange(), securityId.getSecuritySymbol(), transactionFormDTO)
                .map(createSecurityPositionTransactionUpdatedProcessor(securityId));
    }
    //</editor-fold>

    //<editor-fold desc="Buy or Sell Security">
    @NonNull public Observable<SecurityPositionTransactionDTO> doTransactionRx(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO,
            boolean isBuy)
    {
        if (isBuy)
        {
            return buyRx(securityId, transactionFormDTO);
        }
        return sellRx(securityId, transactionFormDTO);
    }

    @Deprecated
    @NonNull public MiddleCallback<SecurityPositionTransactionDTO> doTransaction(
            @NonNull SecurityId securityId,
            @NonNull TransactionFormDTO transactionFormDTO,
            boolean isBuy,
            @Nullable Callback<SecurityPositionTransactionDTO> callback)
    {
        if (isBuy)
        {
            return buy(securityId, transactionFormDTO, callback);
        }
        return sell(securityId, transactionFormDTO, callback);
    }
    //</editor-fold>
}

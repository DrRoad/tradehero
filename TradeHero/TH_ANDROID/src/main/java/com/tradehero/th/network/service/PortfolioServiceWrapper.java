package com.tradehero.th.network.service;

import com.tradehero.common.billing.googleplay.GooglePlayPurchaseDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.models.user.MiddleCallbackAddCash;
import com.tradehero.th.models.user.MiddleCallbackUpdateUserProfile;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit.Callback;

@Singleton public class PortfolioServiceWrapper
{
    private final PortfolioService portfolioService;
    private final PortfolioServiceAsync portfolioServiceAsync;

    @Inject public PortfolioServiceWrapper(
            PortfolioService portfolioService,
            PortfolioServiceAsync portfolioServiceAsync)
    {
        super();
        this.portfolioService = portfolioService;
        this.portfolioServiceAsync = portfolioServiceAsync;
    }

    private void basicCheck(OwnedPortfolioId ownedPortfolioId)
    {
        if (ownedPortfolioId == null)
        {
            throw new NullPointerException("ownedPortfolioId cannot be null");
        }
        if (ownedPortfolioId.userId == null)
        {
            throw new NullPointerException("ownedPortfolioId.userId cannot be null");
        }
        if (ownedPortfolioId.portfolioId == null)
        {
            throw new NullPointerException("ownedPortfolioId.portfolioId cannot be null");
        }
    }

    //<editor-fold desc="Get One User Portfolio">
    public PortfolioDTO getPortfolio(OwnedPortfolioId ownedPortfolioId)
    {
        basicCheck(ownedPortfolioId);
        return this.portfolioService.getPortfolio(ownedPortfolioId.userId, ownedPortfolioId.portfolioId);
    }

    public BaseMiddleCallback<PortfolioDTO> getPortfolio(OwnedPortfolioId ownedPortfolioId, Callback<PortfolioDTO> callback)
    {
        basicCheck(ownedPortfolioId);
        BaseMiddleCallback<PortfolioDTO> middleCallback = new BaseMiddleCallback<PortfolioDTO>(callback);
        this.portfolioServiceAsync.getPortfolio(ownedPortfolioId.userId, ownedPortfolioId.portfolioId, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Reset Cash">
    public UserProfileDTO resetPortfolio(OwnedPortfolioId ownedPortfolioId, GooglePlayPurchaseDTO purchaseDTO)
    {
        basicCheck(ownedPortfolioId);
        return this.portfolioService.resetPortfolio(ownedPortfolioId.userId, ownedPortfolioId.portfolioId, purchaseDTO);
    }

    public MiddleCallbackUpdateUserProfile resetPortfolio(OwnedPortfolioId ownedPortfolioId, GooglePlayPurchaseDTO purchaseDTO, Callback<UserProfileDTO> callback)
    {
        MiddleCallbackUpdateUserProfile middleCallback = new MiddleCallbackUpdateUserProfile(callback);
        basicCheck(ownedPortfolioId);
        this.portfolioServiceAsync.resetPortfolio(ownedPortfolioId.userId, ownedPortfolioId.portfolioId, purchaseDTO, middleCallback);
        return middleCallback;
    }
    //</editor-fold>

    //<editor-fold desc="Add Cash">
    public UserProfileDTO addCash(OwnedPortfolioId ownedPortfolioId, GooglePlayPurchaseDTO purchaseDTO)
    {
        basicCheck(ownedPortfolioId);
        return this.portfolioService.addCash(ownedPortfolioId.userId, ownedPortfolioId.portfolioId, purchaseDTO);
    }

    public MiddleCallbackAddCash addCash(OwnedPortfolioId ownedPortfolioId, GooglePlayPurchaseDTO purchaseDTO, Callback<UserProfileDTO> callback)
    {
        basicCheck(ownedPortfolioId);
        MiddleCallbackAddCash middleCallbackAddCash = new MiddleCallbackAddCash(ownedPortfolioId, callback);
        this.portfolioServiceAsync.addCash(ownedPortfolioId.userId, ownedPortfolioId.portfolioId, purchaseDTO, middleCallbackAddCash);
        return middleCallbackAddCash;
    }
    //</editor-fold>

    //<editor-fold desc="Mark One User Portfolio">
    public PortfolioDTO markPortfolio(OwnedPortfolioId ownedPortfolioId)
    {
        basicCheck(ownedPortfolioId);
        return this.portfolioService.markPortfolio(ownedPortfolioId.userId, ownedPortfolioId.portfolioId);
    }

    public BaseMiddleCallback<PortfolioDTO> markPortfolio(OwnedPortfolioId ownedPortfolioId, Callback<PortfolioDTO> callback)
    {
        BaseMiddleCallback<PortfolioDTO> middleCallback = new BaseMiddleCallback<PortfolioDTO>(callback);
        basicCheck(ownedPortfolioId);
        this.portfolioServiceAsync.markPortfolio(ownedPortfolioId.userId, ownedPortfolioId.portfolioId, middleCallback);
        return middleCallback;
    }
    //</editor-fold>
}

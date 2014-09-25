package com.tradehero.th.fragments.billing;

import android.os.Bundle;
import android.view.View;
import com.tradehero.common.billing.exception.BillingException;
import com.tradehero.common.billing.request.UIBillingRequest;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.portfolio.PortfolioCompactDTOList;
import com.tradehero.th.api.portfolio.PortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.THBasePurchaseActionInteractor;
import com.tradehero.th.billing.THBillingInteractor;
import com.tradehero.th.billing.THPurchaseActionInteractor;
import com.tradehero.th.billing.request.THUIBillingRequest;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.chinabuild.cache.PortfolioCompactNewCache;
import com.tradehero.th.fragments.social.hero.HeroAlertDialogUtil;
import com.tradehero.th.models.user.PremiumFollowUserAssistant;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCache;
import com.tradehero.th.persistence.system.SystemStatusCache;
import javax.inject.Inject;
import javax.inject.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.Callback;
import timber.log.Timber;

abstract public class BasePurchaseManagerFragment extends DashboardFragment
{
    private static final String BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE = BasePurchaseManagerFragment.class.getName() + ".purchaseApplicablePortfolioId";
    public static final String BUNDLE_KEY_THINTENT_BUNDLE = BasePurchaseManagerFragment.class.getName() + ".thIntent";

    protected OwnedPortfolioId purchaseApplicableOwnedPortfolioId;
    private DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList> portfolioCompactListFetchListener;
    private DTOCacheNew.Listener<PortfolioId, PortfolioCompactDTO> portfolioCompactNewFetchListener;
    protected THPurchaseActionInteractor thPurchaseActionInteractor;

    @Inject protected CurrentUserId currentUserId;
    @Inject protected HeroAlertDialogUtil heroAlertDialogUtil;
    @Inject protected Provider<THUIBillingRequest> uiBillingRequestProvider;
    @Inject protected PortfolioCompactListCache portfolioCompactListCache;
    @Inject protected PortfolioCompactNewCache portfolioCompactNewCache;
    @Inject protected THBillingInteractor userInteractor;
    @Inject SystemStatusCache systemStatusCache;


    public static void putApplicablePortfolioId(@NotNull Bundle args, @NotNull OwnedPortfolioId ownedPortfolioId)
    {
        args.putBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE, ownedPortfolioId.getArgs());
    }

    public static OwnedPortfolioId getApplicablePortfolioId(@Nullable Bundle args)
    {
        if (args != null)
        {
            if (args.containsKey(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE))
            {
                return new OwnedPortfolioId(args.getBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE));
            }
        }
        return null;
    }

    abstract protected void initViews(View view);

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        portfolioCompactListFetchListener = createPortfolioCompactListFetchListener();
        portfolioCompactNewFetchListener = createPortfolioCompactNewFetchListener();
    }

    @Override public void onResume()
    {
        super.onResume();

        if(getCompetitionID()==0)
        {
            fetchPortfolioCompactList();
        }
        else//如果是非0则说明是 比赛相关的 股票详情页
        {
            fetchPortfolioCompactNew();
        }

    }

    @Override public void onStop()
    {
        detachPortfolioCompactListCache();
        detachPurchaseActionInteractor();
        super.onStop();
    }

    @Override public void onDestroy()
    {
        portfolioCompactListFetchListener = null;
        super.onDestroy();
    }

    private void detachPortfolioCompactListCache()
    {
        portfolioCompactListCache.unregister(portfolioCompactListFetchListener);
    }

    private void detachPortfolioCompactNewCache()
    {
        portfolioCompactNewCache.unregister(portfolioCompactNewFetchListener);
    }

    private void detachPurchaseActionInteractor()
    {
        if (thPurchaseActionInteractor != null)
        {
            thPurchaseActionInteractor.onDestroy();
        }
        thPurchaseActionInteractor = null;
    }

    private void fetchPortfolioCompactList()
    {
        detachPortfolioCompactListCache();
        portfolioCompactListCache.register(currentUserId.toUserBaseKey(), portfolioCompactListFetchListener);
        portfolioCompactListCache.getOrFetchAsync(currentUserId.toUserBaseKey());
    }

    public int getCompetitionID()
    {
        return 0;
    }

    private void fetchPortfolioCompactNew()
    {
        detachPortfolioCompactNewCache();
        PortfolioId key = new PortfolioId(getCompetitionID());
        portfolioCompactNewCache.register(key, portfolioCompactNewFetchListener);
        portfolioCompactNewCache.getOrFetchAsync(key);
    }

    protected void prepareApplicableOwnedPortolioId(@Nullable PortfolioCompactDTO defaultIfNotInArgs)
    {
        Bundle args = getArguments();
        OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId(args);

        if (applicablePortfolioId == null && defaultIfNotInArgs != null)
        {
            applicablePortfolioId = defaultIfNotInArgs.getOwnedPortfolioId();
        }

        if (applicablePortfolioId == null)
        {
            Timber.e(new NullPointerException(), "Null applicablePortfolio");
        }
        else
        {
            linkWithApplicable(applicablePortfolioId, true);
        }
    }

    protected void linkWithApplicable(OwnedPortfolioId purchaseApplicablePortfolioId, boolean andDisplay)
    {
        this.purchaseApplicableOwnedPortfolioId = purchaseApplicablePortfolioId;
        if (andDisplay)
        {
        }
    }

    @Nullable public OwnedPortfolioId getApplicablePortfolioId()
    {
        return purchaseApplicableOwnedPortfolioId;
    }

    protected THBasePurchaseActionInteractor.Builder createPurchaseActionInteractorBuilder()
    {
        return THBasePurchaseActionInteractor.builder()
                .setBillingInteractor(userInteractor)
                .setPurchaseApplicableOwnedPortfolioId(purchaseApplicableOwnedPortfolioId)
                .setBillingRequest(uiBillingRequestProvider.get())
                .startWithProgressDialog(true) // true by default
                .popIfBillingNotAvailable(true)  // true by default
                .popIfProductIdentifierFetchFailed(true) // true by default
                .popIfInventoryFetchFailed(true) // true by default
                .popIfPurchaseFailed(true) // true by default
                .setPremiumFollowedListener(createPremiumUserFollowedListener())
                .error(new UIBillingRequest.OnErrorListener()
                {
                    @Override public void onError(int requestCode, BillingException billingException)
                    {
                        Timber.e(billingException, "Store had error");
                    }
                });
    }

    // region Following action
    // should call this method where the action takes place
    @Deprecated
    protected final void premiumFollowUser(@NotNull UserBaseKey heroId)
    {
        detachPurchaseActionInteractor();
        thPurchaseActionInteractor = createPurchaseActionInteractorBuilder()
                .setUserToFollow(heroId)
                .setPurchaseApplicableOwnedPortfolioId(purchaseApplicableOwnedPortfolioId)
                .build();

        thPurchaseActionInteractor.premiumFollowUser();
    }

    // should call it where the action takes place
    @Deprecated
    protected final void unfollowUser(@NotNull UserBaseKey heroId)
    {
        detachPurchaseActionInteractor();
        thPurchaseActionInteractor = createPurchaseActionInteractorBuilder()
                .setUserToFollow(heroId)
                .setPurchaseApplicableOwnedPortfolioId(purchaseApplicableOwnedPortfolioId)
                .build();
        thPurchaseActionInteractor.unfollowUser();
    }
    //endregion

    protected DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList> createPortfolioCompactListFetchListener()
    {
        return new BasePurchaseManagementPortfolioCompactListFetchListener();
    }

    protected class BasePurchaseManagementPortfolioCompactListFetchListener implements DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList>
    {
        protected BasePurchaseManagementPortfolioCompactListFetchListener()
        {
            // no unexpected creation
        }

        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull PortfolioCompactDTOList value)
        {
            prepareApplicableOwnedPortolioId(value.getDefaultPortfolio());
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            THToast.show(R.string.error_fetch_portfolio_list_info);
        }
    }

    protected DTOCacheNew.Listener<PortfolioId, PortfolioCompactDTO> createPortfolioCompactNewFetchListener()
    {
        return new BasePurchaseManagementPortfolioCompactNewFetchListener();
    }

    protected class BasePurchaseManagementPortfolioCompactNewFetchListener implements DTOCacheNew.Listener<PortfolioId, PortfolioCompactDTO>
    {
        protected BasePurchaseManagementPortfolioCompactNewFetchListener()
        {
            // no unexpected creation
        }

        @Override public void onDTOReceived(@NotNull PortfolioId key, @NotNull PortfolioCompactDTO value)
        {
            prepareApplicableOwnedPortolioId(value);
        }

        @Override public void onErrorThrown(@NotNull PortfolioId key, @NotNull Throwable error)
        {
            THToast.show(R.string.error_fetch_portfolio_list_info);
        }
    }

    //region Creation and Listener
    @Deprecated
    protected Callback<UserProfileDTO> createFreeUserFollowedCallback()
    {
        // default will be used when this one return null
        return null;
    }

    protected PremiumFollowUserAssistant.OnUserFollowedListener createPremiumUserFollowedListener()
    {
        // default will be used when this one return null
        return null;
    }
    //endregion



}

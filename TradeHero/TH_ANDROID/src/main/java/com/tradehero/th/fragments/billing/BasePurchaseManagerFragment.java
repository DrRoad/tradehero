package com.tradehero.th.fragments.billing;

import android.os.Bundle;
import android.view.View;
import com.tradehero.common.milestone.Milestone;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.billing.googleplay.THIABUserInteractor;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCache;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListRetrievedMilestone;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * It expects its Activity to implement THIABInteractor.
 * Created with IntelliJ IDEA. User: xavier Date: 11/11/13 Time: 11:05 AM To change this template use File | Settings | File Templates. */
abstract public class BasePurchaseManagerFragment extends DashboardFragment
{
    public static final String BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE = BasePurchaseManagerFragment.class.getName() + ".purchaseApplicablePortfolioId";
    public static final String BUNDLE_KEY_THINTENT_BUNDLE = BasePurchaseManagerFragment.class.getName() + ".thIntent";

    protected THIABUserInteractor userInteractor;
    @Inject protected CurrentUserId currentUserId;
    @Inject protected PortfolioCompactListCache portfolioCompactListCache;
    private PortfolioCompactListRetrievedMilestone portfolioCompactListRetrievedMilestone;
    private Milestone.OnCompleteListener portfolioCompactListRetrievedListener;

    protected OwnedPortfolioId purchaseApplicableOwnedPortfolioId;

    abstract protected void initViews(View view);

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        portfolioCompactListRetrievedListener = new BasePurchaseManagementPortfolioCompactListRetrievedListener();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        createUserInteractor();
    }

    /**
     * You are encouraged to override this method to specify your own UserInteractor.
     */
    protected void createUserInteractor()
    {
        userInteractor = new THIABUserInteractor();
    }

    @Override public void onResume()
    {
        super.onResume();
        prepareApplicableOwnedPortolioId();

        Bundle args = getArguments();
        if (args != null)
        {
            Bundle thIntentBundle = args.getBundle(BUNDLE_KEY_THINTENT_BUNDLE);
            if (thIntentBundle != null)
            {
                int action = thIntentBundle.getInt(THIABUserInteractor.BUNDLE_KEY_ACTION);
                if (action > 0)
                {
                    userInteractor.doAction(action); // TODO place the action after portfolio has been set
                }
                args.remove(BUNDLE_KEY_THINTENT_BUNDLE);
            }
        }
    }

    @Override public void onPause()
    {
        userInteractor.onPause();
        super.onPause();
    }

    @Override public void onStop()
    {
        userInteractor.onStop();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        if (userInteractor != null)
        {
            userInteractor.onDestroy();
        }
        userInteractor = null;

        detachPortfolioRetrievedMilestone();

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        portfolioCompactListRetrievedListener = null;
        super.onDestroy();
    }

    protected void prepareApplicableOwnedPortolioId()
    {
        OwnedPortfolioId applicablePortfolioId = null;

        Bundle args = getArguments();
        if (args != null)
        {
            Bundle portfolioIdBundle = args.getBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE);
            if (portfolioIdBundle != null)
            {
                applicablePortfolioId = new OwnedPortfolioId(portfolioIdBundle);
            }
        }

        if (applicablePortfolioId == null)
        {
            applicablePortfolioId = new OwnedPortfolioId(currentUserId.get(), null);
        }
        if (applicablePortfolioId.userId == null)
        {
            applicablePortfolioId = new OwnedPortfolioId(currentUserId.get(), applicablePortfolioId.portfolioId);
        }
        if (applicablePortfolioId.portfolioId == null)
        {
            final OwnedPortfolioId ownedPortfolioId = portfolioCompactListCache.getDefaultPortfolio(applicablePortfolioId.getUserBaseKey());
            if (ownedPortfolioId != null && ownedPortfolioId.portfolioId != null)
            {
                applicablePortfolioId = ownedPortfolioId;
            }
            else
            {
                // This situation will be handled by the milestone
            }
        }

        Timber.d("purchase applicablePortfolio %s", applicablePortfolioId);
        if (applicablePortfolioId.portfolioId == null)
        {
            // At this stage, portfolioId is still null, we need to wait for the fetch
            waitForPortfolioCompactListFetched(applicablePortfolioId.getUserBaseKey());
        }
        else
        {
            linkWithApplicable(applicablePortfolioId, true);
        }
    }

    protected void linkWithApplicable(OwnedPortfolioId purchaseApplicablePortfolioId, boolean andDisplay)
    {
        this.purchaseApplicableOwnedPortfolioId = purchaseApplicablePortfolioId;
        userInteractor.setApplicablePortfolioId(purchaseApplicablePortfolioId);
        if (andDisplay)
        {
        }
    }

    private void detachPortfolioRetrievedMilestone()
    {
        if (portfolioCompactListRetrievedMilestone != null)
        {
            portfolioCompactListRetrievedMilestone.setOnCompleteListener(null);
        }
        portfolioCompactListRetrievedListener = null;
    }


    private void waitForPortfolioCompactListFetched(UserBaseKey userBaseKey)
    {
        detachPortfolioRetrievedMilestone();
        portfolioCompactListRetrievedMilestone = new PortfolioCompactListRetrievedMilestone(userBaseKey);
        portfolioCompactListRetrievedMilestone.setOnCompleteListener(portfolioCompactListRetrievedListener);
        portfolioCompactListRetrievedMilestone.launch();
    }

    public OwnedPortfolioId getApplicablePortfolioId()
    {
        return purchaseApplicableOwnedPortfolioId;
    }

    protected class BasePurchaseManagementPortfolioCompactListRetrievedListener implements Milestone.OnCompleteListener
    {
        @Override public void onComplete(Milestone milestone)
        {
            prepareApplicableOwnedPortolioId();
        }

        @Override public void onFailed(Milestone milestone, Throwable throwable)
        {
            THToast.show(R.string.error_fetch_portfolio_list_info);
            Timber.e(throwable, "Failed to download portfolio compacts");
        }
    }
}

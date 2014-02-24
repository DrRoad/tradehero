package com.tradehero.th.fragments.billing;

import android.os.Bundle;
import android.view.View;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.billing.googleplay.THIABLogicHolder;
import com.tradehero.th.billing.googleplay.THIABUserInteractor;
import com.tradehero.th.fragments.base.DashboardFragment;
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
    @Inject protected THIABLogicHolder billingActor;

    protected OwnedPortfolioId purchaseApplicableOwnedPortfolioId;

    abstract protected void initViews(View view);

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
        OwnedPortfolioId applicablePortfolioId = null;

        Bundle args = getArguments();
        if (args != null)
        {
            Bundle portfolioIdBundle = args.getBundle(BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE);
            if (portfolioIdBundle != null)
            {
                applicablePortfolioId = new OwnedPortfolioId(portfolioIdBundle);
            }

            Bundle thIntentBundle = args.getBundle(BUNDLE_KEY_THINTENT_BUNDLE);
            if (thIntentBundle != null)
            {
                int action = thIntentBundle.getInt(THIABUserInteractor.BUNDLE_KEY_ACTION);
                if (action > 0)
                {
                    userInteractor.doAction(action);
                }
                args.remove(BUNDLE_KEY_THINTENT_BUNDLE);
            }
        }

        Timber.d("purchase applicablePortfolio %s", applicablePortfolioId);
        userInteractor.setApplicablePortfolioId(applicablePortfolioId);
    }

    @Override public void onPause()
    {
        userInteractor.onPause();
        super.onPause();
    }

    @Override public void onDestroyView()
    {
        if (userInteractor != null)
        {
            userInteractor.onDestroy();
        }
        userInteractor = null;

        super.onDestroyView();
    }

    public OwnedPortfolioId getApplicablePortfolioId()
    {
        return userInteractor.getApplicablePortfolioId();
    }
}

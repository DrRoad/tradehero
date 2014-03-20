package com.tradehero.th.billing.googleplay;

import com.tradehero.common.billing.googleplay.IABSKUListKey;
import com.tradehero.common.milestone.BaseMilestoneGroup;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListRetrievedMilestone;
import com.tradehero.th.utils.DaggerUtils;
import java.util.List;
import javax.inject.Inject;

/** Created with IntelliJ IDEA. User: xavier Date: 11/26/13 Time: 12:18 PM To change this template use File | Settings | File Templates. */
public class PurchaseRestorerRequiredMilestone extends BaseMilestoneGroup
{
    public static final String TAG = PurchaseRestorerRequiredMilestone.class.getSimpleName();
    private static final int POSITION_FETCH_INVENTORY = 0;
    private static final int POSITION_FETCH_PURCHASE = 1;
    private static final int POSITION_FETCH_PORTFOLIO = 2;

    @Inject protected CurrentUserId currentUserId;

    public PurchaseRestorerRequiredMilestone(THIABLogicHolder logicHolder)
    {
        super();
        DaggerUtils.inject(this);
        add(new THIABInventoryFetchMilestone(IABSKUListKey.getInApp()));
        add(new THIABPurchaseFetchMilestone(logicHolder));
        add(new PortfolioCompactListRetrievedMilestone(currentUserId.toUserBaseKey()));
    }

    public List<THIABPurchase> getFetchedPurchases()
    {
        return ((THIABPurchaseFetchMilestone) milestones.get(POSITION_FETCH_PURCHASE)).getFetchedPurchases();
    }
}

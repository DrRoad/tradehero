package com.tradehero.th.fragments.billing.store;

import android.util.Pair;
import com.tradehero.th.R;
import com.tradehero.th.api.system.SystemStatusDTO;
import com.tradehero.th.api.system.SystemStatusKey;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.fragments.alert.AlertManagerFragment;
import com.tradehero.th.fragments.social.follower.FollowerRevenueReportFragment;
import com.tradehero.th.fragments.social.hero.HeroManagerFragment;
import com.tradehero.th.persistence.system.SystemStatusCache;
import javax.inject.Inject;
import android.support.annotation.NonNull;
import rx.Observable;
import rx.functions.Func1;

public class StoreItemFactory
{
    public static final boolean WITH_IGNORE_SYSTEM_STATUS = true;
    public static final boolean WITH_FOLLOW_SYSTEM_STATUS = false;

    public static final boolean WITH_INCLUDE_ALERTS = true;
    public static final boolean WITH_EXCLUDE_ALERTS = false;

    @NonNull private final SystemStatusCache systemStatusCache;

    //<editor-fold desc="Constructors">
    @Inject public StoreItemFactory(@NonNull SystemStatusCache systemStatusCache)
    {
        this.systemStatusCache = systemStatusCache;
    }
    //</editor-fold>

    @NonNull public Observable<StoreItemDTOList> createAll(final boolean ignoreSystemStatus)
    {
        return systemStatusCache.get(new SystemStatusKey())
                .map(new Func1<Pair<SystemStatusKey, SystemStatusDTO>, StoreItemDTOList>()
                {
                    @Override public StoreItemDTOList call(Pair<SystemStatusKey, SystemStatusDTO> pair)
                    {
                        return createList(ignoreSystemStatus
                                || pair.second == null
                                || !pair.second.alertsAreFree);
                    }
                });
    }

    @NonNull protected StoreItemDTOList createList(boolean includeAlerts)
    {
        StoreItemDTOList created = new StoreItemDTOList();

        created.add(new StoreItemTitleDTO(R.string.store_header_in_app_purchases));
        created.add(new StoreItemPromptPurchaseDTO(
                R.string.store_buy_virtual_dollars,
                R.drawable.icn_th_dollars,
                R.drawable.btn_buy_thd_large,
                ProductIdentifierDomain.DOMAIN_VIRTUAL_DOLLAR));
        created.add(new StoreItemPromptPurchaseDTO(
                R.string.store_buy_follow_credits,
                R.drawable.icn_follow_credits,
                R.drawable.btn_buy_credits_large,
                ProductIdentifierDomain.DOMAIN_FOLLOW_CREDITS));
        if (includeAlerts)
        {
            created.add(new StoreItemPromptPurchaseDTO(
                    R.string.store_buy_stock_alerts,
                    R.drawable.icn_stock_alert,
                    R.drawable.btn_buy_stock_alerts,
                    ProductIdentifierDomain.DOMAIN_STOCK_ALERTS));
        }
        created.add(new StoreItemPromptPurchaseDTO(
                R.string.store_buy_reset_portfolio,
                R.drawable.icn_reset_portfolio,
                R.drawable.btn_buy_reset_large,
                ProductIdentifierDomain.DOMAIN_RESET_PORTFOLIO));

        created.add(new StoreItemTitleDTO(R.string.store_header_manage_purchases));
        created.add(new StoreItemHasFurtherDTO(
                R.string.store_manage_heroes,
                R.drawable.icn_follow_credits,
                HeroManagerFragment.class));
        created.add(new StoreItemHasFurtherDTO(
                R.string.store_manage_followers,
                R.drawable.icn_view_followers,
                FollowerRevenueReportFragment.class));
        created.add(new StoreItemHasFurtherDTO(
                R.string.store_manage_stock_alerts,
                R.drawable.icn_stock_alert,
                AlertManagerFragment.class));

        return created;
    }
}

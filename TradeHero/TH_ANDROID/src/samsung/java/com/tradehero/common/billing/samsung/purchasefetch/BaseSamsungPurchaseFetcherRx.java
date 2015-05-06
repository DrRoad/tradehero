package com.tradehero.common.billing.samsung.purchasefetch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.sec.android.iap.lib.vo.InboxVo;
import com.tradehero.common.billing.purchasefetch.PurchaseFetchResult;
import com.tradehero.common.billing.samsung.BaseSamsungActorRx;
import com.tradehero.common.billing.samsung.SamsungBillingMode;
import com.tradehero.common.billing.samsung.SamsungOrderId;
import com.tradehero.common.billing.samsung.SamsungPurchase;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.rx.InboxListQueryGroup;
import com.tradehero.common.billing.samsung.rx.SamsungIapHelperFacade;
import java.util.List;
import rx.Observable;
import rx.functions.Func1;

abstract public class BaseSamsungPurchaseFetcherRx<
        SamsungSKUType extends SamsungSKU,
        SamsungOrderIdType extends SamsungOrderId,
        SamsungPurchaseType extends SamsungPurchase<SamsungSKUType, SamsungOrderIdType>>
        extends BaseSamsungActorRx
        implements SamsungPurchaseFetcherRx<
        SamsungSKUType,
        SamsungOrderIdType,
        SamsungPurchaseType>
{
    //<editor-fold desc="Constructors">
    public BaseSamsungPurchaseFetcherRx(
            int requestCode,
            @NonNull Context context,
            @SamsungBillingMode int mode)
    {
        super(requestCode, context, mode);
    }
    //</editor-fold>

    @NonNull @Override public Observable<PurchaseFetchResult<SamsungSKUType, SamsungOrderIdType, SamsungPurchaseType>> get()
    {
        return SamsungIapHelperFacade.getInboxes(context, mode, getInboxListQueryGroups())
                .flatMap(new Func1<Pair<InboxListQueryGroup, List<InboxVo>>,
                        Observable<PurchaseFetchResult<SamsungSKUType, SamsungOrderIdType, SamsungPurchaseType>>>()
                {
                    @Override public Observable<PurchaseFetchResult<SamsungSKUType, SamsungOrderIdType, SamsungPurchaseType>> call(
                            final Pair<InboxListQueryGroup, List<InboxVo>> pair)
                    {
                        return Observable.from(pair.second)
                                .map(new Func1<InboxVo, PurchaseFetchResult<SamsungSKUType, SamsungOrderIdType, SamsungPurchaseType>>()
                                {
                                    @Override public PurchaseFetchResult<SamsungSKUType, SamsungOrderIdType, SamsungPurchaseType> call(
                                            InboxVo inboxVo)
                                    {
                                        return new PurchaseFetchResult<>(
                                                getRequestCode(),
                                                createPurchase(pair.first.groupId, inboxVo));
                                    }
                                });
                    }
                });
    }

    @NonNull abstract protected SamsungPurchaseType createPurchase(@NonNull String groupId, @NonNull InboxVo inboxVo);

    @NonNull abstract protected List<InboxListQueryGroup> getInboxListQueryGroups();
}

package com.tradehero.common.billing.samsung.rx;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.sec.android.iap.lib.vo.InboxVo;
import java.util.List;
import rx.Observable;

public class SamsungInboxOperatorZip
{
    @NonNull protected final Context context;
    protected final int mode;
    @NonNull private final List<InboxListQueryGroup> queryGroups;

    //<editor-fold desc="Constructors">
    public SamsungInboxOperatorZip(
            @NonNull Context context,
            int mode,
            @NonNull List<InboxListQueryGroup> queryGroups)
    {
        this.context = context;
        this.mode = mode;
        this.queryGroups = queryGroups;
    }
    //</editor-fold>

    @NonNull public Observable<Pair<InboxListQueryGroup, Observable<InboxVo>>> getInboxItems()
    {
        // We probably need to control when each operator is called
        // Ideally, the next operator should be called only when the previous has completed
        return Observable.zip(
                Observable.from(queryGroups),
                Observable.from(queryGroups).map(queryGroup ->
                        Observable.create(
                                new SamsungInboxOperator(context, mode, queryGroup))),
                Pair::new);
    }
}

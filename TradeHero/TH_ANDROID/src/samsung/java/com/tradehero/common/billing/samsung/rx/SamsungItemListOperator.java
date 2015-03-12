package com.tradehero.common.billing.samsung.rx;

import android.content.Context;
import android.support.annotation.NonNull;
import com.sec.android.iap.lib.helper.SamsungIapHelper;
import com.sec.android.iap.lib.listener.OnGetItemListener;
import com.sec.android.iap.lib.vo.ErrorVo;
import com.sec.android.iap.lib.vo.ItemVo;
import com.tradehero.common.billing.samsung.BaseSamsungOperator;
import com.tradehero.common.billing.samsung.exception.SamsungItemListException;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;

public class SamsungItemListOperator extends BaseSamsungOperator
        implements Observable.OnSubscribe<List<ItemVo>>
{
    protected final int startNum;
    protected final int endNum;
    @NonNull protected final String itemType;
    @NonNull protected final String groupId;

    //<editor-fold desc="Constructors">
    public SamsungItemListOperator(
            @NonNull Context context,
            int mode,
            @NonNull ItemListQueryGroup queryGroup)
    {
        this(context,
                mode,
                queryGroup.startNum,
                queryGroup.endNum,
                queryGroup.itemType,
                queryGroup.groupId);
    }

    public SamsungItemListOperator(
            @NonNull Context context,
            int mode,
            int startNum,
            int endNum,
            @NonNull String itemType,
            @NonNull String groupId)
    {
        super(context, mode);
        this.startNum = startNum;
        this.endNum = endNum;
        this.itemType = itemType;
        this.groupId = groupId;
    }
    //</editor-fold>

    @Override public void call(final Subscriber<? super List<ItemVo>> subscriber)
    {
        getSamsungIapHelper().getItemList(
                groupId,
                startNum,
                endNum,
                itemType,
                mode,
                new OnGetItemListener()
                {
                    @Override public void onGetItem(ErrorVo errorVo, ArrayList<ItemVo> itemList)
                    {
                        if (errorVo.getErrorCode() == SamsungIapHelper.IAP_ERROR_NONE)
                        {
                            subscriber.onNext(itemList);
                            subscriber.onCompleted();
                        }
                        else
                        {
                            subscriber.onError(new SamsungItemListException(errorVo, groupId, mode));
                        }
                    }
                });
    }
}
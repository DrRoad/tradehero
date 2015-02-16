package com.tradehero.common.billing.samsung.identifier;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.sec.android.iap.lib.vo.ItemVo;
import com.tradehero.common.billing.identifier.ProductIdentifierListResult;
import com.tradehero.common.billing.samsung.BaseSamsungActorRx;
import com.tradehero.common.billing.samsung.BaseSamsungSKUList;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.SamsungSKUListKey;
import com.tradehero.common.billing.samsung.rx.ItemListQueryGroup;
import com.tradehero.common.billing.samsung.rx.SamsungItemListOperatorZip;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Observable;
import rx.functions.Func1;

abstract public class BaseSamsungProductIdentifierFetcherRx<
        SamsungSKUListKeyType extends SamsungSKUListKey,
        SamsungSKUType extends SamsungSKU,
        SamsungSKUListType extends BaseSamsungSKUList<SamsungSKUType>>
        extends BaseSamsungActorRx<ProductIdentifierListResult<
        SamsungSKUListKeyType,
        SamsungSKUType,
        SamsungSKUListType>>
        implements SamsungProductIdentifierFetcherRx<
        SamsungSKUListKeyType,
        SamsungSKUType,
        SamsungSKUListType>
{

    //<editor-fold desc="Constructors">
    public BaseSamsungProductIdentifierFetcherRx(
            int requestCode,
            @NonNull Context context,
            int mode)
    {
        super(requestCode, context, mode);
    }
    //</editor-fold>

    @NonNull @Override public Observable<ProductIdentifierListResult<
            SamsungSKUListKeyType,
            SamsungSKUType,
            SamsungSKUListType>> get()
    {
        return new SamsungItemListOperatorZip(context, mode, getItemListQueryGroups())
                .getItems()
                .flatMap(
                        new Func1<Pair<ItemListQueryGroup, List<ItemVo>>, Observable<? extends ProductIdentifierListResult<SamsungSKUListKeyType, SamsungSKUType, SamsungSKUListType>>>()
                        {
                            @Override
                            public Observable<? extends ProductIdentifierListResult<SamsungSKUListKeyType, SamsungSKUType, SamsungSKUListType>> call(
                                    Pair<ItemListQueryGroup, List<ItemVo>> pair)
                            {
                                return BaseSamsungProductIdentifierFetcherRx.this.createResult(pair);
                            }
                        });
    }

    @NonNull protected Observable<ProductIdentifierListResult<
            SamsungSKUListKeyType,
            SamsungSKUType,
            SamsungSKUListType>> createResult(@NonNull Pair<ItemListQueryGroup, List<ItemVo>> pair)
    {
        Map<SamsungSKUListKeyType, SamsungSKUListType> samsungSKUs = new HashMap<>();
        for (ItemVo itemVo : pair.second)
        {
            SamsungSKUListKeyType key = createSamsungListKey(itemVo.getType());
            if (samsungSKUs.get(key) == null)
            {
                samsungSKUs.put(key, createSamsungSKUList());
            }
            samsungSKUs.get(key).add(createSamsungSku(pair.first.groupId, itemVo.getItemId()));
        }
        return Observable.from(samsungSKUs.entrySet())
                .map(new Func1<Map.Entry<SamsungSKUListKeyType, SamsungSKUListType>, ProductIdentifierListResult<SamsungSKUListKeyType, SamsungSKUType, SamsungSKUListType>>()
                {
                    @Override public ProductIdentifierListResult<SamsungSKUListKeyType, SamsungSKUType, SamsungSKUListType> call(
                            Map.Entry<SamsungSKUListKeyType, SamsungSKUListType> entry)
                    {
                        return new ProductIdentifierListResult<>(
                                BaseSamsungProductIdentifierFetcherRx.this.getRequestCode(),
                                entry.getKey(),
                                entry.getValue());
                    }
                });
    }

    @NonNull abstract protected List<ItemListQueryGroup> getItemListQueryGroups();

    @NonNull abstract protected SamsungSKUListKeyType createSamsungListKey(String itemType);

    @NonNull abstract protected SamsungSKUType createSamsungSku(String groupId, String itemId);

    @NonNull abstract protected SamsungSKUListType createSamsungSKUList();
}

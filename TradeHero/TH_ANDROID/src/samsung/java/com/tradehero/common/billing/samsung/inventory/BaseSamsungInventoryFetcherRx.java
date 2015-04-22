package com.tradehero.common.billing.samsung.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.sec.android.iap.lib.vo.ItemVo;
import com.tradehero.common.billing.inventory.ProductInventoryResult;
import com.tradehero.common.billing.samsung.BaseSamsungActorRx;
import com.tradehero.common.billing.samsung.SamsungItemGroup;
import com.tradehero.common.billing.samsung.SamsungProductDetail;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.common.billing.samsung.rx.ItemListQueryGroup;
import com.tradehero.common.billing.samsung.rx.SamsungItemListOperatorZip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rx.Observable;
import rx.functions.Func1;

/**
 * Product Identifier Fetcher and Inventory Fetcher are essentially making the same calls.
 */
abstract public class BaseSamsungInventoryFetcherRx<
        SamsungSKUType extends SamsungSKU,
        SamsungProductDetailType extends SamsungProductDetail<SamsungSKUType>>
        extends BaseSamsungActorRx<ProductInventoryResult<
        SamsungSKUType,
        SamsungProductDetailType>>
        implements SamsungInventoryFetcherRx<
        SamsungSKUType,
        SamsungProductDetailType>
{
    @NonNull protected final List<SamsungSKUType> skus;

    //<editor-fold desc="Constructors">
    public BaseSamsungInventoryFetcherRx(
            int requestCode,
            @NonNull Context context,
            int mode,
            @NonNull List<SamsungSKUType> skus)
    {
        super(requestCode, context, mode);
        this.skus = skus;
    }
    //</editor-fold>

    @NonNull @Override public List<SamsungSKUType> getProductIdentifiers()
    {
        return skus;
    }

    @NonNull @Override public Observable<ProductInventoryResult<
            SamsungSKUType,
            SamsungProductDetailType>> get()
    {
        return new SamsungItemListOperatorZip(context, mode, getItemListQueryGroups())
                .getItems()
                .flatMap(new Func1<Pair<ItemListQueryGroup, List<ItemVo>>, Observable<? extends Map<SamsungSKUType, SamsungProductDetailType>>>()
                {
                    @Override public Observable<? extends Map<SamsungSKUType, SamsungProductDetailType>> call(Pair<ItemListQueryGroup, List<ItemVo>> pair)
                    {
                        return BaseSamsungInventoryFetcherRx.this.createDetail(pair);
                    }
                })
                .map(new Func1<Map<SamsungSKUType, SamsungProductDetailType>, ProductInventoryResult<SamsungSKUType, SamsungProductDetailType>>()
                {
                    @Override public ProductInventoryResult<SamsungSKUType, SamsungProductDetailType> call(Map<SamsungSKUType, SamsungProductDetailType> detail)
                    {
                        return new ProductInventoryResult<>(BaseSamsungInventoryFetcherRx.this.getRequestCode(), detail);
                    }
                });
    }

    @NonNull protected List<ItemListQueryGroup> getItemListQueryGroups()
    {
        Set<ItemListQueryGroup> groups = new HashSet<>();
        for (SamsungSKUType sku : skus)
        {
            groups.add(createItemListQueryGroup(sku));
        }
        return new ArrayList<>(groups);
    }

    @NonNull abstract protected ItemListQueryGroup createItemListQueryGroup(@NonNull SamsungSKUType sku);

    @NonNull protected Observable<Map<SamsungSKUType, SamsungProductDetailType>> createDetail(
            @NonNull final Pair<ItemListQueryGroup, List<ItemVo>> pair)
    {
        Map<SamsungSKUType, SamsungProductDetailType> created = new HashMap<>();
        SamsungProductDetailType detail;
        for (ItemVo itemVo : pair.second)
        {
            detail = createSamsungProductDetail(
                    new SamsungItemGroup(pair.first.groupId),
                    itemVo);
            created.put(
                    detail.getProductIdentifier(),
                    detail);
        }
        return Observable.just(created);
    }

    @NonNull abstract protected SamsungProductDetailType createSamsungProductDetail(
            @NonNull SamsungItemGroup samsungItemGroup,
            @NonNull ItemVo itemVo);
}

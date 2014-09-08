package com.tradehero.th.fragments.billing;

import android.content.Context;
import android.util.AttributeSet;
import com.tradehero.common.billing.samsung.SamsungSKU;
import com.tradehero.th.billing.samsung.THSamsungProductDetail;
import com.tradehero.th.fragments.billing.StoreProductDetailView;

public class THSamsungStoreProductDetailView extends StoreProductDetailView<
        SamsungSKU,
        THSamsungProductDetail>
{
    public THSamsungStoreProductDetailView(Context context)
    {
        super(context);
    }

    public THSamsungStoreProductDetailView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public THSamsungStoreProductDetailView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
}

package com.tradehero.th.fragments.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th.R;
import com.tradehero.th.billing.googleplay.THIABProductDetail;
import com.tradehero.th.fragments.billing.googleplay.SKUDetailsAdapter;

/** Created with IntelliJ IDEA. User: xavier Date: 11/6/13 Time: 4:14 PM To change this template use File | Settings | File Templates. */
public class THSKUDetailsAdapter extends SKUDetailsAdapter<THIABProductDetail, StoreSKUDetailView>
{
    public static final String TAG = THSKUDetailsAdapter.class.getSimpleName();

    private String skuDomain;

    //<editor-fold desc="Constructors">
    public THSKUDetailsAdapter(Context context, LayoutInflater inflater, String skuDomain)
    {
        super(context, inflater, R.layout.store_sku_detail);
        this.skuDomain = skuDomain;
    }

    public THSKUDetailsAdapter(Context context, LayoutInflater inflater, int layoutResourceId, String skuDomain)
    {
        super(context, inflater, layoutResourceId);
        this.skuDomain = skuDomain;
    }
    //</editor-fold>

    @Override protected View getHeaderView(int position, View convertView, ViewGroup viewGroup)
    {
        ProductDetailQuickDescriptionView
                quickDescription = convertView instanceof ProductDetailQuickDescriptionView ?
                (ProductDetailQuickDescriptionView) convertView :
                (ProductDetailQuickDescriptionView) inflater.inflate(R.layout.store_quick_message, viewGroup, false);
        quickDescription.linkWithProductDomain(skuDomain, true);
        return quickDescription;
    }

    @Override protected void fineTune(int position, THIABProductDetail dto, StoreSKUDetailView dtoView)
    {
    }

    public String getSkuDomain()
    {
        return skuDomain;
    }

    public void setSkuDomain(String skuDomain)
    {
        this.skuDomain = skuDomain;
    }
}

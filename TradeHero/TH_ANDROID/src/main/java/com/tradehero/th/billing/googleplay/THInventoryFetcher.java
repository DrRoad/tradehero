package com.tradehero.th.billing.googleplay;

import android.content.Context;
import android.os.RemoteException;
import com.tradehero.common.billing.googleplay.IABSKU;
import com.tradehero.common.billing.googleplay.InventoryFetcher;
import com.tradehero.common.billing.googleplay.exceptions.IABException;
import com.tradehero.th.persistence.billing.ProductDetailCache;
import com.tradehero.th.persistence.billing.googleplay.THSKUDetailCache;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.json.JSONException;

/** Created with IntelliJ IDEA. User: xavier Date: 11/6/13 Time: 3:48 PM To change this template use File | Settings | File Templates. */
public class THInventoryFetcher extends InventoryFetcher<THSKUDetails>
{
    public static final String TAG = THInventoryFetcher.class.getSimpleName();

    @Inject protected Lazy<THSKUDetailCache> skuDetailCache;

    public THInventoryFetcher(Context ctx, List<IABSKU> iabSKUs)
    {
        super(ctx, iabSKUs);
    }

    @Override protected THSKUDetails createSKUDetails(String itemType, String json) throws JSONException
    {
        return new THSKUDetails(itemType, json);
    }

    @Override protected HashMap<IABSKU, THSKUDetails> internalFetchCompleteInventory() throws IABException, RemoteException, JSONException
    {
        HashMap<IABSKU, THSKUDetails> inventory = super.internalFetchCompleteInventory();
        skuDetailCache.get().put(new ArrayList<>(inventory.values()));
        return inventory;
    }
}

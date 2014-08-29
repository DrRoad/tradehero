package com.tradehero.common.billing.amazon;

import android.content.Context;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.tradehero.common.billing.amazon.exception.AmazonException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

abstract public class BaseAmazonPurchaseFetcher<
        AmazonSKUType extends AmazonSKU,
        AmazonOrderIdType extends AmazonOrderId,
        AmazonPurchaseType extends AmazonPurchase<AmazonSKUType, AmazonOrderIdType>,
        AmazonPurchaseIncompleteType extends AmazonPurchase<AmazonSKUType, AmazonOrderIdType>,
        AmazonExceptionType extends AmazonException>
    extends BaseAmazonActor
    implements AmazonPurchaseFetcher<
            AmazonSKUType,
            AmazonOrderIdType,
            AmazonPurchaseType,
            AmazonExceptionType>
{
    protected boolean fetching;
    @NotNull protected final List<AmazonPurchaseIncompleteType> fetchedIncompletePurchases;
    @NotNull protected final List<AmazonPurchaseType> purchases;
    @Nullable protected OnPurchaseFetchedListener<AmazonSKUType, AmazonOrderIdType, AmazonPurchaseType, AmazonExceptionType> fetchListener;

    //<editor-fold desc="Constructors">
    public BaseAmazonPurchaseFetcher(@NotNull Context context)
    {
        super(context);
        fetchedIncompletePurchases = new ArrayList<>();
        purchases = new ArrayList<>();
    }
    //</editor-fold>

    @Override public void fetchPurchases(int requestCode)
    {
        checkNotFetching();
        this.fetching = true;
        setRequestCode(requestCode);
        prepareAndCallService();
    }

    protected void checkNotFetching()
    {
        if (fetching)
        {
            throw new IllegalStateException("Already fetching");
        }
    }

    protected void prepareAndCallService()
    {
        prepareListener();
        PurchasingService.getPurchaseUpdates(true);
    }

    @Override public void onMyPurchaseUpdatesResponse(@NotNull PurchaseUpdatesResponse purchaseUpdatesResponse)
    {
        super.onPurchaseUpdatesResponse(purchaseUpdatesResponse);
        if (currentRequestId != null && currentRequestId.equals(purchaseUpdatesResponse.getRequestId()))
        {
            switch (purchaseUpdatesResponse.getRequestStatus())
            {
                case SUCCESSFUL:
                    handleReceived(purchaseUpdatesResponse.getReceipts());
                    notifyListenerFetched();
                    break;
                case FAILED:
                case NOT_SUPPORTED:
                    notifyListenerFetchFailed(createException(purchaseUpdatesResponse.getRequestStatus()));
                    break;
            }
        }
    }

    protected void handleReceived(@NotNull List<Receipt> receipts)
    {
        for (Receipt receipt : receipts)
        {
            fetchedIncompletePurchases.add(createIncompletePurchase(receipt));
        }
    }

    @NotNull protected abstract AmazonPurchaseIncompleteType createIncompletePurchase(Receipt receipt);

    abstract protected AmazonExceptionType createException(@NotNull PurchaseUpdatesResponse.RequestStatus requestStatus);

    @Override @Nullable public OnPurchaseFetchedListener<AmazonSKUType, AmazonOrderIdType, AmazonPurchaseType, AmazonExceptionType> getFetchListener()
    {
        return fetchListener;
    }

    @Override public void setPurchaseFetchedListener(@Nullable OnPurchaseFetchedListener<AmazonSKUType, AmazonOrderIdType, AmazonPurchaseType, AmazonExceptionType> fetchListener)
    {
        this.fetchListener = fetchListener;
    }

    protected void notifyListenerFetched()
    {
        OnPurchaseFetchedListener<AmazonSKUType, AmazonOrderIdType, AmazonPurchaseType, AmazonExceptionType> listenerCopy = getFetchListener();
        if (listenerCopy != null)
        {
            listenerCopy.onFetchedPurchases(getRequestCode(), this.purchases);
        }
    }

    protected void notifyListenerFetchFailed(AmazonExceptionType exception)
    {
        Timber.e(exception, "");
        OnPurchaseFetchedListener<AmazonSKUType, AmazonOrderIdType, AmazonPurchaseType, AmazonExceptionType> listenerCopy = getFetchListener();
        if (listenerCopy != null)
        {
            listenerCopy.onFetchPurchasesFailed(getRequestCode(), exception);
        }
    }
}

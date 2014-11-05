package com.tradehero.th.billing.amazon;

import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.tradehero.common.billing.amazon.BaseAmazonPurchase;
import com.tradehero.common.billing.amazon.AmazonSKU;
import com.tradehero.th.api.billing.AmazonPurchaseInProcessDTO;
import com.tradehero.th.api.billing.AmazonPurchaseReportDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.UserBaseKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class THBaseAmazonPurchase
        extends BaseAmazonPurchase<AmazonSKU, THAmazonOrderId>
        implements THAmazonPurchase
{
    @NonNull private OwnedPortfolioId applicablePortfolioId;
    @Nullable private UserBaseKey userToFollow;

    //<editor-fold desc="Constructors">
    protected THBaseAmazonPurchase(
            @NonNull PurchaseResponse purchaseResponse,
            @NonNull OwnedPortfolioId applicablePortfolioId)
    {
        super(purchaseResponse);
        this.applicablePortfolioId = applicablePortfolioId;
    }
    //</editor-fold>

    @NonNull @Override public THAmazonOrderId getOrderId()
    {
        return new THAmazonOrderId(purchaseResponse.getReceipt());
    }

    @NonNull @Override public AmazonSKU getProductIdentifier()
    {
        return new AmazonSKU(purchaseResponse.getReceipt().getSku());
    }

    @Override public void setUserToFollow(@Nullable UserBaseKey userToFollow)
    {
        this.userToFollow = userToFollow;
    }

    @Nullable @Override public UserBaseKey getUserToFollow()
    {
        return userToFollow;
    }

    @Override public void setApplicablePortfolioId(@NonNull OwnedPortfolioId applicablePortfolioId)
    {
        this.applicablePortfolioId = applicablePortfolioId;
    }

    @NonNull @Override public OwnedPortfolioId getApplicableOwnedPortfolioId()
    {
        return applicablePortfolioId;
    }

    @NonNull @Override public AmazonPurchaseReportDTO getPurchaseReportDTO()
    {
        return new AmazonPurchaseReportDTO(
                purchaseResponse.getReceipt().getSku(),
                purchaseResponse.getReceipt().getReceiptId(),
                purchaseResponse.getUserData().getUserId());
    }

    @NonNull public AmazonPurchaseInProcessDTO getPurchaseToSaveDTO()
    {
        return new AmazonPurchaseInProcessDTO(this);
    }

    public void populate(@NonNull AmazonPurchaseInProcessDTO purchaseInProcessDTO)
    {
        if (!purchaseResponse.getReceipt().getReceiptId().equals(purchaseInProcessDTO.amazonPurchaseToken))
        {
            throw new IllegalArgumentException(String.format("Non-matching paymentId %s - %s", purchaseResponse.getReceipt().getReceiptId(), purchaseInProcessDTO.amazonPurchaseToken));
        }
        setApplicablePortfolioId(purchaseInProcessDTO.applicablePortfolioId);
        setUserToFollow(purchaseInProcessDTO.userToFollow);
    }

    @Override public boolean shouldConsume()
    {
        return purchaseResponse.getReceipt().getProductType().equals(ProductType.CONSUMABLE);
    }
}

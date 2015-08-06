package com.tradehero.th.api.kyc.ayondo;

import android.support.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tradehero.th.api.market.Country;
import java.util.UUID;

public class AyondoLeadDTO extends AyondoLeadAddressDTO
{
    @JsonProperty("Guid") @Nullable private UUID uid;
    @JsonProperty("Currency") @Nullable private String currency;
    @JsonProperty("IsTestRecord") @Nullable private Boolean isTestRecord;
    @JsonProperty("Language") @Nullable private Country language;
    @JsonProperty("ProductType") @Nullable private AyondoProductType productType;

    public AyondoLeadDTO()
    {
    }

    @Nullable public UUID getUid()
    {
        return uid;
    }

    public void setUid(@Nullable UUID uid)
    {
        this.uid = uid;
    }

    @Nullable public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(@Nullable String currency)
    {
        this.currency = currency;
    }

    @Nullable public Boolean getIsTestRecord()
    {
        return isTestRecord;
    }

    public void setIsTestRecord(@Nullable Boolean isTestRecord)
    {
        this.isTestRecord = isTestRecord;
    }

    @Nullable public Country getLanguage()
    {
        return language;
    }

    public void setLanguage(@Nullable Country language)
    {
        this.language = language;
    }

    @Nullable public AyondoProductType getProductType()
    {
        return productType;
    }

    public void setProductType(@Nullable AyondoProductType productType)
    {
        this.productType = productType;
    }

    public boolean isValidToCreateAccount()
    {
        return currency != null
                // No need to text isTestRecord
                // TODO decide if we test language
                ;
    }
}

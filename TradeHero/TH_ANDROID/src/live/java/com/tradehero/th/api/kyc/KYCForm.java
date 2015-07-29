package com.tradehero.th.api.kyc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tradehero.th.api.kyc.ayondo.KYCAyondoForm;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.models.fastfill.ScannedDocument;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "formType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmptyKYCForm.class, name = EmptyKYCForm.KEY_EMPTY_TYPE),
        @JsonSubTypes.Type(value = KYCAyondoForm.class, name = KYCAyondoForm.KEY_AYONDO_TYPE),
}) public interface KYCForm
{
    @StringRes int getBrokerName();

    @Nullable Country getCountry();

    void pickFrom(@NonNull ScannedDocument scannedDocument);

    void pickFrom(@NonNull KYCForm other);

    void setStepStatuses(@NonNull List<StepStatus> stepStatuses);

    @Nullable List<StepStatus> getStepStatuses();

    boolean equals(@Nullable Object other);
}

package com.tradehero.th.api.kyc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.tradehero.th.R;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.models.fastfill.ScannedDocument;
import java.util.ArrayList;
import java.util.List;

public class EmptyKYCForm implements KYCForm
{
    public static final String KEY_EMPTY_TYPE = "EMPTY";

    @Override @StringRes public int getBrokerName()
    {
        return R.string.broker_name_none;
    }

    @NonNull @Override public Country getCountry()
    {
        return Country.SG;
    }

    @Override public void pickFrom(@NonNull ScannedDocument scannedDocument)
    {
    }

    @Override public void pickFrom(@NonNull KYCForm other)
    {
    }

    @Override public void setStepStatuses(@NonNull List<StepStatus> stepStatuses)
    {
    }

    @Override @Nullable public List<StepStatus> getStepStatuses()
    {
        return new ArrayList<>();
    }

    @Override public boolean equals(@Nullable Object o)
    {
        return o instanceof EmptyKYCForm;
    }
}
package com.tradehero.th.api.kyc.ayondo;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.tradehero.th.api.kyc.StepStatus;
import com.tradehero.th.api.kyc.StepStatusesDTO;
import java.util.Arrays;

public class DummyKYCAyondoUtil
{
    @NonNull public static StepStatusesDTO getSteps(@NonNull KYCAyondoForm kycForm)
    {
        return new StepStatusesDTO(Arrays.asList(
                getStep1(kycForm),
                getStep2(kycForm),
                getStep3(kycForm),
                getStep4(kycForm),
                getStep5(kycForm)));
    }

    @NonNull public static StepStatus getStep1(@NonNull KYCAyondoForm kycForm)
    {
        return (kycForm.getGender() != null
                && kycForm.getFullName() != null
                && kycForm.getEmail() != null
                && kycForm.getMobileNumberDialingPrefix() != null
                && kycForm.getMobileNumber() != null
                && kycForm.getVerifiedMobileNumberDialingPrefix() != null
                && kycForm.getVerifiedMobileNumber() != null
                && kycForm.getMobileNumberDialingPrefix().equals(kycForm.getVerifiedMobileNumberDialingPrefix())
                && kycForm.getMobileNumber().equals(kycForm.getVerifiedMobileNumber())
                && kycForm.getNationality() != null
                && kycForm.getResidency() != null
                && kycForm.getDob() != null)
                ? StepStatus.COMPLETE
                : StepStatus.UNSTARTED;
    }

    @NonNull public static StepStatus getStep2(@NonNull KYCAyondoForm kycForm)
    {
        return (kycForm.getAnnualIncomeRange() != null
                && kycForm.getNetWorthRange() != null
                && kycForm.getPercentNetWorthForInvestmentRange() != null
                && kycForm.getEmploymentStatus() != null
                && kycForm.isEmployerRegulatedFinancial() != null)
                ? StepStatus.COMPLETE
                : StepStatus.UNSTARTED;
    }

    @NonNull public static StepStatus getStep3(@NonNull KYCAyondoForm kycForm)
    {
        return (kycForm.isWorkedInFinance1Year() != null
                && kycForm.isAttendedSeminarAyondo() != null
                && kycForm.isHaveOtherQualification() != null
                && kycForm.getTradingPerQuarter() != null
                && kycForm.isTradedSharesBonds() != null
                && kycForm.isTradedOtcDerivative() != null
                && kycForm.isTradedEtc() != null)
                ? StepStatus.COMPLETE
                : StepStatus.UNSTARTED;
    }

    @NonNull public static StepStatus getStep4(@NonNull KYCAyondoForm kycForm)
    {
        return (kycForm.getAddresses() != null
                && kycForm.getAddresses().size() > 0
                && !TextUtils.isEmpty(kycForm.getAddresses().get(0).addressLine1)
                && !TextUtils.isEmpty(kycForm.getAddresses().get(0).addressLine2)
                && !TextUtils.isEmpty(kycForm.getAddresses().get(0).city)
                && !TextUtils.isEmpty(kycForm.getAddresses().get(0).postalCode))
                ? StepStatus.COMPLETE
                : StepStatus.UNSTARTED;
    }

    @NonNull public static StepStatus getStep5(@NonNull KYCAyondoForm kycForm)
    {
        return (kycForm.getIdentityDocumentType() != null
                && kycForm.getIdentityDocumentFile() != null
                && kycForm.getResidenceDocumentType() != null
                && kycForm.getResidenceDocumentFile() != null
                && kycForm.isAgreeTermsConditions() != null
                && kycForm.isAgreeRisksWarnings() != null
                && kycForm.isAgreeDataSharing() != null)
                ? StepStatus.COMPLETE
                : StepStatus.UNSTARTED;
    }
}

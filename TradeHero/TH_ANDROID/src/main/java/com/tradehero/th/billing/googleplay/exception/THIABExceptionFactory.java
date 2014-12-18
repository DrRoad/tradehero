package com.tradehero.th.billing.googleplay.exception;

import com.tradehero.common.billing.googleplay.exception.IABException;
import com.tradehero.common.billing.googleplay.exception.IABExceptionFactory;
import javax.inject.Inject;

public class THIABExceptionFactory extends IABExceptionFactory
{
    @Inject public THIABExceptionFactory()
    {
        super();
    }

    @Override public IABException create(int responseStatus, String message)
    {
        IABException exception = super.create(responseStatus, message);
        if (exception == null)
        {
            switch (responseStatus)
            {
                case THIABExceptionConstants.UNHANDLED_DOMAIN: // -2000
                    exception = new UnhandledSKUDomainException(message);
                    break;

                case THIABExceptionConstants.PURCHASE_REPORT_RETROFIT_ERROR: // -2001
                    exception = new PurchaseReportRetrofitException(message);
                    break;

                case THIABExceptionConstants.MISSING_CACHED_DETAIL: // -2002
                    exception = new MissingCachedProductDetailException(message);
                    break;

                case THIABExceptionConstants.MISSING_APPLICABLE_PORTFOLIO_ID: // -2003
                    exception = new MissingApplicablePortfolioIdException(message);
                    break;
            }
        }
        return exception;
    }
}
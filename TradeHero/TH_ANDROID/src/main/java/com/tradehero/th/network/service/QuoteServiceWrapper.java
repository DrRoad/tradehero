package com.tradehero.th.network.service;

import com.tradehero.th.api.SignatureContainer;
import com.tradehero.th.api.quote.QuoteDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.network.UrlEncoderHelper;
import com.tradehero.th.network.retrofit.BaseMiddleCallback;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import retrofit.Callback;
import retrofit.client.Response;
import rx.Observable;

@Singleton public class QuoteServiceWrapper
{
    @NotNull private final QuoteServiceRx quoteServiceRx;
    @NotNull private final QuoteServiceAsync quoteServiceAsync;

    //<editor-fold desc="Constructors">
    @Inject public QuoteServiceWrapper(
            @NotNull QuoteServiceAsync quoteServiceAsync,
            @NotNull QuoteServiceRx quoteServiceRx)
    {
        super();
        this.quoteServiceAsync = quoteServiceAsync;
        this.quoteServiceRx = quoteServiceRx;
    }
    //</editor-fold>

    private void basicCheck(SecurityId securityId)
    {
        if (securityId == null)
        {
            throw new NullPointerException("securityId cannot be null");
        }
        if (securityId.getExchange() == null)
        {
            throw new NullPointerException("securityId.getExchange() cannot be null");
        }
        if (securityId.getSecuritySymbol() == null)
        {
            throw new NullPointerException("securityId.getSecuritySymbol() cannot be null");
        }
    }

    //<editor-fold desc="Get Quote">
    public Observable<SignatureContainer<QuoteDTO>> getQuoteRx(SecurityId securityId)
    {
        basicCheck(securityId);
        return this.quoteServiceRx.getQuote(UrlEncoderHelper.transform(securityId.getExchange()), UrlEncoderHelper.transform(
                securityId.getSecuritySymbol()));
    }
    //</editor-fold>

    //<editor-fold desc="Get Raw Quote">
    public Observable<Response> getRawQuoteRx(SecurityId securityId)
    {
        basicCheck(securityId);
        return this.quoteServiceRx.getRawQuote(UrlEncoderHelper.transform(securityId.getExchange()), UrlEncoderHelper.transform(
                securityId.getSecuritySymbol()));
    }

    public BaseMiddleCallback<Response> getRawQuote(SecurityId securityId, Callback<Response> callback)
    {
        BaseMiddleCallback<Response> middleCallback = new BaseMiddleCallback<>(callback);
        basicCheck(securityId);
        this.quoteServiceAsync.getRawQuote(UrlEncoderHelper.transform(securityId.getExchange()), UrlEncoderHelper.transform(
                securityId.getSecuritySymbol()), middleCallback);
        return middleCallback;
    }
    //</editor-fold>
}

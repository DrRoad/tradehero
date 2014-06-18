package com.tradehero.th.network.service;

import com.tradehero.th.api.translation.TranslationResult;
import com.tradehero.th.api.translation.bing.BingTranslationResult;
import com.tradehero.th.api.translation.bing.BingTranslationToken;
import com.tradehero.th.models.translation.bing.BaseMiddleCallbackBingTranslationResult;
import com.tradehero.th.network.retrofit.CallbackWrapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import retrofit.Callback;

@Singleton public class TranslationServiceBingWrapper
{
    private static final String PREFERRED_CONTENT_TYPE = "text/plain";

    @NotNull private final TranslationServiceBing translationServiceBing;
    @NotNull private final TranslationServiceBingAsync translationServiceBingAsync;

    @Inject public TranslationServiceBingWrapper(
            @NotNull TranslationServiceBing translationServiceBing,
            @NotNull TranslationServiceBingAsync translationServiceBingAsync)
    {
        this.translationServiceBing = translationServiceBing;
        this.translationServiceBingAsync = translationServiceBingAsync;
    }

    public BingTranslationResult translate(BingTranslationToken token, String from, String to, String text)
    {
        return translationServiceBing.requestForTranslation(
                token.getPrefixedAccessToken(),
                from, to, PREFERRED_CONTENT_TYPE, text);
    }

    public CallbackWrapper<TranslationResult> translate(BingTranslationToken token, String from, String to, String text,
            Callback<TranslationResult> callback)
    {
        BaseMiddleCallbackBingTranslationResult middleCallback = new BaseMiddleCallbackBingTranslationResult(callback);
        translationServiceBingAsync.requestForTranslation(
                token.getPrefixedAccessToken(),
                from, to, PREFERRED_CONTENT_TYPE, text,
                middleCallback);
        return middleCallback;
    }
}

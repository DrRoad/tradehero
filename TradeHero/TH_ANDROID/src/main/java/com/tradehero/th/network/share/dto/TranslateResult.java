package com.tradehero.th.network.share.dto;

import android.support.annotation.NonNull;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;

public class TranslateResult implements SocialDialogResult
{
    @NonNull public final AbstractDiscussionCompactDTO toTranslate;
    @NonNull public final AbstractDiscussionCompactDTO translated;

    //<editor-fold desc="Constructors">
    public TranslateResult(
            @NonNull AbstractDiscussionCompactDTO toTranslate,
            @NonNull AbstractDiscussionCompactDTO translated)
    {
        this.toTranslate = toTranslate;
        this.translated = translated;
    }
    //</editor-fold>
}
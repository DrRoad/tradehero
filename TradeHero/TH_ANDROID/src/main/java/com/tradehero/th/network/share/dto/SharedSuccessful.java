package com.tradehero.th.network.share.dto;

import android.support.annotation.NonNull;
import com.tradehero.th.api.share.SocialShareFormDTO;
import com.tradehero.th.api.share.SocialShareResultDTO;

public class SharedSuccessful extends SocialShareResult
{
    @NonNull public final SocialShareResultDTO socialShareResultDTO;

    //<editor-fold desc="Constructors">
    public SharedSuccessful(
            @NonNull SocialShareFormDTO shareFormDTO,
            @NonNull SocialShareResultDTO socialShareResultDTO)
    {
        super(shareFormDTO);
        this.socialShareResultDTO = socialShareResultDTO;
    }
    //</editor-fold>
}

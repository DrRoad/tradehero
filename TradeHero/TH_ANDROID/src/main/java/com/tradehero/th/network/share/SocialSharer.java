package com.tradehero.th.network.share;

import com.tradehero.th.api.share.SocialShareFormDTO;
import com.tradehero.th.api.share.SocialShareResultDTO;
import org.jetbrains.annotations.NotNull;

public interface SocialSharer
{
    void setSharedListener(OnSharedListener sharedListener);
    void share(@NotNull SocialShareFormDTO shareFormDTO);

    public static interface OnSharedListener
    {
        void onConnectRequired(SocialShareFormDTO shareFormDTO);
        void onShared(SocialShareFormDTO shareFormDTO, SocialShareResultDTO socialShareResultDTO);
        void onShareFailed(SocialShareFormDTO shareFormDTO, Throwable throwable);
    }
}

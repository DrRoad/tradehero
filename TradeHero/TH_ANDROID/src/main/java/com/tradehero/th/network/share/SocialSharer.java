package com.tradehero.th.network.share;

import com.tradehero.th.api.share.SocialShareFormDTO;
import com.tradehero.th.api.share.SocialShareResultDTO;
import android.support.annotation.NonNull;

public interface SocialSharer
{
    void setSharedListener(OnSharedListener sharedListener);
    void share(@NonNull SocialShareFormDTO shareFormDTO);

    public static interface OnSharedListener
    {
        void onConnectRequired(SocialShareFormDTO shareFormDTO);
        void onShared(SocialShareFormDTO shareFormDTO, SocialShareResultDTO socialShareResultDTO);
        void onShareFailed(SocialShareFormDTO shareFormDTO, Throwable throwable);
    }
}

package com.tradehero.th.fragments.education;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tradehero.th.api.education.VideoDTO;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.utils.StringUtils;
import java.util.List;

public class VideoDTOUtil
{
    public static void openVideoDTO(
            @NonNull Context context,
            @Nullable DashboardNavigator navigator,
            @NonNull VideoDTO videoDTO)
    {
        if (!videoDTO.locked && !StringUtils.isNullOrEmpty(videoDTO.url))
        {
            Uri url = Uri.parse(videoDTO.url);
            Intent videoIntent = new Intent(Intent.ACTION_VIEW, url);
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> handlerActivities = packageManager.queryIntentActivities(videoIntent, 0);
            if (handlerActivities.size() > 0)
            {
                context.startActivity(videoIntent);
            }
            else if (navigator != null)
            {
                Bundle bundle = new Bundle();
                WebViewFragment.putUrl(bundle, videoDTO.url);
                navigator.pushFragment(WebViewFragment.class, bundle);
            }
        }
    }
}

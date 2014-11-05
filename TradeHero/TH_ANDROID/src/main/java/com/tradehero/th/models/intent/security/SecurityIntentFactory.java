package com.tradehero.th.models.intent.security;

import android.content.Context;
import android.content.Intent;
import com.tradehero.th.R;
import com.tradehero.th.models.intent.THIntentSubFactory;
import java.util.List;
import javax.inject.Inject;
import android.support.annotation.NonNull;

public class SecurityIntentFactory extends THIntentSubFactory<SecurityPushBuyIntent>
{
    @Inject public SecurityIntentFactory(@NonNull Context context)
    {
        super(context.getResources());
    }

    @Override public String getHost()
    {
        return getString(R.string.intent_host_security);
    }

    @Override public boolean isHandlableIntent(Intent intent)
    {
        return super.isHandlableIntent(intent) &&
                isHandlableHost(intent.getData().getHost());
    }

    public boolean isHandlableHost(String host)
    {
        return getHost().equals(host);
    }

    @Override protected SecurityPushBuyIntent create(Intent intent, List<String> pathSegments)
    {
        return new SecurityPushBuyIntent(
                resources,
                SecurityPushBuyIntent.getSecurityIntegerId(resources, pathSegments),
                SecurityPushBuyIntent.getSecurityId(resources, pathSegments));
    }
}

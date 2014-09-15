package com.tradehero.th.models.intent.competition;

import android.content.Intent;
import com.tradehero.th2.R;
import com.tradehero.th.models.intent.THIntentSubFactory;
import java.util.List;
import javax.inject.Inject;

public class ProviderIntentFactory extends THIntentSubFactory<ProviderIntent>
{
    @Inject public ProviderIntentFactory()
    {
    }

    @Override public String getHost()
    {
        return getString(R.string.intent_host_providers);
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

    @Override public String getAction(List<String> pathSegments)
    {
        return pathSegments.get(getInteger(R.integer.intent_uri_action_provider_path_index_action));
    }

    @Override protected ProviderIntent create(Intent intent, List<String> pathSegments)
    {
        String action = getAction(pathSegments);

        ProviderIntent providerIntent = null;

        if (action.equals(getString(R.string.intent_action_provider_pages)))
        {
            providerIntent = new ProviderPageIntent(
                    ProviderPageIntent.getProviderId(pathSegments),
                    ProviderPageIntent.getForwardUriPath(pathSegments));
        }

        return providerIntent;
    }
}

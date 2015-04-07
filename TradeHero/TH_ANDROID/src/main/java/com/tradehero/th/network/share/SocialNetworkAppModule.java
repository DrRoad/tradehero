package com.tradehero.th.network.share;

import android.content.Context;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tradehero.th.R;
import com.tradehero.th.auth.operator.FacebookPermissions;
import com.tradehero.th.models.share.ShareDestination;
import com.tradehero.th.models.share.ShareDestinationFactory;
import com.tradehero.th.models.share.ShareDestinationFactoryByResources;
import com.tradehero.th.models.share.ShareDestinationId;
import com.tradehero.th.models.share.ShareDestinationIndexResComparator;
import dagger.Module;
import dagger.Provides;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;
import timber.log.Timber;

import static com.facebook.FacebookPermissionsConstants.PUBLIC_PROFILE;

@Module(
        injects = {
        },
        complete = false,
        library = true
)
public class SocialNetworkAppModule
{
    @Provides @Singleton @FacebookPermissions List<String> provideFacebookPermissions()
    {
        // TODO separate read permission and publish/write permission
        return Arrays.asList(PUBLIC_PROFILE);
    }

    @Provides @Singleton IWXAPI createWeChatAPI(Context context)
    {
        IWXAPI weChatApi = WXAPIFactory.createWXAPI(context, SocialConstants.WECHAT_APP_ID, false);
        weChatApi.registerApp(SocialConstants.WECHAT_APP_ID);
        return weChatApi;
    }

    @Provides(type = Provides.Type.SET_VALUES) @ShareDestinationId Set<Integer> providesShareDestinationFromResources(Context context)
    {
        Set<Integer> destinationIds = new LinkedHashSet<>();
        for (int id : context.getResources().getIntArray(R.array.ordered_share_destinations))
        {
            if (destinationIds.contains(id))
            {
                Timber.e(new IllegalStateException("Destination ids contains twice the id " + id),
                        null);
            }
            destinationIds.add(id);
        }
        return destinationIds;

    }

    @Provides ShareDestinationFactory providesShareDestinationFactory(ShareDestinationFactoryByResources shareDestinationFactoryByResources)
    {
        return shareDestinationFactoryByResources;
    }

    @Provides Comparator<ShareDestination> providesShareDestinationComparator(ShareDestinationIndexResComparator shareDestinationComparator)
    {
        return shareDestinationComparator;
    }
}

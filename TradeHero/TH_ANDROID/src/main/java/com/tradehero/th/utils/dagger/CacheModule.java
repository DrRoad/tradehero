package com.tradehero.th.utils.dagger;

import android.content.Context;
import android.content.SharedPreferences;
import com.squareup.picasso.LruCache;
import com.tradehero.common.annotation.ForUser;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.common.persistence.prefs.LongPreference;
import com.tradehero.th.fragments.discussion.NewsDiscussionFragment;
import com.tradehero.th.fragments.discussion.TimelineDiscussionFragment;
import com.tradehero.th.fragments.settings.SettingsAlipayFragment;
import com.tradehero.th.fragments.settings.SettingsPayPalFragment;
import com.tradehero.th.fragments.trending.ExtraTileAdapter;
import com.tradehero.th.fragments.trending.ProviderTileView;
import com.tradehero.th.persistence.ListCacheMaxSize;
import com.tradehero.th.persistence.MessageListTimeline;
import com.tradehero.th.persistence.SingleCacheMaxSize;
import com.tradehero.th.persistence.user.UserProfileFetchAssistant;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(
        injects = {
                UserProfileFetchAssistant.class,
                SettingsPayPalFragment.class,
                SettingsAlipayFragment.class,

                // Extra Tile needs to know about userProfile data for survey tile element
                ExtraTileAdapter.class,
                ProviderTileView.class,

                TimelineDiscussionFragment.class,
                NewsDiscussionFragment.class,
        },
        complete = false,
        library = true
)
public class CacheModule
{
    @Provides @Singleton @ForPicasso LruCache providePicassoMemCache(Context context)
    {
        //return new LruMemFileCache(context);
        //return LruMemFileCache.getInstance(context.getApplicationContext());
        return new LruCache(context);
    }

    @Provides @Singleton @SingleCacheMaxSize IntPreference provideDefaultSingleCacheMaxSize(@ForUser SharedPreferences preference)
    {
        return new IntPreference(preference, SingleCacheMaxSize.class.getName(), 1000);
    }

    @Provides @Singleton @ListCacheMaxSize IntPreference provideListSingleCacheMaxSize(@ForUser SharedPreferences preference)
    {
        return new IntPreference(preference, ListCacheMaxSize.class.getName(), 200);
    }

    @Provides @Singleton @MessageListTimeline LongPreference provideMessageListTimeline(@ForUser SharedPreferences preference)
    {
        return new LongPreference(preference, MessageListTimeline.class.getName(), -1L);
    }
}

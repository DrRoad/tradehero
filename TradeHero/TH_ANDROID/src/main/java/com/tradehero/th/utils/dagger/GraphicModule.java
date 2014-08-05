package com.tradehero.th.utils.dagger;

import android.content.Context;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.tradehero.th.fragments.alert.AlertItemView;
import com.tradehero.th.fragments.competition.AdView;
import com.tradehero.th.fragments.discussion.CommentItemViewLinear;
import com.tradehero.th.fragments.trending.EarnCreditTileView;
import com.tradehero.th.fragments.trending.ExtraCashTileView;
import com.tradehero.th.fragments.trending.ResetPortfolioTileView;
import com.tradehero.th.fragments.trending.SurveyTileView;
import com.tradehero.th.models.graphics.TransformationModule;
import com.tradehero.th.utils.BitmapForProfileFactory;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.GraphicUtil;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
        includes = {
                TransformationModule.class
        },
        injects = {
                AlertItemView.class,

                SurveyTileView.class,
                ResetPortfolioTileView.class,
                EarnCreditTileView.class,
                ExtraCashTileView.class,

                AdView.class,
                CommentItemViewLinear.class,
        },
        complete = false,
        library = true // TODO remove
)
public class GraphicModule
{
    @Provides @Singleton Picasso providePicasso(Context context, @ForPicasso LruCache lruFileCache)
    {
        Picasso mPicasso = new Picasso.Builder(context)
                .memoryCache(lruFileCache)
                .build();
		mPicasso.setIndicatorsEnabled(Constants.PICASSO_DEBUG);
		mPicasso.setLoggingEnabled(Constants.PICASSO_DEBUG);
        return mPicasso;
    }

    @Provides BitmapForProfileFactory provideBitmapForProfileFactory(GraphicUtil graphicUtil)
    {
        return graphicUtil;
    }
}

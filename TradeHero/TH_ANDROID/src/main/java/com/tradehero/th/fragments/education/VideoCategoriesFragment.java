package com.tradehero.th.fragments.education;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import com.tradehero.common.persistence.DTOCacheRx;
import com.tradehero.th.R;
import com.tradehero.th.api.education.PagedVideoCategories;
import com.tradehero.th.api.education.PaginatedVideoCategoryDTO;
import com.tradehero.th.api.education.VideoCategoryDTO;
import com.tradehero.th.api.education.VideoCategoryDTOList;
import com.tradehero.th.fragments.BasePagedListRxFragment;
import com.tradehero.th.persistence.education.PaginatedVideoCategoryCacheRx;
import com.tradehero.th.utils.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import javax.inject.Inject;

public class VideoCategoriesFragment extends BasePagedListRxFragment<
        PagedVideoCategories,
        VideoCategoryDTO,
        VideoCategoryDTOList,
        PaginatedVideoCategoryDTO,
        VideoCategoryView
        >
{
    @Inject PaginatedVideoCategoryCacheRx paginatedVideoCategoryCache;
    @Inject Analytics analytics;

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        scheduleRequestData();
    }

    @Override public void onResume()
    {
        super.onResume();
        analytics.fireEvent(new SimpleEvent(AnalyticsConstants.TabBar_Academy));
    }

    @Override protected int getFragmentLayoutResId()
    {
        return R.layout.fragment_video_categories;
    }

    @Override protected VideoCategoriesAdapter createItemViewAdapter()
    {
        return new VideoCategoriesAdapter(getActivity(), R.layout.video_category_item_view);
    }

    @Override protected DTOCacheRx<PagedVideoCategories, PaginatedVideoCategoryDTO> getCache()
    {
        return paginatedVideoCategoryCache;
    }

    @Override public boolean canMakePagedDtoKey()
    {
        return true;
    }

    @NonNull @Override public PagedVideoCategories makePagedDtoKey(int page)
    {
        return new PagedVideoCategories(page, perPage);
    }
}
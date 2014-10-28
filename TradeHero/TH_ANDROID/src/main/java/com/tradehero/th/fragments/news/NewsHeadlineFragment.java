package com.tradehero.th.fragments.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.BottomTabsQuickReturnListViewListener;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.news.NewsItemCompactDTO;
import com.tradehero.th.api.news.key.NewsItemDTOKey;
import com.tradehero.th.api.news.key.NewsItemListKey;
import com.tradehero.th.api.news.key.NewsItemListSecurityKey;
import com.tradehero.th.api.pagination.PaginatedDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.discussion.NewsDiscussionFragment;
import com.tradehero.th.fragments.security.AbstractSecurityInfoFragment;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.persistence.discussion.DiscussionCache;
import com.tradehero.th.persistence.news.NewsItemCompactListCacheNew;
import com.tradehero.th.persistence.security.SecurityCompactCache;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

/**
 * Display a ListView of News object for a given SecurityId - It uses the NewsHeadlineCache to get or fetch the news from an abstract provider as
 * needed. In case the news are not in the cache, the download is done in the background using the `fetchSecurityTask` AsyncTask. The task is
 * cancelled when the fragment is paused.
 */
public class NewsHeadlineFragment extends AbstractSecurityInfoFragment<SecurityCompactDTO>
{
    @Inject SecurityCompactCache securityCompactCache;
    @Inject NewsItemCompactListCacheNew newsTitleCache;
    @Inject protected DiscussionCache discussionCache;
    @Inject Lazy<DashboardNavigator> navigator;
    @InjectView(R.id.list_news_headline_wrapper) BetterViewAnimator listViewWrapper;
    @InjectView(R.id.list_news_headline) ListView listView;
    @InjectView(R.id.list_news_headline_progressbar) ProgressBar progressBar;

    @Inject @BottomTabsQuickReturnListViewListener AbsListView.OnScrollListener dashboardTabListViewScrollListener;

    @Nullable private DTOCacheNew.Listener<NewsItemListKey, PaginatedDTO<NewsItemCompactDTO>> newsCacheListener;
    private NewsHeadlineAdapter adapter;
    private PaginatedDTO<NewsItemCompactDTO> paginatedNews;

    private DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> discussionFetchListener;
    protected AbstractDiscussionCompactDTO abstractDiscussionCompactDTO;

    public static final String TEST_KEY = "News-Test";
    public static long start = 0;

    private int tempPosition = 0;
    private NewsItemDTOKey tempDto = null;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        HierarchyInjector.inject(this);
        newsCacheListener = createNewsCacheListener();
        discussionFetchListener = createDiscussionFetchListener();
        start = System.currentTimeMillis();
    }

    @Override public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_news_headline_list, container, false);

        ButterKnife.inject(this, view);
        initViews(view);
        return view;
    }

    protected DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> createDiscussionFetchListener()
    {
        return new DiscussionFetchListener();
    }

    private class DiscussionFetchListener
            implements DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO>
    {
        @Override
        public void onDTOReceived(@NotNull DiscussionKey key, @NotNull AbstractDiscussionCompactDTO value)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(@NotNull DiscussionKey key, @NotNull Throwable error)
        {
            THToast.show(new THException(error));
        }
    }

    protected void linkWith(AbstractDiscussionCompactDTO abstractDiscussionDTO, boolean andDisplay)
    {
        this.abstractDiscussionCompactDTO = abstractDiscussionDTO;
        Bundle bundle = new Bundle();
        if (abstractDiscussionCompactDTO != null
                && ((NewsItemCompactDTO) abstractDiscussionCompactDTO).url != null)
        {
            WebViewFragment.putUrl(bundle, ((NewsItemCompactDTO) abstractDiscussionCompactDTO).url);
            navigator.get().pushFragment(WebViewFragment.class, bundle);
        }
        else
        {
            handleNewClicked(tempPosition, tempDto);
        }
    }

    private void initViews(View view)
    {
        adapter = new NewsHeadlineAdapter(getActivity(), getActivity().getLayoutInflater(),
                R.layout.news_headline_item_view);

        showLoadingNews();
        if (listView != null)
        {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(@NotNull AdapterView<?> adapterView, View view, int position, long l)
                {
                    Object o = adapterView.getItemAtPosition(position);
                    if (o instanceof NewsItemDTOKey)
                    {
                        handleNewsClicked(position, (NewsItemDTOKey) o);
                    }
                }
            });
            listView.setOnScrollListener(dashboardTabListViewScrollListener);
        }
    }

    private void showNewsList()
    {
        listViewWrapper.setDisplayedChildByLayoutId(listView.getId());
    }

    private void showLoadingNews()
    {
        listViewWrapper.setDisplayedChildByLayoutId(progressBar.getId());
    }

    @Override public void onStop()
    {
        detachSecurityCache();
        detachFetchDiscussionTask();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        newsTitleCache.unregister(newsCacheListener);

        if (listView != null)
        {
            listView.setOnItemClickListener(null);
            listView.setOnScrollListener(null);
        }
        listView = null;
        adapter = null;

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        discussionFetchListener = null;
        newsCacheListener = null;
        super.onDestroy();
    }

    @Override protected SecurityCompactCache getInfoCache()
    {
        return securityCompactCache;
    }

    @Override public void linkWith(@NotNull SecurityId securityId, boolean andDisplay)
    {
        super.linkWith(securityId, andDisplay);
        fetchSecurity(securityId);
    }

    public void linkWith(SecurityCompactDTO securityCompactDTO, boolean andDisplay)
    {
        value = securityCompactDTO;

        if (this.value != null)
        {
            fetchSecurityNews();
        }
    }

    protected void detachSecurityCache()
    {
        securityCompactCache.unregister(this);
    }

    protected void fetchSecurity(@NotNull SecurityId securityId)
    {
        detachSecurityCache();
        securityCompactCache.register(securityId, this);
        getInfoCache().getOrFetchAsync(securityId);
    }

    private void fetchSecurityNews()
    {
        Timber.d("%s fetchSecurityNews,consume: %s", TEST_KEY, (System.currentTimeMillis() - start));
        detachSecurityCache();

        NewsItemListKey listKey = new NewsItemListSecurityKey(value.getSecurityIntegerId(), null, null);
        newsTitleCache.register(listKey, newsCacheListener);
        newsTitleCache.getOrFetchAsync(listKey, false);
    }

    @Override public void display()
    {
        Timber.d("%s display consume: %s", TEST_KEY, (System.currentTimeMillis() - start));
        displayNewsListView();

        showNewsList();
    }

    public void linkWith(PaginatedDTO<NewsItemCompactDTO> news, boolean andDisplay)
    {
        paginatedNews = news;

        if (andDisplay)
        {
            displayNewsListView();
            showNewsList();
        }
    }

    public void displayNewsListView()
    {
        if (!isDetached() && adapter != null && paginatedNews != null)
        {
            List<NewsItemCompactDTO> data = paginatedNews.getData();
            List<NewsItemDTOKey> newsItemDTOKeyList = new ArrayList<>();

            if (data != null)
            {
                for (NewsItemCompactDTO newsItemDTO : data)
                {
                    newsItemDTOKeyList.add(newsItemDTO.getDiscussionKey());
                }
            }
            adapter.setSecurityId(securityId);
            adapter.setItems(newsItemDTOKeyList);
            adapter.notifyDataSetChanged();
        }
    }

    protected void handleNewsClicked(int position, @Nullable NewsItemDTOKey news)
    {
        //if (news != null)
        //{
        //    int resId = adapter.getBackgroundRes(position);
        //    Bundle bundle = new Bundle();
        //    NewsDiscussionFragment.putBackgroundResId(bundle, resId);
        //    NewsDiscussionFragment.putSecuritySymbol(bundle, securityId.getSecuritySymbol());
        //    NewsDiscussionFragment.putDiscussionKey(bundle, news);
        //    getNavigator().pushFragment(NewsDiscussionFragment.class, bundle);
        //
        //}
        tempPosition = position;
        tempDto = news;
        if (news != null)
        {
            fetchDiscussionDetail(true, news);
        }
    }

    protected void handleNewClicked(int position, NewsItemDTOKey news)
    {
        if (news != null)
        {
            int resId = adapter.getBackgroundRes(position);
            Bundle bundle = new Bundle();
            NewsDiscussionFragment.putBackgroundResId(bundle, resId);
            NewsDiscussionFragment.putSecuritySymbol(bundle, securityId.getSecuritySymbol());
            NewsDiscussionFragment.putDiscussionKey(bundle, news);
            navigator.get().pushFragment(NewsDiscussionFragment.class, bundle);
        }
    }

    private void fetchDiscussionDetail(boolean force, NewsItemDTOKey discussionKey)
    {
        detachFetchDiscussionTask();

        discussionCache.register(discussionKey, discussionFetchListener);
        discussionCache.getOrFetchAsync(discussionKey, force);
    }

    private void detachFetchDiscussionTask()
    {
        discussionCache.unregister(discussionFetchListener);
    }

    @NotNull protected DTOCacheNew.Listener<NewsItemListKey, PaginatedDTO<NewsItemCompactDTO>> createNewsCacheListener()
    {
        return new NewsHeadlineNewsListListener();
    }

    protected class NewsHeadlineNewsListListener implements DTOCacheNew.HurriedListener<NewsItemListKey, PaginatedDTO<NewsItemCompactDTO>>
    {
        @Override public void onPreCachedDTOReceived(
                @NotNull NewsItemListKey key,
                @NotNull PaginatedDTO<NewsItemCompactDTO> value)
        {
            linkWith(value, true);
        }

        @Override public void onDTOReceived(
                @NotNull NewsItemListKey key,
                @NotNull PaginatedDTO<NewsItemCompactDTO> value)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(
                @NotNull NewsItemListKey key,
                @NotNull Throwable error)
        {
            THToast.show(R.string.error_fetch_security_info);
        }
    }
}

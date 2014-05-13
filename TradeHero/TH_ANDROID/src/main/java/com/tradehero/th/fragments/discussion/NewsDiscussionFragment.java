package com.tradehero.th.fragments.discussion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.InjectView;
import butterknife.OnClick;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.dialog.THDialog;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.news.NewsCache;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.api.news.key.NewsItemDTOKey;
import com.tradehero.th.fragments.news.NewsDetailFullView;
import com.tradehero.th.fragments.news.NewsDetailSummaryView;
import com.tradehero.th.fragments.news.NewsDialogLayout;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.widget.VotePair;
import com.tradehero.th.wxapi.WeChatMessageType;
import javax.inject.Inject;
import timber.log.Timber;

public class NewsDiscussionFragment extends AbstractDiscussionFragment
{
    public static final String BUNDLE_KEY_TITLE_BACKGROUND_RES =
            NewsDiscussionFragment.class.getName() + ".title_bg";

    public static final String BUNDLE_KEY_SECURITY_SYMBOL =
            NewsDiscussionFragment.class.getName() + ".security_symbol";

    private NewsItemDTO mDetailNewsItemDTO;

    @Inject NewsCache newsCache;

    @InjectView(R.id.discussion_view) NewsDiscussionView newsDiscussionView;

    @InjectView(R.id.news_detail_summary) NewsDetailSummaryView newsDetailSummaryView;
    @InjectView(R.id.news_detail_full) NewsDetailFullView newsDetailFullView;
    private DiscussionEditPostFragment discussionEditPostFragment;

    @OnClick(R.id.news_start_new_discussion) void onStartNewDiscussion()
    {
        Bundle bundle = new Bundle();
        if (newsItemDTOKey != null)
        {
            bundle.putBundle(DiscussionKey.BUNDLE_KEY_DISCUSSION_KEY_BUNDLE,
                    newsItemDTOKey.getArgs());
        }
        discussionEditPostFragment = (DiscussionEditPostFragment) getNavigator().pushFragment(
                DiscussionEditPostFragment.class, bundle);
    }

    // Action buttons
    @InjectView(R.id.vote_pair) VotePair votePair;

    private NewsItemDTOKey newsItemDTOKey;

    private DTOCache.Listener<NewsItemDTOKey, NewsItemDTO> newsCacheFetchListener;
    private DTOCache.GetOrFetchTask<NewsItemDTOKey, NewsItemDTO> newsFetchTask;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_news_discussion, container, false);
        return view;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        Bundle bundle = getArguments();
        String title = bundle.getString(NewsDiscussionFragment.BUNDLE_KEY_SECURITY_SYMBOL);
        //bundle.putString(NewsDiscussionFragment.BUNDLE_KEY_SECURITY_SYMBOL, securityId.securitySymbol);
        actionBar.setTitle(title);
        Timber.d("onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override public void onDestroyView()
    {
        detachNewsFetchTask();

        super.onDestroyView();
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        newsCacheFetchListener = new NewsFetchListener();
    }

    @Override public void onResume()
    {
        super.onResume();

        if (discussionEditPostFragment != null && discussionEditPostFragment.isPosted())
        {
            newsDiscussionView.refresh();
        }
    }

    @Override public void onDetach()
    {
        newsCacheFetchListener = null;

        super.onDetach();
    }

    @Override public void onDestroy()
    {
        discussionEditPostFragment = null;
        super.onDestroy();
    }

    @Override protected void linkWith(DiscussionKey discussionKey, boolean andDisplay)
    {
        super.linkWith(discussionKey, andDisplay);

        if (discussionKey instanceof NewsItemDTOKey)
        {
            linkWith((NewsItemDTOKey) discussionKey, true);
        }
    }

    private void linkWith(NewsItemDTOKey newsItemDTOKey, boolean andDisplay)
    {
        this.newsItemDTOKey = newsItemDTOKey;

        if (newsItemDTOKey != null)
        {
            NewsItemDTO cachedNews = newsCache.get(newsItemDTOKey);

            linkWith(cachedNews, andDisplay);

            fetchNewsDetail(true);
            setRandomBackground();
        }
        else
        {
            resetViews();
        }
    }

    private void setRandomBackground()
    {
        // TODO have to remove this hack, please!
        int bgRes = getArguments().getInt(BUNDLE_KEY_TITLE_BACKGROUND_RES, 0);
        if (bgRes != 0)
        {
            newsDetailSummaryView.setBackground(bgRes);
        }
    }

    private void resetViews()
    {
        // TODO
    }

    private void fetchNewsDetail(boolean force)
    {
        detachNewsFetchTask();
        newsFetchTask = newsCache.getOrFetch(newsItemDTOKey, force, newsCacheFetchListener);
        newsFetchTask.execute();
    }

    private void detachNewsFetchTask()
    {
        if (newsFetchTask != null)
        {
            newsFetchTask.setListener(null);
        }
        newsFetchTask = null;
    }

    private void linkWith(NewsItemDTO newsItemDTO, boolean andDisplay)
    {
        mDetailNewsItemDTO = newsItemDTO;

        if (andDisplay)
        {
            votePair.display(mDetailNewsItemDTO);
            newsDetailSummaryView.display(mDetailNewsItemDTO);
            newsDetailFullView.display(mDetailNewsItemDTO);
        }
    }

    //<editor-fold desc="Related to share dialog">

    // TODO
    @OnClick(R.id.discussion_action_button_more) void onActionButtonMoreClicked()
    {
        showShareDialog();
    }

    private void showShareDialog()
    {
        View contentView = LayoutInflater.from(getSherlockActivity())
                .inflate(R.layout.sharing_translation_dialog_layout, null);
        THDialog.DialogCallback callback = (THDialog.DialogCallback) contentView;
        ((NewsDialogLayout) contentView).setNewsData(mDetailNewsItemDTO.text,
                mDetailNewsItemDTO.description, mDetailNewsItemDTO.langCode, mDetailNewsItemDTO.id,
                WeChatMessageType.CreateDiscussion.getType());
        THDialog.showUpDialog(getSherlockActivity(), contentView, callback);
    }
    //</editor-fold>

    @Override
    public boolean isTabBarVisible()
    {
        return false;
    }

    private class NewsFetchListener implements DTOCache.Listener<NewsItemDTOKey, NewsItemDTO>
    {
        @Override
        public void onDTOReceived(NewsItemDTOKey key, NewsItemDTO value, boolean fromCache)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(NewsItemDTOKey key, Throwable error)
        {
            THToast.show(new THException(error));
        }
    }
}

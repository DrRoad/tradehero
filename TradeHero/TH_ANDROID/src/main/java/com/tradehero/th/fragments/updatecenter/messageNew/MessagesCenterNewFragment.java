package com.tradehero.th.fragments.updatecenter.messageNew;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.special.residemenu.ResideMenu;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.common.widget.FlagNearEdgeScrollListener;
import com.tradehero.common.widget.swipe.util.Attributes;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.discussion.MessageHeaderDTO;
import com.tradehero.th.api.discussion.ReadablePaginatedMessageHeaderDTO;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.discussion.key.DiscussionKeyFactory;
import com.tradehero.th.api.discussion.key.MessageListKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.fragments.social.AllRelationsFragment;
import com.tradehero.th.fragments.social.message.ReplyPrivateMessageFragment;
import com.tradehero.th.fragments.timeline.MeTimelineFragment;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.fragments.updatecenter.UpdateCenterFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.network.service.MessageServiceWrapper;
import com.tradehero.th.persistence.discussion.DiscussionCacheRx;
import com.tradehero.th.persistence.discussion.DiscussionListCacheRx;
import com.tradehero.th.persistence.message.MessageHeaderListCacheRx;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import org.ocpsoft.prettytime.PrettyTime;
import rx.Observer;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

@Routable("messages")
public class MessagesCenterNewFragment extends BaseFragment
        implements
        ResideMenu.OnMenuListener, MessageListViewAdapter.OnMessageItemClicked
{

    @Inject Lazy<MessageHeaderListCacheRx> messageListCache;
    @Inject Lazy<MessageServiceWrapper> messageServiceWrapper;
    @Inject Lazy<DiscussionListCacheRx> discussionListCache;
    @Inject Lazy<DiscussionCacheRx> discussionCache;
    @Inject CurrentUserId currentUserId;
    @Inject THRouter thRouter;
    @Inject Picasso picasso;
    @Inject PrettyTime prettyTime;
    @Inject @ForUserPhoto Transformation userPhotoTransformation;
    @Nullable private MessageListKey nextMoreRecentMessageListKey;

    @InjectView(R.id.layout_listview) RelativeLayout layout_listview;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.listview) ListView listView;
    @InjectView(android.R.id.progress) ProgressBar progressBar;
    @InjectView(android.R.id.empty) TextView emptyView;
    @InjectView(R.id.error) View errorView;
    @InjectView(R.id.composeLayout) View composeLayout;

    private boolean hasMorePage = true;
    private MessageListViewAdapter listAdapter;

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.update_center_messages_new_fragment, container, false);
        init(view);

        return view;
    }

    @Override public void onDestroy()
    {
        nextMoreRecentMessageListKey = null;
        listAdapter = null;
        super.onDestroy();
    }

    @Override public void onStart()
    {
        super.onStart();
        //if size of items already fetched is 0,then force to reload
        if (listAdapter == null || listAdapter.getCount() == 0)
        {
            Timber.d("onStart fetch again");
            displayLoadingView(true);
            getOrFetchMessages();
        }
        else
        {
            Timber.d("onStart don't have to fetch again");
            hideLoadingView();
            setReadAllLayoutVisible();
        }
    }

    @Override public void onResume()
    {
        super.onResume();
        setReadAllLayoutVisible();
    }

    private void init(View view)
    {
        HierarchyInjector.inject(this);
        ButterKnife.inject(this, view);
        if (listAdapter == null)
        {
            listAdapter = new MessageListViewAdapter(getActivity(), prettyTime, picasso, userPhotoTransformation);
            nextMoreRecentMessageListKey = new MessageListKey(MessageListKey.FIRST_PAGE);
        }

        listAdapter.setMode(Attributes.Mode.Multiple);
        listAdapter.setOnMessageItemClicked(this);
        listView.setAdapter(listAdapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Timber.d("onItemClick", "onItemClick:" + position);
                pushMessageFragment(position);
                setMessageRead(position);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override public void onRefresh()
            {
                MessagesCenterNewFragment.this.doRefreshContent();
            }
        });

        setComposeLayoutClickListener();
    }

    private void pushUserProfileFragment(@Nullable MessageHeaderDTO messageHeaderDTO)
    {
        if (messageHeaderDTO != null)
        {
            int currentUser = currentUserId.toUserBaseKey().key;
            Bundle bundle = new Bundle();
            int targetUser = messageHeaderDTO.recipientUserId;
            if (currentUser == messageHeaderDTO.recipientUserId)
            {
                targetUser = messageHeaderDTO.senderUserId;
            }
            UserBaseKey targetUserKey = new UserBaseKey(targetUser);
            thRouter.save(bundle, targetUserKey);
            Timber.d("messageHeaderDTO recipientUserId:%s,senderUserId:%s,currentUserId%s", messageHeaderDTO.recipientUserId,
                    messageHeaderDTO.senderUserId, currentUserId.get());
            if (currentUserId.toUserBaseKey().equals(targetUserKey))
            {
                navigator.get().pushFragment(MeTimelineFragment.class, bundle);
            }
            else
            {
                navigator.get().pushFragment(PushableTimelineFragment.class, bundle);
            }
        }
    }

    protected void pushMessageUser(int position)
    {
        if (listAdapter != null && listAdapter.getItem(position) != null)
        {
            MessageHeaderDTO messageHeaderDTO = listAdapter.getItem(position);
            pushUserProfileFragment(messageHeaderDTO);
        }
    }

    protected void pushMessageFragment(int position)
    {
        if (listAdapter != null && listAdapter.getItem(position) != null)
        {
            MessageHeaderDTO messageHeaderDTO = listAdapter.getItem(position);
            if (messageHeaderDTO != null)
            {
                pushMessageFragment(
                        DiscussionKeyFactory.create(messageHeaderDTO),
                        messageHeaderDTO.getCorrespondentId(currentUserId.toUserBaseKey()));
            }
        }
    }

    protected void pushMessageFragment(@NonNull DiscussionKey discussionKey, @NonNull UserBaseKey correspondentId)
    {
        Bundle args = new Bundle();
        // TODO separate between Private and Broadcast
        ReplyPrivateMessageFragment.putDiscussionKey(args, discussionKey);
        ReplyPrivateMessageFragment.putCorrespondentUserBaseKey(args, correspondentId);
        navigator.get().pushFragment(ReplyPrivateMessageFragment.class, args);
    }

    private void doRefreshContent()
    {
        discussionCache.get().invalidateAll();
        discussionListCache.get().invalidateAll();
        MessageListKey messageListKey = new MessageListKey(MessageListKey.FIRST_PAGE);
        Timber.d("refreshContent %s", messageListKey);
        onStopSubscriptions.add(
                AppObservable.bindFragment(
                        this,
                        messageListCache.get().get(messageListKey))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(createMessageHeaderIdListCacheObserver()));
    }

    @NonNull protected Observer<Pair<MessageListKey, ReadablePaginatedMessageHeaderDTO>> createMessageHeaderIdListCacheObserver()
    {
        return new MessageFetchObserver();
    }

    class MessageFetchObserver implements Observer<Pair<MessageListKey, ReadablePaginatedMessageHeaderDTO>>
    {
        @Override public void onNext(Pair<MessageListKey, ReadablePaginatedMessageHeaderDTO> pair)
        {
            if (pair.second.getData().size() == 0)
            {
                hasMorePage = false;
            }
            if (pair.first.page == MessageListKey.FIRST_PAGE)
            {
                listAdapter.resetListData();
            }
            displayContent(pair.second.getData());
            onRefreshCompleted();
        }

        @Override public void onCompleted()
        {
            onRefreshCompleted();
        }

        @Override public void onError(Throwable e)
        {
            hasMorePage = true;
            onRefreshCompleted();
            decreasePageNumber();

            if (listAdapter != null && listAdapter.getCount() > 0)
            {
                //when already fetch the data,do not show error view
                hideLoadingView();
            }
            else
            {
                displayErrorView();
            }
        }
    }

    private void displayContent(List<MessageHeaderDTO> value)
    {
        showListView();
        appendMessagesList(value);
        setReadAllLayoutVisible();
    }

    private void appendMessagesList(List<MessageHeaderDTO> messageHeaderDTOs)
    {
        if (listAdapter != null)
        {
            listAdapter.closeAllItems();
            if (listAdapter.getCount() == 0)
            {
                listAdapter.setListData(messageHeaderDTOs);
            }
            else
            {
                listAdapter.appendTail(messageHeaderDTOs);
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    private void setReadAllLayoutVisible()
    {
        //boolean haveUnread = false;
        //if (listAdapter == null) return;
        //int itemCount = listAdapter.getCount();
        //for (int i = 0; i < itemCount; i++)
        //{
        //    if (listAdapter.getItem(i).unread)
        //    {
        //        haveUnread = true;
        //        break;
        //    }
        //}

        if (composeLayout != null)
        {
            composeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void decreasePageNumber()
    {
        if (nextMoreRecentMessageListKey == null)
        {
            return;
        }
        nextMoreRecentMessageListKey = nextMoreRecentMessageListKey.prev();
    }

    private void getOrFetchMessages()
    {
        if (nextMoreRecentMessageListKey != null)
        {
            onStopSubscriptions.add(
                    AppObservable.bindFragment(
                            this,
                            messageListCache.get().get(nextMoreRecentMessageListKey))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(createMessageHeaderIdListCacheObserver()));
        }
    }

    @Override public void openMenu()
    {

    }

    @Override public void closeMenu()
    {

    }

    private void onRefreshCompleted()
    {
        if (swipeRefreshLayout != null)
        {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    class OnScrollListener extends FlagNearEdgeScrollListener
    {
        final AbsListView.OnScrollListener onScrollListener;

        //<editor-fold desc="Constructors">
        public OnScrollListener(AbsListView.OnScrollListener onScrollListener)
        {
            activateEnd();
            this.onScrollListener = onScrollListener;
        }
        //</editor-fold>

        @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount)
        {
            if (onScrollListener != null)
            {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            Timber.d("onScroll called");
            // if the count of messages is too fewer，onScroll may not be called
            //updateReadStatus(firstVisibleItem, visibleItemCount);

            super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        @Override public void onScrollStateChanged(AbsListView view, int state)
        {
            if (onScrollListener != null)
            {
                onScrollListener.onScrollStateChanged(view, state);
            }
            super.onScrollStateChanged(view, state);
        }

        @Override public void raiseEndFlag()
        {
            Timber.d("raiseEndFlag");
            if (hasMorePage)
            {
                loadNextMessages();
            }
        }
    }

    private void loadNextMessages()
    {
        increasePageNumber();
        getOrFetchMessages();
    }

    private void increasePageNumber()
    {
        if (nextMoreRecentMessageListKey == null)
        {
            resetPageNumber();
        }
        else
        {
            nextMoreRecentMessageListKey = nextMoreRecentMessageListKey.next();
        }
    }

    private void resetPageNumber()
    {
        nextMoreRecentMessageListKey =
                new MessageListKey(MessageListKey.FIRST_PAGE);
    }

    private void removeMessageIfNecessary(int position)
    {
        if (listAdapter != null && listAdapter.getItem(position) != null)
        {
            MessageHeaderDTO messageHeaderDTO = listAdapter.getItem(position);
            removeMessageOnServer(messageHeaderDTO);
        }
    }

    private void removeMessageOnServer(@NonNull final MessageHeaderDTO messageHeaderDTO)
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                messageServiceWrapper.get().deleteMessageRx(
                        messageHeaderDTO.getDTOKey(),
                        messageHeaderDTO.getSenderId(),
                        messageHeaderDTO.getRecipientId(),
                        currentUserId.toUserBaseKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<BaseResponseDTO>()
                        {
                            @Override public void call(BaseResponseDTO response)
                            {
                                MessagesCenterNewFragment.this.onMessageDeleted(messageHeaderDTO);
                            }
                        },
                        new ToastOnErrorAction()));
        //MessagesCenterNewFragment.this.onMessageDeleted(messageHeaderDTO);
    }

    public void onMessageDeleted(@NonNull MessageHeaderDTO messageHeaderDTO)
    {
        if (listAdapter != null)
        {
            listAdapter.remove(messageHeaderDTO);
            listAdapter.closeAllItems();
            listAdapter.notifyDataSetChanged();
        }
    }

    private void displayLoadingView(boolean onlyShowLoadingView)
    {
        showLoadingView(onlyShowLoadingView);
    }

    private void displayErrorView()
    {
        showErrorView();
    }

    public void showErrorView()
    {
        showOnlyThis(errorView);
    }

    public void showListView()
    {
        showOnlyThis(layout_listview);
    }

    private void hideLoadingView()
    {
        showListView();
    }

    public void showEmptyView()
    {
        showOnlyThis(emptyView);
    }

    public void showLoadingView(boolean onlyShowLoadingView)
    {
        showOnlyThis(progressBar);
        if (!onlyShowLoadingView)
        {
            changeViewVisibility(layout_listview, true);
        }
    }

    private void showOnlyThis(View view)
    {
        changeViewVisibility(layout_listview, view == layout_listview);
        changeViewVisibility(errorView, view == errorView);
        changeViewVisibility(progressBar, view == progressBar);
        changeViewVisibility(emptyView, view == emptyView);
    }

    private void changeViewVisibility(View view, boolean visible)
    {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setComposeLayoutClickListener()
    {
        if (composeLayout != null)
        {
            composeLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    //MessagesCenterNewFragment.this.reportMessageAllRead();
                    navigator.get().pushFragment(AllRelationsFragment.class);
                }
            });
        }
    }

    private void reportMessageAllRead()
    {
        Timber.d("reportMessageAllRead...");
        onStopSubscriptions.add(
                AppObservable.bindFragment(
                        this,
                        messageServiceWrapper.get().readAllMessageRx(
                                currentUserId.toUserBaseKey()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Action1<BaseResponseDTO>()
                                {
                                    @Override public void call(BaseResponseDTO args)
                                    {
                                        MessagesCenterNewFragment.this.updateAllAsRead();
                                    }
                                },
                                new ToastOnErrorAction()
                        ));

        //Mark this locally as read, makes the user feels it's marked instantly for better experience
        updateAllAsRead();
    }

    private void setMessageRead(int position)
    {
        if (listAdapter!=null && position < listAdapter.getCount())
        {
            listAdapter.getItem(position).unread = false;
        }
    }

    private void updateAllAsRead()
    {
        setAllMessageRead();
        setReadAllLayoutVisible();
        requestUpdateTabCounter();
    }

    private void setAllMessageRead()
    {
        if (listAdapter == null) return;
        int itemCount = listAdapter.getCount();
        for (int i = 0; i < itemCount; i++)
        {
            listAdapter.getItem(i).unread = false;
        }
        listAdapter.notifyDataSetChanged();
    }

    private void requestUpdateTabCounter()
    {
        Intent requestUpdateIntent = new Intent(UpdateCenterFragment.REQUEST_UPDATE_UNREAD_COUNTER);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(requestUpdateIntent);
    }

    @Override public void clickedItemUser(int position)
    {
        pushMessageUser(position);
    }

    @Override public void clickedItemDelete(int position)
    {
        removeMessageIfNecessary(position);
    }
}

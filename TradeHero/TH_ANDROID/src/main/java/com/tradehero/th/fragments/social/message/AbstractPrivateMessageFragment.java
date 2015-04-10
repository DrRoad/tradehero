package com.tradehero.th.fragments.social.message;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionKeyList;
import com.tradehero.th.api.discussion.MessageHeaderDTO;
import com.tradehero.th.api.discussion.MessageType;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.discussion.key.DiscussionListKey;
import com.tradehero.th.api.discussion.key.MessageDiscussionListKey;
import com.tradehero.th.api.discussion.key.MessageDiscussionListKeyFactory;
import com.tradehero.th.api.discussion.key.MessageHeaderId;
import com.tradehero.th.api.discussion.key.MessageHeaderUserId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewLinear;
import com.tradehero.th.fragments.discussion.AbstractDiscussionFragment;
import com.tradehero.th.fragments.discussion.DiscussionSetAdapter;
import com.tradehero.th.fragments.discussion.PrivateDiscussionSetAdapter;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.MessageServiceWrapper;
import com.tradehero.th.persistence.message.MessageHeaderCacheRx;
import com.tradehero.th.persistence.message.MessageHeaderListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.TimberOnErrorAction;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

abstract public class AbstractPrivateMessageFragment extends AbstractDiscussionFragment
{
    private static final String CORRESPONDENT_USER_BASE_BUNDLE_KEY =
            AbstractPrivateMessageFragment.class.getName() + ".correspondentUserBaseKey";

    @Inject protected MessageHeaderCacheRx messageHeaderCache;
    @Inject protected MessageHeaderListCacheRx messageHeaderListCache;
    @Inject protected CurrentUserId currentUserId;
    @Inject protected Picasso picasso;
    @Inject protected UserProfileCacheRx userProfileCache;
    @Inject Lazy<MessageServiceWrapper> messageServiceWrapper;

    protected UserBaseKey correspondentId;
    protected UserProfileDTO correspondentProfile;

    @InjectView(android.R.id.list) protected ListView discussionList;
    @InjectView(R.id.discussion_comment_widget) protected PrivatePostCommentView postWidget;
    @InjectView(R.id.private_message_empty) protected TextView emptyHint;
    @InjectView(R.id.post_comment_action_submit) protected TextView buttonSend;
    @InjectView(R.id.post_comment_text) protected EditText messageToSend;

    @Nullable private Subscription messageHeaderFetchSubscription;
    private MessageHeaderId messageHeaderId;

    public static void putCorrespondentUserBaseKey(@NonNull Bundle args, @NonNull UserBaseKey correspondentBaseKey)
    {
        args.putBundle(CORRESPONDENT_USER_BASE_BUNDLE_KEY, correspondentBaseKey.getArgs());
    }

    @NonNull private static UserBaseKey collectCorrespondentId(@NonNull Bundle args)
    {
        return new UserBaseKey(args.getBundle(CORRESPONDENT_USER_BASE_BUNDLE_KEY));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        correspondentId = collectCorrespondentId(getArguments());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_private_message, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        messageToSend.setHint(R.string.private_message_message_hint);
        buttonSend.setText(R.string.private_message_btn_send);
        if (postWidget != null)
        {
            postWidget.linkWith(MessageType.PRIVATE);
            postWidget.setRecipient(correspondentId);
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.private_message_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.private_message_refresh_btn:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchCorrespondentProfile();
    }

    @Override public void onDestroyOptionsMenu()
    {
        setActionBarSubtitle(null);
        super.onDestroyOptionsMenu();
    }

    @Override public void onDestroyView()
    {
        unsubscribe(messageHeaderFetchSubscription);
        messageHeaderFetchSubscription = null;
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        messageHeaderFetchSubscription = null;
        super.onDestroy();
    }

    @NonNull @Override protected DiscussionSetAdapter createDiscussionListAdapter()
    {
        return new PrivateDiscussionSetAdapter(
                getActivity(),
                discussionCache,
                currentUserId,
                R.layout.private_message_bubble_mine,
                R.layout.private_message_bubble_other);
    }

    @Nullable @Override protected DiscussionListKey getNextKey(@NonNull DiscussionListKey latestKey,
            @NonNull List<AbstractDiscussionCompactItemViewLinear.DTO> latestDtos)
    {
        DiscussionKeyList discussionKeys = new DiscussionKeyList();
        for (AbstractDiscussionCompactItemViewLinear.DTO dto : latestDtos)
        {
            discussionKeys.add(dto.viewHolderDTO.discussionDTO.getDiscussionKey());
        }
        DiscussionListKey next = MessageDiscussionListKeyFactory.next((MessageDiscussionListKey) latestKey, discussionKeys);
        if (next != null && next.equals(latestKey))
        {
            // This situation where next is equal to currentNext may happen
            // when the server is still returning the same values
            next = null;
        }
        return next;
    }

    @Nullable @Override protected DiscussionListKey getMostRecentKey(@NonNull DiscussionListKey latestKey,
            @NonNull List<AbstractDiscussionCompactItemViewLinear.DTO> newestDtos)
    {
        DiscussionListKey prev;
        DiscussionKeyList discussionKeys = new DiscussionKeyList();
        for (AbstractDiscussionCompactItemViewLinear.DTO dto : newestDtos)
        {
            discussionKeys.add(dto.viewHolderDTO.discussionDTO.getDiscussionKey());
        }
        prev = MessageDiscussionListKeyFactory.prev((MessageDiscussionListKey) latestKey, discussionKeys);
        if (prev != null && prev.equals(latestKey))
        {
            // This situation where next is equal to currentNext may happen
            // when the server is still returning the same values
            prev = null;
        }
        return prev;
    }

    @NonNull @Override protected Observable<AbstractDiscussionCompactItemViewLinear.DTO> createViewDTO(
            @NonNull AbstractDiscussionCompactDTO discussion)
    {
        return viewDTOFactory.createDiscussionItemViewLinearDTO((DiscussionDTO) discussion);
    }

    @Override protected void displayTopic(@NonNull AbstractDiscussionCompactDTO discussionDTO)
    {
        super.displayTopic(discussionDTO);
        if (topicView == null)
        {
            topicView = inflateTopicView();
            try
            {
                discussionList.addHeaderView(topicView, null, false);
            }
            catch (Exception e)
            {
                // Can happen on older APIs.
                Timber.e(e, "Failed adding topic view");
            }
        }
    }

    @Override protected void linkWith(DiscussionKey discussionKey, boolean andDisplay)
    {
        super.linkWith(discussionKey, andDisplay);

        linkWith(new MessageHeaderUserId(discussionKey.id, correspondentId));
    }

    private void linkWith(MessageHeaderId messageHeaderId)
    {
        this.messageHeaderId = messageHeaderId;
        unsubscribe(messageHeaderFetchSubscription);
        messageHeaderFetchSubscription = AppObservable.bindFragment(
                this,
                messageHeaderCache.get(messageHeaderId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createMessageHeaderCacheObserver());
    }

    protected void refresh()
    {
        //if (getDiscussionKey() != null && discussionView != null)
        //{
        //    discussionView.refresh();
        //
        //    if (messageHeaderId != null)
        //    {
        //        MessageHeaderDTO messageHeaderDTO = messageHeaderCache.getCachedValue(messageHeaderId);
        //        if (messageHeaderDTO != null)
        //        {
        //            reportMessageRead(messageHeaderDTO);
        //        }
        //    }
        //}

        if (messageHeaderId != null)
        {
            MessageHeaderDTO messageHeaderDTO = messageHeaderCache.getCachedValue(messageHeaderId);
            if (messageHeaderDTO != null)
            {
                reportMessageRead(messageHeaderDTO);
            }
        }
    }

    private void fetchCorrespondentProfile()
    {
        Timber.d("fetchCorrespondentProfile");
        onStopSubscriptions.add(AppObservable.bindFragment(this, userProfileCache.get(correspondentId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Pair<UserBaseKey, UserProfileDTO>>()
                        {
                            @Override public void call(Pair<UserBaseKey, UserProfileDTO> pair)
                            {
                                linkWith(pair.second);
                            }
                        },
                        new TimberOnErrorAction("")));
    }

    public void linkWith(UserProfileDTO userProfileDTO)
    {
        Timber.d("userProfile %s", userProfileDTO);
        correspondentProfile = userProfileDTO;
        getActivity().invalidateOptionsMenu();
    }

    //TODO set actionBar with MessageHeaderDTO by alex

    @Override protected void handleCommentPosted(DiscussionDTO discussionDTO)
    {
        addComment(discussionDTO);
        // TODO Move into DTOProcessor?
        messageHeaderListCache.invalidateWithRecipient(correspondentId);
    }

    public void reportMessageRead(MessageHeaderDTO messageHeaderDTO)
    {
        messageHeaderCache.setUnread(messageHeaderDTO.getDTOKey(), false);
        onStopSubscriptions.add(messageServiceWrapper.get().readMessageRx(
                messageHeaderDTO.getDTOKey(),
                messageHeaderDTO.getSenderId(),
                messageHeaderDTO.getRecipientId(),
                messageHeaderDTO.getDTOKey(),
                currentUserId.toUserBaseKey())
                .subscribe(createMessageAsReadCallback(messageHeaderDTO.getDTOKey())));
    }

    private Observer<BaseResponseDTO> createMessageAsReadCallback(MessageHeaderId messageHeaderId)
    {
        return new MessageMarkAsReadObserver(messageHeaderId);
    }

    private class MessageMarkAsReadObserver implements Observer<BaseResponseDTO>
    {
        private final MessageHeaderId messageHeaderId;

        public MessageMarkAsReadObserver(MessageHeaderId messageHeaderId)
        {
            this.messageHeaderId = messageHeaderId;
        }

        @Override public void onNext(BaseResponseDTO baseResponseDTO)
        {
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            Timber.d("Report failure for Message: %s", messageHeaderId);
        }
    }

    private Observer<Pair<MessageHeaderId, MessageHeaderDTO>> createMessageHeaderCacheObserver()
    {
        return new MessageHeaderFetchObserver();
    }

    private class MessageHeaderFetchObserver
            implements Observer<Pair<MessageHeaderId, MessageHeaderDTO>>
    {
        @Override public void onNext(Pair<MessageHeaderId, MessageHeaderDTO> pair)
        {
            Timber.d("MessageHeaderDTO=%s", pair.second);
            setActionBarTitle(pair.second.title);
            setActionBarSubtitle(pair.second.subTitle);
            if (pair.second.unread)
            {
                reportMessageRead(pair.second);
            }
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            if (e instanceof RetrofitError)
            {
                THToast.show(new THException(e));
            }
        }
    }
}

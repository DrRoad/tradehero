package com.tradehero.th.fragments.discussion.stock;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import butterknife.Bind;
import butterknife.OnClick;
import com.etiennelawlor.quickreturn.library.enums.QuickReturnViewType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.key.DiscussionListKey;
import com.tradehero.th.api.discussion.key.PaginatedDiscussionListKey;
import com.tradehero.th.api.discussion.key.SecurityDiscussionKey;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewLinear;
import com.tradehero.th.fragments.discussion.AbstractDiscussionFragment;
import com.tradehero.th.fragments.discussion.DiscussionSetAdapter;
import com.tradehero.th.fragments.discussion.SecurityDiscussionEditPostFragment;
import com.tradehero.th.fragments.discussion.SingleViewDiscussionSetAdapter;
import com.tradehero.th.persistence.discussion.DiscussionListCacheRx;
import com.tradehero.th.persistence.security.SecurityCompactCacheRx;
import com.tradehero.th.widget.MultiScrollListener;
import javax.inject.Inject;
import rx.Observable;
import rx.functions.Func1;

public class SecurityDiscussionFragment extends AbstractDiscussionFragment
{
    private static final String BUNDLE_KEY_SECURITY_ID = "securityId";

    @Inject DiscussionListCacheRx discussionListCache;
    @Inject SecurityCompactCacheRx securityCompactCache;
    @Bind(R.id.security_discussion_add) View buttonAdd;
    private SecurityId securityId;

    public static void putSecurityId(Bundle args, SecurityId securityId)
    {
        args.putBundle(BUNDLE_KEY_SECURITY_ID, securityId.getArgs());
    }

    @Nullable public static SecurityId getSecurityId(Bundle args)
    {
        SecurityId extracted = null;
        if (args != null)
        {
            Bundle securityIdBundle = args.getBundle(BUNDLE_KEY_SECURITY_ID);
            if (securityIdBundle != null)
            {
                extracted = new SecurityId(securityIdBundle);
            }
        }
        return extracted;
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        securityId = getSecurityId(getArguments());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.security_discussion, container, false);
    }

    @Override public void onDestroy()
    {
        //invalidate cache
        if (discussionListCache != null)
        {
            discussionListCache.invalidateAllForDiscussionType(DiscussionType.SECURITY);
        }
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.security_discussion_add) void onAddNewDiscussionRequested(View clickedView)
    {
        if (securityId != null)
        {
            Bundle bundle = new Bundle();
            SecurityDiscussionEditPostFragment.putSecurityId(bundle, securityId);
            navigator.get().pushFragment(SecurityDiscussionEditPostFragment.class, bundle);
        }
    }

    @NonNull @Override protected DiscussionSetAdapter createDiscussionListAdapter()
    {
        return new SingleViewDiscussionSetAdapter(
                getActivity(),
                R.layout.security_discussion_item_view);
    }

    @NonNull @Override protected AbsListView.OnScrollListener createListScrollListener()
    {
        return new MultiScrollListener(super.createListScrollListener(), fragmentElements.getListViewScrollListener(),
                new QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.HEADER).header(buttonAdd)
                        .minHeaderTranslation(-getResources().getDimensionPixelSize(R.dimen.clickable_element_min_dimen))
                        .build());
    }

    @NonNull @Override protected Observable<AbstractDiscussionCompactDTO> getTopicObservable()
    {
        return Observable.empty();
    }

    @Nullable @Override protected View inflateTopicView(@NonNull AbstractDiscussionCompactDTO topicDiscussion)
    {
        return null;
    }

    @NonNull @Override protected Observable<DiscussionListKey> createTopicDiscussionListKey()
    {
        return securityCompactCache.getOne(securityId)
                .map(new Func1<Pair<SecurityId, SecurityCompactDTO>, DiscussionListKey>()
                {
                    @Override public DiscussionListKey call(Pair<SecurityId, SecurityCompactDTO> pair)
                    {
                        discussionKey = new SecurityDiscussionKey(pair.second.id);
                        return new PaginatedDiscussionListKey(new DiscussionListKey(DiscussionType.SECURITY, pair.second.id), 1);
                    }
                });
    }

    @NonNull @Override
    protected Observable<AbstractDiscussionCompactItemViewLinear.DTO> createViewDTO(@NonNull AbstractDiscussionCompactDTO discussion)
    {
        return viewDTOFactory.createDiscussionItemViewLinearDTO((DiscussionDTO) discussion);
    }

    @Override protected void handleCommentPosted(DiscussionDTO discussionDTO)
    {
        // Nothing to do?
    }
}

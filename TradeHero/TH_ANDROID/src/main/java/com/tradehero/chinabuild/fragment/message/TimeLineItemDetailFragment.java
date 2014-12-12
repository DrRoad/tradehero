package com.tradehero.chinabuild.fragment.message;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshBase;
import com.squareup.picasso.Picasso;
import com.tradehero.chinabuild.dialog.DialogFactory;
import com.tradehero.chinabuild.dialog.ShareSheetDialogLayout;
import com.tradehero.chinabuild.dialog.TimeLineDetailDialogLayout;
import com.tradehero.chinabuild.fragment.userCenter.UserMainPage;
import com.tradehero.chinabuild.listview.SecurityListView;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.common.widget.dialog.THDialog;
import com.tradehero.th.R;
import com.tradehero.th.adapters.TimeLineBaseAdapter;
import com.tradehero.th.adapters.TimeLineDetailDiscussSecItem;
import com.tradehero.th.api.discussion.*;
import com.tradehero.th.api.discussion.form.DiscussionFormDTO;
import com.tradehero.th.api.discussion.form.DiscussionFormDTOFactory;
import com.tradehero.th.api.discussion.key.*;
import com.tradehero.th.api.news.NewsItemCompactDTO;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.api.share.wechat.WeChatDTO;
import com.tradehero.th.api.share.wechat.WeChatMessageType;
import com.tradehero.th.api.social.InviteFormDTO;
import com.tradehero.th.api.social.InviteFormWeiboDTO;
import com.tradehero.th.api.timeline.TimelineItemDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.DiscussionServiceWrapper;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.network.share.SocialSharer;
import com.tradehero.th.network.share.SocialSharerImpl;
import com.tradehero.th.persistence.discussion.DiscussionCache;
import com.tradehero.th.persistence.discussion.DiscussionListCacheNew;
import com.tradehero.th.persistence.prefs.ShareSheetTitleCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.utils.InputTools;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.utils.WeiboUtils;
import com.tradehero.th.widget.TradeHeroProgressBar;
import dagger.Lazy;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TimeLineItemDetailFragment extends DashboardFragment implements DiscussionListCacheNew.DiscussionKeyListListener, View.OnClickListener {

    public static final String BUNDLE_ARGUMENT_DISCUSSTION_ID = "bundle_argment_discusstion_id";

    @Inject
    protected DiscussionCache discussionCache;
    private DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> discussionFetchListener;
    DiscussionKey timelineItemDTOKey;
    private PaginatedDiscussionListKey discussionListKey;
    @Inject
    DiscussionListCacheNew discussionListCache;

    @InjectView(R.id.btnSend)
    Button btnSend;
    @InjectView(R.id.edtSend)
    EditText edtSend;

    private TimeLineDetailDiscussSecItem adapter;
    @InjectView(R.id.listTimeLine)
    SecurityListView listTimeLine;

    @Inject
    DiscussionKeyFactory discussionKeyFactory;

    private MiddleCallback<DiscussionDTO> discussionEditMiddleCallback;
    private ProgressDialog progressDialog;
    @Inject
    ProgressDialogUtil progressDialogUtil;
    @Inject
    Lazy<DiscussionServiceWrapper> discussionServiceWrapper;
    @Inject
    DiscussionFormDTOFactory discussionFormDTOFactory;

    AbstractDiscussionCompactDTO dataDto;

    @Inject
    public Lazy<PrettyTime> prettyTime;
    @Inject
    Picasso picasso;
    private MiddleCallback<DiscussionDTO> voteCallback;
    private Dialog mShareSheetDialog;
    @Inject
    @ShareSheetTitleCache
    StringPreference mShareSheetTitleCache;

    @InjectView(R.id.tradeheroprogressbar_timeline_detail)
    TradeHeroProgressBar progressBar;
    @InjectView(R.id.bvaViewAll)
    BetterViewAnimator betterViewAnimator;
    @InjectView(R.id.rlAllView)
    RelativeLayout rlAllView;

    @Inject
    UserProfileCache userProfileCache;
    @Inject
    Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject
    Lazy<SocialSharer> socialSharerLazy;
    @Inject
    CurrentUserId currentUserId;

    private LinearLayout llDisscurssOrNews;
    private ImageView imgSecurityTLUserHeader;
    private TextView tvUserTLTimeStamp;
    private TextView tvUserTLContent;
    private TextView tvUserTLName;
    private LinearLayout llTLPraise;
    private LinearLayout llTLComment;
    private LinearLayout llTLShare;
    private TextView tvTLPraise;
    private TextView tvTLComment;
    private TextView tvTLShare;
    private TextView btnTLPraise;

    private LinearLayout mRefreshView;

    private String strReply = "";

    private final int ITEMS_PER_PAGE = 50;

    private Dialog timeLineMenuDialog;
    private DialogFactory dialogFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        discussionFetchListener = createDiscussionCacheListener();
        initArgment();
        adapter = new TimeLineDetailDiscussSecItem(getActivity());
    }

    public void initArgment() {
        Bundle bundle = getArguments();
        if (bundle.containsKey(BUNDLE_ARGUMENT_DISCUSSTION_ID)) {
            timelineItemDTOKey = discussionKeyFactory.fromBundle(bundle.getBundle(BUNDLE_ARGUMENT_DISCUSSTION_ID));
            fetchDiscussion(timelineItemDTOKey, false);
            discussionListKey = new PaginatedDiscussionListKey(timelineItemDTOKey.getType(), timelineItemDTOKey.id, 1, ITEMS_PER_PAGE);
            fetchDiscussList(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setHeadViewMiddleMain("详情");
        setHeadViewRight0(getActivity().getResources().getString(R.string.discovery_discuss_send_more));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timeline_item_detail, container, false);
        ButterKnife.inject(this, view);
        setNeedToMonitorBackPressed(true);
        ListView lv = listTimeLine.getRefreshableView();
        mRefreshView = (LinearLayout) inflater.inflate(R.layout.security_time_line_item_plus, null);
        lv.addHeaderView(mRefreshView);
        initRoot(mRefreshView);

        initView();

        if (dataDto == null) {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.tradeheroprogressbar_timeline_detail);
            progressBar.startLoading();
        } else {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.rlAllView);
        }

        return view;
    }

    public void initRoot(View view) {
        llDisscurssOrNews = (LinearLayout) view.findViewById(R.id.llItemAll);
        imgSecurityTLUserHeader = (ImageView) view.findViewById(R.id.imgSecurityTLUserHeader);
        tvUserTLTimeStamp = (TextView) view.findViewById(R.id.tvUserTLTimeStamp);
        tvUserTLContent = (TextView) view.findViewById(R.id.tvUserTLContent);
        tvUserTLName = (TextView) view.findViewById(R.id.tvUserTLName);
        llTLPraise = (LinearLayout) view.findViewById(R.id.llTLPraise);
        llTLComment = (LinearLayout) view.findViewById(R.id.llTLComment);
        llTLShare = (LinearLayout) view.findViewById(R.id.llTLShare);
        tvTLPraise = (TextView) view.findViewById(R.id.tvTLPraise);
        tvTLComment = (TextView) view.findViewById(R.id.tvTLComment);
        tvTLShare = (TextView) view.findViewById(R.id.tvTLShare);
        btnTLPraise = (TextView) view.findViewById(R.id.btnTLPraise);

        tvUserTLName.setOnClickListener(this);
        imgSecurityTLUserHeader.setOnClickListener(this);
        llTLPraise.setOnClickListener(this);
        llTLComment.setOnClickListener(this);
        llTLShare.setOnClickListener(this);

        llDisscurssOrNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHintForSender(-1);
            }
        });
    }

    public void initView() {
        tvUserTLContent.setMaxLines(1000);
        listTimeLine.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        listTimeLine.setAdapter(adapter);

        listTimeLine.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                discussionListKey.setPage(1);
                fetchDiscussList(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                discussionListKey = discussionListKey.next();
                fetchDiscussList(true);
            }
        });

        adapter.setListener(new TimeLineBaseAdapter.TimeLineOperater() {
            @Override
            public void OnTimeLineItemClicked(int position) {
                setHintForSender(position);
            }

            @Override
            public void OnTimeLinePraiseClicked(int position) {

            }

            @Override
            public void OnTimeLineCommentsClicked(int position) {

            }

            @Override
            public void OnTimeLineShareClicked(int position) {

            }

            @Override
            public void OnTimeLineBuyClicked(int position) {

            }
        });
    }

    public void setDefaultReply() {
        edtSend.setHint(getResources().getString(R.string.please_to_reply));
        strReply = "";
        isReplayFollower = false;
    }

    boolean isReplayFollower = false;

    public void setHintForSender(long position) {
        if (position == -1)//回复主题
        {
            setDefaultReply();
        } else//回复楼层
        {
            AbstractDiscussionCompactDTO dto = adapter.getItem((int) position);
            if (dto == null) {
                return;
            }
            if (dto instanceof DiscussionDTO) {
                String displayName = ((DiscussionDTO) dto).user.getDisplayName();
                int id = ((DiscussionDTO) dto).userId;
                String strHint = "回复 " + displayName + ":";
                if (edtSend != null) {
                    edtSend.setHint(strHint);
                    //"<@(.+?),(\\d+)@>"
                    strReply = "<@@" + displayName + "," + id + "@>";
                    isReplayFollower = true;
                }
            }
        }
        openInputMethod();
    }

    public void openInputMethod() {
        InputTools.KeyBoard(edtSend, "open");
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isReplayFollower) {
            strReply = "";
            edtSend.setText("");
            edtSend.setHint(getResources().getString(R.string.please_to_reply));
            isReplayFollower = false;
        } else {
            popCurrentFragment();
        }
    }


    @Override
    public void onDestroyView() {
        detachDiscussionFetchTask();
        detachDiscussionFetch();
        unsetDiscussionEditMiddleCallback();
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDiscussion(timelineItemDTOKey, false);
    }

    private void fetchDiscussion(DiscussionKey discussionKey, boolean force) {
        detachDiscussionFetchTask();
        discussionCache.register(discussionKey, discussionFetchListener);
        discussionCache.getOrFetchAsync(discussionKey, force);
    }

    private void detachDiscussionFetchTask() {
        discussionCache.unregister(discussionFetchListener);
    }

    private void detachDiscussionFetch() {
        discussionListCache.unregister(this);
    }

    public void fetchDiscussList(boolean force) {
        if (discussionListKey != null) {
            detachDiscussionFetch();
            discussionListCache.register(discussionListKey, this);
            discussionListCache.getOrFetchAsync(discussionListKey, force);
        }
    }

    protected DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> createDiscussionCacheListener() {
        return new PrivateDiscussionViewDiscussionCacheListener();
    }

    @Override
    public void onClick(View view) {
        onOperaterClicked(view);
    }

    protected class PrivateDiscussionViewDiscussionCacheListener implements DTOCacheNew.Listener<DiscussionKey, AbstractDiscussionCompactDTO> {
        @Override
        public void onDTOReceived(@NotNull DiscussionKey key, @NotNull AbstractDiscussionCompactDTO value) {
            //linkWithInitiating((PrivateDiscussionDTO) value, true);
            linkWithDTO(value);
            OnFinish();
        }

        @Override
        public void onErrorThrown(@NotNull DiscussionKey key, @NotNull Throwable error) {
            OnFinish();
        }

        public void OnFinish() {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.rlAllView);
            progressBar.stopLoading();
        }
    }

    public void linkWithDTO(AbstractDiscussionCompactDTO value) {
        this.dataDto = value;
        fetchDiscussList(true);
        displayDiscussOrNewsDTO();
    }

    public AbstractDiscussionCompactDTO getAbstractDiscussionCompactDTO() {
        return dataDto;
    }

    public void displayDiscussOrNewsDTO() {
        AbstractDiscussionCompactDTO dto = getAbstractDiscussionCompactDTO();
        llDisscurssOrNews.setVisibility(dto == null ? View.INVISIBLE : View.VISIBLE);
        if (dto != null) {
            imgSecurityTLUserHeader.setVisibility(dto instanceof NewsItemCompactDTO ? View.GONE : View.VISIBLE);
            tvUserTLName.setVisibility(dto instanceof NewsItemCompactDTO ? View.GONE : View.VISIBLE);

            if (dto instanceof NewsItemDTO) {
                tvUserTLContent.setText(((NewsItemDTO) dto).text);
                tvUserTLTimeStamp.setText(prettyTime.get().formatUnrounded(((NewsItemCompactDTO) dto).createdAtUtc));
            } else if (dto instanceof DiscussionDTO) {
                tvUserTLName.setText(((DiscussionDTO) dto).user.getDisplayName());
                tvUserTLTimeStamp.setText(prettyTime.get().formatUnrounded(((DiscussionDTO) dto).createdAtUtc));
                tvUserTLContent.setText(((DiscussionDTO) dto).text);
                picasso.load(((DiscussionDTO) dto).user.picture)
                        .placeholder(R.drawable.superman_facebook)
                        .error(R.drawable.superman_facebook)
                        .into(imgSecurityTLUserHeader);
            } else if (dto instanceof TimelineItemDTO) {
                tvUserTLTimeStamp.setText(prettyTime.get().formatUnrounded(((TimelineItemDTO) dto).createdAtUtc));
                tvUserTLName.setText(((TimelineItemDTO) dto).user.getDisplayName());
                tvUserTLContent.setText(((TimelineItemDTO) dto).text);
                picasso.load(((TimelineItemDTO) dto).user.picture)
                        .placeholder(R.drawable.superman_facebook)
                        .error(R.drawable.superman_facebook)
                        .into(imgSecurityTLUserHeader);
            }

            if (dto.voteDirection == 1) {
                btnTLPraise.setBackgroundResource(R.drawable.icon_praise_active);
            }
            if (dto.voteDirection == 0) {
                btnTLPraise.setBackgroundResource(R.drawable.icon_praise_normal);
            }

            tvTLComment.setText("" + dto.commentCount);
            tvTLPraise.setText(Html.fromHtml(dto.getVoteString()));
        }
    }

    @Override
    public void onDTOReceived(@NotNull DiscussionListKey key, @NotNull DiscussionKeyList value) {
        List<AbstractDiscussionCompactDTO> listData = new ArrayList<>();
        for (int i = 0; i < value.size(); i++) {
            AbstractDiscussionCompactDTO dto = discussionCache.get(value.get(i));
            listData.add(dto);
        }
        if (discussionListKey.getPage() == 1) {
            adapter.setListData(listData);
        } else {
            adapter.addListData(listData);
        }
        listTimeLine.onRefreshComplete();
        if (adapter.getCount() >= ITEMS_PER_PAGE) {
            listTimeLine.setMode(PullToRefreshBase.Mode.BOTH);
        } else {
            listTimeLine.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
    }

    @Override
    public void onErrorThrown(@NotNull DiscussionListKey key, @NotNull Throwable error) {
        listTimeLine.onRefreshComplete();
    }

    @OnClick(R.id.btnSend)
    public void OnSendClicked() {
        postDiscussion();
    }

    private void unsetDiscussionEditMiddleCallback() {
        if (discussionEditMiddleCallback != null) {
            discussionEditMiddleCallback.setPrimaryCallback(null);
        }
        discussionEditMiddleCallback = null;
    }

    protected void postDiscussion() {
        if (validate()) {
            DiscussionFormDTO discussionFormDTO = buildDiscussionFormDTO();
            if (discussionFormDTO == null) return;
            unsetDiscussionEditMiddleCallback();
            progressDialog = progressDialogUtil.show(getActivity(), R.string.alert_dialog_please_wait, R.string.processing);
            discussionEditMiddleCallback = discussionServiceWrapper.get().createDiscussion(discussionFormDTO, new SecurityDiscussionEditCallback());
        }
    }

    protected DiscussionFormDTO buildDiscussionFormDTO() {
        DiscussionType discussionType = getDiscussionType();
        if (discussionType != null) {
            DiscussionFormDTO discussionFormDTO = discussionFormDTOFactory.createEmpty(discussionType);
            if (timelineItemDTOKey != null) {
                discussionFormDTO.inReplyToId = timelineItemDTOKey.id;
            }
            discussionFormDTO.text = strReply + " " + edtSend.getText().toString();

            return discussionFormDTO;
        }

        return null;
    }

    protected DiscussionType getDiscussionType() {
        if (timelineItemDTOKey != null) {
            return timelineItemDTOKey.getType();
        }
        return null;
    }

    private class SecurityDiscussionEditCallback implements Callback<DiscussionDTO> {
        @Override
        public void success(DiscussionDTO discussionDTO, Response response) {
            onFinish();
            DeviceUtil.dismissKeyboard(getActivity());
            discussionListKey.setPage(1);
            fetchDiscussList(true);
            fetchDiscussion(timelineItemDTOKey, true);
            strReply = "";
            edtSend.setText("");
        }

        @Override
        public void failure(RetrofitError error) {
            onFinish();
            THToast.show(R.string.error_network_connection);
        }

        private void onFinish() {
            if (progressDialog != null) {
                progressDialog.hide();
            }
        }
    }

    private boolean validate() {
        boolean notEmptyText = validateNotEmptyText();
        if (!notEmptyText) {
            THToast.show(R.string.error_discussion_empty_post);
        }
        return notEmptyText;
    }

    private boolean validateNotEmptyText() {
        return !edtSend.getText().toString().trim().isEmpty();
    }

    public void onOperaterClicked(View view) {
        if (view.getId() == R.id.imgSecurityTLUserHeader || view.getId() == R.id.tvUserTLName) {
            //
            AbstractDiscussionCompactDTO dto = getAbstractDiscussionCompactDTO();
            if (dto instanceof DiscussionDTO) {
                openUserProfile(((DiscussionDTO) getAbstractDiscussionCompactDTO()).user.id);
            } else if (dto instanceof TimelineItemDTO) {
                openUserProfile(((TimelineItemDTO) getAbstractDiscussionCompactDTO()).user.id);
            }
        } else if (view.getId() == R.id.llTLPraise) {
            clickedPraise();
        } else if (view.getId() == R.id.llTLComment) {
            AbstractDiscussionCompactDTO dto = getAbstractDiscussionCompactDTO();
            comments(dto);
        } else if (view.getId() == R.id.llTLShare) {
            share();
        }
    }

    public void comments(AbstractDiscussionCompactDTO dto) {
        DiscussionKey discussionKey = dto.getDiscussionKey();
        Bundle bundle = new Bundle();
        bundle.putBundle(DiscussionKey.BUNDLE_KEY_DISCUSSION_KEY_BUNDLE,
                discussionKey.getArgs());
        pushFragment(DiscussSendFragment.class, bundle);
    }

    public void clickedPraise() {
        AbstractDiscussionCompactDTO item = getAbstractDiscussionCompactDTO();
        updateVoting((item.voteDirection == 0) ? VoteDirection.UpVote : VoteDirection.UnVote, item);

        if (item.voteDirection == 0) {
            item.voteDirection = 1;
            item.upvoteCount += 1;
        } else {
            item.voteDirection = 0;
            item.upvoteCount = item.upvoteCount > 0 ? (item.upvoteCount - 1) : 0;
        }
        displayDiscussOrNewsDTO();
    }

    public void share(String strShare) {
        mShareSheetTitleCache.set(strShare);
        ShareSheetDialogLayout contentView = (ShareSheetDialogLayout) LayoutInflater.from(getActivity())
                .inflate(R.layout.share_sheet_dialog_layout, null);
        contentView.setLocalSocialClickedListener(
                new ShareSheetDialogLayout.OnLocalSocialClickedListener() {
                    @Override
                    public void onShareRequestedClicked() {

                    }
                });
        mShareSheetDialog = THDialog.showUpDialog(getActivity(), contentView);
    }

    //Share to wechat moment and share to weibo on the background
    private void shareToWechatMoment(final String strShare) {
        if (TextUtils.isEmpty(strShare)) {
            return;
        }
        String show = getUnParsedText(strShare);
        UserProfileDTO updatedUserProfileDTO = userProfileCache.get(currentUserId.toUserBaseKey());
        if (updatedUserProfileDTO != null) {
            if (updatedUserProfileDTO.wbLinked) {
                String downloadCNTradeHeroWeibo = getActivity().getResources().getString(R.string.download_tradehero_android_app_on_weibo);
                String outputStr = show;
                outputStr = WeiboUtils.getShareContentWeibo(outputStr, downloadCNTradeHeroWeibo);
                InviteFormDTO inviteFormDTO = new InviteFormWeiboDTO(outputStr);
                userServiceWrapper.get().inviteFriends(
                        currentUserId.toUserBaseKey(), inviteFormDTO, new RequestCallback());
            }
        }
        WeChatDTO weChatDTO = new WeChatDTO();
        weChatDTO.id = 0;
        weChatDTO.type = WeChatMessageType.ShareSellToTimeline;
        weChatDTO.title = strShare;
        ((SocialSharerImpl) socialSharerLazy.get()).share(weChatDTO, getActivity());
    }

    private class RequestCallback implements Callback {

        @Override
        public void success(Object o, Response response) {

        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }

    private void openUserProfile(int userId) {
        if (userId >= 0) {
            Bundle bundle = new Bundle();
            bundle.putInt(UserMainPage.BUNDLE_USER_BASE_KEY, userId);
            pushFragment(UserMainPage.class, bundle);
        }
    }

    private void updateVoting(VoteDirection voteDirection, AbstractDiscussionCompactDTO discussionDTO) {
        if (discussionDTO == null) {
            return;
        }
        DiscussionType discussionType = getDiscussionType();

        DiscussionVoteKey discussionVoteKey = new DiscussionVoteKey(
                discussionType,
                discussionDTO.id,
                voteDirection);
        detachVoteMiddleCallback();
        voteCallback = discussionServiceWrapper.get().vote(discussionVoteKey, new VoteCallback(voteDirection));
    }

    protected void detachVoteMiddleCallback() {
        if (voteCallback != null) {
            voteCallback.setPrimaryCallback(null);
        }
        voteCallback = null;
    }

    protected class VoteCallback implements retrofit.Callback<DiscussionDTO> {
        public VoteCallback(VoteDirection voteDirection) {
        }

        @Override
        public void success(DiscussionDTO discussionDTO, Response response) {
        }

        @Override
        public void failure(RetrofitError error) {
        }
    }


    @Override
    public void onClickHeadRight0() {
        if (dialogFactory == null) {
            dialogFactory = new DialogFactory();
        }
        timeLineMenuDialog = dialogFactory.createTimeLineDetailDialog(getActivity(), new TimeLineDetailDialogLayout.TimeLineDetailMenuClickListener() {
            @Override
            public void onReportClick() {
                timeLineMenuDialog.dismiss();
            }

            @Override
            public void onDeleteClick() {
                timeLineMenuDialog.dismiss();
            }

            @Override
            public void onShareClick() {
                share();
                timeLineMenuDialog.dismiss();
            }
        }, true, true);
    }

    private void share(){
        AbstractDiscussionCompactDTO dto = getAbstractDiscussionCompactDTO();
        String strShare = "";
        if (dto instanceof NewsItemCompactDTO) {
            strShare = (((NewsItemCompactDTO) dto).description);
        } else if (dto instanceof DiscussionDTO) {
            strShare = (((DiscussionDTO) dto).text);
        }
        if (TextUtils.isEmpty(strShare)) {
            if (tvUserTLContent.getText() == null) {
                return;
            }
            shareToWechatMoment(tvUserTLContent.getText().toString());
            return;
        }
        shareToWechatMoment(strShare);
    }
}

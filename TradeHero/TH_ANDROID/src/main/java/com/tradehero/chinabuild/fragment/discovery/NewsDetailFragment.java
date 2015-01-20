package com.tradehero.chinabuild.fragment.discovery;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import butterknife.ButterKnife;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshBase;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshListView;
import com.tradehero.chinabuild.data.DiscussReportDTO;
import com.tradehero.chinabuild.dialog.DialogFactory;
import com.tradehero.chinabuild.dialog.TimeLineCommentDialogLayout;
import com.tradehero.chinabuild.dialog.TimeLineReportDialogLayout;
import com.tradehero.chinabuild.fragment.message.TimeLineItemDetailFragment;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.adapters.TimeLineBaseAdapter;
import com.tradehero.th.adapters.TimeLineDetailDiscussSecItem;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionKeyList;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.form.DiscussionFormDTO;
import com.tradehero.th.api.discussion.form.DiscussionFormDTOFactory;
import com.tradehero.th.api.discussion.key.DiscussionListKey;
import com.tradehero.th.api.discussion.key.PaginatedDiscussionListKey;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.api.share.wechat.WeChatDTO;
import com.tradehero.th.api.share.wechat.WeChatMessageType;
import com.tradehero.th.api.social.InviteFormDTO;
import com.tradehero.th.api.social.InviteFormWeiboDTO;
import com.tradehero.th.api.timeline.TimelineItemDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.DiscussionServiceWrapper;
import com.tradehero.th.network.service.NewsServiceWrapper;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.network.share.SocialSharer;
import com.tradehero.th.network.share.SocialSharerImpl;
import com.tradehero.th.persistence.discussion.DiscussionCache;
import com.tradehero.th.persistence.discussion.DiscussionListCacheNew;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.*;
import dagger.Lazy;
import org.jetbrains.annotations.NotNull;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Detail of News
 *
 * Created by palmer on 15/1/16.
 */
public class NewsDetailFragment extends DashboardFragment implements DiscussionListCacheNew.DiscussionKeyListListener{

    @Inject Lazy<NewsServiceWrapper> newsServiceWrapper;
    @Inject UserProfileCache userProfileCache;
    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject Lazy<SocialSharer> socialSharerLazy;

    //Download comments
    private PaginatedDiscussionListKey discussionListKey;
    @Inject DiscussionListCacheNew discussionListCache;
    @Inject protected DiscussionCache discussionCache;

    private WebView newsWebView;
    private PullToRefreshListView pullToRefreshListView;
    private View headerView;
    private RelativeLayout sendDiscussionRL;
    private TimeLineDetailDiscussSecItem adapter;

    private Button sendCommentBtn;
    private EditText editCommentET;

    private DisplayMetrics dm;

    private long newsId;
    private String newsTitle;
    private String htmlContent;

    public final static String KEY_BUNDLE_NEWS_ID = "key_bundle_news_id";
    public final static String KEY_BUNDLE_NEWS_TITLE = "key_bundle_news_title";

    private String strReply = "";
    private MiddleCallback<DiscussionDTO> discussionEditMiddleCallback;


    //Delete TimeLine confirm dialog or apply comment dialog
    private Dialog deleteOrApplyTimeLineConfirmDialog;
    private TextView deleteOrApplyTLConfirmDlgTitleTV;
    private TextView deleteOrApplyTLConfirmDlgTitle2TV;
    private TextView deleteOrApplyTLConfirmDlgOKTV;
    private TextView deleteOrApplyTLConfirmDlgCancelTV;

    @Inject ProgressDialogUtil progressDialogUtil;
    @Inject Lazy<DiscussionServiceWrapper> discussionServiceWrapper;
    private Dialog timeLineCommentMenuDialog;
    private Dialog timeLineReportMenuDialog;
    private DialogFactory dialogFactory;
    @Inject DiscussionFormDTOFactory discussionFormDTOFactory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TimeLineDetailDiscussSecItem(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.news_detail, container, false);

        ButterKnife.inject(this, view);

        dm = new DisplayMetrics();
        dm = getActivity().getResources().getDisplayMetrics();

        sendDiscussionRL = (RelativeLayout)view.findViewById(R.id.rlSend);
        sendCommentBtn = (Button)view.findViewById(R.id.btnSend);
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postDiscussion();
            }
        });
        editCommentET = (EditText)view.findViewById(R.id.edtSend);

        pullToRefreshListView = (PullToRefreshListView)view.findViewById(R.id.pulltorefreshlistview_discovery_news_comments);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        pullToRefreshListView.setAdapter(adapter);

        adapter.setListener(new TimeLineBaseAdapter.TimeLineOperater()
        {
            @Override
            public void OnTimeLineItemClicked(int position)
            {
                onCommentClick(position);
            }
            @Override
            public void OnTimeLinePraiseClicked(int position){}
            @Override
            public void OnTimeLinePraiseDownClicked(int position){}
            @Override
            public void OnTimeLineCommentsClicked(int position){}
            @Override
            public void OnTimeLineShareClicked(int position){}
            @Override
            public void OnTimeLineBuyClicked(int position){}
        });

        headerView = getActivity().getLayoutInflater().inflate(
                R.layout.discovery_news_detail_header, null);
        newsWebView =  (WebView)headerView.findViewById(R.id.webview_news_html_content);
        newsWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        newsWebView.getSettings().setBuiltInZoomControls(false);
        newsWebView.getSettings().setJavaScriptEnabled(true);
        newsWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });


        Bundle bundle = getArguments();
        newsId = bundle.getLong(KEY_BUNDLE_NEWS_ID);
        retrieveNewsDetail();


        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                discussionListKey.setPage(1);
                fetchComments();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                discussionListKey = discussionListKey.next();
                fetchComments();
            }
        });

        return view;
    }

    public void setDefaultReply()
    {
        editCommentET.setHint(getResources().getString(R.string.please_to_reply));
        strReply = "";
        isReplayFollower = false;
    }

    boolean isReplayFollower = false;

    public void setHintForSender(long position)
    {
        if (position == -1)//回复主题
        {
            setDefaultReply();
        }
        else//回复楼层
        {
            AbstractDiscussionCompactDTO dto = adapter.getItem((int) position);
            if (dto == null)
            {
                return;
            }
            if (dto instanceof DiscussionDTO)
            {
                String displayName = ((DiscussionDTO) dto).user.getDisplayName();
                int id = ((DiscussionDTO) dto).userId;
                String strHint = "回复 " + displayName + ":";
                if (editCommentET != null)
                {
                    editCommentET.setHint(strHint);
                    strReply = "<@@" + displayName + "," + id + "@>";
                    isReplayFollower = true;
                }
            }
        }
        openInputMethod();
    }

    public void openInputMethod()
    {
        InputTools.KeyBoard(editCommentET, "open");
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (isReplayFollower)
        {
            strReply = "";
            editCommentET.setText("");
            editCommentET.setHint(getResources().getString(R.string.please_to_reply));
            isReplayFollower = false;
        }
        else
        {
            popCurrentFragment();
        }
    }

    @Override
    public void onDestroyView()
    {
        detachDiscussionFetch();
        unsetDiscussionEditMiddleCallback();
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public void onCommentClick(final int position)
    {
        if (dialogFactory == null)
        {
            dialogFactory = new DialogFactory();
        }
        if (getActivity() == null)
        {
            return;
        }
        AbstractDiscussionCompactDTO dto = adapter.getItem(position);
        boolean isDeleteAllowed = isDeleteAllowed(dto);
        boolean isReportAllowed = !isDeleteAllowed;

        timeLineCommentMenuDialog = dialogFactory.createTimeLineCommentDialog(getActivity(),
                new TimeLineCommentDialogLayout.TimeLineCommentMenuClickListener()
                {
                    @Override
                    public void onCommentClick()
                    {
                        setHintForSender(position);
                        timeLineCommentMenuDialog.dismiss();
                    }

                    @Override
                    public void onReportClick()
                    {
                        timeLineCommentMenuDialog.dismiss();
                        if (getActivity() == null)
                        {
                            return;
                        }
                        timeLineReportMenuDialog = dialogFactory.createTimeLineReportDialog(getActivity(),
                                new TimeLineReportDialogLayout.TimeLineReportMenuClickListener()
                                {
                                    @Override
                                    public void onItemClickListener(int position_report)
                                    {
                                        timeLineReportMenuDialog.dismiss();
                                        AbstractDiscussionCompactDTO dto = adapter.getItem(position);
                                        sendReport(dto, position_report);
                                    }
                                });
                    }

                    @Override
                    public void onDeleteClick()
                    {
                        timeLineCommentMenuDialog.dismiss();
                        AbstractDiscussionCompactDTO dto = adapter.getItem(position);
                        if (dto != null)
                        {
                            showDeleteTimeLineConfirmDlg(dto.id, TimeLineItemDetailFragment.DIALOG_TYPE_DELETE_COMMENT);
                        }
                    }

                    @Override
                    public void onApplyClick()
                    {
                        timeLineCommentMenuDialog.dismiss();
                        AbstractDiscussionCompactDTO dto = adapter.getItem(position);
                        if (dto != null)
                        {
                            showDeleteTimeLineConfirmDlg(dto.id, TimeLineItemDetailFragment.DIALOG_TYPE_APPLY_COMMENT);
                        }
                    }
                }, false, isDeleteAllowed, isReportAllowed);
    }


    private void fetchComments(){
        if(discussionListKey == null) {
            discussionListKey = new PaginatedDiscussionListKey(DiscussionType.NEWS, (int)newsId, 1, TimeLineItemDetailFragment.ITEMS_PER_PAGE);
        }
        detachDiscussionFetch();
        discussionListCache.register(discussionListKey, this);
        discussionListCache.getOrFetchAsync(discussionListKey, true);
    }

    private void detachDiscussionFetch()
    {
        discussionListCache.unregister(this);
    }


    private void retrieveNewsDetail(){
        newsServiceWrapper.get().getSecurityNewsDetail(newsId, new Callback<NewsItemDTO>() {
            @Override
            public void success(NewsItemDTO newsItemDTO, Response response) {
                if(newsItemDTO!=null && newsItemDTO.text!=null && newsWebView!=null && sendDiscussionRL!=null){
                    htmlContent = StringUtils.convertToHtmlFormat(newsItemDTO.text, (int)(dm.widthPixels/dm.density-36));
                    fetchComments();
                    THLog.d(htmlContent);
                    newsWebView.loadData(htmlContent, "text/html; charset=UTF-8", null);
                    pullToRefreshListView.getRefreshableView().addHeaderView(headerView);
                    sendDiscussionRL.setVisibility(View.VISIBLE);
                }else{
                    sendDiscussionRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                THToast.show(new THException(retrofitError).getMessage());
                if(sendDiscussionRL!=null){
                    sendDiscussionRL.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        Bundle bundle = getArguments();
        newsTitle = bundle.getString(KEY_BUNDLE_NEWS_TITLE);
        setHeadViewMiddleMain(newsTitle);
        setHeadViewRight0(getActivity().getResources().getString(R.string.discovery_discuss_send_share));
    }

    @Override
    public void onClickHeadRight0()
    {
        shareToWechatMoment(newsTitle);
    }

    //Share to wechat moment and share to weibo on the background
    private void shareToWechatMoment(final String strShare)
    {
        if (TextUtils.isEmpty(strShare))
        {
            return;
        }
        String show = getUnParsedText(strShare);
        UserProfileDTO updatedUserProfileDTO = userProfileCache.get(currentUserId.toUserBaseKey());
        if (updatedUserProfileDTO != null)
        {
            if (updatedUserProfileDTO.wbLinked)
            {
                String downloadCNTradeHeroWeibo = getActivity().getResources().getString(R.string.download_tradehero_android_app_on_weibo);
                String outputStr = show;
                outputStr = WeiboUtils.getShareContentWeibo(outputStr, downloadCNTradeHeroWeibo);
                InviteFormDTO inviteFormDTO = new InviteFormWeiboDTO(outputStr);
                userServiceWrapper.get().inviteFriends(
                        currentUserId.toUserBaseKey(), inviteFormDTO, new Callback<Response>()
                        {
                            @Override
                            public void success(Response response, Response response2)
                            {

                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {

                            }
                        });
            }
        }
        WeChatDTO weChatDTO = new WeChatDTO();
        weChatDTO.id = 0;
        weChatDTO.type = WeChatMessageType.ShareSellToTimeline;
        weChatDTO.title = strShare;
        ((SocialSharerImpl) socialSharerLazy.get()).share(weChatDTO, getActivity());
    }

    @Override
    public void onDTOReceived(@NotNull DiscussionListKey key, @NotNull DiscussionKeyList value) {
        List<AbstractDiscussionCompactDTO> listData = new ArrayList<>();
        for (int i = 0; i < value.size(); i++)
        {
            AbstractDiscussionCompactDTO dto = discussionCache.get(value.get(i));
            listData.add(dto);
        }
        if (discussionListKey.getPage() == 1)
        {
            adapter.setListData(listData);
        }
        else
        {
            adapter.addListData(listData);
        }
        if(pullToRefreshListView==null){
            return;
        }
        pullToRefreshListView.onRefreshComplete();
        if (adapter.getCount() >= TimeLineItemDetailFragment.ITEMS_PER_PAGE)
        {
            pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        }
        else
        {
            pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
    }

    @Override
    public void onErrorThrown(@NotNull DiscussionListKey key, @NotNull Throwable error) {
        discussionListKey.setPage(1);
    }


    private boolean isDeleteAllowed(AbstractDiscussionCompactDTO dto)
    {
        if (dto == null)
        {
            return false;
        }
        int userId = currentUserId.toUserBaseKey().getUserId();
        UserBaseDTO userBaseDTO = null;
        if (dto instanceof TimelineItemDTO)
        {
            userBaseDTO = ((TimelineItemDTO) dto).user;
        }
        if (dto instanceof DiscussionDTO)
        {
            userBaseDTO = ((DiscussionDTO) dto).user;
        }
        if (userBaseDTO != null)
        {
            if (userId == userBaseDTO.id)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    private void showDeleteTimeLineConfirmDlg(final int itemId, int dialogType)
    {
        if (getActivity() == null)
        {
            return;
        }
        if (deleteOrApplyTimeLineConfirmDialog == null)
        {
            deleteOrApplyTimeLineConfirmDialog = new Dialog(getActivity());
            deleteOrApplyTimeLineConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            deleteOrApplyTimeLineConfirmDialog.setContentView(R.layout.share_dialog_layout);
            deleteOrApplyTLConfirmDlgTitleTV = (TextView) deleteOrApplyTimeLineConfirmDialog.findViewById(R.id.title);
            deleteOrApplyTLConfirmDlgTitle2TV = (TextView) deleteOrApplyTimeLineConfirmDialog.findViewById(R.id.title2);
            deleteOrApplyTLConfirmDlgCancelTV = (TextView) deleteOrApplyTimeLineConfirmDialog.findViewById(R.id.btn_cancel);
            deleteOrApplyTLConfirmDlgOKTV = (TextView) deleteOrApplyTimeLineConfirmDialog.findViewById(R.id.btn_ok);
            deleteOrApplyTLConfirmDlgOKTV.setText(getActivity().getResources().getString(R.string.discovery_discuss_dlg_btn_ok));
            deleteOrApplyTLConfirmDlgTitle2TV.setVisibility(View.GONE);
            deleteOrApplyTLConfirmDlgCancelTV.setText(getActivity().getResources().getString(R.string.discovery_discuss_dlg_btn_cancel));
            deleteOrApplyTLConfirmDlgCancelTV.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    deleteOrApplyTimeLineConfirmDialog.dismiss();
                }
            });
        }
        if (deleteOrApplyTimeLineConfirmDialog.isShowing())
        {
            return;
        }
        if (dialogType == TimeLineItemDetailFragment.DIALOG_TYPE_DELETE_COMMENT)
        {
            deleteOrApplyTLConfirmDlgTitleTV.setText(getActivity().getResources().getString(R.string.discovery_discuss_dlg_title_deletecomment));
            deleteOrApplyTLConfirmDlgOKTV.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    deleteDiscussionItem(itemId);
                    deleteOrApplyTimeLineConfirmDialog.dismiss();
                }
            });
        }
        deleteOrApplyTimeLineConfirmDialog.show();
    }

    private void deleteDiscussionItem(final int discussionItemId)
    {
        showDeleteProgressDlg();
        discussionServiceWrapper.get().deleteDiscussionItem(discussionItemId, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                adapter.removeDeletedItem(discussionItemId);
                onFinish();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                THException thException = new THException(retrofitError);
                THToast.show(thException);
                onFinish();
            }

            private void onFinish() {
                dismissProgressDlg();
            }
        });
    }

    private void showDeleteProgressDlg()
    {
        progressDialogUtil.show(getActivity(), R.string.alert_dialog_please_wait, R.string.discovery_discuss_dlg_delete);
    }

    private void dismissProgressDlg()
    {
        if (getActivity() == null)
        {
            return;
        }
        progressDialogUtil.dismiss(getActivity());
    }

    private void sendReport(AbstractDiscussionCompactDTO dto, int position)
    {
        if (dto == null)
        {
            return;
        }
        DiscussReportDTO discussReportDTO = new DiscussReportDTO();
        if (dto instanceof TimelineItemDTO)
        {
            discussReportDTO.discussionType = ((TimelineItemDTO) dto).type;
        }
        else if (dto instanceof DiscussionDTO)
        {
            //The type of all DiscussionDTO is 1 when report.
            discussReportDTO.discussionType = 1;
        }
        else
        {
            return;
        }
        discussReportDTO.reportType = position;
        discussReportDTO.discussionId = dto.id;
        discussionServiceWrapper.get().reportTimeLineItem(discussReportDTO, new Callback<Response>()
        {
            @Override
            public void success(Response response, Response response2)
            {
                THToast.show(R.string.discovery_discuss_report_successfully);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                THException thException = new THException(retrofitError);
                THToast.show(thException);
            }
        });
    }

    private void unsetDiscussionEditMiddleCallback()
    {
        if (discussionEditMiddleCallback != null)
        {
            discussionEditMiddleCallback.setPrimaryCallback(null);
        }
        discussionEditMiddleCallback = null;
    }


    protected void postDiscussion()
    {
        if (validate())
        {
            DiscussionFormDTO discussionFormDTO = buildDiscussionFormDTO();
            if (discussionFormDTO == null) return;
            unsetDiscussionEditMiddleCallback();
            progressDialogUtil.show(getActivity(), R.string.alert_dialog_please_wait, R.string.processing);
            discussionEditMiddleCallback = discussionServiceWrapper.get().createDiscussion(discussionFormDTO, new SecurityDiscussionEditCallback());
        }
    }

    protected DiscussionFormDTO buildDiscussionFormDTO()
    {
        DiscussionType discussionType = DiscussionType.NEWS;
        if (discussionType != null)
        {
            DiscussionFormDTO discussionFormDTO = discussionFormDTOFactory.createEmpty(discussionType);
            discussionFormDTO.inReplyToId = (int)newsId;
            discussionFormDTO.text = strReply + " " + editCommentET.getText().toString();

            return discussionFormDTO;
        }

        return null;
    }

    private class SecurityDiscussionEditCallback implements Callback<DiscussionDTO>
    {
        @Override
        public void success(DiscussionDTO discussionDTO, Response response)
        {
            onFinish();
            DeviceUtil.dismissKeyboard(getActivity());
            discussionListKey.setPage(1);
            fetchComments();
            strReply = "";
            editCommentET.setText("");
        }

        @Override
        public void failure(RetrofitError error)
        {
            onFinish();
            THException thException = new THException(error);
            THToast.show(thException);
        }

        private void onFinish()
        {
            dismissProgressDlg();
        }
    }

    private boolean validate()
    {
        boolean notEmptyText = validateNotEmptyText();
        if (!notEmptyText)
        {
            THToast.show(R.string.error_discussion_empty_post);
        }
        return notEmptyText;
    }

    private boolean validateNotEmptyText()
    {
        return !editCommentET.getText().toString().trim().isEmpty();
    }
}

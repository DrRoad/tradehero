package com.tradehero.chinabuild.fragment.message;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshBase;
import com.tradehero.chinabuild.fragment.competition.CompetitionDetailFragment;
import com.tradehero.chinabuild.fragment.userCenter.UserMainPage;
import com.tradehero.chinabuild.listview.SecurityListView;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.R;
import com.tradehero.th.adapters.NotificationListAdapter;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.key.DiscussionKey;
import com.tradehero.th.api.notification.NotificationDTO;
import com.tradehero.th.api.notification.NotificationKey;
import com.tradehero.th.api.notification.NotificationListKey;
import com.tradehero.th.api.notification.PaginatedNotificationDTO;
import com.tradehero.th.api.notification.PaginatedNotificationListKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.network.retrofit.MiddleCallbackWeakList;
import com.tradehero.th.network.service.NotificationServiceWrapper;
import com.tradehero.th.persistence.notification.NotificationListCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.widget.TradeHeroProgressBar;
import dagger.Lazy;
import java.util.ArrayList;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/*
    通知系统
 */
public class NotificationFragment extends DashboardFragment
{
    @InjectView(R.id.tradeheroprogressbar_notifications)TradeHeroProgressBar progressBar;
    @InjectView(R.id.listNotification) SecurityListView listView;
    @InjectView(R.id.bvaViewAll) BetterViewAnimator betterViewAnimator;
    @InjectView(R.id.imgEmpty) ImageView imgEmpty;

    @Inject Lazy<NotificationListCache> notificationListCache;
    @Inject NotificationServiceWrapper notificationServiceWrapper;
    @Inject UserProfileCache userProfileCache;
    @Inject CurrentUserId currentUserId;

    private PaginatedNotificationListKey paginatedNotificationListKey;
    private int currentPage;

    @NotNull private MiddleCallbackWeakList<Response> middleCallbacks;
    private DTOCacheNew.Listener<NotificationListKey, PaginatedNotificationDTO> notificationListFetchListener;
    private DTOCacheNew.Listener<NotificationListKey, PaginatedNotificationDTO> notificationListRefreshListener;
    private NotificationListKey notificationListKey;

    private NotificationListAdapter adapter;

    //Notification Dialog
    private Dialog notificationClearAllDialog;
    private TextView dialogOKBtn;
    private TextView dialogCancelBtn;
    private TextView dialogTitleATV;
    private TextView dialogTitleBTV;

    //Notification Delete Pop Window
    private PopupWindow popWin;
    private RelativeLayout deleteNotificationLL;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        middleCallbacks = new MiddleCallbackWeakList<>();
        notificationListFetchListener = createNotificationFetchListener();
        notificationListRefreshListener = createNotificationRefreshListener();
        adapter = new NotificationListAdapter(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setHeadViewMiddleMain(getActivity().getString(R.string.notification_title));
        setHeadViewRight0(getActivity().getString(R.string.notification_empty));
    }

    @Override
    public void onClickHeadRight0()
    {
        showEmptyAllNotificationsDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.notification_fragment, container, false);
        ButterKnife.inject(this, view);
        initView();
        if (adapter.getCount() == 0)
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.tradeheroprogressbar_notifications);
            progressBar.startLoading();
            resetPage();
            refresh();
        }
        else
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listNotification);
        }
        return view;
    }

    public void initView()
    {
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setAdapter(adapter);
        listView.setEmptyView(imgEmpty);
        adapter.setNotificationLister(new NotificationListAdapter.NotificationClickListener() {
            @Override
            public void OnNotificationItemClicked(int position) {
                NotificationDTO dto = (NotificationDTO) adapter.getItem(position);
                if (dto == null) {
                    return;
                }
                if (dto.unread) {
                    reportNotificationRead(((NotificationDTO) adapter.getItem(position)).pushId);
                }
                jumpToTarget(dto);
            }

            @Override
            public void OnNotificationItemLongClicked(int position, View view) {
                showDeleteNotificationPopWindow(position, view);
            }
        });
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>()
        {
            @Override public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                refresh();
            }

            @Override public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                fetchNextPageIfNecessary();
            }
        });

    }

    @Override public void onDestroyView()
    {
        detachNotificationListFetchTask();
        detachNotificationListRefreshTask();

        ButterKnife.reset(this);
        super.onDestroyView();
    }

    private void initListData(PaginatedNotificationDTO value, NotificationListKey key)
    {
        if (value != null && ((PaginatedNotificationListKey) key).getPage() == 1)
        {
            ArrayList<NotificationDTO> list = new ArrayList<NotificationDTO>();
            list.addAll(value.getData());
            adapter.setListData(list);
            currentPage = ((PaginatedNotificationListKey) key).getPage();
        }
        else if (value != null && ((PaginatedNotificationListKey) key).getPage() > 1)
        {
            ArrayList<NotificationDTO> list = new ArrayList<NotificationDTO>();
            list.addAll(value.getData());
            adapter.addListData(list);
            currentPage = ((PaginatedNotificationListKey) key).getPage();
        }
    }

    private DTOCacheNew.Listener<NotificationListKey, PaginatedNotificationDTO> createNotificationFetchListener()
    {
        return new NotificationFetchListener(true);
    }

    private class NotificationFetchListener implements DTOCacheNew.Listener<NotificationListKey, PaginatedNotificationDTO>
    {
        private final boolean shouldAppend;

        public NotificationFetchListener(boolean shouldAppend)
        {
            this.shouldAppend = shouldAppend;
        }

        @Override public void onDTOReceived(@NotNull NotificationListKey key, @NotNull PaginatedNotificationDTO value)
        {

            onFinish();
            initListData(value, key);
        }

        @Override public void onErrorThrown(@NotNull NotificationListKey key, @NotNull Throwable error)
        {
            onFinish();
        }

        private void onFinish()
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listNotification);
            listView.onRefreshComplete();
            progressBar.stopLoading();
        }
    }

    private DTOCacheNew.Listener<NotificationListKey, PaginatedNotificationDTO> createNotificationRefreshListener()
    {
        return new NotificationRefreshListener();
    }

    private class NotificationRefreshListener implements DTOCacheNew.Listener<NotificationListKey, PaginatedNotificationDTO>
    {
        @Override public void onDTOReceived(@NotNull NotificationListKey key, @NotNull PaginatedNotificationDTO value)
        {
            initListData(value, key);
            onFinish();
        }

        @Override public void onErrorThrown(@NotNull NotificationListKey key, @NotNull Throwable error)
        {
            onFinish();
        }

        private void onFinish()
        {
            if (listView != null)
            {
                listView.onRefreshComplete();
            }
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listNotification);
            listView.onRefreshComplete();
        }
    }

    private void resetPage()
    {
        if (notificationListKey == null)
        {
            notificationListKey = new NotificationListKey();
        }
        currentPage = 0;
        paginatedNotificationListKey = new PaginatedNotificationListKey(notificationListKey, 1);
    }

    protected class NotificationMarkAsReadCallback implements Callback<Response>
    {
        @Override public void success(Response response, Response response2)
        {

        }

        @Override public void failure(RetrofitError retrofitError)
        {
        }
    }

    private void refresh()
    {
        detachNotificationListRefreshTask();
        PaginatedNotificationListKey firstPage = new PaginatedNotificationListKey(notificationListKey, 1);
        notificationListCache.get().register(firstPage, notificationListRefreshListener);
        notificationListCache.get().getOrFetchAsync(firstPage, true);
    }

    private void fetchNextPageIfNecessary()
    {
        fetchNextPageIfNecessary(true);
    }

    public void fetchNextPageIfNecessary(boolean force)
    {
        detachNotificationListFetchTask();

        if (paginatedNotificationListKey == null)
        {
            paginatedNotificationListKey = new PaginatedNotificationListKey(notificationListKey, 1);
        }

        if (currentPage >= 1)
        {
            paginatedNotificationListKey = new PaginatedNotificationListKey(notificationListKey, currentPage + 1);
            notificationListCache.get().register(paginatedNotificationListKey, notificationListFetchListener);
            notificationListCache.get().getOrFetchAsync(paginatedNotificationListKey, force);
        }
    }

    private void detachNotificationListFetchTask()
    {
        notificationListCache.get().unregister(notificationListFetchListener);
    }

    private void detachNotificationListRefreshTask()
    {
        notificationListCache.get().unregister(notificationListRefreshListener);
    }

    protected void reportNotificationRead(int pushId)
    {
        middleCallbacks.add(
                notificationServiceWrapper.markAsRead(
                        currentUserId.toUserBaseKey(),
                        new NotificationKey(pushId),
                        new NotificationMarkAsReadCallback()));

        adapter.setHasRead(pushId);
    }

    private void jumpToTarget(NotificationDTO dto){
        if(dto.replyableTypeId != null && dto.replyableId != null){
            int replyableId = dto.replyableId;
            if(dto.replyableTypeId == 1){
                jumpTimeLine(replyableId, DiscussionType.COMMENT);
                return;
            }
            if(dto.replyableTypeId == 2){
                jumpTimeLine(replyableId, DiscussionType.TIMELINE_ITEM);
                return;
            }
        }
        if(dto.pushTypeId !=null && dto.referencedUserId!=null){
            if(dto.pushTypeId == 18){
                jumpUserPage(dto.referencedUserId);
                return;
            }
        }

        if(dto.pushTypeId !=null && dto.relatesToCompetitionId!=null){
            if(dto.pushTypeId == 30){
                jumpCompetitionDetailPage(dto.relatesToCompetitionId);
                return;
            }
        }

    }

    private void jumpTimeLine(int replyableId, DiscussionType type){
        Bundle bundle = new Bundle();
        Bundle discussBundle = new Bundle();
        discussBundle.putString(DiscussionKey.BUNDLE_KEY_TYPE, type.name());
        discussBundle.putInt(DiscussionKey.BUNDLE_KEY_ID, replyableId);
        bundle.putBundle(TimeLineItemDetailFragment.BUNDLE_ARGUMENT_DISCUSSTION_ID, discussBundle);
        pushFragment(TimeLineItemDetailFragment.class, bundle);
        return;
    }

    private void jumpUserPage(int referencedUserId){
        Bundle bundle = new Bundle();
        bundle.putInt(UserMainPage.BUNDLE_USER_BASE_KEY, referencedUserId);
        pushFragment(UserMainPage.class, bundle);
    }

    private void jumpCompetitionDetailPage(int competitionId){
        Bundle bundle = new Bundle();
        bundle.putInt(CompetitionDetailFragment.BUNDLE_COMPETITION_ID, competitionId);
        pushFragment(CompetitionDetailFragment.class, bundle);
    }


    //Empty All Notifications Dialog
    private void showEmptyAllNotificationsDialog(){
        if(getActivity()==null){
            return;
        }
        if(notificationClearAllDialog==null){
            notificationClearAllDialog = new Dialog(getActivity());
            notificationClearAllDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            notificationClearAllDialog.setCanceledOnTouchOutside(false);
            notificationClearAllDialog.setContentView(R.layout.share_dialog_layout);
            dialogOKBtn = (TextView)notificationClearAllDialog.findViewById(R.id.btn_ok);
            dialogCancelBtn = (TextView)notificationClearAllDialog.findViewById(R.id.btn_cancel);
            dialogTitleATV = (TextView)notificationClearAllDialog.findViewById(R.id.title);
            dialogTitleATV.setText(getActivity().getResources().getString(R.string.notification_empty_confirm));
            dialogTitleBTV = (TextView)notificationClearAllDialog.findViewById(R.id.title2);
            dialogTitleBTV.setVisibility(View.GONE);
            dialogOKBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationClearAllDialog.dismiss();

                    notificationServiceWrapper.deleteAllNotification(new Callback<String>() {
                        @Override
                        public void success(String s, Response response) {
                            onFinish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            onFinish();
                        }

                        private void onFinish(){
                            setReadAllNotifcations();
                            adapter.removeAllNotifications();
                        }
                    });
                }
            });

            dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationClearAllDialog.dismiss();
                }
            });
        }
        if(!notificationClearAllDialog.isShowing()){
            notificationClearAllDialog.show();
        }
    }

    //Show pop window about delete notifications
    private void showDeleteNotificationPopWindow(final int positionId, final View parent){
        if(getActivity()==null){
            return;
        }
        if(popWin==null){
            LayoutInflater lay = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = lay.inflate(R.layout.popwindow_notification_delete, null);
            deleteNotificationLL = (RelativeLayout)v.findViewById(R.id.relativelayout_delete_notification);
            popWin = new PopupWindow(v,(int)getActivity().getResources().getDimension(R.dimen.notification_popwindow_width), (int)getActivity().getResources().getDimension(R.dimen.notification_popwindow_height));
            popWin.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.popwindow_notification_delete_layout_bg));
            popWin.setOutsideTouchable(true);
        }
        deleteNotificationLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationDTO notificationDTO = (NotificationDTO) adapter.getItem(positionId);
                popWin.dismiss();
                adapter.removeNotification(notificationDTO.pushId);
                setReadOneNotification(notificationDTO);
                notificationServiceWrapper.deleteNotification(notificationDTO.pushId, new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {

                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                });
            }
        });
        popWin.setFocusable(true);
        popWin.update();
        popWin.showAsDropDown(parent, 400, -50);
    }

    private void setReadOneNotification(NotificationDTO notificationDTO){
        if(notificationDTO.unread){
            UserProfileDTO userProfileDTO = userProfileCache.get(currentUserId.toUserBaseKey());
            if(userProfileDTO.unreadNotificationsCount>0){
                userProfileDTO.unreadNotificationsCount-- ;
            }
            userProfileCache.getOrFetchAsync(currentUserId.toUserBaseKey(), true);
        }
    }

    private void setReadAllNotifcations(){
        UserProfileDTO userProfileDTO = userProfileCache.get(currentUserId.toUserBaseKey());
        userProfileDTO.unreadNotificationsCount=0;
        userProfileCache.getOrFetchAsync(currentUserId.toUserBaseKey(), true);
    }



}
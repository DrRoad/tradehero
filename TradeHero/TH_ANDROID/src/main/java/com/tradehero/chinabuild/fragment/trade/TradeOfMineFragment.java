package com.tradehero.chinabuild.fragment.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshBase;
import com.handmark.pulltorefresh.library.pulltorefresh.PullToRefreshExpandableListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tradehero.chinabuild.data.PositionInterface;
import com.tradehero.chinabuild.data.SecurityPositionItem;
import com.tradehero.chinabuild.data.WatchPositionItem;
import com.tradehero.chinabuild.data.sp.THSharePreferenceManager;
import com.tradehero.chinabuild.fragment.ShareDialogFragment;
import com.tradehero.chinabuild.fragment.security.SecurityDetailFragment;
import com.tradehero.chinabuild.fragment.userCenter.SettingMineFragment;
import com.tradehero.chinabuild.utils.UniversalImageLoader;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.persistence.prefs.BooleanPreference;
import com.tradehero.common.persistence.prefs.StringPreference;
import com.tradehero.th.R;
import com.tradehero.th.activities.MainActivity;
import com.tradehero.th.adapters.CNPersonTradePositionListAdapter;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.api.portfolio.PortfolioCompactDTOList;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.position.GetPositionsDTO;
import com.tradehero.th.api.position.GetPositionsDTOKey;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.persistence.portfolio.PortfolioCache;
import com.tradehero.th.persistence.portfolio.PortfolioCompactListCache;
import com.tradehero.th.persistence.position.GetPositionsCache;
import com.tradehero.th.persistence.prefs.ShareDialogKey;
import com.tradehero.th.persistence.prefs.ShareDialogROIValueKey;
import com.tradehero.th.persistence.prefs.ShareDialogTotalValueKey;
import com.tradehero.th.persistence.prefs.ShareSheetTitleCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
    交易－我的交易
 */
public class TradeOfMineFragment extends DashboardFragment
{
    @Nullable protected DTOCacheNew.Listener<GetPositionsDTOKey, GetPositionsDTO> fetchGetPositionsDTOListener;
    private DTOCacheNew.Listener<UserBaseKey, WatchlistPositionDTOList> userWatchlistPositionFetchListener;
    private DTOCacheNew.Listener<OwnedPortfolioId, PortfolioDTO> portfolioFetchListener;

    @Inject protected PortfolioCompactListCache portfolioCompactListCache;
    private DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList> portfolioCompactListFetchListener;

    @Inject Lazy<GetPositionsCache> getPositionsCache;
    @Inject UserWatchlistPositionCache userWatchlistPositionCache;
    @Inject PortfolioCache portfolioCache;
    @Inject CurrentUserId currentUserId;

    private RelativeLayout mRefreshView;
    private TextView roiTV;
    private TextView returnTV;
    private TextView nicknameTV;
    private TextView gotoMineTV;
    private ImageView gotoMineIV;
    private ImageView avatarIV;

    @InjectView(R.id.tradeMyPositionList) PullToRefreshExpandableListView listView;

    private OwnedPortfolioId shownPortfolioId;
    private PortfolioDTO shownPortfolioDTO;

    private CNPersonTradePositionListAdapter adapter;
    @Inject @ShareDialogKey BooleanPreference mShareDialogKeyPreference;
    @Inject @ShareDialogTotalValueKey BooleanPreference mShareDialogTotalValueKeyPreference;
    @Inject @ShareDialogROIValueKey BooleanPreference mShareDialogROIValueKeyPreference;
    @Inject @ShareSheetTitleCache StringPreference mShareSheetTitleCache;

    private static long time_stamp = -1;
    private final long duration_showing_dialog = 120000;
    private boolean availableShowDialog = false;


    private DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> userProfileCacheListener;
    @Inject Lazy<UserProfileCache> userProfileCache;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        adapter = new CNPersonTradePositionListAdapter(getActivity());
        fetchGetPositionsDTOListener = new GetPositionsListener();
        userWatchlistPositionFetchListener = new WatchlistPositionFragmentSecurityIdListCacheListener();
        portfolioFetchListener = new WatchlistPositionFragmentPortfolioCacheListener();
        portfolioCompactListFetchListener = new BasePurchaseManagementPortfolioCompactListFetchListener();
        userProfileCacheListener = new UserProfileFetchListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trade_of_mine, container, false);
        ButterKnife.inject(this, view);
        initRefreshView(inflater);
        initView();
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        availableShowDialog = true;
        fetchPortfolioCompactList(true);
        fetchUserProfile();
    }

    @Override public void onStop() {
        availableShowDialog = false;
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        detachUserProfileCache();
        fetchGetPositionsDTOListener = null;
        portfolioFetchListener = null;
        userWatchlistPositionFetchListener = null;
        portfolioCompactListFetchListener = null;
        userProfileCacheListener = null;
        super.onDestroy();
    }

    private void initRefreshView(LayoutInflater inflater) {
        mRefreshView = (RelativeLayout) inflater.inflate(R.layout.trade_of_mine_listview_header, null);
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoDashboard(SettingMineFragment.class, new Bundle());
            }
        });
        roiTV = (TextView)mRefreshView.findViewById(R.id.textview_trade_rateofreturn);
        returnTV = (TextView)mRefreshView.findViewById(R.id.textview_trade_return);
        nicknameTV = (TextView)mRefreshView.findViewById(R.id.textview_trade_myname);
        gotoMineTV = (TextView)mRefreshView.findViewById(R.id.textview_trade_go_mypage);
        gotoMineIV = (ImageView)mRefreshView.findViewById(R.id.imageview_trade_go_mypage);
        avatarIV = (ImageView)mRefreshView.findViewById(R.id.imageview_trade_avatar);
    }

    private void initView() {
        listView.getRefreshableView().addHeaderView(mRefreshView);
        listView.getRefreshableView().setAdapter(adapter);
        listView.getRefreshableView().setChildDivider(null);
        listView.getRefreshableView().setGroupIndicator(null);
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ExpandableListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
                fetchPortfolio(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {

            }
        });

        listView.getRefreshableView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                PositionInterface item = (PositionInterface)adapter.getChild(groupPosition, childPosition);
                dealSecurityItem(item);
                return true;
            }
        });
        listView.getRefreshableView().expandGroup(1);
        listView.getRefreshableView().expandGroup(0);
    }

    private void dealSecurityItem(PositionInterface item)
    {
        if (item instanceof SecurityPositionItem)
        {
            if (((SecurityPositionItem) item).type == SecurityPositionItem.TYPE_CLOSED)
            {
                enterSecurity(((SecurityPositionItem) item).security.getSecurityId(), ((SecurityPositionItem) item).security.name,
                        ((SecurityPositionItem) item).position,true);
            }
            else if (((SecurityPositionItem) item).type == SecurityPositionItem.TYPE_ACTIVE)
            {
                enterSecurity(((SecurityPositionItem) item).security.getSecurityId(), ((SecurityPositionItem) item).security.name,
                        ((SecurityPositionItem) item).position,false);
            }
        }
        else if (item instanceof WatchPositionItem)
        {
            enterSecurity(((WatchPositionItem) item).watchlistPosition.securityDTO.getSecurityId(),
                    ((WatchPositionItem) item).watchlistPosition.securityDTO.name);
        }
    }

    private void enterSecurity(SecurityId securityId, String securityName, PositionDTO positionDTO,boolean isGotoTradeDetail)
    {
        Bundle bundle = new Bundle();
        bundle.putBundle(SecurityDetailFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
        bundle.putString(SecurityDetailFragment.BUNDLE_KEY_SECURITY_NAME, securityName);
        bundle.putBoolean(SecurityDetailFragment.BUNDLE_KEY_GOTO_TRADE_DETAIL,isGotoTradeDetail);
        SecurityDetailFragment.putPositionDTOKey(bundle, positionDTO.getPositionDTOKey());
        if (shownPortfolioId != null)
        {
            SecurityDetailFragment.putApplicablePortfolioId(bundle, shownPortfolioId);
        }
        gotoDashboard(SecurityDetailFragment.class, bundle);
    }

    private void enterSecurity(SecurityId securityId, String securityName)
    {
        Bundle bundle = new Bundle();
        bundle.putBundle(SecurityDetailFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
        bundle.putString(SecurityDetailFragment.BUNDLE_KEY_SECURITY_NAME, securityName);
        gotoDashboard(SecurityDetailFragment.class, bundle);
    }

    //Download portfolio about my stock
    protected void fetchSimplePage(boolean force)
    {
        if (shownPortfolioId != null)
        {
            detachGetPositionsTask();
            fetchGetPositionsDTOListener = new GetPositionsListener();
            getPositionsCache.get().register(shownPortfolioId, fetchGetPositionsDTOListener);
            getPositionsCache.get().getOrFetchAsync(shownPortfolioId, force);
        }
    }

    //Need to download first
    private void fetchPortfolioCompactList(boolean force)
    {
        detachPortfolioCompactListCache();
        portfolioCompactListCache.register(currentUserId.toUserBaseKey(), portfolioCompactListFetchListener);
        portfolioCompactListCache.getOrFetchAsync(currentUserId.toUserBaseKey(), force);
    }

    //Download my stock information
    protected void fetchPortfolio(boolean force)
    {
        if (shownPortfolioId == null || portfolioFetchListener == null) return;
        detachPortfolioFetchTask();
        portfolioCache.register(shownPortfolioId, portfolioFetchListener);
        portfolioCache.getOrFetchAsync(shownPortfolioId, force);
    }

    //Download portfolio about watch list
    protected void fetchWatchPositionList(boolean force)
    {
        detachUserWatchlistFetchTask();
        userWatchlistPositionCache.register(currentUserId.toUserBaseKey(), userWatchlistPositionFetchListener);
        userWatchlistPositionCache.getOrFetchAsync(currentUserId.toUserBaseKey(), force);
    }

    protected void detachGetPositionsTask()
    {
        getPositionsCache.get().unregister(fetchGetPositionsDTOListener);
    }

    protected void detachUserWatchlistFetchTask()
    {
        userWatchlistPositionCache.unregister(userWatchlistPositionFetchListener);
    }

    protected void detachPortfolioFetchTask()
    {
        portfolioCache.unregister(portfolioFetchListener);
    }

    private void detachPortfolioCompactListCache()
    {
        portfolioCompactListCache.unregister(portfolioCompactListFetchListener);
    }

    protected class GetPositionsListener
            implements DTOCacheNew.HurriedListener<GetPositionsDTOKey, GetPositionsDTO>
    {
        @Override public void onPreCachedDTOReceived(
                @NotNull GetPositionsDTOKey key,
                @NotNull GetPositionsDTO value)
        {
            MainActivity.setGetPositionDTO(value);
            initPositionSecurity(value);
            if(listView!=null) {
                listView.onRefreshComplete();
            }
        }

        @Override public void onDTOReceived(
                @NotNull GetPositionsDTOKey key,
                @NotNull GetPositionsDTO value)
        {
            MainActivity.setGetPositionDTO(value);
            initPositionSecurity(value);
            if(listView!=null) {
                listView.onRefreshComplete();
            }
        }

        @Override public void onErrorThrown(
                @NotNull GetPositionsDTOKey key,
                @NotNull Throwable error)
        {
            if(listView!=null) {
                listView.onRefreshComplete();
            }
        }
    }

    protected class WatchlistPositionFragmentSecurityIdListCacheListener implements DTOCacheNew.Listener<UserBaseKey, WatchlistPositionDTOList>
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull WatchlistPositionDTOList value) {
            initWatchList(value);
            if(listView!=null) {
                listView.onRefreshComplete();
            }
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error) {
            if(listView!=null) {
                listView.onRefreshComplete();
            }
        }
    }

    protected class WatchlistPositionFragmentPortfolioCacheListener implements DTOCacheNew.Listener<OwnedPortfolioId, PortfolioDTO>
    {
        @Override public void onDTOReceived(@NotNull OwnedPortfolioId key, @NotNull PortfolioDTO value)
        {
            shownPortfolioDTO = value;
            displayProfolioDTO(shownPortfolioDTO);
            fetchWatchPositionList(true);
            fetchSimplePage(true);
        }

        @Override public void onErrorThrown(@NotNull OwnedPortfolioId key, @NotNull Throwable error)
        {
        }
    }

    protected class BasePurchaseManagementPortfolioCompactListFetchListener implements DTOCacheNew.Listener<UserBaseKey, PortfolioCompactDTOList>
    {
        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull PortfolioCompactDTOList value)
        {
            prepareApplicableOwnedPortolioId(value.getDefaultPortfolio());
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
        }
    }

    protected void prepareApplicableOwnedPortolioId(@Nullable PortfolioCompactDTO defaultIfNotInArgs)
    {
        if (defaultIfNotInArgs != null)
        {
            shownPortfolioId = defaultIfNotInArgs.getOwnedPortfolioId();
        }
        if (shownPortfolioId != null)
        {
            fetchPortfolio(true);
        }
    }

    private void displayProfolioDTO(PortfolioDTO cached)
    {
        if (cached != null && cached.roiSinceInception != null)
        {
            THSignedNumber roi = THSignedPercentage.builder(cached.roiSinceInception * 100)
                    .withSign()
                    .signTypeArrow()
                    .build();
            if (getActivity() != null)
            {
                roiTV.setText(roi.toString());
                roiTV.setTextColor(getActivity().getResources().getColor(roi.getColorResId()));
            }
        }

        //总资产数达到15w
        if (cached.totalValue > 150000 && getActivity() != null && availableShowDialog)
        {
            if(getActivity()==null){
                return;
            }
            String endPoint = THSharePreferenceManager.getShareEndPoint(getActivity());
            int userId = currentUserId.toUserBaseKey().getUserId();
            if (THSharePreferenceManager.isShareDialogMoreThanFifteenAvailable(userId, getActivity()))
            {
                ShareDialogFragment.showDialog(getActivity().getSupportFragmentManager(),
                        getString(R.string.share_amount_total_value_title), getString(R.string.share_amount_total_value_summary,
                        currentUserId.get().toString(), endPoint), THSharePreferenceManager.PROPERTY_MORE_THAN_FIFTEEN, userId);
                time_stamp = System.currentTimeMillis();
                THSharePreferenceManager.isMoreThanFifteenShowed = true;
            }
            else
            {
                if (cached.totalValue > 250000 && (System.currentTimeMillis() - time_stamp) > duration_showing_dialog)
                {
                    if (THSharePreferenceManager.isShareDialogMoreThanTwentyFiveAvailable(userId, getActivity()))
                    {
                        ShareDialogFragment.showDialog(getActivity().getSupportFragmentManager(),
                                getString(R.string.share_amount_total_value_title25), getString(R.string.share_amount_total_value_summary25,
                                currentUserId.get().toString(), endPoint), THSharePreferenceManager.PROPERTY_MORE_THAN_TWENTY_FIVE, userId);
                        time_stamp = -1;
                        THSharePreferenceManager.isMoreThanTwentyShowed = true;
                    }
                }
            }
        }

        Double pl = cached.plSinceInception;
        if (pl == null)
        {
            pl = 0.0;
        }
        THSignedNumber thPlSinceInception = THSignedMoney.builder(pl)
                .withSign()
                .signTypePlusMinusAlways()
                .currency(cached.getNiceCurrency())
                .build();
        returnTV.setTextColor(thPlSinceInception.getColor());
        returnTV.setText(thPlSinceInception.toString());
    }

    //自选股列表显示
    private void initWatchList(WatchlistPositionDTOList watchList)
    {
        if (watchList != null)
        {
            int sizeWatchList = watchList.size();
            if (sizeWatchList > 0)
            {
                ArrayList<WatchPositionItem> list = new ArrayList<>();
                for (int i = 0; i < sizeWatchList; i++)
                {
                    list.add(new WatchPositionItem(watchList.get(i)));
                }
                adapter.setWatchPositionList(list);
            }else{
                adapter.setWatchPositionList(null);
            }
        }
    }

    private void initPositionSecurity(GetPositionsDTO psList)
    {
        if (psList != null && psList.openPositionsCount >= 0)
        {
            ArrayList<SecurityPositionItem> list = new ArrayList<>();
            List<PositionDTO> listData = psList.getOpenPositions();
            int sizePosition = listData.size();
            for (int i = 0; i < sizePosition; i++)
            {
                SecurityCompactDTO securityCompactDTO = psList.getSecurityCompactDTO(listData.get(i));
                if (securityCompactDTO != null)
                {
                    list.add(new SecurityPositionItem(securityCompactDTO, listData.get(i), SecurityPositionItem.TYPE_ACTIVE));
                    //持有股票收益率涨副超过 10% 弹窗提示分享
                    if (listData.get(i).getROISinceInception() * 100 > 10)
                    {
                        if (mShareDialogKeyPreference.get() && mShareDialogROIValueKeyPreference.get())
                        {
                            if(getActivity()==null){
                                return;
                            }
                            String endPoint = THSharePreferenceManager.getShareEndPoint(getActivity());
                            mShareDialogKeyPreference.set(false);
                            mShareDialogROIValueKeyPreference.set(false);
                            mShareSheetTitleCache.set(getString(
                                    R.string.share_amount_roi_value_summary, currentUserId.get().toString(),
                                    String.valueOf(listData.get(i).id), endPoint));
                            ShareDialogFragment.showDialog(getActivity().getSupportFragmentManager(),
                                    getString(R.string.share_amount_roi_value_title), getString(
                                    R.string.share_amount_roi_value_summary, currentUserId.get().toString(),
                                    String.valueOf(listData.get(i).id), endPoint));
                        }
                    }
                }
            }
            adapter.setSecurityPositionList(list);
        }

        if (psList != null && psList.closedPositionsCount > 0)
        {
            ArrayList<SecurityPositionItem> list = new ArrayList<>();
            List<PositionDTO> listData = psList.getClosedPositions();
            int sizePosition = listData.size();
            for (int i = 0; i < sizePosition; i++)
            {
                SecurityCompactDTO securityCompactDTO = psList.getSecurityCompactDTO(listData.get(i));
                if (securityCompactDTO != null)
                {
                    list.add(new SecurityPositionItem(securityCompactDTO, listData.get(i), SecurityPositionItem.TYPE_CLOSED));
                }
            }
            if (adapter != null)
            {
                adapter.setSecurityPositionListClosed(list);
            }
        }
    }


    //Download my profile

    protected void fetchUserProfile() {
        detachUserProfileCache();
        userProfileCache.get().register(currentUserId.toUserBaseKey(), userProfileCacheListener);
        //Get user profile from cache
        userProfileCache.get().getOrFetchAsync(currentUserId.toUserBaseKey(), false);
    }

    private void detachUserProfileCache()
    {
        userProfileCache.get().unregister(userProfileCacheListener);
    }

    protected class UserProfileFetchListener implements DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> {
        @Override
        public void onDTOReceived(@NotNull UserBaseKey key, @NotNull UserProfileDTO value) {
            refreshAvatar(value);
            showUnreadNotificationCount(value);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {

        }
    }

    private void refreshAvatar(@NotNull UserProfileDTO user){
         if(avatarIV == null || nicknameTV == null){
             return;
         }
        if (user != null) {
            if (user.picture != null) {
                ImageLoader.getInstance().displayImage(user.picture, avatarIV, UniversalImageLoader.getAvatarImageLoaderOptions());
            }
            if (user.isVisitor) {
                nicknameTV.setText(R.string.guest_user);
            } else {
                nicknameTV.setText(user.getDisplayName());
            }
        }
    }

    private void showUnreadNotificationCount(@NotNull UserProfileDTO value){
        if(gotoMineTV==null){
            return;
        }
        int count = value.unreadNotificationsCount;
        if(count <= 0){
            gotoMineIV.setVisibility(View.VISIBLE);
            gotoMineTV.setVisibility(View.GONE);
            return;
        }
        if(count > 99){
            count = 99;
        }
        gotoMineIV.setVisibility(View.GONE);
        gotoMineTV.setVisibility(View.VISIBLE);
        gotoMineTV.setText(String.valueOf(count));
    }
}

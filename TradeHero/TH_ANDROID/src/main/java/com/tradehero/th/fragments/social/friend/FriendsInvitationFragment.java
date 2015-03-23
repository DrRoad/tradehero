package com.tradehero.th.fragments.social.friend;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.utils.THToast;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.share.wechat.WeChatDTO;
import com.tradehero.th.api.share.wechat.WeChatMessageType;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.social.UserFriendsDTOList;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.social.SocialLinkHelper;
import com.tradehero.th.fragments.social.SocialLinkHelperFactory;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.network.share.SocialSharer;
import com.tradehero.th.persistence.user.UserProfileCache;
import dagger.Lazy;
import org.jetbrains.annotations.NotNull;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

@Routable("refer-friends")
public class FriendsInvitationFragment extends DashboardFragment
        implements AdapterView.OnItemClickListener, SocialFriendUserView.OnElementClickListener
{
    @InjectView(R.id.search_social_friends) EditText searchTextView;
    @InjectView(R.id.social_friend_type_list) ListView socialListView;
    @InjectView(R.id.social_friends_list) ListView friendsListView;
    @InjectView(R.id.social_search_friends_progressbar) ProgressBar searchProgressBar;
    @InjectView(R.id.social_search_friends_none) TextView friendsListEmptyView;

    @Inject SocialTypeItemFactory socialTypeItemFactory;
    @Inject SocialNetworkFactory socialNetworkFactory;
    @Inject SocialLinkHelperFactory socialLinkHelperFactory;
    @Inject UserServiceWrapper userServiceWrapper;
    @Inject CurrentUserId currentUserId;
    SocialFriendHandler socialFriendHandler;
    @Inject Lazy<UserProfileCache> userProfileCache;
    @Inject Provider<SocialFriendHandler> socialFriendHandlerProvider;
    @Inject Lazy<SocialSharer> socialSharerLazy;

    private UserFriendsDTOList userFriendsDTOs;
    private SocialFriendListItemDTOList socialFriendListItemDTOs;
    private Runnable searchTask;
    private MiddleCallback searchCallback;
    private SocialLinkHelper socialLinkHelper;

    private static final String KEY_BUNDLE = "key_bundle";
    private static final String KEY_LIST_TYPE = "key_list_type";
    private static final int LIST_TYPE_SOCIAL_LIST = 1;
    private static final int LIST_TYPE_FRIEND_LIST = 2;

    private Bundle savedState;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        socialFriendHandler = socialFriendHandlerProvider.get();
        //socialFriendHandlerFacebook = facebookSocialFriendHandlerProvider.get();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setActionBarTitle(getString(R.string.action_invite));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_invite_friends, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        restoreSavedData(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putBundle(KEY_BUNDLE, savedState != null ? savedState : saveState());
        super.onSaveInstanceState(outState);
    }

    @Override public void onStop()
    {
        detachSocialLinkHelper();
        detachSearchTask();
        super.onStop();
    }

    @Override
    public void onDestroyView()
    {
        savedState = saveState();

        super.onDestroyView();
    }

    private void restoreSavedData(Bundle savedInstanceState)
    {
        if (savedInstanceState != null && savedState == null)
        {
            savedState = savedInstanceState.getBundle(KEY_BUNDLE);
        }
        int listType = LIST_TYPE_SOCIAL_LIST;
        if (savedState != null)
        {
            listType = savedState.getInt(KEY_LIST_TYPE);
        }
        savedState = null;
        if (listType == LIST_TYPE_SOCIAL_LIST)
        {
            bindSocialTypeData();
        }
        else
        {
            bindSearchData();
        }
    }

    private Bundle saveState()
    {
        Bundle state = new Bundle();
        if (friendsListView != null)
        {
            state.putInt(KEY_LIST_TYPE, (friendsListView.getVisibility() == View.VISIBLE) ? LIST_TYPE_FRIEND_LIST : LIST_TYPE_SOCIAL_LIST);
        }
        return state;
    }

    private void initView(View rootView)
    {
        searchTextView.addTextChangedListener(new SearchTextWatcher());
    }

    private void bindSocialTypeData()
    {
        List<SocialTypeItem> socialTypeItemList = socialTypeItemFactory.getSocialTypeList();
        SocialTypeListAdapter adapter = new SocialTypeListAdapter(getActivity(), 0, socialTypeItemList);
        socialListView.setAdapter(adapter);
        socialListView.setOnItemClickListener(this);
        showSocialTypeList();
    }

    private void bindSearchData()
    {
        socialFriendListItemDTOs = new SocialFriendListItemDTOList(userFriendsDTOs, (UserFriendsDTO) null);
        if (friendsListView.getAdapter() == null)
        {
            SocialFriendsAdapter socialFriendsListAdapter =
                    new SocialFriendsAdapter(
                            getActivity(),
                            socialFriendListItemDTOs,
                            R.layout.social_friends_item,
                            R.layout.social_friends_item_header);
            socialFriendsListAdapter.setOnElementClickedListener(this);
            friendsListView.setAdapter(socialFriendsListAdapter);
            friendsListView.setEmptyView(friendsListEmptyView);
        }
        else
        {
            SocialFriendsAdapter socialFriendsListAdapter = (SocialFriendsAdapter) friendsListView.getAdapter();
            socialFriendsListAdapter.clear();
            socialFriendsListAdapter.addAll(socialFriendListItemDTOs);
        }
        showSearchList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        SocialTypeItem item = (SocialTypeItem) parent.getItemAtPosition(position);
        if(item.socialNetwork == SocialNetworkEnum.WECHAT)
        {
            inviteWechatFriends();
            return;
        }

        boolean linked = checkLinkedStatus(item.socialNetwork);
        if (linked)
        {
            pushSocialInvitationFragment(item.socialNetwork);
        }
        else
        {
            THToast.show(R.string.friend_link_social_network);
            //pushSettingsFragment();
            linkSocialNetwork(item.socialNetwork);
        }
    }

    private void inviteWechatFriends()
    {
        UserProfileDTO userProfileDTO = userProfileCache.get().get(currentUserId.toUserBaseKey());
        if (userProfileDTO != null)
        {
            WeChatDTO weChatDTO = new WeChatDTO();
            weChatDTO.id = 0;
            weChatDTO.type = WeChatMessageType.Invite;
            weChatDTO.title = getString(WeChatMessageType.Invite.getTitleResId(), userProfileDTO.referralCode);
            socialSharerLazy.get().share(weChatDTO); // TODO proper callback?
        }
    }

    private void canclePendingSearchTask()
    {
        View view = getView();
        if (view != null && searchTask != null)
        {
            view.removeCallbacks(searchTask);
        }
    }

    private void detachSearchTask()
    {
        if (searchCallback != null)
        {
            searchCallback.setPrimaryCallback(null);
        }
    }

    protected void detachSocialLinkHelper()
    {
        if (socialLinkHelper != null)
        {
            socialLinkHelper.setSocialLinkingCallback(null);
        }
        socialLinkHelper = null;
    }

    private void scheduleSearch()
    {
        View view = getView();
        if (view != null)
        {
            if (searchTask != null)
            {
                view.removeCallbacks(searchTask);
            }
            searchTask = new Runnable()
            {
                @Override public void run()
                {
                    if (getView() != null)
                    {
                        showSocialTypeListWithProgress();
                        searchSocialFriends();
                    }
                }
            };
            view.postDelayed(searchTask, 500L);
        }
    }

    private void showSocialTypeList()
    {
        socialListView.setVisibility(View.VISIBLE);
        friendsListView.setVisibility(View.GONE);
        searchProgressBar.setVisibility(View.GONE);
        friendsListEmptyView.setVisibility(View.GONE);
    }

    private void showSocialTypeListWithProgress()
    {
        socialListView.setVisibility(View.VISIBLE);
        friendsListView.setVisibility(View.GONE);
        searchProgressBar.setVisibility(View.VISIBLE);
        friendsListEmptyView.setVisibility(View.GONE);
    }

    private void showSearchList()
    {
        socialListView.setVisibility(View.GONE);
        friendsListView.setVisibility(View.VISIBLE);
        searchProgressBar.setVisibility(View.GONE);
        //friendsListEmptyView.setVisibility(View.GONE);
    }

    private void searchSocialFriends()
    {
        detachSearchTask();
        String query = searchTextView.getText().toString();
        searchCallback = userServiceWrapper.searchSocialFriends(currentUserId.toUserBaseKey(), null, query, new SearchFriendsCallback());
    }

    private void pushSocialInvitationFragment(SocialNetworkEnum socialNetwork)
    {
        Class<? extends SocialFriendsFragment> target = socialNetworkFactory.findProperTargetFragment(socialNetwork);
        Bundle bundle = new Bundle();
        getDashboardNavigator().pushFragment(target, bundle);
    }

    private void linkSocialNetwork(SocialNetworkEnum socialNetworkEnum)
    {
        detachSocialLinkHelper();
        socialLinkHelper = socialLinkHelperFactory.buildSocialLinkerHelper(socialNetworkEnum);
        // TODO Pass a callback to be able to move to the social fragment
        socialLinkHelper.link();
    }

    private boolean checkLinkedStatus(SocialNetworkEnum socialNetwork)
    {
        UserProfileDTO updatedUserProfileDTO =
                userProfileCache.get().get(currentUserId.toUserBaseKey());
        if (updatedUserProfileDTO != null)
        {
            switch (socialNetwork)
            {
                case WB:
                    return updatedUserProfileDTO.wbLinked;
                case QQ:
                    return updatedUserProfileDTO.qqLinked;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public void onFollowButtonClick(@NotNull UserFriendsDTO userFriendsDTO)
    {
        handleFollowUsers(userFriendsDTO);
    }

    @Override
    public void onInviteButtonClick(@NotNull UserFriendsDTO userFriendsDTO)
    {
    }

    public void onCheckBoxClick(@NotNull UserFriendsDTO userFriendsDTO)
    {
        Timber.d("onCheckBoxClicked " + userFriendsDTO);
    }

    protected void handleFollowUsers(UserFriendsDTO userToFollow)
    {
        List<UserFriendsDTO> usersToFollow = Arrays.asList(userToFollow);
        socialFriendHandler.followFriends(usersToFollow, new FollowFriendCallback(usersToFollow));
    }

    private void handleInviteSuccess(List<UserFriendsDTO> usersToInvite)
    {
        //Invite Success will not disappear the friend in Invite
        THToast.show(R.string.invite_friend_request_sent);
    }

    private void handleFollowSuccess(List<UserFriendsDTO> usersToFollow)
    {
        if (userFriendsDTOs != null && usersToFollow != null)
        {
            for (UserFriendsDTO userFriendsDTO : usersToFollow)
            {
                userFriendsDTOs.remove(userFriendsDTO);
            }
        }
        SocialFriendsAdapter socialFriendsAdapter = (SocialFriendsAdapter) friendsListView.getAdapter();

        socialFriendListItemDTOs = new SocialFriendListItemDTOList(userFriendsDTOs, (UserFriendsDTO) null);

        socialFriendsAdapter.clear();
        socialFriendsAdapter.addAll(socialFriendListItemDTOs);
    }

    class FollowFriendCallback extends SocialFriendHandler.RequestCallback<UserProfileDTO>
    {
        final List<UserFriendsDTO> usersToFollow;

        //<editor-fold desc="Constructors">
        private FollowFriendCallback(List<UserFriendsDTO> usersToFollow)
        {
            super(getActivity());
            this.usersToFollow = usersToFollow;
        }
        //</editor-fold>

        @Override
        public void success(UserProfileDTO userProfileDTO, Response response)
        {
            super.success(userProfileDTO, response);
            if (response.getStatus() == 200 || response.getStatus() == 204)
            {
                handleFollowSuccess(usersToFollow);
            }
            else
            {
                THToast.show(R.string.follow_friend_request_error);
            }
        }

        @Override
        public void failure(RetrofitError retrofitError)
        {
            super.failure(retrofitError);
            THToast.show(R.string.follow_friend_request_error);
        }
    }

    private UserFriendsDTOList filterTheDuplicated(List<UserFriendsDTO> friendDTOList)
    {
        TreeSet<UserFriendsDTO> hashSet = new TreeSet<>();
        hashSet.addAll(friendDTOList);
        return new UserFriendsDTOList(hashSet);
    }

    class SearchFriendsCallback implements Callback<UserFriendsDTOList>
    {
        @Override
        public void success(UserFriendsDTOList userFriendsDTOs, Response response)
        {
            FriendsInvitationFragment.this.userFriendsDTOs = filterTheDuplicated(userFriendsDTOs);
            bindSearchData();
        }

        @Override
        public void failure(RetrofitError retrofitError)
        {
            Timber.e(retrofitError, "SearchFriendsCallback error");
            // TODO need to tell user.
            showSocialTypeList();
        }
    }

    class SearchTextWatcher implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            canclePendingSearchTask();
            if (s != null && s.toString().trim().length() > 0)
            {
                scheduleSearch();
            }
            else
            {
                showSocialTypeList();
            }
        }
    }
}

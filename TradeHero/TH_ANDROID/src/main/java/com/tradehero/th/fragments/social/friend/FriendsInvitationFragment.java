package com.tradehero.th.fragments.social.friend;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.UserServiceWrapper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by wanglinag on 14-5-26.
 */
public class FriendsInvitationFragment extends DashboardFragment implements AdapterView.OnItemClickListener, SocialFriendItemView.OnElementClickListener
{
    @InjectView(R.id.search_social_friends) EditText searchTextView;
    @InjectView(R.id.social_friend_type_list) ListView socialListView;
    @InjectView(R.id.social_friends_list) ListView friendsListView;
    @InjectView(R.id.social_search_friends_progressbar) ProgressBar searchProgressBar;
    @InjectView(R.id.social_search_friends_none) TextView friendsListEmptyView;

    @Inject SocialTypeFactory socialTypeFactory;
    @Inject UserServiceWrapper userServiceWrapper;
    @Inject CurrentUserId currentUserId;
    @Inject SocialFriendHandler socialFriendHandler;

    private List<UserFriendsDTO> userFriendsDTOs;
    private Runnable searchTask;
    private MiddleCallback searchCallback;

    private static final String KEY_BUNDLE = "key_bundle";
    private static final String KEY_LIST_TYPE = "key_list_type";
    private static final int LIST_TYPE_SOCIAL_LIST = 1;
    private static final int LIST_TYPE_FRIEND_LIST = 2;

    private Bundle savedState;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(getString(R.string.action_invite));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup v = (ViewGroup)inflater.inflate(R.layout.fragment_invite_friends,container,false);
        ButterKnife.inject(this,v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        restoreSavedData(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(KEY_BUNDLE, savedState != null ? savedState : saveState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        savedState = saveState();

        super.onDestroyView();
    }

    private void restoreSavedData(Bundle savedInstanceState) {
        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(KEY_BUNDLE);
        }
        int listType = LIST_TYPE_SOCIAL_LIST;
        if(savedState != null) {
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

    private Bundle saveState() {
        Bundle state = new Bundle();
        state.putInt(KEY_LIST_TYPE, (friendsListView.getVisibility() == View.VISIBLE) ? LIST_TYPE_FRIEND_LIST : LIST_TYPE_SOCIAL_LIST);
        return state;
    }

    private void initView(View rootView)
    {
        searchTextView.addTextChangedListener(new SearchTextWatcher());
    }

    private void bindSocialTypeData()
    {
        List<SocalTypeItem> socalTypeItemList =  socialTypeFactory.getSocialTypeList();
        SocalTypeListAdapter adapter = new SocalTypeListAdapter(getActivity(),0,socalTypeItemList);
        socialListView.setAdapter(adapter);
        socialListView.setOnItemClickListener(this);
        showSocialTypeList();
    }

    private void bindSearchData()
    {
        if (friendsListView.getAdapter() == null)
        {
            SocialFriendsAdapter  socialFriendsListAdapter =
                    new SocialFriendsAdapter(
                            getActivity(),
                            userFriendsDTOs,
                            R.layout.social_friends_item);
            socialFriendsListAdapter.setOnElementClickedListener(this);
            friendsListView.setAdapter(socialFriendsListAdapter);
            friendsListView.setEmptyView(friendsListEmptyView);
        }
        else
        {
            SocialFriendsAdapter socialFriendsListAdapter = (SocialFriendsAdapter)friendsListView.getAdapter();
            socialFriendsListAdapter.clear();
            socialFriendsListAdapter.addAll(userFriendsDTOs);
        }
        showSearchList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SocalTypeItem item =  (SocalTypeItem)parent.getItemAtPosition(position);

        pushSocialInvitationFragment(item.socialNetwork);
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
        searchCallback =  userServiceWrapper.searchSocialFriends(currentUserId.toUserBaseKey(), null, query, new SearchFriendsCallback());
    }

    private void pushSocialInvitationFragment(SocialNetworkEnum socialNetwork)
    {
        Class<? extends SocialFriendsFragment> target = socialTypeFactory.findProperTargetFragment(socialNetwork);
        Bundle bundle = new Bundle();
        getNavigator().pushFragment(target,bundle);
    }

    @Override
    public void onFollowButtonClick(UserFriendsDTO userFriendsDTO) {
        handleFollowUsers(userFriendsDTO);
    }

    @Override
    public void onInviteButtonClick(UserFriendsDTO userFriendsDTO) {
        handleInviteUsers(userFriendsDTO);
    }

    protected void handleFollowUsers(UserFriendsDTO userToFollow)
    {
        List<UserFriendsDTO> usersToFollow = Arrays.asList(userToFollow);
        socialFriendHandler.followFriends(usersToFollow,new FollowFriendCallback(usersToFollow));
    }

    // TODO via which social network to invite user?
    protected void handleInviteUsers(UserFriendsDTO userToInvite)
    {
        if (!TextUtils.isEmpty(userToInvite.liId) || !TextUtils.isEmpty(userToInvite.twId))
        {
            List<UserFriendsDTO> usersToInvite = Arrays.asList(userToInvite);
            socialFriendHandler.inviteFriends(currentUserId.toUserBaseKey(), usersToInvite, new InviteFriendCallback(usersToInvite));
        }
        else if (!TextUtils.isEmpty(userToInvite.fbId))
        {
            //TODO do invite on the client side.
        }
        else
        {
            //if all ids are empty or only wbId is not empty, how to do?
        }

    }

    private void handleInviteSuccess(List<UserFriendsDTO> usersToInvite)
    {
        if (userFriendsDTOs != null && usersToInvite != null)
        {
            for (UserFriendsDTO userFriendsDTO:usersToInvite)
            {
                userFriendsDTOs.remove(userFriendsDTO);
            }

        }
        SocialFriendsAdapter socialFriendsAdapter = (SocialFriendsAdapter)friendsListView.getAdapter();

        socialFriendsAdapter.clear();
        socialFriendsAdapter.addAll(userFriendsDTOs);
    }

    private void handleFollowSuccess(List<UserFriendsDTO> usersToFollow)
    {
        if (userFriendsDTOs != null && usersToFollow != null)
        {
            for (UserFriendsDTO userFriendsDTO:usersToFollow)
            {
                userFriendsDTOs.remove(userFriendsDTO);
            }

        }
        SocialFriendsAdapter socialFriendsAdapter = (SocialFriendsAdapter)friendsListView.getAdapter();

        socialFriendsAdapter.clear();
        socialFriendsAdapter.addAll(userFriendsDTOs);
    }

    class FollowFriendCallback extends SocialFriendHandler.RequestCallback<UserProfileDTO> {

        List<UserFriendsDTO> usersToFollow;


        private FollowFriendCallback(List<UserFriendsDTO> usersToFollow)
        {
            super(getActivity());
            this.usersToFollow = usersToFollow;
        }

        @Override
        public void success(UserProfileDTO userProfileDTO, Response response) {
            super.success(userProfileDTO,response);
            if (response.getStatus() != 200)
            {
                // TODO
                THToast.show("Error");
                return;
            }
            handleFollowSuccess(usersToFollow);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            super.failure(retrofitError);
            THToast.show("Error");
        }
    };

    class InviteFriendCallback extends SocialFriendHandler.RequestCallback<Response> {

        List<UserFriendsDTO> usersToInvite;


        private InviteFriendCallback(List<UserFriendsDTO> usersToInvite)
        {
            super(getActivity());
            this.usersToInvite = usersToInvite;
        }

        @Override
        public void success(Response data, Response response) {
            super.success(data,response);
            if (response.getStatus() != 200)
            {
                // TODO
                THToast.show("Error");
                return;
            }
            handleInviteSuccess(usersToInvite);

        }

        @Override
        public void failure(RetrofitError retrofitError) {
            super.failure(retrofitError);
            // TODO
            THToast.show("Error");
        }
    };

    private List<UserFriendsDTO> filterTheDublicated(List<UserFriendsDTO> friendDTOList)
    {
        TreeSet<UserFriendsDTO> hashSet = new TreeSet<>();
        hashSet.addAll(friendDTOList);
        ArrayList list = new ArrayList();
        list.addAll(hashSet);
        return list;
    }


    class SearchFriendsCallback implements  Callback<List<UserFriendsDTO>> {

        @Override
        public void success(List<UserFriendsDTO> userFriendsDTOs, Response response) {
            FriendsInvitationFragment.this.userFriendsDTOs = filterTheDublicated(userFriendsDTOs);
            bindSearchData();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Timber.e(retrofitError,"SearchFriendsCallback error");
            // TODO need to tell user.
            showSocialTypeList();
        }
    }

    class SearchTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
                canclePendingSearchTask();
                if (s != null && s.toString().trim().length() > 0) {
                    scheduleSearch();
                } else {
                    showSocialTypeList();
                }
        }
    }

    @Override
    public boolean isTabBarVisible() {
        return false;
    }
}

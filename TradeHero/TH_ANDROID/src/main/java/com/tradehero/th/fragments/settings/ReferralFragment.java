package com.tradehero.th.fragments.settings;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.th.R;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.users.CurrentUserBaseKeyHolder;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.UserService;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ReferralFragment extends DashboardFragment
{
    @Inject protected CurrentUserBaseKeyHolder currentUserBaseKeyHolder;
    @Inject protected Lazy<UserService> userService;

    private FriendListAdapter referFriendListAdapter;
    private ProgressDialog progressDialog;
    private View headerView;
    private Button inviteFriendButton;


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.refer_fragment, container, false);
        headerView = inflater.inflate(R.layout.refer_friend_header, null, false);
        initView(view);
        return view;
    }

    private void initView(View view)
    {
        inviteFriendButton = (Button) view.findViewById(R.id.refer_friend_invite_button);

        referFriendListAdapter = createFriendListAdapter();
        StickyListHeadersListView stickyListHeadersListView = (StickyListHeadersListView) view.findViewById(R.id.sticky_list);

        View emptyView = view.findViewById(R.id.refer_friend_list_empty_view);
        if (emptyView != null)
        {
            //stickyListHeadersListView.getWrappedList().setEmptyView(emptyView);
        }

        if (stickyListHeadersListView.getHeaderViewsCount() == 0)
        {
            stickyListHeadersListView.addHeaderView(headerView);
        }
        stickyListHeadersListView.setAdapter(referFriendListAdapter);

        stickyListHeadersListView.getWrappedList().setOnItemSelectedListener(listItemSelectedListener);
    }

    private FriendListAdapter createFriendListAdapter()
    {
        return new FriendListAdapter(getActivity(), getActivity().getLayoutInflater(), R.layout.refer_friend_list_item_view);
    }

    @Override public void onResume()
    {
        super.onResume();

        getProgressDialog().show();
        userService.get().getFriends(currentUserBaseKeyHolder.getCurrentUserBaseKey().key,
                getFriendsCallback);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setTitle(getString(R.string.invite_friends));

        super.onCreateOptionsMenu(menu, inflater);
    }

    private ProgressDialog getProgressDialog()
    {
        if (progressDialog != null)
        {
            return progressDialog;
        }
        progressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.loading_loading),
                getString(R.string.please_wait), true);
        progressDialog.hide();
        return progressDialog;
    }

    private void handleFriendListReceived(List<UserFriendsDTO> userFriendsDTOs)
    {
        if (referFriendListAdapter != null)
        {
            referFriendListAdapter.setItems(userFriendsDTOs);
            referFriendListAdapter.notifyDataSetChanged();
        }
    }

    private THCallback<List<UserFriendsDTO>> getFriendsCallback = new THCallback<List<UserFriendsDTO>>()
    {
        @Override protected void finish()
        {
            getProgressDialog().dismiss();
        }

        @Override protected void success(List<UserFriendsDTO> userFriendsDTOs, THResponse thResponse)
        {
            handleFriendListReceived(userFriendsDTOs);
        }

        @Override protected void failure(THException ex)
        {

        }
    };

    private AdapterView.OnItemSelectedListener listItemSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            handleItemSelected();
        }

        @Override public void onNothingSelected(AdapterView<?> parent)
        {
            handleNoItemSelected();
        }
    };

    private void handleNoItemSelected()
    {
        if (inviteFriendButton != null)
        {
            inviteFriendButton.setVisibility(View.GONE);
        }
    }

    private void handleItemSelected()
    {
        if (inviteFriendButton != null)
        {
            inviteFriendButton.setVisibility(View.VISIBLE);
        }
    }

    //<editor-fold desc="Tab bar informer">
    @Override public boolean isTabBarVisible()
    {
        return false;
    }
    //</editor-fold>
}
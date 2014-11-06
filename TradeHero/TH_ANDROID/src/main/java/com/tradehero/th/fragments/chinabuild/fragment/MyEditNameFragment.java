package com.tradehero.th.fragments.chinabuild.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.ProgressDialogUtil;
import dagger.Lazy;

import javax.inject.Inject;

public class MyEditNameFragment extends DashboardFragment implements View.OnClickListener
{
    @InjectView(R.id.display_name) EditText mName;
    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCache userProfileCache;
    @Inject ProgressDialogUtil progressDialogUtil;
    @Inject Lazy<UserServiceWrapper> userServiceWrapper;
    private MiddleCallback<UserProfileDTO> middleCallbackUpdateUserProfile;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setHeadViewMiddleMain(R.string.settings_my_name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.setting_my_name_fragment_layout, container, false);
        ButterKnife.inject(this, view);
        setNeedToMonitorBackPressed(true);
        UserProfileDTO userProfileDTO = userProfileCache.get(currentUserId.toUserBaseKey());
        if (userProfileDTO != null)
        {
            mName.setText(userProfileDTO.getDisplayName());
        }
        return view;
    }

    @Override public void onResume()
    {
        super.onResume();
    }

    @Override public void onClick(View view)
    {
        switch (view.getId())
        {
        }
    }

    @Override public void onStop()
    {
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        super.onDestroy();
    }

    @Override public void onClickHeadLeft()
    {
        String displayNameStr = mName.getText().toString();
        if(TextUtils.isEmpty(displayNameStr)){
            popCurrentFragment();
            return;
        }
        if(displayNameStr.contains(" ")){
            THToast.show(R.string.sign_in_display_name_no_blank);
            return;
        }
        UserProfileDTO userProfileDTO = userProfileCache.get(currentUserId.toUserBaseKey());
        if (userProfileDTO != null)
        {
            if (!userProfileDTO.displayName.contentEquals(mName.getText()))
            {
                progressDialogUtil.show(getActivity(), R.string.alert_dialog_please_wait, R.string.updating);
                UserFormDTO userFormDTO = createForm();
                detachMiddleCallbackUpdateUserProfile();
                middleCallbackUpdateUserProfile = userServiceWrapper.get().updateName(
                        currentUserId.toUserBaseKey(),
                        userFormDTO,
                        createUpdateUserProfileCallback());
                return;
            }
        }
        super.onClickHeadLeft();
    }

    public UserFormDTO createForm()
    {
        UserFormDTO created = new UserFormDTO();
            created.displayName = mName.getText().toString();
        return created;
    }

    private void detachMiddleCallbackUpdateUserProfile()
    {
        if (middleCallbackUpdateUserProfile != null)
        {
            middleCallbackUpdateUserProfile.setPrimaryCallback(null);
        }
        middleCallbackUpdateUserProfile = null;
    }

    private THCallback<UserProfileDTO> createUpdateUserProfileCallback()
    {
        return new THCallback<UserProfileDTO>()
        {
            @Override protected void success(UserProfileDTO userProfileDTO, THResponse thResponse)
            {
                progressDialogUtil.dismiss(getActivity());
                userProfileCache.put(currentUserId.toUserBaseKey(), userProfileDTO);
                THToast.show(R.string.settings_update_profile_successful);
                popCurrentFragment();
            }

            @Override protected void failure(THException ex)
            {
                progressDialogUtil.dismiss(getActivity());
                THToast.show(ex.getMessage());
                popCurrentFragment();
            }
        };
    }

    @Override
    public void onBackPressed(){
        onClickHeadLeft();
    }

}

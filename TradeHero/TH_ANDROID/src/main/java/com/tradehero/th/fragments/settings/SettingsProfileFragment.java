package com.tradehero.th.fragments.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.form.UserFormFactory;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.auth.EmailAuthenticationProvider;
import com.tradehero.th.base.JSONCredentials;
import com.tradehero.th.base.Navigator;
import com.tradehero.th.base.NavigatorActivity;
import com.tradehero.th.base.THUser;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.user.auth.CredentialsDTO;
import com.tradehero.th.models.user.auth.EmailCredentialsDTO;
import com.tradehero.th.models.user.auth.MainCredentialsPreference;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.prefs.AuthHeader;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.utils.NetworkUtils;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.widget.ValidationListener;
import com.tradehero.th.widget.ValidationMessage;
import dagger.Lazy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import timber.log.Timber;

public class SettingsProfileFragment extends DashboardFragment implements View.OnClickListener, ValidationListener
{
    //java.lang.IllegalArgumentException: Can only use lower 16 bits for requestCode
    private static final int REQUEST_GALLERY = new Random(new Date().getTime()).nextInt(Short.MAX_VALUE);
    private static final int REQUEST_CAMERA = new Random(new Date().getTime() + 1).nextInt(Short.MAX_VALUE);

    protected Button updateButton;
    private ProfileInfoView profileView;
    private EditText referralCodeEditText;

    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserProfileCache> userProfileCache;
    @Inject Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject ProgressDialogUtil progressDialogUtil;
    @Inject @AuthHeader String authenticationHeader;
    @Inject MainCredentialsPreference mainCredentialsPreference;

    private MiddleCallback<UserProfileDTO> middleCallbackUpdateUserProfile;
    private DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> userProfileCacheListener;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings_profile, container, false);

        initSetup(view);
        setHasOptionsMenu(true);

        this.populateCurrentUser();
        return view;
    }

    protected void initSetup(View view)
    {
        FocusableOnTouchListener touchListener = new FocusableOnTouchListener();

        profileView = (ProfileInfoView) view.findViewById(R.id.profile_info);

        profileView.setOnTouchListenerOnFields(touchListener);
        profileView.addValidationListenerOnFields(this);
        profileView.setListener(createProfileViewListener());

        updateButton = (Button) view.findViewById(R.id.authentication_sign_up_button);
        updateButton.setText(R.string.update);
        updateButton.setOnClickListener(this);

        referralCodeEditText = (EditText) view.findViewById(R.id.authentication_sign_up_referral_code);
        referralCodeEditText.setVisibility(View.GONE);

        //signupButton.setOnTouchListener(this);
    }

    @Override public void onStop()
    {
        detachMiddleCallbackUpdateUserProfile();
        detachUserProfileCache();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        if (profileView != null)
        {
            profileView.setOnTouchListenerOnFields(null);
            profileView.removeAllListenersOnFields();
            profileView.setNullOnFields();
            profileView.setListener(null);
        }
        profileView = null;
        if (updateButton != null)
        {
            updateButton.setOnClickListener(null);
        }
        updateButton = null;
        referralCodeEditText = null;
        super.onDestroyView();
    }

    private void detachMiddleCallbackUpdateUserProfile()
    {
        if (middleCallbackUpdateUserProfile != null)
        {
            middleCallbackUpdateUserProfile.setPrimaryCallback(null);
        }
        middleCallbackUpdateUserProfile = null;
    }

    private void detachUserProfileCache()
    {
        if (userProfileCacheListener != null)
        {
            userProfileCache.get().unregister(userProfileCacheListener);
        }
        userProfileCacheListener = null;
    }

    @Override public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.authentication_sign_up_button:
                updateProfile(view);
                break;
            case R.id.image_optional:
                askImageFromLibrary();
                break;
        }
    }

    protected void forceValidateFields()
    {
        profileView.forceValidateFields();
    }

    public boolean areFieldsValid()
    {
        return profileView.areFieldsValid();
    }

    protected Map<String, Object> getUserFormMap()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(UserFormFactory.KEY_TYPE, EmailCredentialsDTO.EMAIL_AUTH_TYPE);
        profileView.populateUserFormMap(map);
        return map;
    }

    public JSONCredentials getUserFormJSON()
    {
        return new JSONCredentials(getUserFormMap());
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            if ((requestCode == REQUEST_CAMERA || requestCode == REQUEST_GALLERY) && data != null)
            {
                try
                {
                    if (profileView != null)
                    {
                        profileView.handleDataFromLibrary(data);
                    }
                }
                catch (OutOfMemoryError e)
                {
                    THToast.show(R.string.error_decode_image_memory);
                }
                catch (Exception e)
                {
                    THToast.show(R.string.error_fetch_image_library);
                    Timber.e(e, "Failed to extract image from library");
                }
            }
            else if (requestCode == REQUEST_GALLERY)
            {
                Timber.e(new Exception("Got null data from library"), "");
            }
        }
        else if (resultCode != Activity.RESULT_CANCELED)
        {
            Timber.e(new Exception("Failed to get image from libray, resultCode: " + resultCode), "");
        }
    }

    private void populateCurrentUser()
    {
        detachUserProfileCache();
        userProfileCacheListener = createUserProfileCacheListener();
        userProfileCache.get().register(currentUserId.toUserBaseKey(), userProfileCacheListener);
        userProfileCache.get().getOrFetchAsync(currentUserId.toUserBaseKey());
        try
        {
            CredentialsDTO credentials = mainCredentialsPreference.getCredentials();
            if (credentials != null)
            {
                this.profileView.populateCredentials(credentials.createJSON());
            }
        }
        catch (JSONException e)
        {
            Timber.e(e, "Failed to populate current user %s", authenticationHeader);
        }
    }

    private DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> createUserProfileCacheListener()
    {
        return new DTOCacheNew.Listener<UserBaseKey, UserProfileDTO>()
        {
            @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull UserProfileDTO value)
            {
                profileView.populate(value);
            }

            @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
            {
                THToast.show(new THException(error));
            }
        };
    }

    private void updateProfile(View view)
    {
        DeviceUtil.dismissKeyboard(getActivity(), view);
        forceValidateFields();

        if (!NetworkUtils.isConnected(getActivity()))
        {
            THToast.show(R.string.network_error);
        }
        else if (!areFieldsValid())
        {
            THToast.show(R.string.validation_please_correct);
        }
        else
        {
            profileView.progressDialog = progressDialogUtil.show(
                    getActivity(),
                    R.string.alert_dialog_please_wait,
                    R.string.authentication_connecting_tradehero_only);
            profileView.progressDialog.setCancelable(true);
            EmailCredentialsDTO emailCredentialsDTO = profileView.getEmailCredentialsDTO();
            EmailAuthenticationProvider.setCredentials(this.getUserFormJSON());

            UserFormDTO userFormDTO = profileView.createForm();
            if (userFormDTO == null)
            {
                return;
            }

            detachMiddleCallbackUpdateUserProfile();
            middleCallbackUpdateUserProfile = userServiceWrapper.get().updateProfile(
                    currentUserId.toUserBaseKey(),
                    userFormDTO,
                    createUpdateUserProfileCallback(emailCredentialsDTO));
        }
    }

    private THCallback<UserProfileDTO> createUpdateUserProfileCallback(final EmailCredentialsDTO emailCredentialsDTO)
    {
        return new THCallback<UserProfileDTO>()
        {
            @Override protected void success(UserProfileDTO userProfileDTO, THResponse thResponse)
            {
                profileView.progressDialog.hide(); // Before otherwise it is reset
                THToast.show(R.string.settings_update_profile_successful);
                Navigator navigator = ((NavigatorActivity) getActivity()).getNavigator();
                navigator.popFragment();
                if (emailCredentialsDTO != null && mainCredentialsPreference.getCredentials() instanceof EmailCredentialsDTO)
                {
                    THUser.saveCredentialsToUserDefaults(emailCredentialsDTO);
                }
            }

            @Override protected void failure(THException ex)
            {
                THToast.show(ex.getMessage());
            }
        };
    }

    public String getPath(Uri uri)
    {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    protected void askImageFromLibrary()
    {
        Intent libraryIntent = new Intent(Intent.ACTION_PICK);
        libraryIntent.setType("image/jpeg");
        startActivityForResult(libraryIntent, REQUEST_GALLERY);
    }

    protected void askImageFromCamera()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
        catch (ActivityNotFoundException e)
        {
            THToast.show(R.string.error_launch_camera);
        }
    }

    @Override public void notifyValidation(ValidationMessage message)
    {
        if (message != null && !message.getStatus() && message.getMessage() != null)
        {
            THToast.show(message.getMessage());
        }
    }

    protected ProfileInfoView.Listener createProfileViewListener()
    {
        return new SettingsProfileViewListener();
    }

    protected class SettingsProfileViewListener implements ProfileInfoView.Listener
    {
        @Override public void onUpdateRequested()
        {
            // TODO
        }

        @Override public void onImageFromCameraRequested()
        {
            askImageFromCamera();
        }

        @Override public void onImageFromLibraryRequested()
        {
            askImageFromLibrary();
        }
    }
}




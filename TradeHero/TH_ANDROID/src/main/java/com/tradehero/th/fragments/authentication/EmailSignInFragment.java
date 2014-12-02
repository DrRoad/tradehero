package com.tradehero.th.fragments.authentication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.form.UserFormFactory;
import com.tradehero.th.auth.AuthenticationMode;
import com.tradehero.th.base.THUser;
import com.tradehero.th.data.sp.THSharePreferenceManager;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.DeviceUtil;
import com.tradehero.th.utils.ProgressDialogUtil;
import com.tradehero.th.utils.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.MethodEvent;

import javax.inject.Inject;
import java.util.Map;

public class EmailSignInFragment extends EmailSignInOrUpFragment
{
    private EditText email;
    private EditText password;
    private TextView forgotPasswordLink;
    private ImageView backButton;
    @Inject Analytics analytics;

    @Inject UserServiceWrapper userServiceWrapper;
    @Inject ProgressDialogUtil progressDialogUtil;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DaggerUtils.inject(this);
        analytics.addEventAuto(new MethodEvent(AnalyticsConstants.SIGN_IN, AnalyticsConstants.BUTTON_LOGIN_OFFICAL));
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        DeviceUtil.showKeyboardDelayed(email);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setHeadViewMiddleMain(R.string.guide_screen_login);
        setHeadViewRight0(R.string.authentication_register);
        setRight0ButtonOnClickListener(onClickListener);
        setLeftButtonOnClickListener(onClickListener);
    }

    @Override public int getDefaultViewId()
    {
        return R.layout.authentication_email_sign_in;
    }

    @Override protected void initSetup(View view)
    {
        email = (EditText) view.findViewById(R.id.authentication_sign_in_email);

        signButton = (Button) view.findViewById(R.id.btn_login);
        signButton.setOnClickListener(this);
        signButton.setEnabled(false);

        password = (EditText) view.findViewById(R.id.et_pwd_login);
        password.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {
                if (signButton != null)
                {
                    signButton.setEnabled(charSequence.length() > 5);
                }
            }

            @Override public void afterTextChanged(Editable editable)
            {

            }
        });

        email.setText(THSharePreferenceManager.getAccount(getActivity()));

        forgotPasswordLink = (TextView) view.findViewById(R.id.authentication_sign_in_forgot_password);
        forgotPasswordLink.setOnClickListener(this);
    }

    @Override public void onDestroyView()
    {
        this.email = null;

        this.password = null;

        if (this.signButton != null)
        {
            this.signButton.setOnClickListener(null);
        }
        this.signButton = null;

        if (this.forgotPasswordLink != null)
        {
            this.forgotPasswordLink.setOnClickListener(null);
        }
        this.forgotPasswordLink = null;
        if (backButton != null)
        {
            backButton.setOnClickListener(null);
            backButton = null;
        }
        super.onDestroyView();
    }



    @Override public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_login:
                //clear old user info
                THUser.clearCurrentUser();
                if (checkEmailAndPassword())
                {
                    //Save account
                    THSharePreferenceManager.saveAccount(getActivity(), email.getText().toString());
                    handleSignInOrUpButtonClicked(view);

                    if (isValidPhoneNumber(email.getText()))
                    {
                        analytics.addEventAuto(
                                new MethodEvent(AnalyticsConstants.SIGN_IN, AnalyticsConstants.BUTTON_LOGIN_TELNUMBER));
                    }
                    else
                    {
                        analytics.addEventAuto(new MethodEvent(AnalyticsConstants.SIGN_IN, AnalyticsConstants.BUTTON_LOGIN_EMAIL));
                    }
                }
                break;
            case R.id.authentication_sign_in_forgot_password:
                gotoPasswordForgetFragment();
                break;
        }
    }

    private boolean checkEmailAndPassword()
    {
        if (email.getText().toString().isEmpty())
        {
            THToast.show(R.string.register_error_account);
            return false;
        }
        else if (password.getText().length() < 6)
        {
            THToast.show(R.string.register_error_password);
            return false;
        }
        return true;
    }

    @Override protected void forceValidateFields()
    {
    }

    @Override public boolean areFieldsValid()
    {
        return true;
    }

    @Override protected Map<String, Object> getUserFormMap()
    {
        Map<String, Object> map = super.getUserFormMap();
        map.put(UserFormFactory.KEY_EMAIL, email.getText().toString());
        map.put(UserFormFactory.KEY_PASSWORD, password.getText().toString());
        return map;
    }

    private void gotoPasswordForgetFragment(){
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_right_in, R.anim.slide_left_out,
                        R.anim.slide_left_in, R.anim.slide_right_out)
                .replace(R.id.fragment_content, new PasswordResetFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override public AuthenticationMode getAuthenticationMode()
    {
        return AuthenticationMode.SignIn;
    }
}

package com.tradehero.th.fragments.chinabuild.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.th.R;
import com.tradehero.th.activities.ActivityHelper;
import com.tradehero.th.base.THUser;
import com.tradehero.th.fragments.base.DashboardFragment;
import timber.log.Timber;

public class SettingFragment extends DashboardFragment implements View.OnClickListener
{
    @InjectView(R.id.settings_score) RelativeLayout mScoreLayout;
    @InjectView(R.id.settings_logout) LinearLayout mLogoutLayout;
    @InjectView(R.id.settings_version_code) TextView mVersionCode;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setHeadViewMiddleMain(R.string.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.setting_fragment_layout, container, false);
        ButterKnife.inject(this, view);
        mScoreLayout.setOnClickListener(this);
        PackageInfo packageInfo = null;
        try
        {
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        if (packageInfo != null)
        {
            mVersionCode.setText("V"+packageInfo.versionName+"."+packageInfo.versionCode);
        }
        mLogoutLayout.setOnClickListener(this);
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
            case R.id.settings_score:
                showAppOnMarket();
                break;
            case R.id.settings_logout:
                ActivityHelper.launchAuthentication(getActivity());
                THUser.clearCurrentUser();
                break;
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

    public void showAppOnMarket()
    {
        try
        {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "market://details?id=" + getActivity().getPackageName())));
                    //"market://details?id=" + PLAYSTORE_APP_ID)));
        }
        catch (ActivityNotFoundException ex)
        {
            try
            {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id="
                                + getActivity().getPackageName())));
                                //+ PLAYSTORE_APP_ID)));
            }
            catch (Exception e)
            {
                Timber.e(e, "Cannot send to Google Play store");
                //alertDialogUtil.popWithNegativeButton(
                //        activity,
                //        R.string.webview_error_no_browser_for_intent_title,
                //        R.string.webview_error_no_browser_for_intent_description,
                //        R.string.cancel);
            }
        }
    }
}

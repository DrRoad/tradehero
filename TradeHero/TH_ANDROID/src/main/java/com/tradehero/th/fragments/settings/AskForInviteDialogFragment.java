package com.tradehero.th.fragments.settings;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.OnClick;
import com.tradehero.common.persistence.prefs.IntPreference;
import com.tradehero.th.R;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.base.BaseDialogFragment;
import com.tradehero.th.fragments.social.friend.FriendsInvitationFragment;
import com.tradehero.th.persistence.prefs.ShowAskForInviteDialog;
import com.tradehero.th.persistence.prefs.ShowAskForInviteDialogCloseTimes;
import com.tradehero.th.persistence.timing.TimingIntervalPreference;
import dagger.Lazy;
import javax.inject.Inject;

public class AskForInviteDialogFragment extends BaseDialogFragment
{
    @Inject Lazy<DashboardNavigator> navigator;
    @Inject @ShowAskForInviteDialog TimingIntervalPreference mShowAskForInviteDialogPreference;
    @Inject @ShowAskForInviteDialogCloseTimes IntPreference mShowAskForInviteDialogCloseTimesPreference;

    public static AskForInviteDialogFragment showInviteDialog(FragmentManager fragmentManager)
    {
        AskForInviteDialogFragment dialogFragment = new AskForInviteDialogFragment();
        dialogFragment.show(fragmentManager, AskForInviteDialogFragment.class.getName());
        return dialogFragment;
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mShowAskForInviteDialogPreference.addInFuture(TimingIntervalPreference.MINUTE);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.ask_for_invite_dialog_layout, container, false);
    }

    @OnClick(R.id.btn_cancel)
    public void onCancel()
    {
        dismiss();
        mShowAskForInviteDialogPreference.pushInFuture(TimingIntervalPreference.WEEK * mShowAskForInviteDialogCloseTimesPreference.get());
        mShowAskForInviteDialogCloseTimesPreference.set(mShowAskForInviteDialogCloseTimesPreference.get()+1);

    }

    @OnClick(R.id.btn_invite)
    public void onInvite()
    {
        pushInvitationFragment();
        dismiss();
        mShowAskForInviteDialogPreference.pushInFuture(TimingIntervalPreference.YEAR);
    }

    private void pushInvitationFragment()
    {
        navigator.get().pushFragment(FriendsInvitationFragment.class);
    }

}

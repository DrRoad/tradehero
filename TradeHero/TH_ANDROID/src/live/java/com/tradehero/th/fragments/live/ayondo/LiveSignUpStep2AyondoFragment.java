package com.tradehero.th.fragments.live.ayondo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th.R;
import com.tradehero.th.fragments.live.LiveSignUpStepBaseFragment;

public class LiveSignUpStep2AyondoFragment extends LiveSignUpStepBaseFragment
{
    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_sign_up_live_ayondo_step_2, container, false);
    }
}
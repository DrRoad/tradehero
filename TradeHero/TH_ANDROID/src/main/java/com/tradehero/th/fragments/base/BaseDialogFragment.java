package com.tradehero.th.fragments.base;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import butterknife.ButterKnife;
import com.tradehero.th.R;
import com.tradehero.th.inject.HierarchyInjector;

public abstract class BaseDialogFragment extends DialogFragment
{
    private OnDismissedListener dismissedListener;

    @Override public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog d = super.onCreateDialog(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TH_Dialog);
        return d;
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        HierarchyInjector.inject(this);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public void setDismissedListener(OnDismissedListener dismissedListener)
    {
        this.dismissedListener = dismissedListener;
    }

    @Override public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        notifyDismissed(dialog);
    }

    protected void notifyDismissed(DialogInterface dialog)
    {
        OnDismissedListener dismissedListenerCopy = dismissedListener;
        if (dismissedListenerCopy != null)
        {
            dismissedListenerCopy.onDismissed(dialog);
        }
        dismissedListener = null;
    }

    public static interface OnDismissedListener
    {
        void onDismissed(DialogInterface dialog);
    }
}

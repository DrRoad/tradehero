package com.tradehero.th.fragments.base;

import android.app.Activity;
import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.th.utils.DaggerUtils;

/** Created with IntelliJ IDEA. User: tho Date: 9/27/13 Time: 5:14 PM Copyright (c) TradeHero */
public class BaseFragment extends SherlockFragment
{
    public static final String BUNDLE_KEY_HAS_OPTION_MENU = BaseFragment.class.getName() + ".hasOptionMenu";
    public static final String BUNDLE_KEY_IS_OPTION_MENU_VISIBLE = BaseFragment.class.getName() + ".isOptionMenuVisible";

    protected boolean hasOptionMenu = true;
    protected boolean isOptionMenuVisible = true;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
        {
            hasOptionMenu = args.getBoolean(BUNDLE_KEY_HAS_OPTION_MENU, hasOptionMenu);
            isOptionMenuVisible = args.getBoolean(BUNDLE_KEY_IS_OPTION_MENU_VISIBLE, isOptionMenuVisible);
        }
        setHasOptionsMenu(hasOptionMenu);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        if (isOptionMenuVisible)
        {
            actionBar.show();
        }
        else
        {
            actionBar.hide();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        DaggerUtils.inject(this);
    }

    public static interface TabBarVisibilityInformer
    {
        boolean isTabBarVisible();
    }
}

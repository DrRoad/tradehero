package com.tradehero.th.fragments.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.special.ResideMenu.ResideMenu;
import com.tradehero.th.activities.ActivityHelper;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.tutorial.WithTutorial;
import com.tradehero.th.utils.AlertDialogUtil;
import com.tradehero.th2.R;
import dagger.Lazy;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

abstract public class DashboardFragment extends BaseFragment
{
    private static final String BUNDLE_KEY_TITLE = DashboardFragment.class.getName() + ".title";
    private static final String BUNDLE_KEY_SHOW_HOME_AS_UP = DashboardFragment.class.getName() + ".show_home_as_up";
    public static final String BUNDLE_OPEN_CLASS_NAME = DashboardFragment.class.getName() + ".oepn_class_name";
    private static final boolean DEFAULT_SHOW_HOME_AS_UP = true;

    @Inject protected AlertDialogUtil alertDialogUtil;
    @Inject Lazy<ResideMenu> resideMenuLazy;

    private RelativeLayout rlCustomHeadView;
    private TextView tvHeadLeft;
    private TextView tvHeadRight0;
    private TextView tvHeadRight1;
    private TextView tvHeadMiddleMain;
    private TextView tvHeadMiddleSub;

    public void updateHeadView(boolean display)
    {
        if (rlCustomHeadView != null)
        {
            rlCustomHeadView.setVisibility(display ? View.VISIBLE : View.GONE);
        }
    }


    public void onClickHeadLeft()
    {
        popCurrentFragment();
    }


    public void onClickHeadRight0()
    {

    }


    public void onClickHeadRight1()
    {

    }

    public void setHeadViewLeft(String leftText)
    {
        if (tvHeadLeft != null)
        {
            tvHeadLeft.setVisibility(View.VISIBLE);
            tvHeadLeft.setText(leftText);
        }
    }

    public void setHeadViewMiddleMain(String middleMainText)
    {
        if (tvHeadMiddleMain != null)
        {
            tvHeadMiddleMain.setVisibility(View.VISIBLE);
            tvHeadMiddleMain.setText(middleMainText);
        }
    }

    public void setHeadViewMiddleMain(int middleMainText)
    {
        if (tvHeadMiddleMain != null)
        {
            tvHeadMiddleMain.setVisibility(View.VISIBLE);
            tvHeadMiddleMain.setText(middleMainText);
        }
    }

    public void setHeadViewMiddleSub(String middleSubText)
    {
        if (tvHeadMiddleSub != null)
        {
            tvHeadMiddleSub.setVisibility(View.VISIBLE);
            tvHeadMiddleSub.setText(middleSubText);
        }
    }

    public void setHeadViewRight0(String right0)
    {
        if (tvHeadRight0 != null)
        {
            tvHeadRight0.setVisibility(View.VISIBLE);
            tvHeadRight0.setText(right0);
        }
    }

    public void setHeadViewRight0(int right0)
    {
        if (tvHeadRight0 != null)
        {
            tvHeadRight0.setVisibility(View.VISIBLE);
            tvHeadRight0.setText(right0);
        }
    }

    public void setHeadViewRight0Drawable(Drawable left, Drawable top, Drawable right, Drawable bottom)
    {
        if (tvHeadRight0 != null)
        {
            if(right!=null){right.setBounds(0, 0, right.getMinimumWidth(), right.getMinimumHeight());}
            if(left!=null){left.setBounds(0, 0, left.getMinimumWidth(), left.getMinimumHeight());}
            if(top!=null){top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());}
            if(bottom!=null){bottom.setBounds(0, 0, bottom.getMinimumWidth(), bottom.getMinimumHeight());}
            tvHeadRight0.setCompoundDrawables(left, top, right, bottom);
        }
    }

    public void setHeadViewRight1(String right1)
    {
        if (tvHeadRight1 != null)
        {
            tvHeadRight1.setVisibility(View.VISIBLE);
            tvHeadRight1.setText(right1);
        }
    }

    public static void putKeyShowHomeAsUp(@NotNull Bundle args, boolean showAsUp)
    {
        args.putBoolean(BUNDLE_KEY_SHOW_HOME_AS_UP, showAsUp);
    }

    protected static boolean getKeyShowHomeAsUp(@Nullable Bundle args)
    {
        if (args == null)
        {
            return DEFAULT_SHOW_HOME_AS_UP;
        }
        return args.getBoolean(BUNDLE_KEY_SHOW_HOME_AS_UP, DEFAULT_SHOW_HOME_AS_UP);
    }

    public static void putActionBarTitle(Bundle args, String title)
    {
        if (args != null)
        {
            args.putString(BUNDLE_KEY_TITLE, title);
        }
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!(getActivity() instanceof DashboardNavigatorActivity))
        {
            throw new IllegalArgumentException("DashboardActivity needs to implement DashboardNavigator");
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        if (this instanceof WithTutorial)
        {
            inflater.inflate(R.menu.menu_with_tutorial, menu);
        }

        Bundle argument = getArguments();

        if (argument != null && argument.containsKey(BUNDLE_KEY_TITLE))
        {
            String title = argument.getString(BUNDLE_KEY_TITLE);

            if (title != null && !title.isEmpty())
            {
                setActionBarTitle(title);
            }
        }

        ActionBar actionBar = getSupportActionBar();

        actionBar.setCustomView(R.layout.custom_head_layout);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //actionBar.getCustomView();

        if (actionBar != null)
        {
            //if (shouldShowHomeAsUp())
            //{
            //    actionBar.setDisplayOptions(
            //            ActionBar.DISPLAY_HOME_AS_UP
            //            | ActionBar.DISPLAY_SHOW_TITLE
            //            | ActionBar.DISPLAY_SHOW_HOME
            //    );
            //    actionBar.setLogo(R.drawable.icon_return_xml);
            //}
            //else
            //{
            //    actionBar.setDisplayOptions(
            //            ActionBar.DISPLAY_SHOW_TITLE
            //                    | ActionBar.DISPLAY_SHOW_HOME
            //                    | ActionBar.DISPLAY_USE_LOGO
            //    );
            //    actionBar.setLogo(R.drawable.icon_return_xml);
            //}
            //actionBar.setHomeButtonEnabled(true);

            initHeadViewCustomLayout();
        }
    }

    public void initHeadViewCustomLayout()
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            View view = actionBar.getCustomView();

            rlCustomHeadView = (RelativeLayout) view.findViewById(R.id.rlCustomHeadView);
            tvHeadLeft = (TextView) view.findViewById(R.id.tvHeadLeft);
            tvHeadRight0 = (TextView) view.findViewById(R.id.tvHeadRight0);
            tvHeadRight1 = (TextView) view.findViewById(R.id.tvHeadRight1);
            tvHeadMiddleMain = (TextView) view.findViewById(R.id.tvHeadMiddleMain);
            tvHeadMiddleSub = (TextView) view.findViewById(R.id.tvHeadMiddleSub);

            tvHeadLeft.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    onClickHeadLeft();
                }
            });

            tvHeadRight0.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    onClickHeadRight0();
                }
            });

            tvHeadRight1.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    onClickHeadRight1();
                }
            });

        }
    }

    public void popCurrentFragment()
    {
        DashboardNavigator navigator = getDashboardNavigator();
        if (navigator != null)
        {
            navigator.popFragment();
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                //if (shouldShowHomeAsUp())
                //{
                DashboardNavigator navigator = getDashboardNavigator();
                if (navigator != null)
                {
                    navigator.popFragment();
                }
                //}
                //else
                //{
                //    resideMenuLazy.get().openMenu();
                //}
                return true;

            case R.id.menu_info:
                handleInfoMenuItemClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void handleInfoMenuItemClicked()
    {
        if (this instanceof WithTutorial)
        {
            alertDialogUtil.popTutorialContent(getActivity(), ((WithTutorial) this).getTutorialLayout());
        }
        else
        {
            Timber.d("%s is not implementing WithTutorial interface, but has info menu", getClass().getName());
        }
    }

    protected boolean shouldShowHomeAsUp()
    {
        return getKeyShowHomeAsUp(getArguments());
    }

    @Nullable protected DashboardNavigator getDashboardNavigator()
    {
        @Nullable DashboardNavigatorActivity activity = ((DashboardNavigatorActivity) getActivity());
        if (activity != null)
        {
            return activity.getDashboardNavigator();
        }
        return null;
    }

    public <T extends Fragment> boolean allowNavigateTo(@NotNull Class<T> fragmentClass, Bundle args)
    {
        return true;
    }

    public void gotoDashboard(String strFragment)
    {
        Bundle args = new Bundle();
        args.putString(DashboardFragment.BUNDLE_OPEN_CLASS_NAME, strFragment);
        ActivityHelper.launchDashboard(this.getActivity(), args);
    }

    public void gotoDashboard(String strFragment, Bundle bundle)
    {
        Bundle args = new Bundle();
        args.putString(DashboardFragment.BUNDLE_OPEN_CLASS_NAME, strFragment);
        args.putAll(bundle);
        ActivityHelper.launchDashboard(this.getActivity(), args);
    }

    public void gotoDashboard(Class Fragment, Bundle bundle)
    {
        Bundle args = new Bundle();
        args.putString(DashboardFragment.BUNDLE_OPEN_CLASS_NAME, Fragment.getName());
        args.putAll(bundle);
        ActivityHelper.launchDashboard(this.getActivity(), args);
    }

    public void pushFragment(@NotNull Class fragmentClass, Bundle args)
    {
        getDashboardNavigator().pushFragment(fragmentClass, args);
    }

    public void setRight0ButtonOnClickListener(View.OnClickListener listener)
    {
        tvHeadRight0.setOnClickListener(listener);
    }

    public void setLeftButtonOnClickListener(View.OnClickListener listener)
    {
        tvHeadLeft.setOnClickListener(listener);
    }
}

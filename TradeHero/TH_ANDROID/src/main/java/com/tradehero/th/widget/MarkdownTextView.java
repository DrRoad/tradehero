package com.tradehero.th.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.tradehero.common.text.OnElementClickListener;
import com.tradehero.common.text.RichTextCreator;
import com.tradehero.th.activities.ActivityHelper;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.chinabuild.fragment.competition.CompetitionDetailFragment;
import com.tradehero.th.fragments.chinabuild.fragment.security.SecurityDetailFragment;
import com.tradehero.th.fragments.chinabuild.fragment.userCenter.UserMainPage;
import com.tradehero.th.models.intent.THIntentFactory;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.route.THRouter;
import timber.log.Timber;

import javax.inject.Inject;

public class MarkdownTextView extends TextView implements OnElementClickListener
{
    @Inject THIntentFactory thIntentFactory;
    @Inject RichTextCreator parser;
    @Inject CurrentUserId currentUserId;
    @Inject THRouter thRouter;

    //<editor-fold desc="Constructors">
    public MarkdownTextView(Context context)
    {
        super(context);
    }

    public MarkdownTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MarkdownTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        DaggerUtils.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        setMovementMethod(LinkMovementMethod.getInstance());//是用来增加Text监听
    }

    @Override protected void onDetachedFromWindow()
    {
        setMovementMethod(null);//删除Text的超链监听
        super.onDetachedFromWindow();
    }

    @Override public void setText(CharSequence text, BufferType type)
    {
        if (parser != null && text != null)
        {
            //if(text.toString().startsWith("*") && text.toString().endsWith("*"))
            //{
            //    text = text.subSequence(1,text.length()-1);
            //}
            text = text.toString().replace("*", "");
            //text = text.toString().replace("\n", "");
            text = parser.load(text.toString().trim()).create();
        }
        super.setText(text, BufferType.SPANNABLE);
    }

    public boolean isClicked = false;

    @Override public void onClick(View textView, String data, String key, String[] matchStrings)
    {
        switch (key)
        {
            case "competition":
                Timber.d("");
                int competitionId = Integer.parseInt(matchStrings[2]);
                openCompetition(competitionId);
                isClicked = true;
                return;
            case "user":
                int userId = Integer.parseInt(matchStrings[2]);
                openUserProfile(userId);
                isClicked = true;
                return;
            case "security":
                if (matchStrings.length < 3) break;
                String exchange = matchStrings[1];
                String symbol = matchStrings[2];
                openSecurityProfile(exchange, symbol);
                isClicked = true;
                return;
            case "link":
                isClicked = true;
                String USER = "tradehero://user/";
                if (matchStrings.length < 3) break;
                String link = matchStrings[1];
                String link2 = matchStrings[2];
                //"$NASDAQ:GOOG"
                if (link != null && link.startsWith("$"))
                {
                    String str[] = link.substring(1).split(":");
                    if (str.length == 2)
                    {
                        openSecurityProfile(str[0], str[1]);
                    }
                }
                //"tradehero://user/99106"
                else if (link2 != null && link2.startsWith(USER))
                {
                    int uid = Integer.parseInt(link2.substring(USER.length()));
                    openUserProfile(uid);
                }
                return;
        }
    }

    private DashboardNavigator getNavigator()
    {
        return ((DashboardNavigatorActivity) getContext()).getDashboardNavigator();
    }

    private void openSecurityProfile(String exchange, String symbol)
    {
        Timber.d("openSecurity " + exchange + " : " + symbol);
        Bundle bundle = new Bundle();
        SecurityId securityId = new SecurityId(exchange, symbol);
        bundle.putBundle(SecurityDetailFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
        bundle.putString(SecurityDetailFragment.BUNDLE_KEY_SECURITY_NAME, securityId.getDisplayName());
        enterFragment(SecurityDetailFragment.class, bundle);
    }

    private void openCompetition(int competitionId)
    {
        Timber.d("openCompetition : " + competitionId);
        if (competitionId >= 0)
        {
            Bundle bundle = new Bundle();
            bundle.putInt(CompetitionDetailFragment.BUNDLE_COMPETITION_ID, competitionId);
            enterFragment(CompetitionDetailFragment.class, bundle);
        }
    }

    private void openUserProfile(int userId)
    {
        Timber.d("openUserProfile : " + userId);
        if (userId >= 0)
        {
            Bundle bundle = new Bundle();
            bundle.putInt(UserMainPage.BUNDLE_USER_BASE_KEY, userId);
            //getNavigator().pushFragment(UserMainPage.class, bundle);
            enterFragment(UserMainPage.class, bundle);
        }
    }

    private void enterFragment(Class fragmentClass, Bundle args)
    {
        if (getNavigator() != null)
        {
            getNavigator().pushFragment(fragmentClass, args);
        }
        else
        {
            gotoDashboard(fragmentClass.getName(), args);
        }
    }

    public void gotoDashboard(String strFragment, Bundle bundle)
    {
        bundle.putString(DashboardFragment.BUNDLE_OPEN_CLASS_NAME, strFragment);
        ActivityHelper.launchDashboard((Activity) getContext(), bundle);
    }
}

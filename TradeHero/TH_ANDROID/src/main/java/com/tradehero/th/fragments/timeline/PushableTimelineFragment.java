package com.tradehero.th.fragments.timeline;

import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.route.Routable;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.social.hero.HeroAlertDialogUtil;
import com.tradehero.th.utils.metrics.Analytics;
import javax.inject.Inject;

/**
 * This fragment will not be the main, but one that is pushed from elsewhere
 */
@Routable({
        "user/:userId"
})
public class PushableTimelineFragment extends TimelineFragment
{
    @Inject HeroAlertDialogUtil heroAlertDialogUtil;
    @Inject Analytics analytics;

    @Override protected void initViews(View view)
    {
        super.initViews(view);
        mIsOtherProfile = true;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.timeline_menu_pushable_other, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onPrepareOptionsMenu(Menu menu)
    {
        Boolean isFollowing = isPurchaserFollowingUserShown();
        updateBottomButton();

        //MenuItem settingsButton = menu.findItem(R.id.menu_settings);
        //if (settingsButton != null)
        //{
        //    settingsButton.setVisible(false);
        //}

        super.onPrepareOptionsMenu(menu);
    }

    @Override protected void linkWith(UserProfileDTO userProfileDTO, boolean andDisplay)
    {
        super.linkWith(userProfileDTO, andDisplay);
        if (andDisplay)
        {
            displayActionBarTitle();
            //displayFollowButton();
        }
    }

    /**
     * Null means unsure.
     */
    protected Boolean isPurchaserFollowingUserShown()
    {
        if (userInteractor != null)
        {
            OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId();
            if (applicablePortfolioId != null)
            {
                UserBaseKey purchaserKey = applicablePortfolioId.getUserBaseKey();
                if (purchaserKey != null)
                {
                    UserProfileDTO purchaserProfile = userProfileCache.get().get(purchaserKey);
                    if (purchaserProfile != null)
                    {
                        return purchaserProfile.isFollowingUser(shownUserBaseKey);
                    }
                }
            }
        }
        return null;
    }

    private void handleInfoButtonPressed()
    {
        //TODO hacked by alipay alex
        //heroAlertDialogUtil.popAlertFollowHero(getActivity(), new DialogInterface.OnClickListener()
        //{
        //    @Override public void onClick(DialogInterface dialog, int which)
        //    {
                premiumFollowUser(shownUserBaseKey);
            //}
        //});
    }
}

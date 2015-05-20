package com.tradehero.th.fragments.timeline;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;
import com.tradehero.route.Routable;
import com.tradehero.th.api.users.UserProfileDTO;
import javax.inject.Inject;

/**
 * This fragment will not be the main, but one that is pushed from elsewhere
 */
@Routable({
        "user/:userId"
})
public class PushableTimelineFragment extends TimelineFragment
{
    @SuppressWarnings("UnusedDeclaration") @Inject Context doNotRemoveOrItFails;

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mIsOtherProfile = true;
    }

    @Override public void onPrepareOptionsMenu(Menu menu)
    {
        Boolean isFollowing = isPurchaserFollowingUserShown();
        updateBottomButton();
        super.onPrepareOptionsMenu(menu);
    }

    @Override protected void linkWith(@NonNull UserProfileDTO userProfileDTO)
    {
        super.linkWith(userProfileDTO);
        displayActionBarTitle();
    }

    /**
     * Null means unsure.
     */
    @Nullable protected Boolean isPurchaserFollowingUserShown()
    {
//        OwnedPortfolioId applicablePortfolioId = getApplicablePortfolioId();
//        if (applicablePortfolioId != null)
//        {
            UserProfileDTO purchaserProfile = userProfileCache.get().getCachedValue(currentUserId.toUserBaseKey());
            if (purchaserProfile != null)
            {
                return purchaserProfile.isFollowingUser(shownUserBaseKey);
            }
//        }
        return null;
    }

    @Override public <T extends Fragment> boolean allowNavigateTo(@NonNull Class<T> fragmentClass, Bundle args)
    {
        return !(
                ((Object) this).getClass().isAssignableFrom(fragmentClass) &&
                    shownUserBaseKey != null &&
                    shownUserBaseKey.equals(getUserBaseKey(args)))
                && super.allowNavigateTo(fragmentClass, args);
    }
}

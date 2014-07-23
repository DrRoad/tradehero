package com.tradehero.th.fragments.leaderboard;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefKey;
import com.tradehero.th.models.leaderboard.key.LeaderboardDefKeyKnowledge;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerFragment;
import com.tradehero.th.fragments.social.hero.HeroManagerFragment;
import com.tradehero.th.persistence.user.UserProfileCache;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

abstract public class BaseLeaderboardFragment extends BasePurchaseManagerFragment
{
    private static final String BUNDLE_KEY_LEADERBOARD_ID = BaseLeaderboardFragment.class.getName() + ".leaderboardId";
    public static final String BUNDLE_KEY_LEADERBOARD_DEF_TITLE = BaseLeaderboardFragment.class.getName() + ".leaderboardDefTitle";
    public static final String BUNDLE_KEY_LEADERBOARD_DEF_DESC = BaseLeaderboardFragment.class.getName() + ".leaderboardDefDesc";

    @Inject LeaderboardSortHelper leaderboardSortHelper;
    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCache userProfileCache;

    protected LeaderboardDefKey leaderboardDefKey;
    protected UserProfileDTO currentUserProfileDTO;
    protected DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> userProfileCacheListener;

    public static void putLeaderboardDefKey(@NotNull Bundle args, @NotNull LeaderboardDefKey leaderboardDefKey)
    {
        args.putInt(BUNDLE_KEY_LEADERBOARD_ID, leaderboardDefKey.key);
    }

    public static LeaderboardDefKey getLeadboardDefKey(@NotNull Bundle args)
    {
        return new LeaderboardDefKey(args.getInt(BUNDLE_KEY_LEADERBOARD_ID));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        leaderboardDefKey = getLeadboardDefKey(getArguments());
        this.userProfileCacheListener = createUserProfileListener();
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(getMenuResource(), menu);
        super.onCreateOptionsMenu(menu, inflater);


        Bundle args = getArguments();
        if (args != null)
        {
            String title = args.getString(BUNDLE_KEY_LEADERBOARD_DEF_TITLE);
            setActionBarTitle(title == null ? "" : title);
        }
    }
    //</editor-fold>

    @Override public void onResume()
    {
        super.onResume();
        fetchCurrentUserProfile();
    }

    @Override public void onStop()
    {
        detachUserProfileCache();
        super.onStop();
    }

    @Override public void onDestroy()
    {
        this.userProfileCacheListener = null;
        super.onDestroy();
    }

    protected void detachUserProfileCache()
    {
        if (userProfileCacheListener != null)
        {
            userProfileCache.unregister(userProfileCacheListener);
        }
    }

    protected void fetchCurrentUserProfile()
    {
        detachUserProfileCache();
        userProfileCache.register(currentUserId.toUserBaseKey(), userProfileCacheListener);
        userProfileCache.getOrFetchAsync(currentUserId.toUserBaseKey());
    }

    protected int getMenuResource()
    {
        return R.menu.leaderboard_menu;
    }

    protected void setCurrentUserProfileDTO(UserProfileDTO currentUserProfileDTO)
    {
        this.currentUserProfileDTO = currentUserProfileDTO;
    }

    protected void pushLeaderboardListViewFragment(@NotNull LeaderboardDefDTO dto)
    {
        Bundle bundle = new Bundle(getArguments());
        bundle.putString(BUNDLE_KEY_LEADERBOARD_DEF_TITLE, dto.name);
        bundle.putString(BUNDLE_KEY_LEADERBOARD_DEF_DESC, dto.desc);

        switch (dto.id)
        {
            case LeaderboardDefKeyKnowledge.FRIEND_ID:
                pushFriendsFragment(dto);
                break;
            case LeaderboardDefKeyKnowledge.HERO_ID:
                pushHeroFragment();
                break;
            case LeaderboardDefKeyKnowledge.FOLLOWER_ID:
                pushFollowerFragment();
                break;
            default:
                Timber.d("LeaderboardMarkUserListFragment %s",bundle);
                LeaderboardMarkUserListFragment.putLeaderboardDefKey(bundle, dto.getLeaderboardDefKey());
                getDashboardNavigator().pushFragment(LeaderboardMarkUserListFragment.class, bundle);
                break;
        }
    }

    protected void pushFriendsFragment(LeaderboardDefDTO dto)
    {
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_LEADERBOARD_DEF_TITLE, dto.name);
        args.putString(BUNDLE_KEY_LEADERBOARD_DEF_DESC, dto.desc);

        FriendLeaderboardMarkUserListFragment.putLeaderboardDefKey(args, dto.getLeaderboardDefKey());

        getDashboardNavigator().pushFragment(FriendLeaderboardMarkUserListFragment.class, args);
    }

    protected void pushHeroFragment()
    {
        Bundle bundle = new Bundle();
        HeroManagerFragment.putFollowerId(bundle, currentUserId.toUserBaseKey());
        OwnedPortfolioId applicablePortfolio = getApplicablePortfolioId();
        if (applicablePortfolio != null)
        {
            HeroManagerFragment.putApplicablePortfolioId(bundle, applicablePortfolio);
        }
        getDashboardNavigator().pushFragment(HeroManagerFragment.class, bundle);
    }

    protected void pushFollowerFragment()
    {
        Bundle bundle = new Bundle();
        FollowerManagerFragment.putHeroId(bundle, currentUserId.toUserBaseKey());
        OwnedPortfolioId applicablePortfolio = getApplicablePortfolioId();
        if (applicablePortfolio != null)
        {
            //FollowerManagerFragment.putApplicablePortfolioId(bundle, applicablePortfolio);
        }
        getDashboardNavigator().pushFragment(FollowerManagerFragment.class, bundle);
    }

    protected DTOCacheNew.Listener<UserBaseKey, UserProfileDTO> createUserProfileListener()
    {
        return new BaseLeaderboardFragmentProfileCacheListener();
    }

    protected class BaseLeaderboardFragmentProfileCacheListener implements DTOCacheNew.Listener<UserBaseKey, UserProfileDTO>
    {
        public BaseLeaderboardFragmentProfileCacheListener()
        {
            super();
        }

        @Override public void onDTOReceived(@NotNull UserBaseKey key, @NotNull UserProfileDTO value)
        {
            setCurrentUserProfileDTO(value);
        }

        @Override public void onErrorThrown(@NotNull UserBaseKey key, @NotNull Throwable error)
        {
            Timber.e("Failed to download current UserProfile", error);
            THToast.show(R.string.error_fetch_your_user_profile);
        }
    }
}

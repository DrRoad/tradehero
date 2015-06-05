package com.tradehero.th.fragments.leaderboard;

import com.tradehero.th.fragments.leaderboard.filter.LeaderboardFilterFragment;
import com.tradehero.th.fragments.leaderboard.filter.LeaderboardFilterSliderContainer;
import com.tradehero.th.fragments.leaderboard.main.LeaderboardCommunityFragment;
import dagger.Module;

@Module(
        injects = {
                LeaderboardFriendsItemView.class,
                LeaderboardMarkUserListFragment.class,
                LeaderboardMarkUserItemView.class,
                CompetitionLeaderboardMarkUserItemView.class,
                CompetitionLeaderboardMarkUserOwnRankingView.class,
                LeaderboardMarkUserOwnRankingView.class,
                FriendLeaderboardMarkUserListFragment.class,
                CompetitionLeaderboardMarkUserListFragment.class,
                LeaderboardCommunityFragment.class,
                LeaderboardFilterFragment.class,
                LeaderboardFilterSliderContainer.class,
        },
        library = true,
        complete = false
)
public class FragmentLeaderboardModule
{
}
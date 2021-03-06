package com.tradehero.th.fragments.competition;

import com.tradehero.th.fragments.competition.zone.CompetitionZoneLeaderboardListItemView;
import com.tradehero.th.fragments.competition.zone.CompetitionZoneListItemView;
import com.tradehero.th.fragments.competition.zone.CompetitionZonePortfolioView;
import dagger.Module;

@Module(
        injects = {
                CompetitionWebViewFragment.class,
                CompetitionEnrollmentWebViewFragment.class,
                CompetitionZoneListItemAdapter.class,
                CompetitionZoneListItemView.class,
                CompetitionZoneLeaderboardListItemView.class,
                CompetitionZonePortfolioView.class,
                MainCompetitionFragment.class,
                ProviderVideoListFragment.class,
                ProviderVideoListItemView.class,
                ProviderSecurityListFragment.class,
                AdView.class,
        },
        library = true,
        complete = false
)
public class FragmentCompetitionModule
{
}

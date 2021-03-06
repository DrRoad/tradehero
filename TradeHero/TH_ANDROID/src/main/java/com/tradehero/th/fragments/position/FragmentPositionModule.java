package com.tradehero.th.fragments.position;

import com.tradehero.th.fragments.position.partial.PositionPartialBottomClosedView;
import com.tradehero.th.fragments.position.partial.PositionPartialBottomInPeriodViewHolder;
import com.tradehero.th.fragments.position.partial.PositionPartialBottomOpenView;
import com.tradehero.th.fragments.position.partial.PositionPartialTopView;
import com.tradehero.th.fragments.position.view.PositionLockedView;
import dagger.Module;

/**
 * Created by tho on 9/9/2014.
 */
@Module(
        injects = {

                PositionListFragment.class,
                LeaderboardPositionListFragment.class,
                CompetitionLeaderboardPositionListFragment.class,

                PositionPartialTopView.class,
                PositionPartialBottomClosedView.class,
                PositionPartialBottomOpenView.class,
                PositionLockedView.class,
                PositionPartialBottomInPeriodViewHolder.class,
        },
        library = true,
        complete = false
)
public class FragmentPositionModule
{
}

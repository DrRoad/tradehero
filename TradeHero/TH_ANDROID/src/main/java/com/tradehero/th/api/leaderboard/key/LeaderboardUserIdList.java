package com.tradehero.th.api.leaderboard.key;

import com.tradehero.common.api.BaseArrayList;
import com.tradehero.common.persistence.DTO;
import com.tradehero.th.api.leaderboard.LeaderboardUserDTO;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaderboardUserIdList extends BaseArrayList<LeaderboardUserId>
    implements DTO
{
    //<editor-fold desc="Constructors">
    public LeaderboardUserIdList()
    {
        super();
    }

    public LeaderboardUserIdList(
            @NotNull Collection<? extends LeaderboardUserId> c,
            @Nullable LeaderboardUserId typeQualifier)
    {
        super(c);
    }

    public LeaderboardUserIdList(
            @NotNull Collection<? extends LeaderboardUserDTO> c,
            @Nullable LeaderboardUserDTO typeQualifier)
    {
        super();
        for (LeaderboardUserDTO leaderboardUserDTO : c)
        {
            add(leaderboardUserDTO.getLeaderboardUserId());
        }
    }
    //</editor-fold>
}

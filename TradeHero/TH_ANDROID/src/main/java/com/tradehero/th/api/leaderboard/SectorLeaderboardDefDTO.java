package com.tradehero.th.api.leaderboard;

import android.content.Context;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.def.DrillDownLeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;

public class SectorLeaderboardDefDTO extends DrillDownLeaderboardDefDTO
{
    //<editor-fold desc="Constructors">
    public SectorLeaderboardDefDTO(Context context)
    {
        super();
        id = LeaderboardDefDTO.LEADERBOARD_DEF_SECTOR_ID;
        name = context.getString(R.string.leaderboard_community_by_sector);
    }
    //</editor-fold>
}

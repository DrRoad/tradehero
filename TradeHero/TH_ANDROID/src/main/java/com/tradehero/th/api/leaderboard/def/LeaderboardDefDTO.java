package com.tradehero.th.api.leaderboard.def;

import android.support.annotation.NonNull;
import com.tradehero.common.persistence.DTO;
import com.tradehero.th.api.leaderboard.CountryCodeList;
import com.tradehero.th.api.leaderboard.LeaderboardSortTypeDTOList;
import com.tradehero.th.api.leaderboard.key.ExchangeLeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefKey;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.MostSkilledLeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.SectorLeaderboardDefListKey;
import com.tradehero.th.api.leaderboard.key.TimePeriodLeaderboardDefListKey;
import com.tradehero.th.models.leaderboard.key.LeaderboardDefKeyKnowledge;
import java.util.Date;

public class LeaderboardDefDTO implements DTO
{
    public int id;
    public String name;

    // LB with no restrictions here is global king of kings LB
    public boolean sectorRestrictions;
    public boolean exchangeRestrictions;
    public Date fromUtcRestricted;
    public Date toUtcRestricted;
    public Integer toDateDays;

    // count of # of users in most recent LB mark; zero here means client should not display the empty LB
    public int countLeaderboardEntries;

    // description String
    public String desc;

    // sort & cap fields
    public LeaderboardSortTypeDTOList sortTypes;
    public Integer defaultSortTypeId;
    public Integer capAt;
    public CountryCodeList countryCodes;

    //<editor-fold desc="Constructors">
    public LeaderboardDefDTO()
    {
        super();
    }
    //</editor-fold>

    @NonNull public LeaderboardDefKey getLeaderboardDefKey()
    {
        return new LeaderboardDefKey(id);
    }

    @NonNull public LeaderboardDefListKey getLeaderboardDefListKey()
    {
        if (exchangeRestrictions)
        {
            return new ExchangeLeaderboardDefListKey(1);
        }
        if (sectorRestrictions)
        {
            return new SectorLeaderboardDefListKey(1);
        }
        if (isTimeRestrictedLeaderboard())
        {
            return new TimePeriodLeaderboardDefListKey(1);
        }
        if (id == LeaderboardDefKeyKnowledge.MOST_SKILLED_ID)
        {
            return new MostSkilledLeaderboardDefListKey(1);
        }
        throw new IllegalStateException("Unhandled situation " + this);
    }

    public boolean isTimeRestrictedLeaderboard()
    {
        return (this.fromUtcRestricted != null && this.toUtcRestricted != null) || (this.toDateDays != null && this.toDateDays > 0);
    }

    public long getTimeRestrictionRangeInMillis()
    {
        if(toUtcRestricted == null || fromUtcRestricted == null)
        {
            return 0;
        }
        return toUtcRestricted.getTime() - fromUtcRestricted.getTime();
    }

    public boolean isSectorRestricted()
    {
        return sectorRestrictions;
    }

    public boolean isExchangeRestricted()
    {
        return exchangeRestrictions;
    }

    @NonNull public Boolean isWithinUtcRestricted()
    {
        return isWithinUtcRestricted(new Date());
    }

    @NonNull public Boolean isWithinUtcRestricted(@NonNull Date now)
    {
        return (fromUtcRestricted == null || now.equals(fromUtcRestricted) || now.after(fromUtcRestricted)) &&
                (toUtcRestricted == null || now.equals(toUtcRestricted) || now.before(toUtcRestricted));
    }
}


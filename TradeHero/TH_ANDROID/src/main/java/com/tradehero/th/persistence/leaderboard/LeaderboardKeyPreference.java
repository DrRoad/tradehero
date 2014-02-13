package com.tradehero.th.persistence.leaderboard;

import android.content.SharedPreferences;
import com.tradehero.common.persistence.prefs.StringSetPreference;
import com.tradehero.th.api.leaderboard.key.LeaderboardKey;
import java.util.Set;
import javax.inject.Inject;

/**
 * Created by xavier on 2/13/14.
 */
public class LeaderboardKeyPreference extends StringSetPreference
{
    public static final String TAG = LeaderboardKeyPreference.class.getSimpleName();

    @Inject public LeaderboardKeyPreference(SharedPreferences preference, String key, Set<String> defaultValue)
    {
        super(preference, key, defaultValue);
    }

    public LeaderboardKey getLeaderboardKey()
    {
        Set<String> set = get();
        if (set == null)
        {
            return null;
        }
        return new LeaderboardKey(set);
    }

    public void set(LeaderboardKey perLeaderboardKey)
    {
        super.set(perLeaderboardKey.getFilterStringSet());
    }
}

package com.tradehero.th.api.leaderboard.key;

import android.os.Bundle;
import com.tradehero.common.persistence.AbstractIntegerDTOKey;


public class LeaderboardDefKey extends AbstractIntegerDTOKey
{
    private static final String BUNDLE_KEY_KEY = LeaderboardDefKey.class.getName() + ".key";
    private static final String TAG = LeaderboardDefKey.class.getName();

    //<editor-fold desc="Constructors">
    public LeaderboardDefKey(Integer key)
    {
        super(key);
    }

    public LeaderboardDefKey(Bundle args)
    {
        super(args);
    }
    //</editor-fold>

    @Override public String getBundleKey()
    {
        return BUNDLE_KEY_KEY;
    }

    @Override public String toString()
    {
        return String.format("%s %d", TAG, key);
    }
}

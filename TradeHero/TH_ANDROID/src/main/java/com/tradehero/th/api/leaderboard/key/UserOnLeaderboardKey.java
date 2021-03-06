package com.tradehero.th.api.leaderboard.key;

import com.tradehero.th.api.users.UserBaseKey;
import android.support.annotation.NonNull;

public class UserOnLeaderboardKey extends LeaderboardKey
{
    @NonNull public final UserBaseKey userBaseKey;

    //<editor-fold desc="Constructors">
    public UserOnLeaderboardKey(int leaderboardId, int userId)
    {
        super(leaderboardId);
        this.userBaseKey = new UserBaseKey(userId);
    }

    public UserOnLeaderboardKey(
            @NonNull LeaderboardKey leaderboardKey,
            @NonNull UserBaseKey userBaseKey)
    {
        super(leaderboardKey.id);
        this.userBaseKey = userBaseKey;
    }
    //</editor-fold>

    @Override public int hashCode()
    {
        return super.hashCode() ^
                userBaseKey.hashCode();
    }

    @Override protected boolean equalFields(@NonNull LeaderboardKey other)
    {
        return super.equalFields(other)
                && other instanceof UserOnLeaderboardKey
                && equalFields((UserOnLeaderboardKey) other);
    }

    protected boolean equalFields(@NonNull UserOnLeaderboardKey other)
    {
        return super.equalFields(other)
                && userBaseKey.equals(other.userBaseKey);
    }
}

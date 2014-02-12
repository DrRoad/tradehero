package com.tradehero.th.api.leaderboard.key;

import android.os.Bundle;

/** Created with IntelliJ IDEA. User: xavier Date: 10/16/13 Time: 3:30 PM To change this template use File | Settings | File Templates. */
public class PerPagedLeaderboardKey extends PagedLeaderboardKey
{
    public final static String BUNDLE_KEY_PER_PAGE = PerPagedLeaderboardKey.class.getName() + ".perPage";

    public final Integer perPage;

    //<editor-fold desc="Constructors">
    public PerPagedLeaderboardKey(Integer leaderboardKey, Integer page, Integer perPage)
    {
        super(leaderboardKey, page);
        this.perPage = perPage;
    }

    public PerPagedLeaderboardKey(PerPagedLeaderboardKey other, Integer page)
    {
        super(other, page);
        this.perPage = other.perPage;
    }

    public PerPagedLeaderboardKey(Bundle args)
    {
        super(args);
        this.perPage = args.containsKey(BUNDLE_KEY_PER_PAGE) ? args.getInt(BUNDLE_KEY_PER_PAGE) : null;
    }
    //</editor-fold>

    @Override public int hashCode()
    {
        return super.hashCode() ^ (perPage == null ? 0 : perPage.hashCode());
    }

    @Override public boolean equals(PagedLeaderboardKey other)
    {
        return super.equals(other) && other instanceof PerPagedLeaderboardKey &&
                equals((PerPagedLeaderboardKey) other);
    }

    public boolean equals(PerPagedLeaderboardKey other)
    {
        return other != null &&
                super.equals(other) &&
                (perPage == null ? other.perPage == null : perPage.equals(other.perPage));
    }

    public int compareTo(PerPagedLeaderboardKey other)
    {
        if (this == other)
        {
            return 0;
        }

        if (other == null)
        {
            return 1;
        }

        int parentComp = super.compareTo(other);
        if (parentComp != 0)
        {
            return parentComp;
        }

        return perPage.compareTo(other.perPage);
    }

    @Override public PagedLeaderboardKey cloneAtPage(int page)
    {
        return new PerPagedLeaderboardKey(this, page);
    }

    @Override public void putParameters(Bundle args)
    {
        super.putParameters(args);
        if (perPage == null)
        {
            args.remove(BUNDLE_KEY_PER_PAGE);
        }
        else
        {
            args.putInt(BUNDLE_KEY_PER_PAGE, perPage);
        }
    }
}

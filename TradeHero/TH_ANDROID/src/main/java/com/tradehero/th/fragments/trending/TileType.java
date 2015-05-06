package com.tradehero.th.fragments.trending;

import android.support.annotation.LayoutRes;
import com.tradehero.th.R;

public enum TileType
{
    Normal(0, false),
    EarnCredit(R.layout.tile_earn_credit),
    ExtraCash(R.layout.tile_extra_cash),
    ResetPortfolio(R.layout.tile_reset_portfolio),
    Survey(R.layout.tile_survey),
    FromProvider(R.layout.tile_from_provider),
    PopQuiz(R.layout.tile_extra_popquiz);

    private final boolean extra;
    @LayoutRes private final int layoutResourceId;
    private final boolean enable;

    TileType(@LayoutRes int layoutResourceId)
    {
        this(layoutResourceId, true);
    }

    TileType(@LayoutRes int layoutResourceId, boolean extra)
    {
        this(layoutResourceId, extra, true);
    }

    /**
     *
     * @param layoutResourceId layout to display this tile
     * @param extra whether this tile is kind of extra or not (security item, or normal tile is not)
     * @param enable whether this tile is enable (clickable, react to user interaction)
     */
    TileType(@LayoutRes int layoutResourceId, boolean extra, boolean enable)
    {
        this.layoutResourceId = layoutResourceId;
        this.extra = extra;
        this.enable = enable;
    }

    public boolean isExtra()
    {
        return extra;
    }

    public int getLayoutResourceId()
    {
        return layoutResourceId;
    }

    static TileType at(int i)
    {
        return TileType.values()[i];
    }

    public boolean isEnable()
    {
        return enable;
    }
}
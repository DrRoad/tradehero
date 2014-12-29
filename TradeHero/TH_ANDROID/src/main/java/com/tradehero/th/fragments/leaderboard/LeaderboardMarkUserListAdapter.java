package com.tradehero.th.fragments.leaderboard;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th.R;
import com.tradehero.th.adapters.LoaderDTOAdapter;
import com.tradehero.th.api.leaderboard.LeaderboardUserDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.UserBaseDTO;
import com.tradehero.th.api.users.UserProfileDTO;

public class LeaderboardMarkUserListAdapter
        extends
        LoaderDTOAdapter<
                LeaderboardUserDTO, LeaderboardMarkUserItemView, LeaderboardMarkUserLoader>
        implements SwipeRefreshLayout.OnRefreshListener
{
    @LayoutRes private static final int stockLeaderboardLayoutResId = R.layout.lbmu_item_roi_mode;

    protected UserProfileDTO currentUserProfileDTO;
    @Nullable protected OwnedPortfolioId applicablePortfolioId;
    protected LeaderboardMarkUserItemView.OnFollowRequestedListener followRequestedListener;
    private boolean hideStatistics;

    //<editor-fold desc="Constructors">
    public LeaderboardMarkUserListAdapter(Context context, int loaderId)
    {
        super(context, loaderId, 0);
    }
    //</editor-fold>

    public void setCurrentUserProfileDTO(UserProfileDTO currentUserProfileDTO)
    {
        this.currentUserProfileDTO = currentUserProfileDTO;
    }

    public void setApplicablePortfolioId(@Nullable OwnedPortfolioId ownedPortfolioId)
    {
        this.applicablePortfolioId = ownedPortfolioId;
    }

    public void setFollowRequestedListener(LeaderboardMarkUserItemView.OnFollowRequestedListener followRequestedListener)
    {
        this.followRequestedListener = followRequestedListener;
    }

    @Override public LeaderboardUserDTO getItem(int position)
    {
        LeaderboardUserDTO dto = (LeaderboardUserDTO) super.getItem(position);
        dto.setPosition(position);
        dto.setLeaderboardId(getLoader().getLeaderboardId());
        dto.setIncludeFoF(getLoader().isIncludeFoF());

        return dto;
    }

    @Override protected View conditionalInflate(int position, View convertView, ViewGroup viewGroup)
    {
        if (convertView == null)
        {
            convertView = getInflater().inflate(stockLeaderboardLayoutResId, viewGroup, false);
        }
        return super.conditionalInflate(position, convertView, viewGroup);
    }

    @Override protected void fineTune(int position, LeaderboardUserDTO dto, LeaderboardMarkUserItemView dtoView)
    {
        dtoView.linkWith(currentUserProfileDTO, true);
        dtoView.linkWith(applicablePortfolioId);
        dtoView.shouldHideStatistics(hideStatistics);
        dtoView.setFollowRequestedListener(createChildFollowRequestedListener());

        final ExpandingLayout expandingLayout = (ExpandingLayout) dtoView.findViewById(R.id.expanding_layout);
        if (expandingLayout != null)
        {
            expandingLayout.expandWithNoAnimation(dto.isExpanded());
            dtoView.onExpand(dto.isExpanded());
        }
    }

    @Override public void onRefresh()
    {
        getLoader().loadPrevious();
    }

    protected LeaderboardMarkUserItemView.OnFollowRequestedListener createChildFollowRequestedListener()
    {
        return this::notifyFollowRequested;
    }

    protected void notifyFollowRequested(@NonNull UserBaseDTO userBaseDTO)
    {
        LeaderboardMarkUserItemView.OnFollowRequestedListener followRequestedListenerCopy = followRequestedListener;
        if (followRequestedListenerCopy != null)
        {
            followRequestedListenerCopy.onFollowRequested(userBaseDTO);
        }
    }

    public void setHideStatistics(boolean hideStatistics)
    {
        this.hideStatistics = hideStatistics;
    }
}

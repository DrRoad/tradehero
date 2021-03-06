package com.tradehero.th.network.service;

import android.support.annotation.NonNull;
import com.tradehero.th.api.leaderboard.LeaderboardDTO;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTOFactory;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTOList;
import com.tradehero.th.api.leaderboard.key.FriendsPerPagedLeaderboardKey;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefKey;
import com.tradehero.th.api.leaderboard.key.LeaderboardKey;
import com.tradehero.th.api.leaderboard.key.PagedLeaderboardKey;
import com.tradehero.th.api.leaderboard.key.PerPagedFilteredLeaderboardKey;
import com.tradehero.th.api.leaderboard.key.PerPagedLeaderboardKey;
import com.tradehero.th.api.leaderboard.key.SortedPerPagedLeaderboardKey;
import com.tradehero.th.api.leaderboard.key.UserOnLeaderboardKey;
import com.tradehero.th.api.leaderboard.position.LeaderboardFriendsDTO;
import com.tradehero.th.api.leaderboard.position.LeaderboardMarkUserId;
import com.tradehero.th.api.leaderboard.position.PagedLeaderboardMarkUserId;
import com.tradehero.th.api.leaderboard.position.PerPagedLeaderboardMarkUserId;
import com.tradehero.th.api.position.GetPositionsDTO;
import com.tradehero.th.models.leaderboard.def.DTOProcessorLeaderboardDefDTOList;
import com.tradehero.th.models.position.DTOProcessorGetPositions;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class LeaderboardServiceWrapper
{
    @NonNull private final LeaderboardServiceRx leaderboardServiceRx;
    @NonNull private final LeaderboardDefDTOFactory leaderboardDefDTOFactory;

    //<editor-fold desc="Constructors">
    @Inject public LeaderboardServiceWrapper(
            @NonNull LeaderboardServiceRx leaderboardServiceRx,
            @NonNull LeaderboardDefDTOFactory leaderboardDefDTOFactory)
    {
        super();
        this.leaderboardServiceRx = leaderboardServiceRx;
        this.leaderboardDefDTOFactory = leaderboardDefDTOFactory;
    }
    //</editor-fold>

    protected DTOProcessorGetPositions createProcessorReceivedGetPositions(LeaderboardMarkUserId leaderboardMarkUserId)
    {
        return new DTOProcessorGetPositions(leaderboardMarkUserId);
    }

    protected DTOProcessorLeaderboardDefDTOList createProcessorLeaderboardDefDTOList()
    {
        return new DTOProcessorLeaderboardDefDTOList(leaderboardDefDTOFactory);
    }

    //<editor-fold desc="Get Leaderboard Definitions">
    @NonNull public Observable<LeaderboardDefDTOList> getLeaderboardDefinitionsRx()
    {
        return leaderboardServiceRx.getLeaderboardDefinitions()
                .map(createProcessorLeaderboardDefDTOList());
    }
    //</editor-fold>

    //<editor-fold desc="Get Leaderboard Definition">
    @NonNull public Observable<LeaderboardDefDTO> getLeaderboardDef(
            @NonNull LeaderboardDefKey leaderboardDefKey)
    {
        return leaderboardServiceRx.getLeaderboardDef(leaderboardDefKey.key);
    }
    //</editor-fold>

    //<editor-fold desc="Get Leaderboard">
    public Observable<LeaderboardDTO> getLeaderboardRx(@NonNull LeaderboardKey leaderboardKey)
    {
        if (leaderboardKey instanceof UserOnLeaderboardKey)
        {
            return leaderboardServiceRx.getUserOnLeaderboard(
                    leaderboardKey.id,
                    ((UserOnLeaderboardKey) leaderboardKey).userBaseKey.key,
                    null);
        }
        else if (leaderboardKey instanceof SortedPerPagedLeaderboardKey)
        {
            SortedPerPagedLeaderboardKey sortedPerPagedLeaderboardKey = (SortedPerPagedLeaderboardKey) leaderboardKey;
            return leaderboardServiceRx.getLeaderboard(
                    sortedPerPagedLeaderboardKey.id,
                    sortedPerPagedLeaderboardKey.page,
                    sortedPerPagedLeaderboardKey.perPage,
                    sortedPerPagedLeaderboardKey.sortType);
        }
        else if (leaderboardKey instanceof PerPagedFilteredLeaderboardKey)
        {
            PerPagedFilteredLeaderboardKey perPagedFilteredLeaderboardKey = (PerPagedFilteredLeaderboardKey) leaderboardKey;
            return leaderboardServiceRx.getFilteredLeaderboard(perPagedFilteredLeaderboardKey.id,
                    perPagedFilteredLeaderboardKey.winRatio,
                    perPagedFilteredLeaderboardKey.averageMonthlyTradeCount,

                    // HACK https://www.pivotaltracker.com/story/show/73042972
                    Math.max(1, perPagedFilteredLeaderboardKey.averageHoldingDays),

                    perPagedFilteredLeaderboardKey.minSharpeRatio,
                    perPagedFilteredLeaderboardKey.minConsistency == null ? null : 1 / perPagedFilteredLeaderboardKey.minConsistency,
                    perPagedFilteredLeaderboardKey.page,
                    perPagedFilteredLeaderboardKey.perPage);
        }
        else if (leaderboardKey instanceof FriendsPerPagedLeaderboardKey)
        {
            return leaderboardServiceRx.getNewFriendsLeaderboard()
                    .map(leaderboardFriendsDTO -> leaderboardFriendsDTO.leaderboard);
        }
        else if (leaderboardKey instanceof PerPagedLeaderboardKey)
        {
            PerPagedLeaderboardKey perPagedLeaderboardKey = (PerPagedLeaderboardKey) leaderboardKey;
            return leaderboardServiceRx.getLeaderboard(
                    perPagedLeaderboardKey.id,
                    perPagedLeaderboardKey.page,
                    perPagedLeaderboardKey.perPage);
        }
        else if (leaderboardKey instanceof PagedLeaderboardKey)
        {
            PagedLeaderboardKey pagedLeaderboardKey = (PagedLeaderboardKey) leaderboardKey;
            return leaderboardServiceRx.getLeaderboard(
                    pagedLeaderboardKey.id,
                    pagedLeaderboardKey.page,
                    null);
        }
        return leaderboardServiceRx.getLeaderboard(leaderboardKey.id, null, null);
    }

    public Observable<LeaderboardFriendsDTO> getNewFriendsLeaderboardRx()
    {
        return leaderboardServiceRx.getNewFriendsLeaderboard();
    }
    //</editor-fold>

    //<editor-fold desc="Get Positions For Leaderboard Mark User">
    public Observable<GetPositionsDTO> getPositionsForLeaderboardMarkUserRx(
            @NonNull LeaderboardMarkUserId key)
    {
        Observable<GetPositionsDTO> received;
        if (key instanceof PerPagedLeaderboardMarkUserId)
        {
            PerPagedLeaderboardMarkUserId perPagedLeaderboardMarkUserId = (PerPagedLeaderboardMarkUserId) key;
            received = leaderboardServiceRx.getPositionsForLeaderboardMarkUser(
                    perPagedLeaderboardMarkUserId.key,
                    perPagedLeaderboardMarkUserId.page,
                    perPagedLeaderboardMarkUserId.perPage);
        }
        else if (key instanceof PagedLeaderboardMarkUserId)
        {
            PagedLeaderboardMarkUserId pagedLeaderboardMarkUserId = (PagedLeaderboardMarkUserId) key;
            received = leaderboardServiceRx.getPositionsForLeaderboardMarkUser(
                    pagedLeaderboardMarkUserId.key,
                    pagedLeaderboardMarkUserId.page,
                    null);
        }
        else
        {
            received = leaderboardServiceRx.getPositionsForLeaderboardMarkUser(
                    key.key,
                    null,
                    null);
        }
        return received.map(createProcessorReceivedGetPositions(key));
    }
    //</editor-fold>
}

package com.tradehero.th.fragments.leaderboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.api.competition.ProviderUtil;
import com.tradehero.th.api.leaderboard.LeaderboardUserDTO;
import com.tradehero.th.api.leaderboard.def.LeaderboardDefDTO;
import com.tradehero.th.api.leaderboard.key.LeaderboardDefKey;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.position.GetPositionsDTOKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.UserProfileDTOUtil;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.competition.CompetitionWebViewFragment;
import com.tradehero.th.fragments.position.TabbedPositionListFragment;
import com.tradehero.th.fragments.timeline.MeTimelineFragment;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.leaderboard.LeaderboardDefCacheRx;
import com.tradehero.th.rx.ReplaceWithFunc1;
import com.tradehero.th.rx.TimberOnErrorAction1;
import com.tradehero.th.rx.dialog.OnDialogClickEvent;
import com.tradehero.th.utils.AlertDialogRxUtil;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.ScreenFlowEvent;
import java.text.SimpleDateFormat;
import javax.inject.Inject;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LeaderboardMarkUserListFragmentUtil
        implements Action1<LeaderboardItemUserAction>
{
    @NonNull private final DashboardNavigator navigator;
    @NonNull private final CurrentUserId currentUserId;
    @NonNull private final LeaderboardDefCacheRx leaderboardDefCache;
    @NonNull private final Analytics analytics;
    @NonNull private final ProviderUtil providerUtil;

    @Inject protected UserServiceWrapper userServiceWrapper;

    private BaseLeaderboardPagedRecyclerRxFragment fragment;
    private LeaderboardType leaderboardType;
    private SubscriptionList onStopSubscriptions;

    //<editor-fold desc="Constructors">
    @Inject public LeaderboardMarkUserListFragmentUtil(
            @NonNull DashboardNavigator navigator,
            @NonNull CurrentUserId currentUserId,
            @NonNull LeaderboardDefCacheRx leaderboardDefCache,
            @NonNull Analytics analytics,
            @NonNull ProviderUtil providerUtil)
    {
        this.navigator = navigator;
        this.currentUserId = currentUserId;
        this.leaderboardDefCache = leaderboardDefCache;
        this.analytics = analytics;
        this.providerUtil = providerUtil;
    }
    //</editor-fold>

    public void linkWith(
            @NonNull BaseLeaderboardPagedRecyclerRxFragment fragment,
            @NonNull LeaderboardType leaderboardType)
    {
        this.fragment = fragment;
        this.leaderboardType = leaderboardType;
        HierarchyInjector.inject(fragment.getActivity(), this);
    }

    public void onStart()
    {
        onStopSubscriptions = new SubscriptionList();
    }

    public void onStop()
    {
        onStopSubscriptions.unsubscribe();
    }

    public void onDestroy()
    {
        this.fragment = null;
        this.leaderboardType = null;
    }

    @Override public void call(LeaderboardItemUserAction userAction)
    {
        if (userAction.dto instanceof LeaderboardMarkedUserItemDisplayDto)
        {
            switch (userAction.actionType)
            {
                case PROFILE:
                    openTimeline((LeaderboardMarkedUserItemDisplayDto) userAction.dto);
                    break;

                case FOLLOW:
                    UserBaseDTO toFollow = ((LeaderboardMarkedUserItemDisplayDto) userAction.dto).leaderboardUserDTO;
                    if (toFollow != null)
                    {
                        handleFollowRequested(toFollow);
                        LeaderboardMarkedUserItemDisplayDto markedUserItemDisplayDto = (LeaderboardMarkedUserItemDisplayDto) userAction.dto;
                        markedUserItemDisplayDto.setIsFollowing(true);
                        fragment.updateRow(markedUserItemDisplayDto);
                    }
                    else
                    {
                        Timber.e(new NullPointerException(), "ToFollow was null");
                    }
                    break;

                case UNFOLLOW:
                    handleUnfollowRequested(userAction.dto);
                    break;

                case POSITIONS:
                    handlePositionsRequested((LeaderboardMarkedUserItemDisplayDto) userAction.dto);
                    break;

                case RULES:
                    handleRulesRequested((CompetitionLeaderboardItemDisplayDTO) userAction.dto);
                    break;
            }
        }
    }

    private void handleUnfollowRequested(LeaderboardItemDisplayDTO toUnfollow)
    {
        LeaderboardMarkedUserItemDisplayDto markedUser = (LeaderboardMarkedUserItemDisplayDto) toUnfollow;
        UserBaseDTO user = markedUser.leaderboardUserDTO;
        onStopSubscriptions.add(
                AlertDialogRxUtil.build(fragment.getActivity())
                        .setTitle(fragment.getString(R.string.manage_heroes_alert_unfollow_title,
                                user != null ? user.displayName : fragment.getString(R.string.hero)))
                        .setMessage(R.string.manage_heroes_alert_unfollow_message)
                        .setPositiveButton(R.string.manage_heroes_alert_unfollow_ok)
                        .setNegativeButton(R.string.manage_heroes_alert_unfollow_cancel)
                        .build()
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(new Func1<OnDialogClickEvent, Boolean>()
                        {
                            @Override public Boolean call(OnDialogClickEvent onDialogClickEvent)
                            {
                                return onDialogClickEvent.isPositive();
                            }
                        })
                        .map(new ReplaceWithFunc1<OnDialogClickEvent, LeaderboardMarkedUserItemDisplayDto>(
                                markedUser))
                        .doOnNext(new Action1<LeaderboardMarkedUserItemDisplayDto>()
                        {
                            @Override public void call(LeaderboardMarkedUserItemDisplayDto leaderboardMarkedUserItemDisplayDto)
                            {
                                leaderboardMarkedUserItemDisplayDto.setIsFollowing(false);
                                fragment.updateRow(leaderboardMarkedUserItemDisplayDto);
                            }
                        })
                        .observeOn(Schedulers.io())
                        .flatMap(new Func1<LeaderboardMarkedUserItemDisplayDto, Observable<UserProfileDTO>>()
                        {
                            @Override public Observable<UserProfileDTO> call(LeaderboardMarkedUserItemDisplayDto leaderboardMarkedUserItemDisplayDto)
                            {
                                if (leaderboardMarkedUserItemDisplayDto.leaderboardUserDTO != null)
                                {
                                    return userServiceWrapper.unfollowRx(leaderboardMarkedUserItemDisplayDto.leaderboardUserDTO.getBaseKey());
                                }
                                return Observable.empty();
                            }
                        })
                        .subscribe(new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO userProfileDTO)
                            {
                                fragment.setCurrentUserProfileDTO(userProfileDTO);
                            }
                        }, new TimberOnErrorAction1("Failed to unfollow hero"))
        );
    }

    protected void openTimeline(@NonNull LeaderboardMarkedUserItemDisplayDto dto)
    {
        Bundle bundle = new Bundle();
        if (dto.leaderboardUserDTO == null)
        {
            navigator.pushFragment(MeTimelineFragment.class, bundle);
            return;
        }
        UserBaseKey userToSee = dto.leaderboardUserDTO.getBaseKey();
        if (currentUserId.toUserBaseKey().equals(userToSee))
        {
            navigator.pushFragment(MeTimelineFragment.class, bundle);
        }
        else
        {
            PushableTimelineFragment.putUserBaseKey(bundle, userToSee);
            navigator.pushFragment(PushableTimelineFragment.class, bundle);
        }
    }

    protected void handleFollowRequested(@NonNull final UserBaseDTO userBaseDTO)
    {
        onStopSubscriptions.add(
                AppObservable.bindSupportFragment(
                        fragment,
                        userServiceWrapper.freeFollowRx(userBaseDTO.getBaseKey())
                )
                        .subscribe(new Action1<UserProfileDTO>()
                                   {
                                       @Override public void call(UserProfileDTO userProfileDTO)
                                       {
                                           fragment.setCurrentUserProfileDTO(userProfileDTO);
                                           int followType = userProfileDTO.getFollowType(userBaseDTO);
                                           if (followType == UserProfileDTOUtil.IS_FREE_FOLLOWER)
                                           {
                                               analytics.addEvent(new ScreenFlowEvent(AnalyticsConstants.FreeFollow_Success,
                                                       AnalyticsConstants.Leaderboard));
                                           }
                                           //else if (followType == UserProfileDTOUtil.IS_PREMIUM_FOLLOWER)
                                           //{
                                           //    analytics.addEvent(new ScreenFlowEvent(AnalyticsConstants.PremiumFollow_Success,
                                           //            AnalyticsConstants.Leaderboard));
                                           //}
                                       }
                                   },
                                new Action1<Throwable>()
                                {
                                    @Override public void call(Throwable throwable)
                                    {
                                        Timber.e(throwable, "Failed to follow hero");
                                    }
                                })
        );
    }

    protected void handlePositionsRequested(@NonNull LeaderboardMarkedUserItemDisplayDto dto)
    {
        LeaderboardUserDTO userDTO = dto.leaderboardUserDTO;
        if (userDTO == null)
        {
            Timber.e(new NullPointerException(), "LeaderboardUserDTO was null %s", dto);
            THToast.show(R.string.leaderboard_friends_position_failed);
            return;
        }
        GetPositionsDTOKey getPositionsDTOKey = userDTO.getGetPositionsDTOKey();
        if (getPositionsDTOKey == null)
        {
            Timber.e(new NullPointerException(), "Unable to get positions %s", dto);
            THToast.show(R.string.leaderboard_friends_position_failed);
            return;
        }

        // get leaderboard definition from cache, supposedly it exists coz this view appears after leaderboard definition list
        LeaderboardDefDTO leaderboardDef = null;
        Integer leaderboardId = dto.leaderboardUserDTO.getLeaderboardId();
        if (leaderboardId != null)
        {
            leaderboardDef = leaderboardDefCache
                    .getCachedValue(new LeaderboardDefKey(dto.leaderboardUserDTO.getLeaderboardId()));
        }

        // leaderboard mark user id, to get marking user information
        Bundle bundle = new Bundle();
        TabbedPositionListFragment.putGetPositionsDTOKey(bundle, getPositionsDTOKey);
        TabbedPositionListFragment.putShownUser(bundle, dto.leaderboardUserDTO.getBaseKey());
        if (leaderboardDef != null)
        {
            TabbedPositionListFragment.putLeaderboardTimeRestricted(bundle, leaderboardDef.isTimeRestrictedLeaderboard());
        }
        TabbedPositionListFragment.putIsFX(bundle, leaderboardType.assetClass);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf =
                new SimpleDateFormat(fragment.getResources().getString(R.string.leaderboard_datetime_format));
        String formattedStartPeriodUtc = sdf.format(dto.leaderboardUserDTO.periodStartUtc);
        TabbedPositionListFragment.putLeaderboardPeriodStartString(bundle, formattedStartPeriodUtc);

        OwnedPortfolioId applicablePortfolioId = fragment.getApplicablePortfolioId();
        if (applicablePortfolioId != null)
        {
            TabbedPositionListFragment.putApplicablePortfolioId(bundle, applicablePortfolioId);
        }

        if (dto instanceof CompetitionLeaderboardItemDisplayDTO)
        {
            TabbedPositionListFragment.putProviderId(bundle,
                    ((CompetitionLeaderboardItemDisplayDTO) dto).providerDTO.getProviderId());
        }

        navigator.pushFragment(TabbedPositionListFragment.class, bundle);
    }

    protected void handleRulesRequested(@NonNull CompetitionLeaderboardItemDisplayDTO dto)
    {
        Bundle args = new Bundle();
        CompetitionWebViewFragment.putUrl(args, providerUtil.getRulesPage(dto.providerDTO.getProviderId()));
        navigator.pushFragment(CompetitionWebViewFragment.class, args);
    }
}

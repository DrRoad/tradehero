package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.article.ArticleInfoDTO;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.news.NewsItemCompactDTO;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIntegerId;
import com.tradehero.th.api.security.SecurityIntegerIdList;
import com.tradehero.th.api.timeline.TimelineItemDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.watchlist.WatchlistPositionDTOList;
import com.tradehero.th.fragments.discovery.ArticleItemView;
import com.tradehero.th.fragments.news.NewsHeadlineViewLinear;
import com.tradehero.th.fragments.news.NewsViewLinear;
import com.tradehero.th.fragments.timeline.TimelineItemViewLinear;
import com.tradehero.th.models.share.SocialShareTranslationHelper;
import com.tradehero.th.persistence.alert.AlertCompactListCacheRx;
import com.tradehero.th.persistence.security.SecurityMultiFetchAssistant;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCacheRx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.ocpsoft.prettytime.PrettyTime;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;

public class AbstractDiscussionCompactItemViewLinearDTOFactory
{
    @NonNull private final Resources resources;
    @NonNull private final PrettyTime prettyTime;
    @NonNull private final CurrentUserId currentUserId;
    @NonNull private final SocialShareTranslationHelper shareTranslationHelper;
    @NonNull private final UserWatchlistPositionCacheRx userWatchlistPositionCache;
    @NonNull private final AlertCompactListCacheRx alertCompactListCache;
    @NonNull private final SecurityMultiFetchAssistant securityMultiFetchAssistant;

    //<editor-fold desc="Constructors">
    @Inject public AbstractDiscussionCompactItemViewLinearDTOFactory(
            @NonNull Context context,
            @NonNull PrettyTime prettyTime,
            @NonNull CurrentUserId currentUserId,
            @NonNull SocialShareTranslationHelper shareTranslationHelper,
            @NonNull UserWatchlistPositionCacheRx userWatchlistPositionCache,
            @NonNull AlertCompactListCacheRx alertCompactListCache,
            @NonNull SecurityMultiFetchAssistant securityMultiFetchAssistant)
    {
        this.resources = context.getResources();
        this.prettyTime = prettyTime;
        this.currentUserId = currentUserId;
        this.shareTranslationHelper = shareTranslationHelper;
        this.userWatchlistPositionCache = userWatchlistPositionCache;
        this.alertCompactListCache = alertCompactListCache;
        this.securityMultiFetchAssistant = securityMultiFetchAssistant;
    }
    //</editor-fold>

    @NonNull public Observable<AbstractDiscussionCompactItemViewLinear.DTO> createAbstractDiscussionCompactItemViewLinearDTO(
            @NonNull final AbstractDiscussionCompactDTO discussionDTO)
    {
        return shareTranslationHelper.getTranslateFlags(discussionDTO)
                .map(new Func1<SocialShareTranslationHelper.TranslateFlags, AbstractDiscussionCompactItemViewLinear.DTO>()
                {
                    @Override
                    public AbstractDiscussionCompactItemViewLinear.DTO call(SocialShareTranslationHelper.TranslateFlags translateFlags)
                    {
                        return new AbstractDiscussionCompactItemViewLinear.DTO(
                                new AbstractDiscussionCompactItemViewLinear.Requisite(
                                        resources,
                                        prettyTime,
                                        discussionDTO,
                                        translateFlags.canTranslate,
                                        translateFlags.autoTranslate));
                    }
                });
    }

    @NonNull public Observable<AbstractDiscussionCompactItemViewLinear.DTO> createDiscussionItemViewLinearDTO(
            @NonNull final DiscussionDTO discussionDTO)
    {
        return shareTranslationHelper.getTranslateFlags(discussionDTO)
                .map(new Func1<SocialShareTranslationHelper.TranslateFlags, AbstractDiscussionCompactItemViewLinear.DTO>()
                {
                    @Override
                    public AbstractDiscussionCompactItemViewLinear.DTO call(SocialShareTranslationHelper.TranslateFlags translateFlags)
                    {
                        return new DiscussionItemViewLinear.DTO(
                                new DiscussionItemViewLinear.Requisite(
                                        resources,
                                        prettyTime,
                                        discussionDTO,
                                        translateFlags.canTranslate,
                                        translateFlags.autoTranslate));
                    }
                });
    }

    @NonNull public Observable<AbstractDiscussionCompactItemViewLinear.DTO> createArticleItemViewDTO(
            @NonNull final ArticleInfoDTO discussionDTO)
    {
        return shareTranslationHelper.getTranslateFlags(discussionDTO)
                .map(new Func1<SocialShareTranslationHelper.TranslateFlags, AbstractDiscussionCompactItemViewLinear.DTO>()
                {
                    @Override
                    public AbstractDiscussionCompactItemViewLinear.DTO call(SocialShareTranslationHelper.TranslateFlags translateFlags)
                    {
                        return new ArticleItemView.DTO(
                                new ArticleItemView.Requisite(
                                        resources,
                                        prettyTime,
                                        discussionDTO,
                                        translateFlags.canTranslate,
                                        translateFlags.autoTranslate));
                    }
                });
    }

    @NonNull
    public Observable<AbstractDiscussionCompactItemViewLinear.DTO> createNewsHeadlineViewLinearDTO(@NonNull final NewsItemCompactDTO discussionDTO)
    {
        return shareTranslationHelper.getTranslateFlags(discussionDTO)
                .map(new Func1<SocialShareTranslationHelper.TranslateFlags, AbstractDiscussionCompactItemViewLinear.DTO>()
                {
                    @Override
                    public AbstractDiscussionCompactItemViewLinear.DTO call(SocialShareTranslationHelper.TranslateFlags translateFlags)
                    {
                        return new NewsHeadlineViewLinear.DTO(
                                new NewsHeadlineViewLinear.Requisite(
                                        resources,
                                        prettyTime,
                                        discussionDTO,
                                        translateFlags.canTranslate,
                                        translateFlags.autoTranslate));
                    }
                });
    }

    @NonNull
    public Observable<List<AbstractDiscussionCompactItemViewLinear.DTO>> createNewsHeadlineViewLinearDTOs(
            @NonNull final Collection<? extends NewsItemCompactDTO> discussionDTOs)
    {
        return Observable.from(discussionDTOs)
                .flatMap(new Func1<NewsItemCompactDTO, Observable<AbstractDiscussionCompactItemViewLinear.DTO>>()
                {
                    @Override public Observable<AbstractDiscussionCompactItemViewLinear.DTO> call(NewsItemCompactDTO newsItemCompactDTO)
                    {
                        return createNewsHeadlineViewLinearDTO(newsItemCompactDTO);
                    }
                })
                .toList();
    }

    @NonNull
    public Observable<AbstractDiscussionCompactItemViewLinear.DTO> createNewsViewLinearDTO(@NonNull final NewsItemDTO discussionDTO)
    {
        final SecurityIntegerIdList idsList;
        if (discussionDTO.securityIds == null)
        {
            idsList = new SecurityIntegerIdList();
        }
        else
        {
            idsList = new SecurityIntegerIdList(discussionDTO.securityIds, 0);
        }
        return Observable.combineLatest(
                shareTranslationHelper.getTranslateFlags(discussionDTO),
                securityMultiFetchAssistant.get(idsList),
                new Func2<SocialShareTranslationHelper.TranslateFlags,
                        Map<SecurityIntegerId, SecurityCompactDTO>,
                        AbstractDiscussionCompactItemViewLinear.DTO > ()
        {
                            @Override
                            public AbstractDiscussionCompactItemViewLinear.DTO call(
                                    SocialShareTranslationHelper.TranslateFlags translateFlags,
                                    Map<SecurityIntegerId, SecurityCompactDTO> securities)
                            {
                                return new NewsViewLinear.DTO(
                                        new NewsViewLinear.Requisite(
                                                resources,
                                                prettyTime,
                                                discussionDTO,
                                                translateFlags.canTranslate,
                                                translateFlags.autoTranslate,
                                                new ArrayList<>(securities.values())));
                            }
                        });
    }

    @NonNull
    public Observable<AbstractDiscussionCompactItemViewLinear.DTO> createTimelineItemViewLinearDTO(@NonNull final TimelineItemDTO discussionDTO)
    {
        return Observable.zip(
                shareTranslationHelper.getTranslateFlags(discussionDTO),
                userWatchlistPositionCache.getOne(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<UserBaseKey, WatchlistPositionDTOList>()),
                alertCompactListCache.getSecurityMappedAlerts(currentUserId.toUserBaseKey()),
                new Func3<SocialShareTranslationHelper.TranslateFlags,
                        WatchlistPositionDTOList,
                        Map<SecurityId, AlertCompactDTO>,
                        AbstractDiscussionCompactItemViewLinear.DTO>()
                {
                    @Override public AbstractDiscussionCompactItemViewLinear.DTO call(
                            SocialShareTranslationHelper.TranslateFlags translateFlags,
                            WatchlistPositionDTOList userWatchlist,
                            Map<SecurityId, AlertCompactDTO> alertCompactDTOMap)
                    {
                        return createTimelineItemViewLinearDTO(
                                discussionDTO,
                                translateFlags,
                                userWatchlist,
                                alertCompactDTOMap);
                    }
                });
    }

    @NonNull
    public Observable<List<AbstractDiscussionCompactItemViewLinear.DTO>> createTimelineItemViewLinearDTOs(
            @NonNull final Collection<? extends TimelineItemDTO> discussionDTOs)
    {
        return Observable.zip(
                userWatchlistPositionCache.getOne(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<UserBaseKey, WatchlistPositionDTOList>()),
                alertCompactListCache.getSecurityMappedAlerts(currentUserId.toUserBaseKey()),
                new Func2<WatchlistPositionDTOList, Map<SecurityId, AlertCompactDTO>,
                        Pair<WatchlistPositionDTOList, Map<SecurityId, AlertCompactDTO>>>()
                {
                    @Override public Pair<WatchlistPositionDTOList, Map<SecurityId, AlertCompactDTO>> call(
                            WatchlistPositionDTOList watchlistPositionDTOs,
                            Map<SecurityId, AlertCompactDTO> alertCompactDTOMap)
                    {
                        return Pair.create(watchlistPositionDTOs, alertCompactDTOMap);
                    }
                })
                .flatMap(new Func1<Pair<WatchlistPositionDTOList, Map<SecurityId, AlertCompactDTO>>,
                        Observable<AbstractDiscussionCompactItemViewLinear.DTO>>()
                {
                    @Override public Observable<AbstractDiscussionCompactItemViewLinear.DTO> call(
                            final Pair<WatchlistPositionDTOList, Map<SecurityId, AlertCompactDTO>> positionDTOs)
                    {
                        return Observable.from(discussionDTOs)
                                .flatMap(new Func1<TimelineItemDTO, Observable<AbstractDiscussionCompactItemViewLinear.DTO>>()
                                {
                                    @Override
                                    public Observable<AbstractDiscussionCompactItemViewLinear.DTO> call(final TimelineItemDTO discussionDTO)
                                    {
                                        return shareTranslationHelper.getTranslateFlags(discussionDTO)
                                                .map(new Func1<SocialShareTranslationHelper.TranslateFlags,
                                                        AbstractDiscussionCompactItemViewLinear.DTO>()
                                                {
                                                    @Override
                                                    public AbstractDiscussionCompactItemViewLinear.DTO call(
                                                            SocialShareTranslationHelper.TranslateFlags translateFlags)
                                                    {
                                                        return createTimelineItemViewLinearDTO(
                                                                discussionDTO,
                                                                translateFlags,
                                                                positionDTOs.first,
                                                                positionDTOs.second);
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .toList();
    }

    @NonNull public TimelineItemViewLinear.DTO createTimelineItemViewLinearDTO(
            @NonNull TimelineItemDTO discussionDTO,
            @NonNull SocialShareTranslationHelper.TranslateFlags translateFlags,
            @NonNull WatchlistPositionDTOList watchlistPositionDTOs,
            @NonNull Map<SecurityId, AlertCompactDTO> alertCompactDTOs)
    {
        SecurityId flavorSecurity = discussionDTO.createFlavorSecurityIdForDisplay();
        AlertCompactDTO alertCompactDTO = flavorSecurity == null ? null : alertCompactDTOs.get(flavorSecurity);
        return new TimelineItemViewLinear.DTO(
                new TimelineItemViewLinear.Requisite(
                        resources,
                        prettyTime,
                        discussionDTO,
                        translateFlags.canTranslate,
                        translateFlags.autoTranslate,
                        watchlistPositionDTOs.contains(flavorSecurity),
                        alertCompactDTO == null ? null : alertCompactDTO.getAlertId(currentUserId.get())));
    }
}

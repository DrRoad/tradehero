package com.tradehero.th.fragments.games;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.Menu;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.route.Routable;
import com.tradehero.route.RouteProperty;
import com.tradehero.th.R;
import com.tradehero.th.api.BaseResponseDTO;
import com.tradehero.th.api.games.GameScore;
import com.tradehero.th.api.games.MiniGameDefDTO;
import com.tradehero.th.api.games.MiniGameDefKey;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.web.BaseWebViewFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.MiniGameServiceWrapper;
import com.tradehero.th.persistence.games.MiniGameDefCache;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.AnalyticsDuration;
import com.tradehero.th.utils.metrics.events.AttributesEvent;
import com.tradehero.th.utils.route.THRouter;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import timber.log.Timber;

@Routable({
        "games/:" + GameWebViewFragment.GAME_ID_KEY
                + "/recordScore/:" + GameWebViewFragment.GAME_SCORE_KEY
                + "/level/:" + GameWebViewFragment.GAME_LEVEL_KEY
})
public class GameWebViewFragment extends BaseWebViewFragment
{
    static final String GAME_ID_ARG_KEY = GameWebViewFragment.class.getName() + ".gameId";
    static final String GAME_ID_KEY = "gameId";
    static final String GAME_SCORE_KEY = "scoreNum";
    static final String GAME_LEVEL_KEY = "levelNum";

    @Inject THRouter thRouter;
    @Inject MiniGameDefCache miniGameDefCache;
    @Inject MiniGameServiceWrapper gamesServiceWrapper;
    @Inject Analytics analytics;

    protected MiniGameDefKey miniGameDefKey;
    @RouteProperty(GAME_ID_KEY) protected Integer gameId;
    @RouteProperty(GAME_SCORE_KEY) protected Integer score;
    @RouteProperty(GAME_LEVEL_KEY) protected Integer level;

    private long beginTime;
    @Nullable Subscription miniGameDefSubscription;
    protected MiniGameDefDTO miniGameDefDTO;

    public static void putUrl(@NonNull Bundle args, @NonNull MiniGameDefDTO miniGameDefDTO, @NonNull UserBaseKey userBaseKey)
    {
        putUrl(args, miniGameDefDTO.url + "?userId=" + userBaseKey.getUserId());
    }

    public static void putGameId(@NonNull Bundle args, @NonNull MiniGameDefKey miniGameDefKey)
    {
        args.putBundle(GAME_ID_ARG_KEY, miniGameDefKey.getArgs());
    }

    @NonNull public static MiniGameDefKey getGameId(@NonNull Bundle args)
    {
        return new MiniGameDefKey(args.getBundle(GAME_ID_ARG_KEY));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        HierarchyInjector.inject(this);
        thRouter.inject(this);
        miniGameDefKey = getGameId(getArguments());
    }

    @Override protected int getLayoutResId()
    {
        return R.layout.fragment_webview;
    }

    @Override public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        displayName();
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchMiniGameDef();
    }

    @Override public void onResume()
    {
        super.onResume();
        thRouter.inject(this);
        submitScore();
        beginTime = System.currentTimeMillis();
    }

    @Override public void onPause()
    {
        reportAnalytics();
        super.onPause();
    }

    @Override public void onStop()
    {
        unsubscribe(miniGameDefSubscription);
        miniGameDefSubscription = null;
        super.onStop();
    }

    private void reportAnalytics()
    {
        AnalyticsDuration duration = AnalyticsDuration.sinceTimeMillis(beginTime);
        Map<String, String> map = new HashMap<>();
        map.put(AnalyticsConstants.GamePlayed, miniGameDefKey.key.toString());
        map.put(AnalyticsConstants.TimeInGame, duration.toString());
        analytics.fireEvent(new AttributesEvent(AnalyticsConstants.GamePlaySummary, map));
    }

    protected void fetchMiniGameDef()
    {
        unsubscribe(miniGameDefSubscription);
        miniGameDefSubscription = AndroidObservable.bindFragment(
                this,
                miniGameDefCache.get(miniGameDefKey))
                .subscribe(createMiniGameDefObserver());
    }

    @NonNull protected Observer<Pair<MiniGameDefKey, MiniGameDefDTO>> createMiniGameDefObserver()
    {
        return new MiniGameDefObserver();
    }

    protected class MiniGameDefObserver implements Observer<Pair<MiniGameDefKey, MiniGameDefDTO>>
    {
        @Override public void onNext(Pair<MiniGameDefKey, MiniGameDefDTO> pair)
        {
            linkWith(pair.second);
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
        }
    }

    protected void linkWith(@NonNull MiniGameDefDTO miniGameDefDTO)
    {
        this.miniGameDefDTO = miniGameDefDTO;
        displayName();
    }

    protected void displayName()
    {
        if (miniGameDefDTO != null)
        {
            setActionBarTitle(miniGameDefDTO.name);
        }
    }

    protected void submitScore()
    {
        if (gameId != null && score != null && level != null)
        {
            if (!gameId.equals(miniGameDefKey.key))
            {
                Timber.e(new IllegalArgumentException("Got gameId " + gameId + ", while it is for " + miniGameDefKey.key),
                        "Got gameId %d, while it is for %d", gameId, miniGameDefKey.key);
                THToast.show(R.string.error_submit_score_game_id_mismatch);
            }
            else
            {
                gamesServiceWrapper.recordScore(new MiniGameDefKey(gameId), new GameScore(score, level))
                        .subscribe(new Observer<BaseResponseDTO>()
                        {
                            @Override public void onNext(BaseResponseDTO baseResponseDTO)
                            {
                                Timber.d("Received %s", baseResponseDTO);
                            }

                            @Override public void onCompleted()
                            {
                                clearScore();
                            }

                            @Override public void onError(Throwable e)
                            {
                                Timber.e(e, "Failed to report score");
                            }
                        });
            }
        }
    }

    protected void clearScore()
    {
        this.gameId = null;
        this.score = null;
        this.level = null;
        getArguments().remove(GAME_ID_KEY);
        getArguments().remove(GAME_SCORE_KEY);
        getArguments().remove(GAME_LEVEL_KEY);
    }
}

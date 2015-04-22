package com.tradehero.th.fragments.fxonboard;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ViewAnimator;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.education.VideoDTO;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.base.BaseDialogFragment;
import com.tradehero.th.fragments.education.VideoAdapter;
import com.tradehero.th.fragments.education.VideoDTOUtil;
import com.tradehero.th.fragments.trending.TrendingMainFragment;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.network.service.VideoServiceWrapper;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.TimberOnErrorAction;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class FxOnBoardDialogFragment extends BaseDialogFragment
{
    private static final String TAG = FxOnBoardDialogFragment.class.getName();

    @InjectView(R.id.view_animator) ViewAnimator viewAnimator;
    @InjectView(R.id.introduction_videos_grid) GridView videosGrid;
    @InjectView(android.R.id.empty) View emptyView;
    @InjectView(R.id.progress) View progressBar;

    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserProfileCacheRx> userProfileCache;
    @Inject Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject VideoServiceWrapper videoServiceWrapper;
    @Inject Lazy<DashboardNavigator> navigator;
    private VideoAdapter videoAdapter;
    @NonNull private BehaviorSubject<UserAction> userActionTypeBehaviorSubject;

    //<editor-fold desc="Constructors">
    public FxOnBoardDialogFragment()
    {
        super();
        userActionTypeBehaviorSubject = BehaviorSubject.create();
    }
    //</editor-fold>

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fx_onboard_dialog, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        videoAdapter = new VideoAdapter(getActivity(), null, R.layout.video_view);
        videosGrid.setAdapter(videoAdapter);

        videosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> parent, View view1, int position, long id)
            {
                VideoDTO videoDTO = videoAdapter.getItem(position);
                VideoDTOUtil.openVideoDTO(FxOnBoardDialogFragment.this.getActivity(), navigator.get(), videoDTO);
            }
        });
    }

    @Override public void onStart()
    {
        super.onStart();

        List<FxOnBoardView<Boolean>> onBoardViews = new ArrayList<>();
        for (int i = 0; i < viewAnimator.getChildCount(); ++i)
        {
            View child = viewAnimator.getChildAt(i);
            if (child instanceof FxOnBoardView)
            {
                @SuppressWarnings("unchecked")
                FxOnBoardView<Boolean> fxOnBoardView = (FxOnBoardView<Boolean>) child;
                onBoardViews.add(fxOnBoardView);
            }
        }

        onStopSubscriptions.add(Observable.from(onBoardViews)
                .flatMap(new Func1<FxOnBoardView<Boolean>, Observable<? extends Boolean>>()
                {
                    @Override public Observable<? extends Boolean> call(FxOnBoardView<Boolean> view)
                    {
                        return view.result();
                    }
                })
                .subscribe(
                        new Action1<Boolean>()
                        {
                            @Override public void call(Boolean shouldShowNext)
                            {
                                if (shouldShowNext)
                                {
                                    if (viewAnimator.getDisplayedChild() == 1)
                                    {
                                        enrollFXAndNotify();
                                    }
                                    viewAnimator.showNext();
                                }
                                else
                                {
                                    FxOnBoardDialogFragment.this.onCloseClicked();
                                }
                            }
                        },
                        new TimberOnErrorAction("Unable to handle Forex onboard views")));
        onStopSubscriptions.add(AppObservable.bindFragment(this, videoServiceWrapper.getFXVideosRx())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<VideoDTO>>()
                {
                    @Override public void onCompleted()
                    {
                        progressBar.setVisibility(View.GONE);
                        if (videoAdapter.isEmpty())
                        {
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override public void onError(Throwable e)
                    {
                        THToast.show(R.string.error_loading_videos);
                    }

                    @Override public void onStart()
                    {
                        super.onStart();
                        progressBar.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                    @Override public void onNext(List<VideoDTO> videoDTOs)
                    {
                        videoAdapter.appendHead(videoDTOs);
                        videoAdapter.notifyDataSetChanged();
                    }
                }));
    }

    @Override public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        userActionTypeBehaviorSubject.onCompleted();
        TrendingMainFragment.fxDialogShowed = false;
    }

    @NonNull public Observable<UserAction> getUserActionTypeObservable()
    {
        return userActionTypeBehaviorSubject.asObservable();
    }

    private void enrollFXAndNotify()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get().getOne(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<UserBaseKey, UserProfileDTO>())
                        .flatMap(new Func1<UserProfileDTO, Observable<PortfolioDTO>>()
                        {
                            @Override public Observable<PortfolioDTO> call(UserProfileDTO profile)
                            {
                                if (profile.fxPortfolio == null)
                                {
                                    return userServiceWrapper.get()
                                            .createFXPortfolioRx(currentUserId.toUserBaseKey());
                                }
                                return Observable.just(profile.fxPortfolio);
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<PortfolioDTO>()
                        {
                            @Override public void call(PortfolioDTO fxPortfolio)
                            {
                                notifyUserAction(new UserAction(UserActionType.ENROLLED, fxPortfolio));
                            }
                        },
                        new EmptyAction1<Throwable>()));
    }

    @OnClick(R.id.close)
    public void onCloseClicked()
    {
        notifyUserAction(new UserAction(UserActionType.CANCELLED, null));
        dismiss();
        // TODO mark fx onboard handled
    }

    protected void notifyUserAction(@NonNull UserAction action)
    {
        userActionTypeBehaviorSubject.onNext(action);
        userActionTypeBehaviorSubject.onCompleted();
    }

    public static FxOnBoardDialogFragment showOnBoardDialog(FragmentManager fragmentManager)
    {
        FxOnBoardDialogFragment dialogFragment = new FxOnBoardDialogFragment();
        dialogFragment.show(fragmentManager, TAG);
        return dialogFragment;
    }

    public enum UserActionType
    {
        CANCELLED, ENROLLED
    }

    public static class UserAction
    {
        @NonNull public final UserActionType type;
        @Nullable public final PortfolioDTO created;

        public UserAction(@NonNull UserActionType type, @Nullable PortfolioDTO created)
        {
            this.type = type;
            this.created = created;
        }
    }
}

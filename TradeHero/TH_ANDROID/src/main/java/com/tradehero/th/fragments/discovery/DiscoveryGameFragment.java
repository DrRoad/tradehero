package com.tradehero.th.fragments.discovery;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClickSticky;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.R;
import com.tradehero.th.api.games.MiniGameDefDTO;
import com.tradehero.th.api.games.MiniGameDefListKey;
import com.tradehero.th.api.games.ViralMiniGameDefKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.games.GameWebViewFragment;
import com.tradehero.th.fragments.games.ViralGamePopupDialogFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.persistence.games.MiniGameDefListCache;
import com.tradehero.th.rx.RxLoaderManager;
import com.tradehero.th.rx.ToastOnErrorAction;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Observer;
import rx.android.observables.AndroidObservable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;

public class DiscoveryGameFragment extends DashboardFragment
{
    private static final String MINIGAMES_LIST_LOADER_ID = DiscoveryGameFragment.class.getName() + ".gameList";

    @InjectView(R.id.switcher) BetterViewAnimator switcher;
    @InjectView(R.id.game_list) StickyListHeadersListView stickyListHeadersListView;
    @InjectView(android.R.id.empty) View emptyView;
    @Inject CurrentUserId currentUserId;

    @SuppressWarnings("UnusedDeclaration")
    @OnItemClickSticky(R.id.game_list)
    void handleItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof MiniGameDefDTO)
        {
            MiniGameDefDTO miniGameDefDTO = (MiniGameDefDTO) item;

            Bundle args = new Bundle();
            GameWebViewFragment.putGameId(args, miniGameDefDTO.getDTOKey());
            GameWebViewFragment.putUrl(args, miniGameDefDTO, currentUserId.toUserBaseKey());
            if(miniGameDefDTO.viralMiniGameId != null && miniGameDefDTO.url == null)
            {
                ViralGamePopupDialogFragment dialogFragment = ViralGamePopupDialogFragment.newInstance(new ViralMiniGameDefKey(miniGameDefDTO.viralMiniGameId));
                dialogFragment.show(getChildFragmentManager(), ViralGamePopupDialogFragment.class.getName());
            }
            else if (navigator != null)
            {
                navigator.get().pushFragment(GameWebViewFragment.class, args);
            }
        }
    }

    @Inject MiniGameDefListCache miniGameDefListCache;
    @Inject RxLoaderManager rxLoaderManager;
    @Inject ToastOnErrorAction toastOnErrorAction;

    private CompositeSubscription subscriptions;

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.discovery_games, container, false);
        initViews(view);
        return view;
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        HierarchyInjector.inject(this);
    }

    private void initViews(View view)
    {
        ButterKnife.inject(this, view);

        DiscoveryGameAdapter adapter = new DiscoveryGameAdapter(getActivity(), R.layout.discovery_game_item_view);
        stickyListHeadersListView.setAdapter(adapter);
        stickyListHeadersListView.setEmptyView(emptyView);

        subscriptions = new CompositeSubscription();
        PublishSubject<List<MiniGameDefDTO>> miniGamesSubject = PublishSubject.create();
        subscriptions.add(miniGamesSubject.subscribe(
                adapter::setItems,
                e -> Timber.e(e, "Gotcha")));
        subscriptions.add(miniGamesSubject.subscribe(new UpdateUIObserver()));

        subscriptions.add(
                rxLoaderManager.create(
                        MINIGAMES_LIST_LOADER_ID,
                        AndroidObservable.bindFragment(
                                this,
                                miniGameDefListCache.get(new MiniGameDefListKey()).map(pair -> pair.second)))
                        .doOnError(toastOnErrorAction)
                        .onErrorResumeNext(Observable.empty())
                        .subscribe(miniGamesSubject));
    }

    @Override public void onDestroyView()
    {
        subscriptions.unsubscribe();
        rxLoaderManager.remove(MINIGAMES_LIST_LOADER_ID);
        super.onDestroyView();
    }

    private class UpdateUIObserver implements Observer<List<MiniGameDefDTO>>
    {
        private void showListView()
        {
            switcher.setDisplayedChildByLayoutId(R.id.game_list);
        }

        @Override public void onCompleted()
        {
            showListView();
        }

        @Override public void onError(Throwable e)
        {
            showListView();
        }

        @Override public void onNext(List<MiniGameDefDTO> miniGameDefDTOs)
        {
            showListView();
        }
    }
}

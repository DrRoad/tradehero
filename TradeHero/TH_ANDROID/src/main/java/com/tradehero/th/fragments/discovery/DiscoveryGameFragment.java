package com.tradehero.th.fragments.discovery;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.games.MiniGameDefDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.games.GameWebViewFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.MiniGameServiceWrapper;
import com.tradehero.th.rx.RxLoaderManager;
import com.tradehero.th.rx.ToastOnErrorAction;
import java.util.List;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class DiscoveryGameFragment extends DashboardFragment
{
    private static final String MINIGAMES_LIST_LOADER_ID = DiscoveryGameFragment.class.getName() + ".gameList";

    @InjectView(R.id.game_list) StickyListHeadersListView stickyListHeadersListView;
    @InjectView(android.R.id.progress) ProgressBar progressBar;
    @Inject CurrentUserId currentUserId;

    /*@OnItemClick(android.R.id.list) */void handleItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof MiniGameDefDTO)
        {
            MiniGameDefDTO miniGameDefDTO = (MiniGameDefDTO) item;
            THToast.show("Push the game in, game url is: " + miniGameDefDTO.url);

            Bundle args = new Bundle();
            GameWebViewFragment.putUrl(args, ((MiniGameDefDTO) parent.getItemAtPosition(position)).url + "?userId=" + currentUserId.toUserBaseKey().getUserId());
            if (navigator != null)
            {
                navigator.get().pushFragment(GameWebViewFragment.class, args);
            }
        }
    }

    @Inject MiniGameServiceWrapper miniGameServiceWrapper;
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
        stickyListHeadersListView.setOnItemClickListener(this::handleItemClick);

        subscriptions = new CompositeSubscription();
        PublishSubject<List<MiniGameDefDTO>> miniGamesSubject = PublishSubject.create();
        subscriptions.add(miniGamesSubject.subscribe(adapter::setItems));

        subscriptions.add(rxLoaderManager.create(MINIGAMES_LIST_LOADER_ID, miniGameServiceWrapper.getAllGames())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(toastOnErrorAction)
                .onErrorResumeNext(Observable.empty())
                .doOnNext(miniGameDefDTOs -> progressBar.setVisibility(View.INVISIBLE))
                .subscribe(miniGamesSubject));
    }

    @Override public void onDestroyView()
    {
        super.onDestroyView();

        subscriptions.unsubscribe();
        rxLoaderManager.remove(MINIGAMES_LIST_LOADER_ID);
    }
}

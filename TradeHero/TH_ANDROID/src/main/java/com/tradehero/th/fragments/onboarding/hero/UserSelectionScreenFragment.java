package com.tradehero.th.fragments.onboarding.hero;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.LeaderboardUserDTO;
import com.tradehero.th.api.leaderboard.LeaderboardUserDTOList;
import com.tradehero.th.api.market.ExchangeCompactSectorListDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.SuggestHeroesListTypeNew;
import com.tradehero.th.api.users.UserBaseDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserListType;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.fragments.onboarding.OnBoardEmptyOrItemAdapter;
import com.tradehero.th.persistence.leaderboard.LeaderboardUserListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.ToastAndLogOnErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class UserSelectionScreenFragment extends BaseFragment
{
    private static final String BUNDLE_KEY_INITIAL_HEROES = UserSelectionScreenFragment.class.getName() + ".initialHeroes";
    private static final int MAX_SELECTABLE_USERS = 5;

    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject LeaderboardUserListCacheRx leaderboardUserListCache;

    @InjectView(android.R.id.list) ListView userList;
    @InjectView(android.R.id.button1) View nextButton;
    Observable<ExchangeCompactSectorListDTO> selectedExchangesSectorsObservable;
    ArrayAdapter<OnBoardUserItemView.DTO> userAdapter;
    @NonNull Map<UserBaseKey, LeaderboardUserDTO> knownUsers;
    @NonNull Set<UserBaseKey> selectedUsers;
    @NonNull BehaviorSubject<LeaderboardUserDTOList> selectedUsersSubject;
    @NonNull PublishSubject<Boolean> nextClickedSubject;

    public static void putInitialHeroes(@NonNull Bundle args, @NonNull List<? extends UserBaseDTO> initialHeroes)
    {
        int[] ids = new int[initialHeroes.size()];
        for (int index = 0; index < initialHeroes.size(); index++)
        {
            ids[index] = initialHeroes.get(index).id;
        }
        args.putIntArray(BUNDLE_KEY_INITIAL_HEROES, ids);
    }

    @NonNull private static Set<UserBaseKey> getInitialHeroes(@NonNull Bundle args)
    {
        Set<UserBaseKey> initialHeroes = new HashSet<>();
        if (args.containsKey(BUNDLE_KEY_INITIAL_HEROES))
        {
            for (int id : args.getIntArray(BUNDLE_KEY_INITIAL_HEROES))
            {
                initialHeroes.add(new UserBaseKey(id));
            }
        }
        return initialHeroes;
    }

    public UserSelectionScreenFragment()
    {
        knownUsers = new HashMap<>();
        selectedUsers = new HashSet<>();
        selectedUsersSubject = BehaviorSubject.create();
        nextClickedSubject = PublishSubject.create();
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        userAdapter = new OnBoardEmptyOrItemAdapter<>(
                activity,
                R.layout.on_board_user_item_view,
                R.layout.on_board_empty_item);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        selectedUsers = getInitialHeroes(getArguments());
    }

    @SuppressLint("InflateParams")
    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // We can reuse the same list
        return inflater.inflate(R.layout.on_board_map_exchange, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        userList.addHeaderView(LayoutInflater.from(getActivity()).inflate(R.layout.on_board_user_header, null), "title", false);
        userList.setAdapter(userAdapter);
        displayNextButton();
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchUsersInfo();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDetach()
    {
        userAdapter = null;
        super.onDetach();
    }

    public void setSelectedExchangesSectorsObservable(@NonNull Observable<ExchangeCompactSectorListDTO> selectedExchangesSectorsObservable)
    {
        this.selectedExchangesSectorsObservable = selectedExchangesSectorsObservable;
    }

    public void fetchUsersInfo()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.getOne(currentUserId.toUserBaseKey())
                        .subscribeOn(Schedulers.computation())
                        .map(new PairGetSecond<UserBaseKey, UserProfileDTO>())
                        .flatMap(new Func1<UserProfileDTO, Observable<List<OnBoardUserItemView.DTO>>>()
                        {
                            @Override public Observable<List<OnBoardUserItemView.DTO>> call(final UserProfileDTO currentUserProfile)
                            {
                                return selectedExchangesSectorsObservable.flatMap(
                                        new Func1<ExchangeCompactSectorListDTO, Observable<Pair<UserListType, LeaderboardUserDTOList>>>()
                                        {
                                            @Override public Observable<Pair<UserListType, LeaderboardUserDTOList>> call(
                                                    ExchangeCompactSectorListDTO selectedExchanges)
                                            {
                                                return leaderboardUserListCache.getOne(new SuggestHeroesListTypeNew(
                                                        selectedExchanges.exchanges.getExchangeIds(),
                                                        selectedExchanges.getSectorIds(),
                                                        null, null))
                                                        .subscribeOn(Schedulers.computation());
                                            }
                                        })
                                        .map(new Func1<Pair<UserListType, LeaderboardUserDTOList>, List<OnBoardUserItemView.DTO>>()
                                        {
                                            @Override public List<OnBoardUserItemView.DTO> call(
                                                    Pair<UserListType, LeaderboardUserDTOList> leaderboardUserDTOListPair)
                                            {
                                                List<OnBoardUserItemView.DTO> onBoardUsers = new ArrayList<>();
                                                Set<UserBaseKey> validSelectedIds = new HashSet<>();
                                                for (LeaderboardUserDTO userDTO : leaderboardUserDTOListPair.second)
                                                {
                                                    knownUsers.put(userDTO.getBaseKey(), userDTO);
                                                    onBoardUsers.add(
                                                            new OnBoardUserItemView.DTO(getResources(),
                                                                    userDTO,
                                                                    selectedUsers.contains(userDTO.getBaseKey()),
                                                                    currentUserProfile.mostSkilledLbmu));
                                                    // Make sure we do not keep stale user ids
                                                    if (selectedUsers.contains(userDTO.getBaseKey()))
                                                    {
                                                        validSelectedIds.add(userDTO.getBaseKey());
                                                    }
                                                }
                                                selectedUsers = validSelectedIds;
                                                return onBoardUsers;
                                            }
                                        });
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<OnBoardUserItemView.DTO>>()
                        {
                            @Override public void call(List<OnBoardUserItemView.DTO> onBoardUsers)
                            {
                                userAdapter.setNotifyOnChange(false);
                                userAdapter.clear();
                                userAdapter.addAll(onBoardUsers);
                                userAdapter.notifyDataSetChanged();
                                userAdapter.setNotifyOnChange(true);
                                informSelectedHeroes();
                            }
                        },
                        new ToastAndLogOnErrorAction("Failed to load exchanges")));
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnItemClick(android.R.id.list)
    protected void onUserClicked(AdapterView<?> parent, View view, int position, long id)
    {
        OnBoardUserItemView.DTO dto = (OnBoardUserItemView.DTO) parent.getItemAtPosition(position);
        if (!dto.selected && selectedUsers.size() >= MAX_SELECTABLE_USERS)
        {
            THToast.show(getString(R.string.on_board_user_selected_max, MAX_SELECTABLE_USERS));
        }
        else
        {
            dto.selected = !dto.selected;
            if (dto.selected)
            {
                selectedUsers.add(dto.value.getBaseKey());
            }
            else
            {
                selectedUsers.remove(dto.value.getBaseKey());
            }
            ((OnBoardUserItemView) view).display(dto);

            informSelectedHeroes();
        }
        displayNextButton();
    }

    protected void informSelectedHeroes()
    {
        LeaderboardUserDTOList selectedDTOs = new LeaderboardUserDTOList();
        for (UserBaseKey selected : selectedUsers)
        {
            selectedDTOs.add(knownUsers.get(selected));
        }
        selectedUsersSubject.onNext(selectedDTOs);
    }

    protected void displayNextButton()
    {
        nextButton.setEnabled(selectedUsers.size() > 0);
    }

    @SuppressWarnings("unused")
    @OnClick(android.R.id.button2)
    protected void onBackClicked(View view)
    {
        nextClickedSubject.onNext(false);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(android.R.id.button1)
    protected void onNextClicked(@SuppressWarnings("UnusedParameters") View view)
    {
        nextClickedSubject.onNext(true);
    }

    @NonNull public Observable<LeaderboardUserDTOList> getSelectedUsersObservable()
    {
        return selectedUsersSubject.asObservable();
    }

    @NonNull public Observable<Boolean> getNextClickedObservable()
    {
        return nextClickedSubject.asObservable();
    }
}

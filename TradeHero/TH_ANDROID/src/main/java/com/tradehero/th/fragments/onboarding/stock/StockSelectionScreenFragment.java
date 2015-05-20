package com.tradehero.th.fragments.onboarding.stock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.market.ExchangeCompactSectorListDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.key.ExchangeSectorSecurityListTypeNew;
import com.tradehero.th.api.security.key.SecurityListType;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.fragments.onboarding.OnBoardEmptyOrItemAdapter;
import com.tradehero.th.persistence.security.SecurityCompactListCacheRx;
import com.tradehero.th.rx.ToastAndLogOnErrorAction;
import in.srain.cube.views.GridViewWithHeaderAndFooter;
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
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class StockSelectionScreenFragment extends BaseFragment
{
    private static final String BUNDLE_KEY_INITIAL_STOCKS = StockSelectionScreenFragment.class.getName() + ".initialStocks";
    private static final int MAX_SELECTABLE_SECURITIES = 10;

    @Inject SecurityCompactListCacheRx securityCompactListCache;

    @InjectView(android.R.id.list) GridViewWithHeaderAndFooter stockList;
    @InjectView(android.R.id.button1) View nextButton;
    ArrayAdapter<SelectableSecurityDTO> stockAdapter;
    @NonNull Map<SecurityId, SecurityCompactDTO> knownStocks;
    @NonNull Set<SecurityId> selectedStocks;
    @NonNull BehaviorSubject<SecurityCompactDTOList> selectedStocksSubject;
    @NonNull PublishSubject<Boolean> nextClickedSubject;
    Observable<ExchangeCompactSectorListDTO> selectedExchangesSectorsObservable;

    public static void putInitialStocks(@NonNull Bundle args, @NonNull List<SecurityId> securityIds)
    {
        String[] ids = new String[securityIds.size() * 2];
        for (int index = 0; index < securityIds.size(); index++)
        {
            ids[2 * index] = securityIds.get(index).getExchange();
            ids[2 * index + 1] = securityIds.get(index).getSecuritySymbol();
        }
        args.putStringArray(BUNDLE_KEY_INITIAL_STOCKS, ids);
    }

    @NonNull private static List<SecurityId> getInitialStocks(@NonNull Bundle args)
    {
        List<SecurityId> initialStocks = new ArrayList<>();
        String exchange;
        String symbol;
        if (args.containsKey(BUNDLE_KEY_INITIAL_STOCKS))
        {
            String[] values = args.getStringArray(BUNDLE_KEY_INITIAL_STOCKS);
            for (int index = 0; index < values.length / 2; index++)
            {
                exchange = values[2 * index];
                symbol = values[2 * index + 1];
                initialStocks.add(new SecurityId(exchange, symbol));
            }
        }
        return initialStocks;
    }

    public StockSelectionScreenFragment()
    {
        knownStocks = new HashMap<>();
        selectedStocks = new HashSet<>();
        selectedStocksSubject = BehaviorSubject.create();
        nextClickedSubject = PublishSubject.create();
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        stockAdapter = new OnBoardEmptyOrItemAdapter<>(
                activity,
                R.layout.on_board_security_item_view,
                R.layout.on_board_empty_item);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        selectedStocks.addAll(getInitialStocks(getArguments()));
    }

    @SuppressLint("InflateParams")
    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.on_board_security_list, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        stockList.addHeaderView(LayoutInflater.from(getActivity()).inflate(R.layout.on_board_stock_header, null), "title", false);
        stockList.setAdapter(stockAdapter);
        displayNextButton();
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchStockInfo();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDetach()
    {
        stockAdapter = null;
        super.onDetach();
    }

    public void setSelectedExchangesSectorsObservable(@NonNull Observable<ExchangeCompactSectorListDTO> selectedExchangesSectorsObservable)
    {
        this.selectedExchangesSectorsObservable = selectedExchangesSectorsObservable;
    }

    protected void fetchStockInfo()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                selectedExchangesSectorsObservable.flatMap(new Func1<ExchangeCompactSectorListDTO, Observable<SecurityCompactDTOList>>()
                {
                    @Override public Observable<SecurityCompactDTOList> call(ExchangeCompactSectorListDTO exchangeSectorListDTO)
                    {
                        return securityCompactListCache.getOne(new ExchangeSectorSecurityListTypeNew(
                                exchangeSectorListDTO.exchanges.getExchangeIds(),
                                exchangeSectorListDTO.sectors.getSectorIds(),
                                null, null))
                                .map(new PairGetSecond<SecurityListType, SecurityCompactDTOList>());
                    }
                }))
                .map(new Func1<SecurityCompactDTOList, List<SelectableSecurityDTO>>()
                {
                    @Override public List<SelectableSecurityDTO> call(SecurityCompactDTOList stockList)
                    {
                        List<SelectableSecurityDTO> onBoardStocks = new ArrayList<>();
                        Set<SecurityId> validIds = new HashSet<>();
                        for (SecurityCompactDTO security : stockList)
                        {
                            knownStocks.put(security.getSecurityId(), security);
                            onBoardStocks.add(new SelectableSecurityDTO(security, selectedStocks.contains(security.getSecurityId())));
                            // Make sure we do not keep stale stock ids
                            if (selectedStocks.contains(security.getSecurityId()))
                            {
                                validIds.add(security.getSecurityId());
                            }
                        }
                        selectedStocks = validIds;
                        return onBoardStocks;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<SelectableSecurityDTO>>()
                        {
                            @Override public void call(List<SelectableSecurityDTO> stockList)
                            {
                                stockAdapter.setNotifyOnChange(false);
                                stockAdapter.clear();
                                stockAdapter.addAll(stockList);
                                stockAdapter.setNotifyOnChange(true);
                                stockAdapter.notifyDataSetChanged();
                                informSelectedStocks();
                            }
                        },
                        new ToastAndLogOnErrorAction("Failed to load securities")));
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnItemClick(android.R.id.list)
    protected void onSecurityClicked(AdapterView<?> parent, View view, int position, long id)
    {
        SelectableSecurityDTO dto = (SelectableSecurityDTO) parent.getItemAtPosition(position);
        if (!dto.selected && selectedStocks.size() >= MAX_SELECTABLE_SECURITIES)
        {
            THToast.show(getString(R.string.on_board_stock_selected_max, MAX_SELECTABLE_SECURITIES));
        }
        else
        {
            dto.selected = !dto.selected;
            if (dto.selected)
            {
                selectedStocks.add(dto.value.getSecurityId());
            }
            else
            {
                selectedStocks.remove(dto.value.getSecurityId());
            }
            ((OnBoardStockItemView) view).display(dto);

            informSelectedStocks();
        }
        displayNextButton();
    }

    protected void informSelectedStocks()
    {
        SecurityCompactDTOList selectedDTOs = new SecurityCompactDTOList();
        for (SecurityId selected : selectedStocks)
        {
            selectedDTOs.add(knownStocks.get(selected));
        }
        selectedStocksSubject.onNext(selectedDTOs);
    }

    protected void displayNextButton()
    {
        nextButton.setEnabled(selectedStocks.size() > 0);
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

    @NonNull public Observable<SecurityCompactDTOList> getSelectedStocksObservable()
    {
        return selectedStocksSubject.asObservable();
    }

    @NonNull public Observable<Boolean> getNextClickedObservable()
    {
        return nextClickedSubject.asObservable();
    }
}

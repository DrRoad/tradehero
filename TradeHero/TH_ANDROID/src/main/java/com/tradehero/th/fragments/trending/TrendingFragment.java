package com.tradehero.th.fragments.trending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.market.ExchangeDTO;
import com.tradehero.th.api.market.ExchangeDTOList;
import com.tradehero.th.api.market.ExchangeListType;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.TrendingSecurityListType;
import com.tradehero.th.fragments.security.SecurityListFragment;
import com.tradehero.th.fragments.security.SimpleSecurityItemViewAdapter;
import com.tradehero.th.fragments.trade.BuySellFragment;
import com.tradehero.th.fragments.trending.filter.TrendingFilterSelectorView;
import com.tradehero.th.fragments.trending.filter.TrendingFilterTypeBasicDTO;
import com.tradehero.th.fragments.trending.filter.TrendingFilterTypeDTO;
import com.tradehero.th.fragments.trending.filter.TrendingFilterTypeDTOFactory;
import com.tradehero.th.models.market.ExchangeDTODescriptionNameComparator;
import com.tradehero.th.persistence.market.ExchangeListCache;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

public class TrendingFragment extends SecurityListFragment
{
    private final static String TAG = TrendingFragment.class.getSimpleName();

    public static final String BUNDLE_KEY_TRENDING_FILTER_TYPE_DTO = TrendingFragment.class.getName() + ".trendingFilterTypeDTO";
    public final static int SECURITY_ID_LIST_LOADER_ID = 2532;

    @Inject TrendingFilterTypeDTOFactory trendingFilterTypeDTOFactory;
    @Inject protected Lazy<ExchangeListCache> exchangeListCache;

    private TrendingFilterSelectorView filterSelectorView;
    private TrendingOnFilterTypeChangedListener onFilterTypeChangedListener;
    private TrendingFilterTypeDTO trendingFilterTypeDTO;

    private DTOCache.GetOrFetchTask<ExchangeListType, ExchangeDTOList> exchangeListCacheFetchTask;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Saved instance takes precedence
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_TRENDING_FILTER_TYPE_DTO))
        {
            this.trendingFilterTypeDTO = this.trendingFilterTypeDTOFactory.create(savedInstanceState.getBundle(BUNDLE_KEY_TRENDING_FILTER_TYPE_DTO));
        }
        else if (getArguments() != null && getArguments().containsKey(BUNDLE_KEY_TRENDING_FILTER_TYPE_DTO))
        {
            this.trendingFilterTypeDTO = this.trendingFilterTypeDTOFactory.create(getArguments().getBundle(BUNDLE_KEY_TRENDING_FILTER_TYPE_DTO));
        }
        else
        {
            this.trendingFilterTypeDTO = new TrendingFilterTypeBasicDTO();
        }

        createExchangeListTypeCacheListener();
    }

    private void createExchangeListTypeCacheListener()
    {
        exchangeListTypeCacheListener =
                new DTOCache.Listener<ExchangeListType, ExchangeDTOList>()
                {
                    @Override public void onDTOReceived(ExchangeListType key, ExchangeDTOList value, boolean fromCache)
                    {
                        linkWith(value, true);
                    }

                    @Override public void onErrorThrown(ExchangeListType key, Throwable error)
                    {
                        THToast.show(getString(R.string.error_fetch_exchange_list_info));
                        THLog.e(TAG, "Error fetching the list of exchanges " + key, error);
                    }
                };
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        initViews(view);
        return view;
    }

    @Override protected void initViews(View view)
    {
        super.initViews(view);

        this.onFilterTypeChangedListener = new TrendingOnFilterTypeChangedListener();
        this.filterSelectorView = (TrendingFilterSelectorView) view.findViewById(R.id.trending_filter_selector_view);
        if (this.filterSelectorView != null)
        {
            this.filterSelectorView.apply(this.trendingFilterTypeDTO);
            this.filterSelectorView.setChangedListener(this.onFilterTypeChangedListener);
        }

        fetchExchangeList();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //THLog.i(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setTitle(R.string.header_trending);

        inflater.inflate(R.menu.trending_menu, menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.btn_search:
                pushSearchIn();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onDestroyView()
    {
        //THLog.d(TAG, "onDestroyView");
        this.onFilterTypeChangedListener = null;

        if (filterSelectorView != null)
        {
            filterSelectorView.setChangedListener(null);
        }
        filterSelectorView = null;

        if (exchangeListCacheFetchTask != null)
        {
            exchangeListCacheFetchTask.setListener(null);
        }
        exchangeListCacheFetchTask = null;

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        exchangeListTypeCacheListener = null;
        super.onDestroy();
    }

    @Override protected OnItemClickListener createOnItemClickListener()
    {
        return new OnSecurityViewClickListener();
    }

    @Override protected ListAdapter createSecurityItemViewAdapter()
    {
        SimpleSecurityItemViewAdapter simpleSecurityItemViewAdapter =
                new SimpleSecurityItemViewAdapter(getActivity(), getActivity().getLayoutInflater(), R.layout.trending_security_item);
        return new ExtraTileAdapter(getActivity(), simpleSecurityItemViewAdapter);
    }

    @Override public int getSecurityIdListLoaderId()
    {
        return SECURITY_ID_LIST_LOADER_ID;
    }

    @Override public void onSaveInstanceState(Bundle outState)
    {
        THLog.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (this.trendingFilterTypeDTO != null)
        {
            outState.putBundle(BUNDLE_KEY_TRENDING_FILTER_TYPE_DTO, this.trendingFilterTypeDTO.getArgs());
        }
    }

    private void fetchExchangeList()
    {
        if (exchangeListCacheFetchTask != null)
        {
            exchangeListCacheFetchTask.setListener(null);
        }

        exchangeListCacheFetchTask = exchangeListCache.get().getOrFetch(new ExchangeListType(), exchangeListTypeCacheListener);
        exchangeListCacheFetchTask.execute();
    }

    private void linkWith(ExchangeDTOList exchangeDTOs, boolean andDisplay)
    {
        if (filterSelectorView != null && exchangeDTOs != null)
        {
            // We keep only those included in Trending and order by desc / name
            List<ExchangeDTO> exchangeDTOList = new ArrayList<>();
            for (ExchangeDTO exchangeDTO: exchangeDTOs)
            {
                if (exchangeDTO.isIncludedInTrending)
                {
                    exchangeDTOList.add(exchangeDTO);
                }
            }
            Collections.sort(exchangeDTOList, new ExchangeDTODescriptionNameComparator());

            filterSelectorView.setUpExchangeSpinner(exchangeDTOList);
            filterSelectorView.apply(trendingFilterTypeDTO);
        }
    }

    @Override public TrendingSecurityListType getSecurityListType(int page)
    {
        return trendingFilterTypeDTO.getSecurityListType(getUsableExchangeName(), page, perPage);
    }

    protected String getUsableExchangeName()
    {
        if (trendingFilterTypeDTO != null && trendingFilterTypeDTO.exchange != null && trendingFilterTypeDTO.exchange.name != null)
        {
            return trendingFilterTypeDTO.exchange.name;
        }
        return TrendingSecurityListType.ALL_EXCHANGES;
    }

    public void pushSearchIn()
    {
        navigator.pushFragment(SearchStockPeopleFragment.class);
    }

    //<editor-fold desc="BaseFragment.TabBarVisibilityInformer">
    @Override public boolean isTabBarVisible()
    {
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Listeners">
    private class OnSecurityViewClickListener implements OnItemClickListener
    {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof SecurityCompactDTO)
            {
                SecurityCompactDTO securityCompactDTO = (SecurityCompactDTO) item;
                Bundle args = new Bundle();
                args.putBundle(BuySellFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityCompactDTO.getSecurityId().getArgs());
                navigator.pushFragment(BuySellFragment.class, args);
            }
        }
    }

    private class TrendingOnFilterTypeChangedListener implements TrendingFilterSelectorView.OnFilterTypeChangedListener
    {
        @Override public void onFilterTypeChanged(TrendingFilterTypeDTO trendingFilterTypeDTO)
        {
            TrendingFragment.this.trendingFilterTypeDTO = trendingFilterTypeDTO;
            // TODO
            forceInitialLoad();
        }
    }

    private DTOCache.Listener<ExchangeListType, ExchangeDTOList> exchangeListTypeCacheListener;
    //</editor-fold>
}

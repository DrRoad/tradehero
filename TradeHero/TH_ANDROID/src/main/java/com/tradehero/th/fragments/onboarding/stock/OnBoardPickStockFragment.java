package com.tradehero.th.fragments.onboarding.stock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityCompactDTOList;
import com.tradehero.th.api.security.key.SecurityListType;
import com.tradehero.th.api.security.key.TrendingBasicSecurityListType;
import com.tradehero.th.api.watchlist.WatchlistPositionFormDTO;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.network.service.WatchlistServiceWrapper;
import com.tradehero.th.persistence.security.SecurityCompactListCache;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnBoardPickStockFragment extends BaseFragment
{
    @Inject SecurityCompactListCache securityCompactListCache;
    @Inject WatchlistServiceWrapper watchlistServiceWrapper;
    @NotNull OnBoardPickStockViewHolder viewHolder;
    @Nullable DTOCacheNew.Listener<SecurityListType, SecurityCompactDTOList> securityListCacheListener;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        viewHolder = new OnBoardPickStockViewHolder(getActivity());
        securityListCacheListener = createSecurityListCacheListener();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.onboard_select_stock, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewHolder.attachView(view);
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchTrendingSecurities();
    }

    @Override public void onStop()
    {
        detachSecurityListCache();
        doAddToWatchlist();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        viewHolder.detachView();
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        this.securityListCacheListener = null;
        super.onDestroy();
    }

    protected void fetchTrendingSecurities()
    {
        detachSecurityListCache();
        SecurityListType key = new TrendingBasicSecurityListType(1, 10);
        securityCompactListCache.register(key, securityListCacheListener);
        securityCompactListCache.getOrFetchAsync(key);
    }

    protected void detachSecurityListCache()
    {
        securityCompactListCache.unregister(securityListCacheListener);
    }

    protected DTOCacheNew.Listener<SecurityListType, SecurityCompactDTOList> createSecurityListCacheListener()
    {
        return new OnBoardPickStockCacheListener();
    }

    protected class OnBoardPickStockCacheListener implements DTOCacheNew.Listener<SecurityListType, SecurityCompactDTOList>
    {
        @Override public void onDTOReceived(@NotNull SecurityListType key, @NotNull SecurityCompactDTOList value)
        {
            viewHolder.setStocks(value);
        }

        @Override public void onErrorThrown(@NotNull SecurityListType key, @NotNull Throwable error)
        {
            THToast.show(R.string.error_fetch_security_list_info);
        }
    }

    public void doAddToWatchlist()
    {
        List<SecurityCompactDTO> selected = viewHolder.getSelectedStocks();
        for (SecurityCompactDTO securityCompactDTO : selected)
        {
            watchlistServiceWrapper.createWatchlistEntry(
                    new WatchlistPositionFormDTO(securityCompactDTO, 1), null);
        }
    }
}

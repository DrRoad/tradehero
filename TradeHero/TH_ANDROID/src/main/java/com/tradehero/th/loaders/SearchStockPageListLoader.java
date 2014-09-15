package com.tradehero.th.loaders;

import android.content.Context;
import com.tradehero.common.utils.THToast;
import com.tradehero.th2.R;
import com.tradehero.th.network.service.SecurityService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import retrofit.RetrofitError;

public class SearchStockPageListLoader extends PaginationListLoader<ListedSecurityCompact>
{
    @Inject SecurityService securityService;

    private String searchText;
    /**
     * Starts at 0
     */
    private int page;

    public SearchStockPageListLoader(Context context)
    {
        super(context);
    }

    @Override protected void onLoadPrevious(ListedSecurityCompact startItem)
    {
        page = getPageOfItem(startItem) - 1;
        forceLoad();
    }

    @Override protected void onLoadNext(ListedSecurityCompact lastItem)
    {
        page = getPageOfItem(lastItem) + 1;
        forceLoad();
    }

    /**
     *
     * @param item
     * @return page value from 0
     */
    private int getPageOfItem(ListedSecurityCompact item)
    {
        return item.id / getPerPage();
    }

    @Override protected boolean shouldReload()
    {
        return true;
        // TODO be cleverer
    }

    @Override public List<ListedSecurityCompact> loadInBackground()
    {
        List<ListedSecurityCompact> listed;
        try
        {
            listed = ListedSecurityCompactFactory.createList(
                    securityService.searchSecurities(searchText, page, getPerPage()),
                    page * getPerPage());
        }
        catch (RetrofitError retrofitError)
        {
            THToast.show(R.string.network_error);
            listed = new ArrayList<>();
            retrofitError.printStackTrace();
        }
        return listed;
    }
}

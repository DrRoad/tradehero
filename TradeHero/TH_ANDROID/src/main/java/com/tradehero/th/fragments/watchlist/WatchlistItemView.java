package com.tradehero.th.fragments.watchlist;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.graphics.WhiteToTransparentTransformation;
import com.tradehero.common.utils.THLog;
import com.tradehero.th.R;
import com.tradehero.th.adapters.OnSizeChangedListener;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIdList;
import com.tradehero.th.api.users.CurrentUserBaseKeyHolder;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.WatchlistService;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistPositionCache;
import com.tradehero.th.utils.DaggerUtils;
import dagger.Lazy;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import javax.inject.Inject;
import retrofit.Callback;

/**
 * Created with IntelliJ IDEA. User: tho Date: 1/10/14 Time: 4:40 PM Copyright (c) TradeHero
 */
public class WatchlistItemView extends FrameLayout implements DTOView<SecurityId>
{
    private static final String TAG = WatchlistItemView.class.getName();

    @Inject protected Lazy<WatchlistPositionCache> watchlistPositionCache;
    @Inject protected Lazy<UserWatchlistPositionCache> userWatchlistPositionCache;
    @Inject protected Lazy<WatchlistService> watchlistService;
    @Inject protected Lazy<Picasso> picasso;
    @Inject protected CurrentUserBaseKeyHolder currentUserBaseKeyHolder;

    private WeakReference<OnSizeChangedListener> weakAdapterOnSizeChangedListener = new WeakReference<>(null);

    private ImageView stockLogo;
    private TextView stockSymbol;
    private TextView companyName;
    private TextView numberOfShares;
    private WatchlistPositionDTO watchlistPositionDTO;
    private TextView gainLossLabel;
    private TextView positionLastAmount;
    private SecurityId securityId;
    private Button delete;

    //<editor-fold desc="Constructors">
    public WatchlistItemView(Context context)
    {
        super(context);
    }

    public WatchlistItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public WatchlistItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();

        init();
    }

    private void init()
    {
        DaggerUtils.inject(this);
        stockLogo = (ImageView) findViewById(R.id.stock_logo);
        stockSymbol = (TextView) findViewById(R.id.stock_symbol);
        companyName = (TextView) findViewById(R.id.company_name);
        numberOfShares = (TextView) findViewById(R.id.number_of_shares);
        gainLossLabel = (TextView) findViewById(R.id.position_percentage);
        positionLastAmount = (TextView) findViewById(R.id.position_last_amount);
        delete = (Button) findViewById(R.id.position_watchlist_delete);

        if (delete != null)
        {
            delete.setOnClickListener(watchlistItemDeleteHandler);
        }
    }

    @Override public void display(SecurityId securityId)
    {
        this.securityId = securityId;

        linkWith(securityId, true);
    }

    private void linkWith(SecurityId securityId, boolean andDisplay)
    {

        watchlistPositionDTO = watchlistPositionCache.get().get(securityId);

        if (watchlistPositionDTO == null)
        {
            return;
        }

        if (andDisplay)
        {
            displayStockLogo();

            displayExchangeSymbol();

            displayNumberOfShares();

            displayCompanyName();

            displayLastPrice();
        }
    }

    public void displayPlPercentage(boolean showInPercentage)
    {
        SecurityCompactDTO securityCompactDTO = watchlistPositionDTO.securityDTO;

        if (securityCompactDTO != null)
        {
            Double lastPrice = securityCompactDTO.lastPrice;
            Double watchlistPrice = watchlistPositionDTO.getWatchlistPrice();
            // pl percentage
            if (watchlistPrice != 0)
            {
                double gainLoss = (lastPrice - watchlistPrice);
                double pl = gainLoss * 100 / watchlistPrice;

                if (showInPercentage)
                {
                    gainLossLabel.setText(String.format(getContext().getString(R.string.watchlist_pl_percentage_format),
                            new DecimalFormat("##.##").format(pl)
                    ));
                }
                else
                {
                    gainLossLabel.setText(watchlistPositionDTO.securityDTO.currencyDisplay + " " +
                            new DecimalFormat("##.##").format(gainLoss));
                }

                if (pl > 0)
                {
                    gainLossLabel.setTextColor(getResources().getColor(R.color.number_green));
                }
                else if (pl < 0)
                {
                    gainLossLabel.setTextColor(getResources().getColor(R.color.number_red));
                }
                else
                {
                    gainLossLabel.setTextColor(getResources().getColor(R.color.text_gray_normal));
                }
            }
            else
            {
                gainLossLabel.setText("");
            }
        }
        else
        {
            gainLossLabel.setText("");
        }
    }

    public void setAdapterOnSizeChangedListener(OnSizeChangedListener listener)
    {
        weakAdapterOnSizeChangedListener = new WeakReference<>(listener);
    }

    private void displayLastPrice()
    {
        SecurityCompactDTO securityCompactDTO = watchlistPositionDTO.securityDTO;

        if (securityCompactDTO != null)
        {
            Double lastPrice = securityCompactDTO.lastPrice;
            Double watchlistPrice = watchlistPositionDTO.getWatchlistPrice();
            if (lastPrice == null)
            {
                lastPrice = 0.0;
            }
            // last price
            positionLastAmount.setText(formatLastPrice(securityCompactDTO.currencyDisplay, lastPrice));

            // pl percentage
            if (watchlistPrice != 0)
            {
                double pl = (lastPrice - watchlistPrice) * 100 / watchlistPrice;
                gainLossLabel.setText(String.format(getContext().getString(R.string.watchlist_pl_percentage_format),
                        new DecimalFormat("##.##").format(pl)
                ));

                if (pl > 0)
                {
                    gainLossLabel.setTextColor(getResources().getColor(R.color.number_green));
                }
                else if (pl < 0)
                {
                    gainLossLabel.setTextColor(getResources().getColor(R.color.number_red));
                }
                else
                {
                    gainLossLabel.setTextColor(getResources().getColor(R.color.text_gray_normal));
                }
            }
            else
            {
                gainLossLabel.setText("");
            }
        }
        else
        {
            gainLossLabel.setText("");
        }
    }

    private Spanned formatLastPrice(String currencyDisplay, Double lastPrice)
    {
        return Html.fromHtml(String.format(getContext().getString(R.string.watchlist_last_price_format),
                currencyDisplay,
                new DecimalFormat("#.##").format(lastPrice)));
    }

    private void displayNumberOfShares()
    {
        SecurityCompactDTO securityCompactDTO = watchlistPositionDTO.securityDTO;
        if (numberOfShares != null)
        {
            if (securityCompactDTO != null)
            {
                Double watchListPrice = watchlistPositionDTO.getWatchlistPrice();
                numberOfShares.setText(formatNumberOfShares(watchlistPositionDTO.shares, securityCompactDTO.currencyDisplay, watchListPrice));
            }
            else
            {
                numberOfShares.setText("");
            }
        }
    }

    private Spanned formatNumberOfShares(Integer shares, String currencyDisplay, Double formattedPrice)
    {
        if (formattedPrice == null)
        {
            formattedPrice = 0.0;
        }
        if (shares == null)
        {
            shares = 0;
        }
        return Html.fromHtml(String.format(
                getContext().getString(R.string.watchlist_number_of_shares),
                shares, currencyDisplay, formattedPrice
        ));
    }

    private void displayCompanyName()
    {
        SecurityCompactDTO securityCompactDTO = watchlistPositionDTO.securityDTO;
        if (companyName != null)
        {
            if (securityCompactDTO != null)
            {
                companyName.setText(securityCompactDTO.name);
            }
            else
            {
                companyName.setText("");
            }
        }
    }

    private void displayStockLogo()
    {
        SecurityCompactDTO securityCompactDTO = watchlistPositionDTO.securityDTO;

        if (stockLogo != null)
        {
            if (securityCompactDTO != null && securityCompactDTO.imageBlobUrl != null)
            {
                picasso.get()
                        .load(securityCompactDTO.imageBlobUrl)
                        .transform(new WhiteToTransparentTransformation())
                        .into(stockLogo);
            }
            else if (securityCompactDTO != null)
            {
                picasso.get()
                        .load(securityCompactDTO.getExchangeLogoId())
                        .into(stockLogo);
            }
            else
            {
                stockLogo.setImageResource(R.drawable.default_image);
            }
        }
    }

    private void displayExchangeSymbol()
    {
        SecurityCompactDTO securityCompactDTO = watchlistPositionDTO.securityDTO;

        if (stockSymbol != null)
        {
            if (securityCompactDTO != null)
            {
                stockSymbol.setText(securityCompactDTO.getExchangeSymbol());
            }
            else
            {
                stockSymbol.setText("");
            }
        }
    }


    private OnClickListener watchlistItemDeleteHandler = new OnClickListener()
    {
        @Override public void onClick(View v)
        {
            // remove current security from the watchlist
            SecurityIdList securityIds = userWatchlistPositionCache.get().get(currentUserBaseKeyHolder.getCurrentUserBaseKey());
            securityIds.remove(securityId);

            // not to show dialog but request deletion in background
            watchlistService.get().deleteWatchlist(watchlistPositionDTO.id, watchlistDeletionCallback);

            // notify adapter
            if (weakAdapterOnSizeChangedListener != null)
            {
                OnSizeChangedListener adapterOnSizeChangedListener = weakAdapterOnSizeChangedListener.get();
                if (adapterOnSizeChangedListener != null)
                {
                    adapterOnSizeChangedListener.onSizeChanged(securityIds.size());
                }
            }
        }
    };

    private Callback<WatchlistPositionDTO> watchlistDeletionCallback = new THCallback<WatchlistPositionDTO>()
    {
        @Override protected void success(WatchlistPositionDTO watchlistPositionDTO, THResponse thResponse)
        {
            if (watchlistPositionDTO != null)
            {
                THLog.d(TAG, String.format(getContext().getString(R.string.watchlist_item_deleted_successfully), watchlistPositionDTO.id));
            }
        }

        @Override protected void failure(THException ex)
        {
            if (watchlistPositionDTO != null)
            {
                THLog.e(TAG, String.format(getContext().getString(R.string.watchlist_item_deleted_failed), watchlistPositionDTO.id), ex);
            }
        }
    };
}

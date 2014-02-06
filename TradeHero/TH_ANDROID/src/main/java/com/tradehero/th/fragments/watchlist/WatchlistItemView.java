package com.tradehero.th.fragments.watchlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.graphics.WhiteToTransparentTransformation;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.api.security.SecurityIdList;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.watchlist.WatchlistPositionDTO;
import com.tradehero.th.base.Navigator;
import com.tradehero.th.base.NavigatorActivity;
import com.tradehero.th.fragments.alert.AlertEditFragment;
import com.tradehero.th.fragments.security.StockInfoFragment;
import com.tradehero.th.fragments.security.WatchlistEditFragment;
import com.tradehero.th.fragments.trade.BuySellFragment;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.WatchlistService;
import com.tradehero.th.persistence.watchlist.UserWatchlistPositionCache;
import com.tradehero.th.persistence.watchlist.WatchlistPositionCache;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.THSignedNumber;
import dagger.Lazy;
import java.text.DecimalFormat;
import javax.inject.Inject;
import retrofit.Callback;

/**
 * Created with IntelliJ IDEA. User: tho Date: 1/10/14 Time: 4:40 PM Copyright (c) TradeHero
 */
public class WatchlistItemView extends FrameLayout implements DTOView<SecurityId>
{
    private static final String TAG = WatchlistItemView.class.getName();
    public static final String WATCHLIST_ITEM_DELETED = "watchlistItemDeleted";
    public static final String BUNDLE_KEY_WATCHLIST_ITEM_INDEX = "watchlistItemId";

    @Inject protected Lazy<WatchlistPositionCache> watchlistPositionCache;
    @Inject protected Lazy<UserWatchlistPositionCache> userWatchlistPositionCache;
    @Inject protected Lazy<WatchlistService> watchlistService;
    @Inject protected Lazy<Picasso> picasso;
    @Inject protected CurrentUserId currentUserId;

    private ImageView stockLogo;
    private TextView stockSymbol;
    private TextView companyName;
    private TextView numberOfShares;
    private WatchlistPositionDTO watchlistPositionDTO;
    private TextView gainLossLabel;
    private TextView positionLastAmount;
    private SecurityId securityId;
    private Button deleteButton;
    private PopupMenu morePopupMenu;

    private Button moreButton;

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

        deleteButton = (Button) findViewById(R.id.position_watchlist_delete);
        if (deleteButton != null)
        {
            deleteButton.setOnClickListener(watchlistItemDeleteClickHandler);
        }

        moreButton = (Button) findViewById(R.id.position_watchlist_more);
        if (moreButton != null)
        {
            moreButton.setOnClickListener(watchlistItemMoreButtonClickHandler);
        }
    }

    @Override protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (deleteButton != null)
        {
            deleteButton.setOnClickListener(null);
        }

        if (moreButton != null)
        {
            moreButton.setOnClickListener(null);
        }

        if (morePopupMenu != null)
        {
            morePopupMenu.setOnMenuItemClickListener(null);
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

        THSignedNumber thSignedNumber = new THSignedNumber(THSignedNumber.TYPE_MONEY, formattedPrice, false, currencyDisplay);
        return Html.fromHtml(String.format(
                getContext().getString(R.string.watchlist_number_of_shares),
                shares, thSignedNumber.toString()
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


    private PopupMenu createMoreOptionsPopupMenu()
    {
        PopupMenu popupMenu = new PopupMenu(getContext(), moreButton);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.watchlist_more_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(moreButtonPopupMenuClickHandler);
        return popupMenu;
    }

    private OnClickListener watchlistItemDeleteClickHandler = new OnClickListener()
    {
        @Override public void onClick(View v)
        {
            // remove current security from the watchlist
            SecurityIdList securityIds = userWatchlistPositionCache.get().get(currentUserId.toUserBaseKey());

            // not to show dialog but request deletion in background
            watchlistService.get().deleteWatchlist(watchlistPositionDTO.id, watchlistDeletionCallback);

            Intent itemDeletionIntent = new Intent(WatchlistItemView.WATCHLIST_ITEM_DELETED);
            itemDeletionIntent.putExtra(WatchlistItemView.BUNDLE_KEY_WATCHLIST_ITEM_INDEX, securityIds.indexOf(securityId));
            LocalBroadcastManager.getInstance(WatchlistItemView.this.getContext())
                    .sendBroadcast(itemDeletionIntent);
            securityIds.remove(securityId);
            watchlistPositionCache.get().invalidate(securityId);
        }
    };
    private OnClickListener watchlistItemMoreButtonClickHandler = new OnClickListener()
    {
        @Override public void onClick(View v)
        {
            if (morePopupMenu == null)
            {
                morePopupMenu = createMoreOptionsPopupMenu();
            }
            morePopupMenu.show();
        }
    };

    private PopupMenu.OnMenuItemClickListener moreButtonPopupMenuClickHandler = new PopupMenu.OnMenuItemClickListener()
    {
        @Override public boolean onMenuItemClick(MenuItem item)
        {
            if (item == null)
            {
                return false;
            }
            switch (item.getItemId())
            {
                case R.id.watchlist_item_add_alert:
                    openAlertEditor();
                    break;
                case R.id.watchlist_item_edit_in_watchlist:
                    openWatchlistEditor();
                    break;
                case R.id.watchlist_item_new_discussion:
                    THToast.show(getContext().getString(R.string.not_yet_implemented));
                    break;
                case R.id.watchlist_item_view_graph:
                    openSecurityGraph();
                    break;
                case R.id.watchlist_item_trade:
                    openSecurityProfile();
                    break;
            }
            return true;
        }
    };

    private void openAlertEditor()
    {
        Bundle args = new Bundle();
        args.putBundle(AlertEditFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
        getNavigator().pushFragment(AlertEditFragment.class, args);
    }

    private void openSecurityProfile()
    {
        Bundle args = new Bundle();
        args.putBundle(BuySellFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
        getNavigator().pushFragment(BuySellFragment.class, args);
    }

    private void openSecurityGraph()
    {
        Bundle args = new Bundle();
        if (securityId != null)
        {
            args.putBundle(StockInfoFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
        }
        getNavigator().pushFragment(StockInfoFragment.class, args);
    }

    private void openWatchlistEditor()
    {
        Bundle args = new Bundle();
        if (securityId != null)
        {
            args.putBundle(WatchlistEditFragment.BUNDLE_KEY_SECURITY_ID_BUNDLE, securityId.getArgs());
            args.putString(WatchlistEditFragment.BUNDLE_KEY_TITLE, getContext().getString(R.string.edit_in_watch_list));
        }
        getNavigator().pushFragment(WatchlistEditFragment.class, args, Navigator.PUSH_UP_FROM_BOTTOM);
    }

    private Navigator getNavigator()
    {
        return ((NavigatorActivity) getContext()).getNavigator();
    }

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

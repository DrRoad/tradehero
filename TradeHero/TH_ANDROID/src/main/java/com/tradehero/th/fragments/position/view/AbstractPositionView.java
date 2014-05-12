package com.tradehero.th.fragments.position.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.tradehero.common.widget.ColorIndicator;
import com.tradehero.th.R;
import com.tradehero.th.adapters.ExpandableListItem;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.fragments.position.PositionListener;
import com.tradehero.th.fragments.position.partial.AbstractPartialBottomView;
import com.tradehero.th.fragments.position.partial.PositionPartialTopView;
import timber.log.Timber;

public abstract class AbstractPositionView<
            PositionDTOType extends PositionDTO,
            ExpandableListItemType extends ExpandableListItem<PositionDTOType>>
        extends LinearLayout
{
    protected PositionPartialTopView topView;
    protected AbstractPartialBottomView<PositionDTOType, ExpandableListItemType> bottomView;

    protected ColorIndicator colorIndicator;
    protected View btnBuy;
    protected View btnSell;
    protected View btnAddAlert;
    protected View btnStockInfo;
    protected View historyButton;

    protected boolean hasHistoryButton = true;
    protected ExpandableListItemType expandableListItem;
    protected PositionDTOType positionDTO;

    protected PositionListener<PositionDTOType> listener = null;

    //<editor-fold desc="Constructors">
    public AbstractPositionView(Context context)
    {
        super(context);
    }

    public AbstractPositionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AbstractPositionView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        initViews();
    }

    protected void initViews()
    {
        colorIndicator = (ColorIndicator) findViewById(R.id.color_indicator);
        btnBuy = findViewById(R.id.btn_buy_now);
        btnSell = findViewById(R.id.btn_sell_now);
        btnAddAlert = findViewById(R.id.btn_add_alert);
        btnStockInfo = findViewById(R.id.btn_stock_info);

        topView = (PositionPartialTopView) findViewById(R.id.position_partial_top);
        if (topView != null)
        {
            historyButton = topView.getTradeHistoryButton();
        }
        bottomView = (AbstractPartialBottomView) findViewById(R.id.expanding_layout);
    }

    @Override protected void onAttachedToWindow()
    {
        if (btnBuy != null)
        {
            btnBuy.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    PositionListener<PositionDTOType> listenerCopy = listener;
                    if (listenerCopy != null)
                    {
                        listenerCopy.onBuyClicked(getPositionDTO());
                    }
                }
            });
        }

        if (btnSell != null)
        {
            btnSell.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    PositionListener<PositionDTOType> listenerCopy = listener;
                    if (listenerCopy != null)
                    {
                        listenerCopy.onSellClicked(getPositionDTO());
                    }
                }
            });
        }

        if (btnAddAlert != null)
        {
            btnAddAlert.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    PositionListener<PositionDTOType> listenerCopy = listener;
                    if (listenerCopy != null)
                    {
                        listenerCopy.onAddAlertClicked(getPositionDTO());
                    }
                }
            });
        }

        if (btnStockInfo != null)
        {
            btnStockInfo.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    PositionListener<PositionDTOType> listenerCopy = listener;
                    if (listenerCopy != null)
                    {
                        listenerCopy.onStockInfoClicked(getPositionDTO());
                    }
                }
            });
        }

        if (historyButton != null)
        {
            historyButton.setOnClickListener(new OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    PositionListener<PositionDTOType> listenerCopy = listener;
                    if (listenerCopy != null)
                    {
                        listenerCopy.onTradeHistoryClicked(getPositionDTO());
                    }
                }
            });
        }
        super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow()
    {
        if (btnBuy != null)
        {
            btnBuy.setOnClickListener(null);
        }
        btnBuy = null;

        if (btnSell != null)
        {
            btnSell.setOnClickListener(null);
        }
        btnSell = null;

        if (btnAddAlert != null)
        {
            btnAddAlert.setOnClickListener(null);
        }
        btnAddAlert = null;

        if (btnStockInfo != null)
        {
            btnStockInfo.setOnClickListener(null);
        }
        btnStockInfo = null;

        if (historyButton != null)
        {
            historyButton.setOnClickListener(null);
        }
        historyButton = null;

        this.listener = null;

        super.onDetachedFromWindow();
    }

    public void linkWithHasHistoryButton(boolean hasHistoryButton, boolean andDisplay)
    {
        this.hasHistoryButton = hasHistoryButton;
        if (andDisplay)
        {
            displayHistoryButton();
        }
    }

    public void linkWith(ExpandableListItemType expandableListItem, boolean andDisplay)
    {
        this.expandableListItem = expandableListItem;
        linkWith(expandableListItem == null ? null : expandableListItem.getModel(), andDisplay);
        if (bottomView != null)
        {
            this.bottomView.linkWith(expandableListItem, andDisplay);
        }
        if (andDisplay)
        {
            displayExpandingPart();
        }
    }

    public void linkWith(PositionDTOType positionDTO, boolean andDisplay)
    {
        this.positionDTO = positionDTO;

        if (this.topView != null)
        {
            this.topView.linkWith(positionDTO, andDisplay);
        }
        if (this.bottomView != null)
        {
            this.bottomView.linkWith(positionDTO, andDisplay);
        }

        if (andDisplay)
        {
            displayModelPart();
        }
    }

    public PositionDTOType getPositionDTO()
    {
        Timber.d("getPositionDTO %s", positionDTO);
        Timber.d("getPositionDTO %s", positionDTO.getOwnedPositionId());
        return positionDTO;
    }

    public void display()
    {
        displayTopView();
        displayBottomView();
        displayModelPart();
        displayExpandingPart();
        displayHistoryButton();
    }

    public void displayModelPart()
    {
        displayColorIndicator();
        displayButtonSell();
    }

    public void displayExpandingPart()
    {
    }

    public void displayTopView()
    {
        if (topView != null)
        {
            topView.display();
        }
    }

    public void displayBottomView()
    {
        if (bottomView != null)
        {
            bottomView.display();
        }
    }

    protected void displayColorIndicator()
    {
        if (colorIndicator != null && positionDTO != null)
        {
            Double roi = positionDTO.getROISinceInception();
            colorIndicator.linkWith(roi);
        }
    }

    protected void displayButtonSell()
    {
        if (btnSell != null)
        {
            btnSell.setVisibility(this.positionDTO == null || this.positionDTO.isClosed() ? GONE : VISIBLE);
        }
    }

    protected void displayHistoryButton()
    {
        if (historyButton != null)
        {
            historyButton.setVisibility(hasHistoryButton ? VISIBLE : GONE);
        }
    }

    public PositionListener<PositionDTOType> getListener()
    {
        return listener;
    }

    public void setListener(PositionListener<PositionDTOType> listener)
    {
        this.listener = listener;
    }
}

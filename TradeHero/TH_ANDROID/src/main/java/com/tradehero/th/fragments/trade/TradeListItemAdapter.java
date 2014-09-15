package com.tradehero.th.fragments.trade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th2.R;
import com.tradehero.th.adapters.ExpandableDTOAdapter;
import com.tradehero.th.adapters.ExpandableListItem;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.position.PositionInPeriodDTO;
import com.tradehero.th.fragments.position.view.PositionView;
import com.tradehero.th.fragments.trade.view.TradeListItemView;
import com.tradehero.th.widget.list.BaseListHeaderView;
import java.util.ArrayList;
import java.util.List;

public class TradeListItemAdapter
        extends ExpandableDTOAdapter<
            PositionTradeDTOKey,
            TradeListItemAdapter.ExpandableTradeItem,
            TradeListItemView>
{
    public static final int ITEM_TYPE_HEADER_POSITION_SUMMARY = 0;
    public static final int ITEM_TYPE_POSITION_SUMMARY = 1;
    public static final int ITEM_TYPE_HEADER_TRADE_HISTORY = 2;
    public static final int ITEM_TYPE_TRADE = 3;

    public static final int LAYOUT_RES_ID_ITEM_HEADER = R.layout.trade_list_item_header;
    public static final int LAYOUT_RES_ID_ITEM_TRADE = R.layout.trade_list_item;

    public static final int LAYOUT_RES_ID_POSITION_OPEN = R.layout.position_open_no_period;
    public static final int LAYOUT_RES_ID_POSITION_CLOSED = R.layout.position_closed_no_period;
    public static final int LAYOUT_RES_ID_POSITION_IN_PERIOD_OPEN = R.layout.position_open_in_period;
    public static final int LAYOUT_RES_ID_POSITION_IN_PERIOD_CLOSED = R.layout.position_closed_in_period;

    private List<Integer> itemTypes;
    private List<Object> objects;

    protected PositionDTO shownPositionDTO;

    public TradeListItemAdapter(final Context context, final LayoutInflater inflater)
    {
        super(context, inflater, LAYOUT_RES_ID_ITEM_TRADE);
        this.itemTypes = new ArrayList<>();
        this.objects = new ArrayList<>();
    }

    @Override public void setUnderlyingItems(final List<PositionTradeDTOKey> underlyingItems)
    {
        super.setUnderlyingItems(underlyingItems);

        List<Integer> itemTypesTemp = new ArrayList<>();
        List<Object> objectsTemp = new ArrayList<>();
        if (underlyingItems == null)
        {
            this.items = null;
        }
        else
        {
            this.items = new ArrayList<>(underlyingItems.size());
            int i = 0;
            for (final PositionTradeDTOKey id : underlyingItems)
            {
                ExpandableTradeItem item = new ExpandableTradeItem(id, i == 0);
                item.setExpanded(i == 0);
                items.add(item);
                ++i;
            }

            itemTypesTemp.add(ITEM_TYPE_HEADER_POSITION_SUMMARY);
            if (this.shownPositionDTO.isClosed())
            {
                objectsTemp.add(R.string.trade_list_header_closed_summary);
            }
            else
            {
                objectsTemp.add(R.string.trade_list_header_open_summary);
            }

            itemTypesTemp.add(ITEM_TYPE_POSITION_SUMMARY);
            objectsTemp.add(this.shownPositionDTO);

            itemTypesTemp.add(ITEM_TYPE_HEADER_TRADE_HISTORY);
            objectsTemp.add(R.string.trade_list_header_history);

            for (Object item : items)
            {
                itemTypesTemp.add(ITEM_TYPE_TRADE);
                objectsTemp.add(item);
            }
        }
        this.itemTypes = itemTypesTemp;
        this.objects = objectsTemp;
        notifyDataSetChanged();
    }

    @Override protected void fineTune(int position, ExpandableTradeItem dto, TradeListItemView convertView)
    {
    }

    @Override protected ExpandableTradeItem wrap(final PositionTradeDTOKey underlyingItem)
    {
        return new ExpandableTradeItem(underlyingItem);
    }

    public void setShownPositionDTO(PositionDTO positionDTO)
    {
        this.shownPositionDTO = positionDTO;
    }

    @Override public int getViewTypeCount()
    {
        return 5;
    }

    @Override public int getCount()
    {
        return itemTypes.size();
    }

    @Override public int getItemViewType(int position)
    {
        return itemTypes.get(position);
    }

    @Override public Object getItem(int position)
    {
        return objects.get(position);
    }

    public int getPositionLayoutResId()
    {
        boolean isClosed = this.shownPositionDTO.isClosed();
        if (isClosed && this.shownPositionDTO instanceof PositionInPeriodDTO)
        {
            return LAYOUT_RES_ID_POSITION_IN_PERIOD_CLOSED;
        }
        else if (isClosed)
        {
            return LAYOUT_RES_ID_POSITION_CLOSED;
        }
        else if (this.shownPositionDTO instanceof PositionInPeriodDTO)
        {
            return LAYOUT_RES_ID_POSITION_IN_PERIOD_OPEN;
        }
        else
        {
            return LAYOUT_RES_ID_POSITION_OPEN;
        }
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        int itemType = getItemViewType(position);
        Object item = getItem(position);

        switch (itemType)
        {
            case ITEM_TYPE_HEADER_POSITION_SUMMARY:
                if (convertView == null)
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_ITEM_HEADER, viewGroup, false);
                }
                ((BaseListHeaderView) convertView).setHeaderTextContent((int) item);
                break;

            case ITEM_TYPE_POSITION_SUMMARY:
                if (convertView == null)
                {
                    convertView = inflater.inflate(getPositionLayoutResId(), viewGroup, false);
                }

                ((PositionView) convertView).linkWith(this.shownPositionDTO, false);
                ((PositionView) convertView).linkWithHasHistoryButton(false, false);
                ((PositionView) convertView).display();
                View buttons = convertView.findViewById(R.id.position_shortcuts);
                if (buttons != null)
                {
                    buttons.setVisibility(View.GONE);
                }
                break;

            case ITEM_TYPE_HEADER_TRADE_HISTORY:
                if (convertView == null)
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_ITEM_HEADER, viewGroup, false);
                }
                ((BaseListHeaderView) convertView).setHeaderTextContent((int) item);
                break;

            case ITEM_TYPE_TRADE:
                if (convertView == null)
                {
                    convertView = inflater.inflate(layoutResourceId, viewGroup, false);
                }
                ((TradeListItemView) convertView).display((ExpandableTradeItem) item);
                toggleExpanded((ExpandableTradeItem) item, convertView);
                break;

            default:
                throw new IllegalStateException("Unknown ItemType " + itemType);
        }
        return convertView;
    }

    @Override public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override public boolean isEnabled(int position)
    {
        int itemType = getItemViewType(position);
        switch (itemType)
        {
            case ITEM_TYPE_HEADER_POSITION_SUMMARY:
            case ITEM_TYPE_POSITION_SUMMARY:
            case ITEM_TYPE_HEADER_TRADE_HISTORY:
                return false;

            case ITEM_TYPE_TRADE:
                return true;

            default:
                throw new IllegalStateException("Unknown Item Type " + itemType);
        }
    }

    public static class ExpandableTradeItem extends ExpandableListItem<PositionTradeDTOKey>
    {
        private final boolean lastTrade;

        //<editor-fold desc="Constructors">
        public ExpandableTradeItem(final PositionTradeDTOKey key)
        {
            this(key, false);
        }

        public ExpandableTradeItem(final PositionTradeDTOKey key, final boolean lastTrade)
        {
            super(key);
            this.lastTrade = lastTrade;
        }
        //</editor-fold>

        public boolean isLastTrade()
        {
            return this.lastTrade;
        }
    }
}

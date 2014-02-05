package com.tradehero.th.fragments.trade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th.R;
import com.tradehero.th.adapters.ExpandableDTOAdapter;
import com.tradehero.th.adapters.ExpandableListItem;
import com.tradehero.th.api.position.OwnedPositionId;
import com.tradehero.th.api.position.PositionDTO;
import com.tradehero.th.api.trade.OwnedTradeId;
import com.tradehero.th.fragments.position.view.AbstractPositionView;
import com.tradehero.th.fragments.position.view.PositionClosedView;
import com.tradehero.th.fragments.position.view.PositionOpenView;
import com.tradehero.th.persistence.position.PositionCache;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.widget.list.BaseListHeaderView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by julien on 23/10/13
 */
public class TradeListItemAdapter extends ExpandableDTOAdapter<OwnedTradeId, TradeListItemAdapter.ExpandableTradeItem, TradeListItemView>
{
    public static final String TAG = TradeListItemAdapter.class.getName();

    public static final int ITEM_TYPE_BUTTONS = 0;
    public static final int ITEM_TYPE_HEADER_POSITION_SUMMARY = 1;
    public static final int ITEM_TYPE_POSITION_SUMMARY = 2;
    public static final int ITEM_TYPE_HEADER_TRADE_HISTORY = 3;
    public static final int ITEM_TYPE_TRADE = 4;

    public static final int LAYOUT_RES_ID_BUTTONS = R.layout.trade_list_header;
    public static final int LAYOUT_RES_ID_ITEM_HEADER = R.layout.trade_list_item_header;
    public static final int LAYOUT_RES_ID_POSITION_OPEN = R.layout.position_open_no_period;
    public static final int LAYOUT_RES_ID_POSITION_CLOSED = R.layout.position_closed_no_period;
    public static final int LAYOUT_RES_ID_ITEM_TRADE = R.layout.trade_list_item;

    @Inject PositionCache positionCache;

    private List<Integer> itemTypes;
    private List<Object> objects;

    private OwnedPositionId shownPositionId;
    private PositionDTO shownPositionDTO;
    private WeakReference<TradeListHeaderView.TradeListHeaderClickListener> parentTradeListHeaderClickListenerWeak = new WeakReference<>(null);

    public TradeListItemAdapter(final Context context, final LayoutInflater inflater)
    {
        super(context, inflater, LAYOUT_RES_ID_ITEM_TRADE);
        this.itemTypes = new ArrayList<>();
        this.objects = new ArrayList<>();
        DaggerUtils.inject(this);
    }

    @Override public void setUnderlyingItems(final List<OwnedTradeId> underlyingItems)
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
            for (final OwnedTradeId id : underlyingItems)
            {
                ExpandableTradeItem item = new ExpandableTradeItem(id, i == 0);
                item.setExpanded(i == 0);
                items.add(item);
                ++i;
            }

            itemTypesTemp.add(ITEM_TYPE_BUTTONS);
            objectsTemp.add("Buttons");

            itemTypesTemp.add(ITEM_TYPE_HEADER_POSITION_SUMMARY);
            if (this.shownPositionDTO.isClosed())
            {
                objectsTemp.add(R.string.position_list_header_closed_summary);
            }
            else
            {
                objectsTemp.add(R.string.position_list_header_open_summary);
            }

            itemTypesTemp.add(ITEM_TYPE_POSITION_SUMMARY);
            objectsTemp.add(this.shownPositionId);

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
    }

    @Override protected void fineTune(int position, ExpandableTradeItem dto, TradeListItemView convertView)
    {
    }

    @Override protected ExpandableTradeItem wrap(final OwnedTradeId underlyingItem)
    {
        return new ExpandableTradeItem(underlyingItem);
    }

    public OwnedPositionId getShownPositionId()
    {
        return shownPositionId;
    }

    public void setShownPositionId(OwnedPositionId shownPositionId)
    {
        this.shownPositionId = shownPositionId;
        this.shownPositionDTO = positionCache.get(shownPositionId);
    }

    /**
     * The listener should be strongly referenced elsewhere
     * @param tradeListHeaderClickListener
     */
    public void setTradeListHeaderClickListener(TradeListHeaderView.TradeListHeaderClickListener tradeListHeaderClickListener)
    {
        this.parentTradeListHeaderClickListenerWeak = new WeakReference<>(tradeListHeaderClickListener);
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
        if (position < itemTypes.size())
        {
            return itemTypes.get(position);
        }
        return ITEM_TYPE_BUTTONS;
    }

    @Override public Object getItem(int position)
    {
        if (position < objects.size())
        {
            return objects.get(position);
        }
        return "Buttons";
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        int itemType = getItemViewType(position);
        Object item = getItem(position);

        switch (itemType)
        {
            case ITEM_TYPE_BUTTONS:
                if (!(convertView instanceof TradeListHeaderView))
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_BUTTONS, viewGroup, false);
                }
                ((TradeListHeaderView) convertView).setListener(parentTradeListHeaderClickListenerWeak.get());
                ((TradeListHeaderView) convertView).bindOwnedPositionId(shownPositionId);
                break;

            case ITEM_TYPE_HEADER_POSITION_SUMMARY:
                if (!(convertView instanceof BaseListHeaderView))
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_ITEM_HEADER, viewGroup, false);
                }
                ((BaseListHeaderView) convertView).setHeaderTextContent((int) item);
                break;

            case ITEM_TYPE_POSITION_SUMMARY:
                if (!this.shownPositionDTO.isClosed() && !(convertView instanceof PositionOpenView))
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_POSITION_OPEN, viewGroup, false);
                }
                else if (this.shownPositionDTO.isClosed() && !(convertView instanceof PositionClosedView))
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_POSITION_CLOSED, viewGroup, false);
                }

                ((AbstractPositionView) convertView).linkWith(this.shownPositionDTO, true);
                ((AbstractPositionView) convertView).linkWithHasHistoryButton(false, true);
                View buttons = convertView.findViewById(R.id.position_shortcuts);
                if (buttons != null)
                {
                    buttons.setVisibility(View.GONE);
                }
                break;

            case ITEM_TYPE_HEADER_TRADE_HISTORY:
                if (!(convertView instanceof BaseListHeaderView))
                {
                    convertView = inflater.inflate(LAYOUT_RES_ID_ITEM_HEADER, viewGroup, false);
                }
                ((BaseListHeaderView) convertView).setHeaderTextContent((int) item);
                break;

            case ITEM_TYPE_TRADE:
                if (!(convertView instanceof TradeListItemView))
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
            case ITEM_TYPE_BUTTONS:
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

    public static class ExpandableTradeItem extends ExpandableListItem<OwnedTradeId>
    {
        private boolean lastTrade;

        public ExpandableTradeItem(final OwnedTradeId model)
        {
            this(model, false);
        }

        public ExpandableTradeItem(final OwnedTradeId model, final boolean lastTrade)
        {
            super(model);
            this.lastTrade = lastTrade;
        }

        public boolean isLastTrade()
        {
            return this.lastTrade;
        }
    }
}

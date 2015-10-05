package com.tradehero.th.fragments.position;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tradehero.common.graphics.WhiteToTransparentTransformation;
import com.tradehero.th.R;
import com.tradehero.th.adapters.TypedRecyclerAdapter;
import com.tradehero.th.api.position.PositionStatus;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.position.partial.PositionDisplayDTO;
import com.tradehero.th.fragments.position.partial.PositionPartialTopView;
import com.tradehero.th.fragments.position.view.PositionLockedView;
import com.tradehero.th.fragments.position.view.PositionNothingView;
import com.tradehero.th.inject.HierarchyInjector;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class PositionItemAdapter extends TypedRecyclerAdapter<Object>
{
    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_PLACEHOLDER = 1;
    public static final int VIEW_TYPE_LOCKED = 2;
    public static final int VIEW_TYPE_POSITION = 3;

    protected Map<Integer, Integer> itemTypeToLayoutId;
    private UserProfileDTO userProfileDTO;

    @NonNull protected final CurrentUserId currentUserId;
    @NonNull protected final PublishSubject<PositionPartialTopView.CloseUserAction> userActionSubject;
    @Inject Picasso picasso;

    //<editor-fold desc="Constructors">
    public PositionItemAdapter(
            Context context,
            @NonNull Map<Integer, Integer> itemTypeToLayoutId,
            @NonNull CurrentUserId currentUserId)
    {
        super(Object.class, new PositionItemComparator());
        this.currentUserId = currentUserId;
        this.userActionSubject = PublishSubject.create();
        this.itemTypeToLayoutId = itemTypeToLayoutId;
        setHasStableIds(true);
        HierarchyInjector.inject(context, this);
    }
    //</editor-fold>

    @Override public int getItemViewType(int position)
    {
        Object item = getItem(position);
        if (item instanceof PositionLockedView.DTO)
        {
            return VIEW_TYPE_LOCKED;
        }
        else if (item instanceof PositionDisplayDTO)
        {
            return VIEW_TYPE_POSITION;
        }
        else if (item instanceof PositionNothingView.DTO)
        {
            return VIEW_TYPE_PLACEHOLDER;
        }
        else if (item instanceof PositionSectionHeaderDisplayDTO)
        {
            return VIEW_TYPE_HEADER;
        }
        throw new IllegalArgumentException("Unhandled item " + item);
    }

    public boolean isEnabled(int position)
    {
        int viewType = getItemViewType(position);
        return viewType != VIEW_TYPE_HEADER
                && (viewType != VIEW_TYPE_PLACEHOLDER
                || userProfileDTO == null
                || userProfileDTO.getBaseKey().equals(currentUserId.toUserBaseKey()));
    }

    protected int getLayoutForViewType(int viewType)
    {
        return itemTypeToLayoutId.get(viewType);
    }

    @Override public long getItemId(int position)
    {
        Object item = getItem(position);
        return item == null ? 0 : item.hashCode();
    }

    public void linkWith(UserProfileDTO userProfileDTO)
    {
        this.userProfileDTO = userProfileDTO;
    }

    @NonNull public Observable<PositionPartialTopView.CloseUserAction> getUserActionObservable()
    {
        return userActionSubject.asObservable();
    }

    @Override public int add(@NonNull Object o)
    {
        updateLatestTradeOrRemove(o);
        return super.add(o);
    }

    @Override protected void addAllForBatch(@NonNull Collection<Object> collection)
    {
        for (Object o : collection)
        {
            updateLatestTradeOrRemove(o);
        }
        super.addAllForBatch(collection);
    }

    // TODO find a way to move the calculations in another thread
    private void updateLatestTradeOrRemove(@NonNull Object o)
    {
        if (mSortedList.size() > 0)
        {
            Object item;
            boolean areSame;
            for (int index = 0; index < mSortedList.size(); index++)
            {
                item = mSortedList.get(index);
                areSame = mComparator.areItemsTheSame(o, item);
                if (areSame && o instanceof PositionDisplayDTO)
                {
                    ((PositionDisplayDTO) item).positionDTO.latestTradeUtc = ((PositionDisplayDTO) o).positionDTO.latestTradeUtc;
                    mSortedList.recalculatePositionOfItemAt(index);
                }
                else
                {
                    mSortedList.removeItemAt(index);
                }
            }
        }
    }

    @NonNull @Override public TypedViewHolder<Object> onCreateViewHolder(ViewGroup parent, int viewType)
    {
        int layoutToInflate = getLayoutForViewType(viewType);
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutToInflate, parent, false);

        if (viewType == VIEW_TYPE_LOCKED)
        {
            return new PositionLockedView.ViewHolder((PositionLockedView) v);
        }
        else if (viewType == VIEW_TYPE_PLACEHOLDER)
        {
            return new PositionNothingView.ViewHolder((PositionNothingView) v);
        }
        else if (viewType == VIEW_TYPE_HEADER)
        {
            return new SectionHeaderViewHolder(v);
        }
        else if (viewType == VIEW_TYPE_POSITION)
        {
            return new PositionDisplayDTOViewHolder(v, picasso);
        }
        return null;
    }

    @Override public void onBindViewHolder(TypedViewHolder<Object> holder, int position)
    {
        super.onBindViewHolder(holder, position);
        if (!isEnabled(position))
        {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setOnLongClickListener(null);
        }
        if (holder instanceof PositionPartialTopView.ViewHolder)
        {
            ((PositionPartialTopView.ViewHolder) holder).getUserActionObservable().subscribe(userActionSubject);
        }
    }

    private static class PositionItemComparator extends TypedRecyclerComparator<Object>
    {
        @NonNull private final Comparator<PositionStatus> positionStatusComparator;
        @NonNull private final Comparator<PositionDisplayDTO> topViewComparator;

        public PositionItemComparator()
        {
            this.positionStatusComparator = new PositionStatus.StatusComparator();
            this.topViewComparator = new PositionPartialTopView.AscendingLatestTradeDateComparator();
        }

        @Override public int compare(Object o1, Object o2)
        {
            if (o1 instanceof PositionNothingView.DTO && o2 instanceof PositionNothingView.DTO)
            {
                return 0;
            }
            if (o1 instanceof PositionNothingView.DTO)
            {
                return -1;
            }
            if (o2 instanceof PositionNothingView.DTO)
            {
                return 1;
            }
            else if (o1 instanceof PositionLockedView.DTO && o2 instanceof PositionLockedView.DTO)
            {
                return Integer.valueOf(((PositionLockedView.DTO) o1).id).compareTo(((PositionLockedView.DTO) o2).id);
            }
            else if (o1 instanceof PositionLockedView.DTO)
            {
                return -1;
            }
            else if (o2 instanceof PositionLockedView.DTO)
            {
                return 1;
            }
            else if (o1 instanceof PositionSectionHeaderDisplayDTO && o2 instanceof PositionSectionHeaderDisplayDTO)
            {
                return positionStatusComparator.compare(
                        ((PositionSectionHeaderDisplayDTO) o1).status,
                        ((PositionSectionHeaderDisplayDTO) o2).status);
            }
            else if (o1 instanceof PositionDisplayDTO && o2 instanceof PositionDisplayDTO)
            {
                int comp = positionStatusComparator.compare(((PositionDisplayDTO) o1).positionDTO.positionStatus,
                        ((PositionDisplayDTO) o2).positionDTO.positionStatus);
                if (comp == 0)
                {
                    comp = topViewComparator.compare((PositionDisplayDTO) o1, (PositionDisplayDTO) o2);
                }
                return comp;
            }
            else if (o1 instanceof PositionDisplayDTO && o2 instanceof PositionSectionHeaderDisplayDTO)
            {
                PositionStatus s1 = ((PositionDisplayDTO) o1).positionDTO.positionStatus;
                PositionStatus s2 = ((PositionSectionHeaderDisplayDTO) o2).status;
                if (s1 == null || s1.equals(s2))
                {
                    return 1;
                }
                return positionStatusComparator.compare(
                        ((PositionDisplayDTO) o1).positionDTO.positionStatus,
                        ((PositionSectionHeaderDisplayDTO) o2).status);
            }
            else if (o1 instanceof PositionSectionHeaderDisplayDTO && o2 instanceof PositionDisplayDTO)
            {
                if (((PositionSectionHeaderDisplayDTO) o1).status.equals(((PositionDisplayDTO) o2).positionDTO.positionStatus))
                {
                    return -1;
                }
                return positionStatusComparator.compare(
                        ((PositionSectionHeaderDisplayDTO) o1).status,
                        ((PositionDisplayDTO) o2).positionDTO.positionStatus);
            }
            else
            {
                Timber.e(new IllegalStateException(), "Unhandled  " + o1.getClass() + " " + o2.getClass());
                throw new IllegalStateException("Unhandled  " + o1.getClass() + " " + o2.getClass());
            }
        }

        @Override public boolean areContentsTheSame(Object oldItem, Object newItem)
        {
            if (oldItem instanceof PositionNothingView.DTO)
            {
                return ((PositionNothingView.DTO) oldItem).description.equals(((PositionNothingView.DTO) newItem).description);
            }
            else if (oldItem instanceof PositionSectionHeaderDisplayDTO)
            {
                return ((PositionSectionHeaderDisplayDTO) oldItem).header.equals((((PositionSectionHeaderDisplayDTO) newItem).header)) &&
                        ((PositionSectionHeaderDisplayDTO) oldItem).timeBase.equals((((PositionSectionHeaderDisplayDTO) newItem).timeBase));
            }
            else if (oldItem instanceof PositionLockedView.DTO)
            {
                PositionLockedView.DTO o1 = (PositionLockedView.DTO) oldItem;
                PositionLockedView.DTO o2 = (PositionLockedView.DTO) newItem;
                return o1.positionPercent.equals(o2.positionPercent)
                        &&
                        o1.unrealisedPLValueHeader.equals(o2.unrealisedPLValueHeader)
                        &&
                        o1.unrealisedPLValue.equals(o2.unrealisedPLValue)
                        &&
                        o1.realisedPLValueHeader.equals(o2.realisedPLValueHeader)
                        &&
                        o1.realisedPLValue.equals(o2.realisedPLValue)
                        &&
                        o1.totalInvestedValue.equals(o2.totalInvestedValue);
            }
            else if (oldItem instanceof PositionDisplayDTO)
            {
                PositionDisplayDTO o1 = (PositionDisplayDTO) oldItem;
                PositionDisplayDTO o2 = (PositionDisplayDTO) newItem;
                if (o1.stockLogoVisibility != o2.stockLogoVisibility) return false;
                if (o1.stockLogoRes != o2.stockLogoRes) return false;
                if (o1.flagsContainerVisibility != o2.flagsContainerVisibility) return false;
                if (o1.btnCloseVisibility != o2.btnCloseVisibility) return false;
                if (o1.companyNameVisibility != o2.companyNameVisibility) return false;
                if (o1.shareCountRowVisibility != o2.shareCountRowVisibility) return false;
                if (o1.shareCountVisibility != o2.shareCountVisibility) return false;
                if (o1.lastAmountContainerVisibility != o2.lastAmountContainerVisibility) return false;
                if (o1.positionPercentVisibility != o2.positionPercentVisibility) return false;
                if (o1.gainIndicator != o2.gainIndicator) return false;
                if (o1.gainLossColor != o2.gainLossColor) return false;
                if (o1.unrealisedPLVisibility != o2.unrealisedPLVisibility) return false;
                if (o1.lastAmountHeaderVisibility != o2.lastAmountHeaderVisibility) return false;
                if (o1.stockLogoUrl != null ? !o1.stockLogoUrl.equals(o2.stockLogoUrl) : o2.stockLogoUrl != null) return false;
                if (o1.fxPair != null ? !o1.fxPair.equals(o2.fxPair) : o2.fxPair != null) return false;
                if (!o1.stockSymbol.equals(o2.stockSymbol)) return false;
                if (!o1.companyName.equals(o2.companyName)) return false;
                if (!o1.lastPriceAndRise.equals(o2.lastPriceAndRise)) return false;
                if (!o1.shareCountHeader.equals(o2.shareCountHeader)) return false;
                if (!o1.shareCount.equals(o2.shareCount)) return false;
                if (!o1.shareCountText.equals(o2.shareCountText)) return false;
                if (!o1.positionPercent.equals(o2.positionPercent)) return false;
                if (!o1.gainLossHeader.equals(o2.gainLossHeader)) return false;
                if (!o1.gainLoss.equals(o2.gainLoss)) return false;
                if (!o1.gainLossPercent.equals(o2.gainLossPercent)) return false;
                if (o1.totalInvested != null ? !o1.totalInvested.equals(o2.totalInvested) : o2.totalInvested != null) return false;
                if (!o1.unrealisedPL.equals(o2.unrealisedPL)) return false;
                return o1.lastAmount.equals(o2.lastAmount);
            }
            else
            {
                throw new IllegalStateException("Unhandled  " + oldItem.getClass() + " " + newItem.getClass());
            }
        }

        @Override public boolean areItemsTheSame(Object item1, Object item2)
        {
            if (item1.getClass().equals(item2.getClass()))
            {
                if (item1 instanceof PositionNothingView.DTO)
                {
                    return true; //There can only be one empty view
                }
                else if (item1 instanceof PositionSectionHeaderDisplayDTO)
                {
                    return ((PositionSectionHeaderDisplayDTO) item1).type.equals((((PositionSectionHeaderDisplayDTO) item2).type));
                }
                else if (item1 instanceof PositionLockedView.DTO)
                {
                    return ((PositionLockedView.DTO) item1).id == ((PositionLockedView.DTO) item2).id;
                }
                else if (item1 instanceof PositionDisplayDTO)
                {
                    return ((PositionDisplayDTO) item1).positionDTO.id == ((PositionDisplayDTO) item2).positionDTO.id;
                }
            }
            return false;
        }
    }

    protected static class PositionDisplayDTOViewHolder extends TypedViewHolder<Object>
    {
        @Bind(R.id.stock_logo) ImageView stockLogo;
        @Bind(R.id.stock_symbol) TextView stockSymbol;
        @Bind(R.id.company_name) TextView companyName;
        @Bind(R.id.share_count) TextView shareCount;
        @Bind(R.id.position_value) TextView totalValue;

        private final Picasso picasso;

        public PositionDisplayDTOViewHolder(View itemView, Picasso picasso)
        {
            super(itemView);
            this.picasso = picasso;
        }

        @Override public void onDisplay(Object o)
        {
            if (o instanceof PositionDisplayDTO)
            {
                final PositionDisplayDTO dto = (PositionDisplayDTO) o;

                stockLogo.setVisibility(dto.stockLogoVisibility);
                RequestCreator request;
                if (dto.stockLogoUrl != null)
                {
                    request = picasso.load(dto.stockLogoUrl);
                }
                else
                {
                    request = picasso.load(dto.stockLogoRes);
                }
                request.placeholder(R.drawable.default_image)
                        .transform(new WhiteToTransparentTransformation())
                        .into(stockLogo, new Callback()
                        {
                            @Override public void onSuccess()
                            {
                            }

                            @Override public void onError()
                            {
                                stockLogo.setImageResource(dto.stockLogoRes);
                            }
                        });

                stockSymbol.setText(dto.stockSymbol);

                companyName.setVisibility(dto.companyNameVisibility);
                companyName.setText(dto.companyName);

                shareCount.setText(dto.shareCountText);
                totalValue.setText(dto.unrealisedPL);
            }
        }
    }

    public static class SectionHeaderViewHolder extends TypedRecyclerAdapter.TypedViewHolder<Object>
    {
        @Bind(R.id.header_text) protected TextView headerText;
        @Bind(R.id.header_time_base) protected TextView timeBaseText;

        public SectionHeaderViewHolder(View itemView)
        {
            super(itemView);
        }

        @Override public void onDisplay(Object o)
        {
            if (o instanceof PositionSectionHeaderDisplayDTO)
            {
                PositionSectionHeaderDisplayDTO dto = (PositionSectionHeaderDisplayDTO) o;
                if (headerText != null)
                {
                    headerText.setText(dto.header);
                }

                if (timeBaseText != null)
                {
                    timeBaseText.setText(dto.timeBase);
                }
            }
        }
    }
}

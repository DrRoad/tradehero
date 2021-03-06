package com.tradehero.th.fragments.contestcenter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.inject.HierarchyInjector;
import android.support.annotation.NonNull;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ContestItemAdapter extends ArrayAdapter<ContestPageDTO>
{
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_VIP = 1;

    @NonNull @LayoutRes private Integer[] typeToResIds;

    //<editor-fold desc="Constructors">
    public ContestItemAdapter(
            @NonNull Context context,
            @LayoutRes int vipViewResourceId,
            @LayoutRes int normalViewResourceId)
    {
        super(context, 0);
        typeToResIds = new Integer[2];
        typeToResIds[TYPE_VIP] = vipViewResourceId;
        typeToResIds[TYPE_NORMAL] = normalViewResourceId;
        HierarchyInjector.inject(context, this);
    }
    //</editor-fold>

    @Override public int getViewTypeCount()
    {
        return typeToResIds.length;
    }

    @Override public int getItemViewType(int position)
    {
        ContestPageDTO item = getItem(position);
        if (item instanceof ProviderContestPageDTO)
        {
            ProviderDTO providerDTO = ((ProviderContestPageDTO) item).providerDTO;
            if (providerDTO.vip != null && providerDTO.vip)
            {
                return TYPE_VIP;
            }
            else
            {
                return TYPE_NORMAL;
            }
        }
        throw new IllegalArgumentException("Unhandled item " + item);
    }

    @LayoutRes public int getItemViewResId(int position)
    {
        return typeToResIds[getItemViewType(position)];
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(getItemViewResId(position), viewGroup, false);
        }
        if (convertView instanceof DTOView)
        {
            //noinspection unchecked
            ((DTOView<ContestPageDTO>) convertView).display(getItem(position));
        }
        return convertView;
    }

    @Override public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override public boolean isEnabled(int position)
    {
        return getItem(position) instanceof ProviderContestPageDTO;
    }
}

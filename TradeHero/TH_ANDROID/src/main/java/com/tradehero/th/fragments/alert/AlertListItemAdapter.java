package com.tradehero.th.fragments.alert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th.R;
import com.tradehero.th.adapters.ViewDTOSetAdapter;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.inject.HierarchyInjector;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class AlertListItemAdapter extends ViewDTOSetAdapter<AlertCompactDTO, AlertItemView>
    implements StickyListHeadersAdapter
{
    private static final long HEADER_ID_INACTIVE = 0;
    private static final long HEADER_ID_ACTIVE = 1;

    @Inject protected CurrentUserId currentUserId;

    protected final int alertResId;

    //<editor-fold desc="Constructors">
    public AlertListItemAdapter(@NotNull Context context, int alertResId)
    {
        super(context);
        this.alertResId = alertResId;

        HierarchyInjector.inject(context, this);
    }
    //</editor-fold>

    @Override @NotNull protected Set<AlertCompactDTO> createSet(@Nullable Collection<AlertCompactDTO> objects)
    {
        Set<AlertCompactDTO> set = new TreeSet<>(new Comparator<AlertCompactDTO>()
        {
            @Override public int compare(AlertCompactDTO lhs, AlertCompactDTO rhs)
            {
                if (lhs == rhs)
                {
                    return 0;
                }

                if (lhs.active && !rhs.active)
                {
                    return -1;
                }
                if (!lhs.active && rhs.active)
                {
                    return 1;
                }

                if (lhs.security == rhs.security)
                {
                    return 0;
                }
                if (lhs.security != null && rhs.security != null)
                {
                    return lhs.security.symbol.compareTo(rhs.security.symbol);
                }

                if (lhs.security == null)
                {
                    return -1;
                }
                return 1;
            }
        });
        if (objects != null)
        {
            set.addAll(objects);
        }
        return set;
    }

    @Override protected int getViewResId(int position)
    {
        return alertResId;
    }

    @Override public boolean hasStableIds()
    {
        return true;
    }

    @Override public View getHeaderView(int position, View convertView, ViewGroup parent)
    {
        TextHolder holder = null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.alert_management_title, parent, false);
            holder = new TextHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (TextHolder) convertView.getTag();
        }

        holder.text.setText(getHeaderId(position) == 1 ?
                context.getString(R.string.stock_alert_active) :
                        context.getString(R.string.stock_alert_inactive_title)
        );
        return convertView;
    }

    @Override public long getHeaderId(int position)
    {
        return getItem(position).active ? HEADER_ID_ACTIVE : HEADER_ID_INACTIVE;
    }
}

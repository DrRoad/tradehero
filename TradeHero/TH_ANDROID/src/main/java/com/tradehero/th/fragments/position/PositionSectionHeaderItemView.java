package com.tradehero.th.fragments.position;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.adapters.TypedRecyclerAdapter;
import com.tradehero.th.api.DTOView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PositionSectionHeaderItemView extends RelativeLayout
{
    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public PositionSectionHeaderItemView(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public PositionSectionHeaderItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public PositionSectionHeaderItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    public static class DTO
    {
        @NonNull public final String header;
        @NonNull public final String timeBase;
        @NonNull public final Type type;

        public DTO(
                @NonNull Resources resources,
                @NonNull String header,
                @Nullable Date left,
                @Nullable Date right,
                @NonNull Type type)
        {
            this.header = header;
            SimpleDateFormat sdf = new SimpleDateFormat(resources.getString(R.string.data_format_dd_mmm_yyyy), Locale.ENGLISH);
            if (left != null || right != null)
            {
                this.timeBase = resources.getString(
                        R.string.position_list_header_time_base,
                        left != null ? sdf.format(left) : "",
                        right != null ? sdf.format(right) : "");
            }
            else
            {
                this.timeBase = "";
            }
            this.type = type;
        }
    }

    public enum Type
    {
        LONG, SHORT, CLOSED;
    }

    public static class ViewHolder extends TypedRecyclerAdapter.TypedViewHolder<Object>
    {
        @InjectView(R.id.header_text) protected TextView headerText;
        @InjectView(R.id.header_time_base) protected TextView timeBaseText;

        public ViewHolder(PositionSectionHeaderItemView itemView)
        {
            super(itemView);
        }

        @Override public void display(Object o)
        {
            if (o instanceof DTO)
            {
                DTO dto = (DTO) o;
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

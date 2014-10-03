package com.tradehero.th.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tradehero.th.api.DTOView;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class ViewDTOSetAdapter<T, ViewType extends View & DTOView<T>>
        extends DTOSetAdapter<T>
{
    //<editor-fold desc="Constructors">
    public ViewDTOSetAdapter(@NotNull Context context)
    {
        super(context);
    }

    public ViewDTOSetAdapter(@NotNull Context context, @Nullable Collection<T> objects)
    {
        super(context, objects);
    }
    //</editor-fold>

    @Override public ViewType getView(int position, @Nullable View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(getViewResId(position), parent, false);
        }

        //noinspection unchecked
        ViewType dtoView = (ViewType) convertView;
        fineTune(position, getItem(position), dtoView);

        return dtoView;
    }

    @LayoutRes abstract protected int getViewResId(int position);

    protected void fineTune(int position, T dto, ViewType dtoView)
    {
        dtoView.display(dto);
    }
}

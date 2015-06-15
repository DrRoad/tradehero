package com.tradehero.th.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TypedRecyclerAdapter<T>
        extends RecyclerView.Adapter<TypedRecyclerAdapter.TypedViewHolder<T>>
{
    protected final SortedList<T> mSortedList;
    private TypedRecyclerComparator<T> mComparator;
    protected OnItemClickedListener<T> mOnItemClickedListener;
    protected OnItemLongClickedListener<T> mOnItemLongClickedListener;

    public TypedRecyclerAdapter(Class<T> klass)
    {
        this(klass, null);
    }

    public TypedRecyclerAdapter(Class<T> klass, @Nullable TypedRecyclerComparator<T> comparator)
    {
        if (comparator == null)
        {
            comparator = this.createDefaultComparator();
        }
        this.mComparator = comparator;

        this.mSortedList = new SortedList<>(klass, new SortedListAdapterCallback<T>(this)
        {
            @Override
            public int compare(T o1, T o2)
            {
                return TypedRecyclerAdapter.this.mComparator.compare(o1, o2);
            }

            @Override
            public boolean areContentsTheSame(T oldItem, T newItem)
            {
                return TypedRecyclerAdapter.this.mComparator.areContentsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(T item1, T item2)
            {
                return TypedRecyclerAdapter.this.mComparator.areItemsTheSame(item1, item2);
            }
        });
    }

    private TypedRecyclerComparator<T> createDefaultComparator()
    {
        return new TypedRecyclerComparator<>();
    }

    public void setOnItemClickedListener(@NonNull OnItemClickedListener<T> onItemClickedListener)
    {
        if (mSortedList.size() > 0)
        {
            throw new IllegalStateException("");
        }
        this.mOnItemClickedListener = onItemClickedListener;
    }

    public void setOnItemLongClickedListener(@NonNull OnItemLongClickedListener<T> onItemLongClickedListener)
    {
        if (mSortedList.size() > 0)
        {
            throw new IllegalStateException("");
        }
        this.mOnItemLongClickedListener = onItemLongClickedListener;
    }

    public void setComparator(@NonNull TypedRecyclerComparator<T> comparator)
    {
        if (!comparator.equals(mComparator))
        {
            this.mComparator = comparator;
            int size = mSortedList.size();
            if (size > 0)
            {
                List<T> temp = new ArrayList<>();
                mSortedList.beginBatchedUpdates();
                for (int i = 0; i < size; i++)
                {
                    T e = mSortedList.get(0);
                    temp.add(e);
                    mSortedList.removeItemAt(0);
                }
                mSortedList.endBatchedUpdates();
                addAll(temp);
            }
        }
    }

    public T getItem(int position)
    {
        return mSortedList.get(position);
    }

    public int add(@NonNull T t)
    {
        return this.mSortedList.add(t);
    }

    public void addAll(Collection<T> collection)
    {
        this.mSortedList.beginBatchedUpdates();
        for (T t : collection)
        {
            this.mSortedList.add(t);
        }
        this.mSortedList.endBatchedUpdates();
    }

    public boolean remove(T t)
    {
        return this.mSortedList.remove(t);
    }

    public T removeItemAt(int index)
    {
        return this.mSortedList.removeItemAt(index);
    }

    public void removeAll()
    {
        this.mSortedList.beginBatchedUpdates();
        int size = mSortedList.size();
        for (int i = 0; i < size; i++)
        {
            mSortedList.removeItemAt(0);
        }
        this.mSortedList.endBatchedUpdates();
    }

    public int indexOf(T t)
    {
        return this.mSortedList.indexOf(t);
    }

    public void recalculatePositionOfItemAt(int index)
    {
        this.mSortedList.recalculatePositionOfItemAt(index);
    }

    @Override
    public int getItemCount()
    {
        return mSortedList.size();
    }

    @NonNull public abstract TypedViewHolder<T> onCreateTypedViewHolder(ViewGroup parent, int viewType);

    @Override
    final public TypedViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType)
    {
        final TypedViewHolder<T> vh = onCreateTypedViewHolder(parent, viewType);
        if (mOnItemClickedListener != null)
        {
            vh.itemView.setOnClickListener(new View.OnClickListener()
            {

                @Override public void onClick(View v)
                {
                    int position = vh.getAdapterPosition();
                    mOnItemClickedListener.onItemClicked(position, vh, getItem(position));
                }
            });
        }
        if (mOnItemLongClickedListener != null)
        {
            vh.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override public boolean onLongClick(View v)
                {
                    int position = vh.getAdapterPosition();
                    return mOnItemLongClickedListener.onItemLongClicked(position, vh, getItem(position));
                }
            });
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(TypedViewHolder<T> holder, int position)
    {
        holder.display(getItem(position));
    }

    public interface OnItemClickedListener<T>
    {
        void onItemClicked(int position, TypedViewHolder<T> viewHolder, T object);
    }

    public interface OnItemLongClickedListener<T>
    {
        boolean onItemLongClicked(int position, TypedViewHolder<T> viewHolder, T object);
    }

    public static class TypedRecyclerComparator<T>
    {
        protected int compare(T o1, T o2)
        {
            return 0;
        }

        protected boolean areContentsTheSame(T oldItem, T newItem)
        {
            return oldItem.toString().equalsIgnoreCase(newItem.toString());
        }

        protected boolean areItemsTheSame(T item1, T item2)
        {
            return item1.equals(item2);
        }
    }

    public static abstract class TypedViewHolder<T> extends RecyclerView.ViewHolder
    {
        public TypedViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        public abstract void display(T t);
    }

    public static class DividerItemDecoration extends RecyclerView.ItemDecoration
    {
        private Drawable mDivider;
        private boolean mShowFirstDivider = false;
        private boolean mShowLastDivider = false;

        public DividerItemDecoration(Context context)
        {
            this(context, null);
        }

        public DividerItemDecoration(Context context, AttributeSet attrs)
        {
            final TypedArray a = context
                    .obtainStyledAttributes(attrs, new int[] {android.R.attr.listDivider});
            mDivider = a.getDrawable(0);
            a.recycle();
        }

        public DividerItemDecoration(Context context, AttributeSet attrs, boolean showFirstDivider,
                boolean showLastDivider)
        {
            this(context, attrs);
            mShowFirstDivider = showFirstDivider;
            mShowLastDivider = showLastDivider;
        }

        public DividerItemDecoration(Drawable divider)
        {
            mDivider = divider;
        }

        public DividerItemDecoration(Drawable divider, boolean showFirstDivider,
                boolean showLastDivider)
        {
            this(divider);
            mShowFirstDivider = showFirstDivider;
            mShowLastDivider = showLastDivider;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                RecyclerView.State state)
        {
            super.getItemOffsets(outRect, view, parent, state);
            if (mDivider == null)
            {
                return;
            }
            if (parent.getChildAdapterPosition(view) < 1)
            {
                return;
            }

            if (getOrientation(parent) == LinearLayoutManager.VERTICAL)
            {
                outRect.top = mDivider.getIntrinsicHeight();
            }
            else
            {
                outRect.left = mDivider.getIntrinsicWidth();
            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
        {
            if (mDivider == null)
            {
                super.onDrawOver(c, parent, state);
                return;
            }

            // Initialization needed to avoid compiler warning
            int left = 0, right = 0, top = 0, bottom = 0, size;
            int orientation = getOrientation(parent);
            int childCount = parent.getChildCount();

            if (orientation == LinearLayoutManager.VERTICAL)
            {
                size = mDivider.getIntrinsicHeight();
                left = parent.getPaddingLeft();
                right = parent.getWidth() - parent.getPaddingRight();
            }
            else
            { //horizontal
                size = mDivider.getIntrinsicWidth();
                top = parent.getPaddingTop();
                bottom = parent.getHeight() - parent.getPaddingBottom();
            }

            for (int i = mShowFirstDivider ? 0 : 1; i < childCount; i++)
            {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                if (orientation == LinearLayoutManager.VERTICAL)
                {
                    top = child.getTop() - params.topMargin - mDivider.getIntrinsicHeight();
                    bottom = top + size;
                }
                else
                { //horizontal
                    left = child.getLeft() - params.leftMargin - mDivider.getIntrinsicWidth();
                    right = left + size;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }

            // show last divider
            if (mShowLastDivider && childCount > 0)
            {
                View child = parent.getChildAt(childCount - 1);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (orientation == LinearLayoutManager.VERTICAL)
                {
                    top = child.getBottom() + params.bottomMargin;
                    bottom = top + size;
                }
                else
                { // horizontal
                    left = child.getRight() + params.rightMargin;
                    right = left + size;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private int getOrientation(RecyclerView parent)
        {
            if (parent.getLayoutManager() instanceof LinearLayoutManager)
            {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                return layoutManager.getOrientation();
            }
            else
            {
                throw new IllegalStateException(
                        "DividerItemDecoration can only be used with a LinearLayoutManager.");
            }
        }
    }
}

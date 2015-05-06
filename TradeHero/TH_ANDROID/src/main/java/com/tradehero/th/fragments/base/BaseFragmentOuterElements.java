package com.tradehero.th.fragments.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.AbsListView;
import android.widget.ScrollView;
import com.etiennelawlor.quickreturn.library.views.NotifyingScrollView;
import com.tradehero.th.fragments.MovableBottom;
import com.tradehero.th.fragments.OnMovableBottomTranslateListener;

public class BaseFragmentOuterElements implements FragmentOuterElements
{
    @Override public void openMenu()
    {
        // Do nothing
    }

    @NonNull @Override public AbsListView.OnScrollListener getListViewScrollListener()
    {
        return new AbsListView.OnScrollListener()
        {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                // Do nothing
            }

            @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                // Do nothing
            }
        };
    }

    @NonNull @Override public NotifyingScrollView.OnScrollChangedListener getScrollViewListener()
    {
        return new NotifyingScrollView.OnScrollChangedListener()
        {
            @Override public void onScrollChanged(ScrollView scrollView, int i, int i1, int i2, int i3)
            {
                // Do nothing
            }
        };
    }

    @NonNull @Override public MovableBottom getMovableBottom()
    {
        return new MovableBottom()
        {
            @Override public void animateShow()
            {
                // Do nothing
            }

            @Override public void animateHide()
            {
                // Do nothing
            }

            @Override public void setOnMovableBottomTranslateListener(@Nullable OnMovableBottomTranslateListener listener)
            {
                // Do nothing
            }

            @Override public int getHeight()
            {
                return 0;
            }
        };
    }
}
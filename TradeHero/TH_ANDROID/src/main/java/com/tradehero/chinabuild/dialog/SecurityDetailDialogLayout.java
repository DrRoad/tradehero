package com.tradehero.chinabuild.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import com.tradehero.th.R;
import com.tradehero.th.utils.DaggerUtils;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

public class SecurityDetailDialogLayout extends LinearLayout
{
    @InjectView(R.id.action_list_items) protected ListView listView;

    @Nullable protected OnMenuClickedListener menuClickedListener;

    public static final int INDEX_CANCEL_WATCH = 0;//取消自选
    public static final int INDEX_ADD_WARNING = 1;//添加预警
    public static final int INDEX_SHARE_SECURITY = 2;//分享个股

    //<editor-fold desc="Constructors">
    public SecurityDetailDialogLayout(Context context)
    {
        super(context);
    }

    public SecurityDetailDialogLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SecurityDetailDialogLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        DaggerUtils.inject(this);
        ButterKnife.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ButterKnife.inject(this);
        fillData();
    }

    @Override protected void onDetachedFromWindow()
    {
        ButterKnife.reset(this);
        super.onDetachedFromWindow();
    }

    protected void fillData()
    {
        listView.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.security_detail_dialog_item, getData()));

        listView.setDividerHeight(1);
    }

    private ArrayList<String> getData()
    {
        ArrayList<String> data = new ArrayList<String>();
        data.add("取消自选");
        //data.add("添加预警");
        //data.add("分享个股");
        return data;
    }

    public void setMenuClickedListener(@Nullable OnMenuClickedListener menuClickedListener)
    {
        this.menuClickedListener = menuClickedListener;
    }

    //@OnClick(R.id.news_action_share_cancel)
    //protected void onCancelClicked(View view)
    //{
    //    OnMenuClickedListener listenerCopy = menuClickedListener;
    //    if (listenerCopy != null)
    //    {
    //        listenerCopy.onCancelClicked();
    //    }
    //}

    @OnItemClick(R.id.action_list_items)
    protected void onShareOptionsItemClicked(AdapterView<?> parent, View view, int position, long id)
    {
        OnMenuClickedListener listenerCopy = menuClickedListener;
        if (listenerCopy != null)
        {
            listenerCopy.onShareRequestedClicked(position);
        }
    }

    public static interface OnMenuClickedListener
    {
        void onCancelClicked();

        void onShareRequestedClicked(int position);
    }
}
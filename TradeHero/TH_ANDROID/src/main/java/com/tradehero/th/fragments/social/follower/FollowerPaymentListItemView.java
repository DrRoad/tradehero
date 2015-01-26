package com.tradehero.th.fragments.social.follower;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.social.FollowerTransactionDTO;
import com.tradehero.th.utils.SecurityUtils;
import java.text.SimpleDateFormat;

public class FollowerPaymentListItemView extends RelativeLayout implements DTOView<FollowerTransactionDTO>
{
    private TextView durationInfo;
    private TextView dateStart;
    private TextView revenueInfo;

    private FollowerTransactionDTO userFollowerDTO;

    //<editor-fold desc="Constructors">
    public FollowerPaymentListItemView(Context context)
    {
        super(context);
    }

    public FollowerPaymentListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FollowerPaymentListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        initViews();
    }

    private void initViews()
    {
        durationInfo = (TextView) findViewById(R.id.duration_info);
        dateStart = (TextView) findViewById(R.id.date_start);
        revenueInfo = (TextView) findViewById(R.id.revenue_info);
    }

    public void display(FollowerTransactionDTO followerDTO)
    {
        this.userFollowerDTO = followerDTO;
        displayDurationInfo();
        displayDateStart();
        displayRevenue();
    }

    //<editor-fold desc="Display Methods">
    public void display()
    {
        displayDurationInfo();
        displayDateStart();
        displayRevenue();
    }

    public void displayDurationInfo()
    {
        if (durationInfo != null)
        {
            if (userFollowerDTO != null)
            {
                // TODO get the duration from somewhere
                durationInfo.setText(String.format(getResources().getString(R.string.manage_follower_payment_duration), 90));
            }
            else
            {
                durationInfo.setText(String.format(getResources().getString(R.string.manage_follower_payment_duration), 0));
            }
        }
    }

    public void displayDateStart()
    {
        if (dateStart != null)
        {
            if (userFollowerDTO != null)
            {
                SimpleDateFormat sdf = new SimpleDateFormat(getContext().getString(R.string.manage_follower_payment_datetime_format));
                dateStart.setText(sdf.format(userFollowerDTO.paidAt));
            }
            else
            {
                dateStart.setText(R.string.na);
            }
        }
    }

    public void displayRevenue()
    {
        if (revenueInfo != null)
        {
            if (userFollowerDTO != null)
            {
                revenueInfo.setText(String.format(getResources().getString(R.string.manage_followers_revenue_follower), SecurityUtils.DEFAULT_VIRTUAL_CASH_CURRENCY_DISPLAY, userFollowerDTO.revenue));
            }
            else
            {
                revenueInfo.setText(R.string.na);
            }
        }
    }
    //</editor-fold>
}

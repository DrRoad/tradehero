package com.tradehero.th.widget.user;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.graphics.GradientTransformation;
import com.tradehero.common.graphics.RoundedShapeTransformation;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.models.THSignedNumber;
import com.tradehero.th.utils.DaggerUtils;
import javax.inject.Inject;

/** Created with IntelliJ IDEA. User: tho Date: 9/10/13 Time: 6:34 PM Copyright (c) TradeHero */
public class ProfileView extends FrameLayout implements DTOView<UserProfileDTO>
{
    private ImageView avatar;
    private ImageView background;

    private TextView roiSinceInception;
    private TextView plSinceInception;


    private TextView followersCount;
    private TextView heroesCount;
    private TextView tradesCount;
    private TextView exchangesCount;

    @Inject protected Picasso picasso;
    private boolean initiated;

    //<editor-fold desc="Constructors">
    public ProfileView(Context context)
    {
        this(context, null);
    }

    public ProfileView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ProfileView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        init();
    }

    private void init()
    {
        if (initiated) return;
        initiated = true;

        avatar = (ImageView) findViewById(R.id.user_profile_avatar);
        background = (ImageView) findViewById(R.id.user_profile_background_by_sketched_avatar);

        roiSinceInception = (TextView) findViewById(R.id.txt_roi);
        plSinceInception = (TextView) findViewById(R.id.txt_profile_tradeprofit);

        followersCount = (TextView) findViewById(R.id.user_profile_followers_count);
        heroesCount = (TextView) findViewById(R.id.user_profile_heroes_count);
        tradesCount = (TextView) findViewById(R.id.user_profile_trade_count);
        exchangesCount = (TextView) findViewById(R.id.user_profile_exchanges_count);

        DaggerUtils.inject(this);
    }

    @Override public void display(UserProfileDTO dto)
    {
        if (dto.picture != null)
        {
            picasso
                .load(dto.picture)
                .transform(new RoundedShapeTransformation())
                .into(avatar);

            picasso
                .load(dto.picture)
                .transform(new GradientTransformation())
                .into(background);
        }

        Double roi = dto.portfolio.roiSinceInception;
        if (roi == null) {
            roi = 0.0;
        }
        THSignedNumber thRoiSinceInception = new THSignedNumber(THSignedNumber.TYPE_PERCENTAGE, roi*100);
        roiSinceInception.setText(thRoiSinceInception.toString());
        roiSinceInception.setTextColor(getResources().getColor(thRoiSinceInception.getColor()));

        Double pl = dto.portfolio.plSinceInception;
        if (pl == null) {
            pl = 0.0;
        }
        THSignedNumber thPlSinceInception = new THSignedNumber(THSignedNumber.TYPE_MONEY, pl);
        plSinceInception.setText(thPlSinceInception.toString());
        plSinceInception.setTextColor(getResources().getColor(thPlSinceInception.getColor()));

        followersCount.setText(Integer.toString(dto.followerCount));
        heroesCount.setText(Integer.toString(dto.heroIds.size()));
        tradesCount.setText(Integer.toString(dto.portfolio.countTrades));
        exchangesCount.setText(Integer.toString(dto.portfolio.countExchanges));
    }
}

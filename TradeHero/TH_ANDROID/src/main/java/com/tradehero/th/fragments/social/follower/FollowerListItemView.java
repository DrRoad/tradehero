package com.tradehero.th.fragments.social.follower;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.th2.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.api.social.UserFollowerDTO;
import com.tradehero.th.api.users.UserBaseDTOUtil;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import javax.inject.Inject;
import org.ocpsoft.prettytime.PrettyTime;

public class FollowerListItemView extends RelativeLayout
        implements DTOView<UserFollowerDTO>, View.OnClickListener
{
    @InjectView(R.id.follower_profile_picture) ImageView userIcon;
    @InjectView(R.id.follower_title) TextView title;
    @InjectView(R.id.follower_revenue) TextView revenueInfo;
    //@InjectView(R.id.follower_time) TextView followTime;
    @InjectView(R.id.hint_open_follower_info) ImageView country;

    private UserFollowerDTO userFollowerDTO;
    @Inject @ForUserPhoto protected Transformation peopleIconTransformation;
    @Inject Lazy<Picasso> picasso;
    @Inject UserBaseDTOUtil userBaseDTOUtil;
    @Inject PrettyTime prettyTime;
    @Inject THRouter thRouter;

    //<editor-fold desc="Constructors">
    public FollowerListItemView(Context context)
    {
        super(context);
    }

    public FollowerListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FollowerListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
        DaggerUtils.inject(this);
        if (userIcon != null)
        {
            picasso.get().load(R.drawable.superman_facebook)
                    .transform(peopleIconTransformation)
                    .into(userIcon);
        }
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (userIcon != null)
        {
            userIcon.setOnClickListener(this);
        }
    }

    @Override protected void onDetachedFromWindow()
    {
        if (userIcon != null)
        {
            userIcon.setImageDrawable(null);
            userIcon.setOnClickListener(null);
        }
        if (country != null)
        {
            country.setOnClickListener(this);
        }
        //ButterKnife.reset(this);

        super.onDetachedFromWindow();
    }

    @Override public void onClick(View v)
    {
        if (v.getId() == R.id.follower_profile_picture)
        {
            if (userFollowerDTO != null)
            {
                pushTimelineFragment();
            }
        }
    }

    private void pushTimelineFragment()
    {
        Bundle bundle = new Bundle();
        DashboardNavigator navigator = ((DashboardNavigatorActivity) getContext()).getDashboardNavigator();
        thRouter.save(bundle, new UserBaseKey(userFollowerDTO.id));
        navigator.pushFragment(PushableTimelineFragment.class, bundle);
    }

    public UserFollowerDTO getUserFollowerDTO()
    {
        return userFollowerDTO;
    }

    public void display(UserFollowerDTO followerDTO)
    {
        linkWith(followerDTO, true);
    }

    public void linkWith(UserFollowerDTO followerDTO, boolean andDisplay)
    {
        this.userFollowerDTO = followerDTO;
        if (andDisplay)
        {
            display();
        }
    }

    //<editor-fold desc="Display Methods">
    public void display()
    {
        displayUserIcon();
        displayTitle();
        displayRevenue();
        displayCountryLogo();
    }

    public void displayUserIcon()
    {

        displayDefaultUserIcon();

        if (userIcon != null && userFollowerDTO != null)
        {
            picasso.get().load(userFollowerDTO.picture)
                    .transform(peopleIconTransformation)
                            //TODO if this view is reused, userIcon.getDrawable() may returns the different drawable
                    .placeholder(userIcon.getDrawable())
                    .error(R.drawable.superman_facebook)
                    .into(userIcon);
        }
    }

    public void displayDefaultUserIcon()
    {
        picasso.get().load(R.drawable.superman_facebook)
                .transform(peopleIconTransformation)
                .into(userIcon);
    }

    public void displayCountryLogo()
    {
        if (country != null)
        {
            int imageResId = R.drawable.default_image;
            if (userFollowerDTO != null)
            {
                imageResId = Country.getCountryLogo(R.drawable.default_image, userFollowerDTO.countryCode);
            }
            country.setImageResource(imageResId);
        }
    }

    public void displayTitle()
    {
        if (title != null)
        {
            title.setText(userBaseDTOUtil.getShortDisplayName(getContext(), userFollowerDTO));
        }
    }

    public void displayRevenue()
    {
        if (revenueInfo != null)
        {

            if (userFollowerDTO != null)
            {
                THSignedNumber thRoiSinceInception = THSignedPercentage
                        .builder(userFollowerDTO.roiSinceInception * 100)
                        .build();
                revenueInfo.setText(thRoiSinceInception.toString());
                revenueInfo.setTextColor(
                        getContext().getResources().getColor(thRoiSinceInception.getColorResId()));

                //revenueInfo.setText(String.format(getResources().getString(R.string.manage_followers_revenue_follower), SecurityUtils.DEFAULT_VIRTUAL_CASH_CURRENCY_DISPLAY, userFollowerDTO.totalRevenue));
            }
            else
            {
                revenueInfo.setText(R.string.na);
            }
        }
    }

    //</editor-fold>
}

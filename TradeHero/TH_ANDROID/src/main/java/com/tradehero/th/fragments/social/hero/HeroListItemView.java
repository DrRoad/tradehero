package com.tradehero.th.fragments.social.hero;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.api.social.HeroDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseDTOUtil;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import java.text.SimpleDateFormat;
import javax.inject.Inject;

public class HeroListItemView extends RelativeLayout
        implements DTOView<HeroDTO>
{
    public static final int RES_ID_ACTIVE = R.drawable.image_icon_validation_valid;
    public static final int RES_ID_INACTIVE = R.drawable.buyscreen_info;
    public static final int RES_ID_CROSS_RED = R.drawable.cross_red;

    @InjectView(R.id.follower_profile_picture) ImageView userIcon;
    @InjectView(R.id.hero_title) TextView title;
    @InjectView(R.id.hero_revenue) TextView revenueInfo;
    @InjectView(R.id.hero_date_info) TextView dateInfo;
    @InjectView(R.id.ic_status) ImageView statusIcon;
    @InjectView(R.id.country_logo) ImageView countryLogo;

    private UserBaseKey followerId;
    private HeroDTO heroDTO;
    @Inject @ForUserPhoto Transformation peopleIconTransformation;
    @Inject Lazy<Picasso> picasso;
    @Inject UserBaseDTOUtil userBaseDTOUtil;
    @Inject CurrentUserId currentUserId;
    @Inject THRouter thRouter;
    @Inject DashboardNavigator navigator;

    private OnHeroStatusButtonClickedListener heroStatusButtonClickedListener;

    //<editor-fold desc="Constructors">
    public HeroListItemView(Context context)
    {
        super(context);
    }

    public HeroListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public HeroListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
        HierarchyInjector.inject(this);
    }

    @OnClick(R.id.ic_status) void onStatusIconClicked()
    {
        //OnHeroStatusButtonClickedListener heroStatusButtonClickedListener = HeroListItemView.this.heroStatusButtonClickedListener.get();
        if (heroStatusButtonClickedListener != null)
        {
            heroStatusButtonClickedListener.onHeroStatusButtonClicked(HeroListItemView.this, heroDTO);
        }
    }

    //<editor-fold desc="Reset views">
    private void resetIcons()
    {
        resetStatusIcon();

        resetUserIcon();
    }

    private void resetUserIcon()
    {
        picasso.get().cancelRequest(userIcon);
        userIcon.setImageDrawable(null);
    }

    private void resetStatusIcon()
    {
        picasso.get().cancelRequest(statusIcon);
        statusIcon.setImageDrawable(null);
    }
    //</editor-fold>

    @Override protected void onDetachedFromWindow()
    {
        resetIcons();

        ButterKnife.reset(this);
        super.onDetachedFromWindow();
    }

    @OnClick(R.id.follower_profile_picture) void onFollowerProfilePictureClicked(View v)
    {
        if (heroDTO != null)
        {
            Bundle bundle = new Bundle();
            thRouter.save(bundle, new UserBaseKey(heroDTO.id));
            navigator.pushFragment(PushableTimelineFragment.class, bundle);
        }
    }

    public void setFollowerId(UserBaseKey followerId)
    {
        this.followerId = followerId;
    }

    public void setHeroStatusButtonClickedListener(OnHeroStatusButtonClickedListener heroStatusButtonClickedListener)
    {
        this.heroStatusButtonClickedListener = heroStatusButtonClickedListener;
    }

    public void display(HeroDTO heroDTO)
    {
        displayDefaultUserIcon();
        linkWith(heroDTO, true);
    }

    public void linkWith(HeroDTO heroDTO, boolean andDisplay)
    {
        this.heroDTO = heroDTO;
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
        displayDateInfo();
        displayStatus();
        displayRevenue();
        displayCountryLogo();
    }

    public void displayUserIcon()
    {
        displayDefaultUserIcon();

        if (heroDTO != null)
        {
            picasso.get().load(heroDTO.picture)
                    .placeholder(userIcon.getDrawable())
                    .transform(peopleIconTransformation)
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

    public void displayTitle()
    {
        title.setText(userBaseDTOUtil.getShortDisplayName(getContext(), heroDTO));
    }

    public void displayDateInfo()
    {
        if (heroDTO != null)
        {
            SimpleDateFormat df = new SimpleDateFormat(
                    getResources().getString(R.string.manage_heroes_datetime_format));
            if (heroDTO.active && heroDTO.followingSince != null)
            {
                dateInfo.setText(String.format(
                        getResources().getString(R.string.manage_heroes_following_since),
                        df.format(heroDTO.followingSince)));
            }
            else if (!heroDTO.active && heroDTO.stoppedFollowingOn != null)
            {
                dateInfo.setText(String.format(
                        getResources().getString(R.string.manage_heroes_not_following_since),
                        df.format(heroDTO.stoppedFollowingOn)));
            }
            else
            {
                dateInfo.setText(R.string.na);
            }
        }
        else
        {
            dateInfo.setText(R.string.na);
        }
    }

    public void displayStatus()
    {
        if (statusIcon != null)
        {
            statusIcon.setImageResource(RES_ID_CROSS_RED);
            statusIcon.setVisibility((isFollowerCurrentUser() && !isHeroOfficial()) ? View.VISIBLE : View.GONE);
        }
    }

    public void displayCountryLogo()
    {
        if (countryLogo != null)
        {
            int imageResId = R.drawable.default_image;
            if (heroDTO != null)
            {
                imageResId = Country.getCountryLogo(R.drawable.default_image, heroDTO.countryCode);
            }
            countryLogo.setImageResource(imageResId);
        }
    }

    public void displayRevenue()
    {
        if (revenueInfo != null)
        {
            if (heroDTO != null && heroDTO.roiSinceInception != null)
            {
                THSignedNumber thRoiSinceInception = THSignedPercentage.builder(heroDTO.roiSinceInception * 100)
                        .build();
                revenueInfo.setText(thRoiSinceInception.toString());
                revenueInfo.setTextColor(
                        getContext().getResources().getColor(thRoiSinceInception.getColorResId()));
            }
            else
            {
                revenueInfo.setText(R.string.na);
            }
        }
    }

    public boolean isFollowerCurrentUser()
    {
        return followerId != null && followerId.equals(currentUserId.toUserBaseKey());
    }

    public boolean isHeroOfficial()
    {
        return heroDTO != null && heroDTO.isOfficialAccount();
    }
    //</editor-fold>

    public static interface OnHeroStatusButtonClickedListener
    {
        void onHeroStatusButtonClicked(HeroListItemView heroListItemView, HeroDTO heroDTO);
    }
}

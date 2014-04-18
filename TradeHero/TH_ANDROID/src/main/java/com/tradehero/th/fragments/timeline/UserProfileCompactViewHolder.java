package com.tradehero.th.fragments.timeline;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.th.R;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerFragment;
import com.tradehero.th.fragments.social.follower.FollowerManagerTabFragment;
import com.tradehero.th.fragments.social.hero.HeroManagerFragment;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.SecurityUtils;
import com.tradehero.th.utils.THSignedNumber;
import javax.inject.Inject;

/**
 * Created by xavier on 2/6/14.
 */
public class UserProfileCompactViewHolder
{
    public static final String TAG = UserProfileCompactViewHolder.class.getSimpleName();

    public ImageView avatar;
    public TextView roiSinceInception;
    public TextView profitValue;
    public TextView followersCount;
    public TextView heroesCount;
    public TextView displayName;
    public ImageView btnDefaultPortfolio;

    @Inject protected Context context;
    @Inject @ForUserPhoto protected Transformation peopleIconTransformation;
    @Inject protected Picasso picasso;
    protected UserProfileDTO userProfileDTO;

    public UserProfileCompactViewHolder(View view)
    {
        super();
        DaggerUtils.inject(this);
        initViews(view);
    }

    public void initViews(View view)
    {
        avatar = (ImageView) view.findViewById(R.id.user_profile_avatar);
        roiSinceInception = (TextView) view.findViewById(R.id.user_profile_roi);
        profitValue = (TextView) view.findViewById(R.id.user_profile_profit_value);
        followersCount = (TextView) view.findViewById(R.id.user_profile_followers_count);
        heroesCount = (TextView) view.findViewById(R.id.user_profile_heroes_count);
        displayName = (TextView) view.findViewById(R.id.user_profile_display_name);
        btnDefaultPortfolio =
                (ImageView) view.findViewById(R.id.btn_user_profile_default_portfolio);
    }

    public void display(UserProfileDTO dto)
    {
        this.userProfileDTO = dto;
        loadUserPicture();
        displayRoiSinceInception();
        displayProfitValue();
        displayFollowersCount();
        displayHeroesCount();
        displayDisplayName();
    }

    protected void displayProfitValue()
    {
        if (profitValue != null)
        {
            if (userProfileDTO != null && userProfileDTO.portfolio != null)
            {
                Double pl = userProfileDTO.portfolio.plSinceInception;
                if (pl == null)
                {
                    pl = 0.0;
                }
                THSignedNumber thPlSinceInception = new THSignedNumber(
                        THSignedNumber.TYPE_MONEY,
                        pl,
                        true,
                        SecurityUtils.DEFAULT_VIRTUAL_CASH_CURRENCY_DISPLAY,
                        THSignedNumber.TYPE_SIGN_PLUS_MINUS_ALWAYS);
                profitValue.setText(thPlSinceInception.toString());
                profitValue.setTextColor(
                        context.getResources().getColor(thPlSinceInception.getColor()));
            }
            else
            {
                profitValue.setText(R.string.na);
                profitValue.setTextColor(context.getResources().getColor(R.color.black));
            }
        }
    }

    protected void loadUserPicture()
    {
        if (avatar != null)
        {
            loadDefaultPicture();
            if (userProfileDTO != null && userProfileDTO.picture != null)
            {
                picasso
                        .load(userProfileDTO.picture)
                        .transform(peopleIconTransformation)
                        .placeholder(avatar.getDrawable())
                        .into(avatar);
            }
        }
    }

    protected void loadDefaultPicture()
    {
        if (avatar != null)
        {
            picasso
                    .load(R.drawable.superman_facebook)
                    .transform(peopleIconTransformation)
                    .into(avatar);
        }
    }

    protected void displayRoiSinceInception()
    {
        if (roiSinceInception != null)
        {
            if (userProfileDTO != null
                    && userProfileDTO.portfolio != null
                    && userProfileDTO.portfolio.roiSinceInception != null)
            {
                THSignedNumber thRoiSinceInception = new THSignedNumber(
                        THSignedNumber.TYPE_PERCENTAGE,
                        userProfileDTO.portfolio.roiSinceInception * 100);
                roiSinceInception.setText(thRoiSinceInception.toString());
                roiSinceInception.setTextColor(
                        context.getResources().getColor(thRoiSinceInception.getColor()));
            }
            else
            {
                roiSinceInception.setText(R.string.na);
            }
        }
    }

    protected void displayFollowersCount()
    {
        if (followersCount != null)
        {
            if (userProfileDTO != null)
            {
                followersCount.setText(Integer.toString(
                        userProfileDTO.followerCount == null ? 0 : userProfileDTO.allFollowerCount));
            }
            else
            {
                followersCount.setText(R.string.na);
            }
        }
    }

    protected void displayHeroesCount()
    {
        if (heroesCount != null)
        {
            if (userProfileDTO != null)
            {
                heroesCount.setText(Integer.toString(
                        userProfileDTO.heroIds == null ? 0 : userProfileDTO.heroIds.size()));
            }
            else
            {
                heroesCount.setText(R.string.na);
            }
        }
    }

    protected void displayDisplayName()
    {
        if (displayName != null)
        {
            if (userProfileDTO != null)
            {
                displayName.setText(userProfileDTO.displayName);
            }
            else
            {
                displayName.setText(R.string.na);
            }
        }
    }
}

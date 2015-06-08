package com.tradehero.th.fragments.social.hero;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.common.annotation.ViewVisibilityValue;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.api.social.HeroDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseDTOUtil;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.models.number.THSignedPercentage;
import dagger.Lazy;
import java.text.SimpleDateFormat;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class HeroListItemView extends RelativeLayout
        implements DTOView<HeroListItemView.DTO>
{
    @InjectView(R.id.follower_profile_picture) ImageView userIcon;
    @InjectView(R.id.hero_title) TextView title;
    @InjectView(R.id.hero_revenue) TextView revenueInfo;
    @InjectView(R.id.hero_date_info) TextView dateInfo;
    @InjectView(R.id.ic_status) ImageView statusIcon;
    @InjectView(R.id.country_logo) ImageView countryLogo;

    @Inject @ForUserPhoto Transformation peopleIconTransformation;
    @Inject Lazy<Picasso> picasso;

    @NonNull private BehaviorSubject<UserAction> userActionSubject;
    @Nullable private DTO dto;

    //<editor-fold desc="Constructors">
    public HeroListItemView(Context context)
    {
        super(context);
        userActionSubject = BehaviorSubject.create();
    }

    public HeroListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        userActionSubject = BehaviorSubject.create();
    }

    public HeroListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        userActionSubject = BehaviorSubject.create();
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        HierarchyInjector.inject(this);
        ButterKnife.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ButterKnife.inject(this);
    }

    @Override protected void onDetachedFromWindow()
    {
        if (userIcon != null)
        {
            picasso.get().cancelRequest(userIcon);
            userIcon.setImageDrawable(null);
        }
        ButterKnife.reset(this);
        super.onDetachedFromWindow();
    }

    @NonNull public Observable<UserAction> getUserActionObservable()
    {
        return userActionSubject.asObservable();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.ic_status) void onStatusIconClicked()
    {
        if (dto != null)
        {
            userActionSubject.onNext(new UserActionDelete(dto.heroDTO));
        }
    }

    @Override public void display(@NonNull DTO dto)
    {
        this.dto = dto;

        if (userIcon != null)
        {
            picasso.get().load(dto.heroDTO.picture)
                    .placeholder(userIcon.getDrawable())
                    .transform(peopleIconTransformation)
                    .error(R.drawable.superman_facebook)
                    .into(userIcon);
        }

        if (title != null)
        {
            title.setText(dto.titleText);
        }

        if (dateInfo != null)
        {
            dateInfo.setText(dto.dateText);
        }

        if (statusIcon != null)
        {
            statusIcon.setVisibility(dto.statusIconVisibility);
        }

        if (countryLogo != null)
        {
            countryLogo.setImageResource(dto.countryFlagRes);
        }

        if (revenueInfo != null)
        {
            revenueInfo.setText(dto.revenueSpan);
        }
    }

    public static class DTO
    {
        @NonNull public final UserBaseKey followerId;
        @NonNull public final HeroDTO heroDTO;
        @NonNull public final String titleText;
        @NonNull public final String dateText;
        @ViewVisibilityValue public final int statusIconVisibility;
        @DrawableRes public final int countryFlagRes;
        @NonNull public final CharSequence revenueSpan;

        public DTO(@NonNull Resources resources,
                @NonNull CurrentUserId currentUserId,
                @NonNull UserBaseKey followerId,
                @NonNull HeroDTO heroDTO)
        {
            this.followerId = followerId;
            this.heroDTO = heroDTO;
            this.titleText = UserBaseDTOUtil.getShortDisplayName(resources, heroDTO);

            SimpleDateFormat df = new SimpleDateFormat(
                    resources.getString(R.string.manage_heroes_datetime_format));
            if (heroDTO.active && heroDTO.followingSince != null)
            {
                dateText = String.format(
                        resources.getString(R.string.manage_heroes_following_since),
                        df.format(heroDTO.followingSince));
            }
            else if (!heroDTO.active && heroDTO.stoppedFollowingOn != null)
            {
                dateText = String.format(
                        resources.getString(R.string.manage_heroes_not_following_since),
                        df.format(heroDTO.stoppedFollowingOn));
            }
            else
            {
                dateText = resources.getString(R.string.na);
            }

            statusIconVisibility = currentUserId.toUserBaseKey().equals(followerId) && !heroDTO.isOfficialAccount()
                    ? VISIBLE
                    : GONE;

            countryFlagRes = Country.getCountryLogo(R.drawable.default_image, heroDTO.countryCode);

            if (heroDTO.roiSinceInception != null)
            {
                revenueSpan = THSignedPercentage.builder(heroDTO.roiSinceInception * 100)
                        .withDefaultColor()
                        .build()
                        .createSpanned();
            }
            else
            {
                revenueSpan = resources.getString(R.string.na);
            }
        }
    }

    public static class UserAction
    {
        @NonNull public final HeroDTO heroDTO;

        public UserAction(@NonNull HeroDTO heroDTO)
        {
            this.heroDTO = heroDTO;
        }
    }

    public static class UserActionDelete extends UserAction
    {
        public UserActionDelete(@NonNull HeroDTO heroDTO)
        {
            super(heroDTO);
        }
    }
}

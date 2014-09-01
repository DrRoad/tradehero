package com.tradehero.th.fragments.social;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.api.users.AllowableRecipientDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.base.DashboardNavigatorActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.fragments.timeline.PushableTimelineFragment;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.models.social.OnPremiumFollowRequestedListener;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.route.THRouter;
import dagger.Lazy;
import javax.inject.Inject;

public class RelationsListItemView extends RelativeLayout
        implements DTOView<AllowableRecipientDTO>, View.OnClickListener
{
    @InjectView(R.id.user_name) TextView name;
    @InjectView(R.id.user_profile_avatar) ImageView avatar;
    @InjectView(R.id.country_logo) ImageView countryLogo;
    @InjectView(R.id.user_type) TextView userType;
    @InjectView(R.id.upgrade_now) TextView upgradeNow;
    private AllowableRecipientDTO allowableRecipientDTO;
    private OnPremiumFollowRequestedListener premiumFollowRequestedListener;

    @Inject protected Lazy<Picasso> picassoLazy;
    @Inject @ForUserPhoto protected Lazy<Transformation> peopleIconTransformationLazy;
    @Inject THRouter thRouter;

    //<editor-fold desc="Constructors">
    public RelationsListItemView(Context context)
    {
        super(context);
    }

    public RelationsListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RelationsListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        DaggerUtils.inject(this);
        ButterKnife.inject(this);
        initViews();
    }

    private void initViews()
    {
        upgradeNow.setOnClickListener(this);
        avatar.setOnClickListener(this);
        loadDefaultPicture();
    }

    @Override protected void onDetachedFromWindow()
    {
        premiumFollowRequestedListener = null;
        super.onDetachedFromWindow();
    }

    @Override public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.user_profile_avatar:
                handleOpenProfileButtonClicked();
                break;
            case R.id.upgrade_now:
                handleUpgradeNowButtonClicked();
                break;
        }
    }

    private void handleOpenProfileButtonClicked()
    {
        int userId = allowableRecipientDTO.user.id;

        Bundle bundle = new Bundle();
        thRouter.save(bundle, new UserBaseKey(userId));
        DashboardNavigator navigator = ((DashboardNavigatorActivity) getContext()).getDashboardNavigator();
        navigator.pushFragment(PushableTimelineFragment.class, bundle);
    }

    private void handleUpgradeNowButtonClicked()
    {
        notifyFollowRequested();
    }

    public void setPremiumFollowRequestedListener(
            OnPremiumFollowRequestedListener premiumFollowRequestedListener)
    {
        this.premiumFollowRequestedListener = premiumFollowRequestedListener;
    }

    @Override public void display(AllowableRecipientDTO allowableRecipientDTO)
    {
        linkWith(allowableRecipientDTO, true);
    }

    public void linkWith(AllowableRecipientDTO allowableRecipientDTO, boolean andDisplay)
    {
        this.allowableRecipientDTO = allowableRecipientDTO;
        if (andDisplay)
        {
            displayPicture();
            displayTitle();
            displayUpgradeNow();
            displayUserType();
            displayCountryLogo();
        }
    }

    //<editor-fold desc="Display Methods">
    public void display()
    {
        displayPicture();
        displayTitle();
        displayUpgradeNow();
        displayUserType();
        displayCountryLogo();
    }

    public void displayPicture()
    {
        if (avatar != null)
        {
            loadDefaultPicture();
            if (allowableRecipientDTO != null && allowableRecipientDTO.user.picture != null)
            {
                picassoLazy.get().load(allowableRecipientDTO.user.picture)
                        .transform(peopleIconTransformationLazy.get())
                        .placeholder(avatar.getDrawable())
                        .into(avatar, new Callback()
                        {
                            @Override public void onSuccess()
                            {
                            }

                            @Override public void onError()
                            {
                                //loadDefaultPicture();
                            }
                        });
            }
        }
    }

    protected void loadDefaultPicture()
    {
        if (avatar != null)
        {
            picassoLazy.get().load(R.drawable.superman_facebook)
                    .transform(peopleIconTransformationLazy.get())
                    .into(avatar);
        }
    }

    public void displayTitle()
    {
        if (name != null)
        {
            if (allowableRecipientDTO != null && allowableRecipientDTO.user != null)
            {
                name.setText(allowableRecipientDTO.user.displayName);
            }
            else
            {
                name.setText(R.string.na);
            }
        }
    }

    public void displayUpgradeNow()
    {
        if (upgradeNow != null)
        {
            if (allowableRecipientDTO == null || allowableRecipientDTO.relationship == null)
            {
                upgradeNow.setVisibility(INVISIBLE);
            }
            else if (allowableRecipientDTO.relationship.isFollower)
            {
                upgradeNow.setVisibility(INVISIBLE);
            }
            else if (allowableRecipientDTO.relationship.isHero)
            {
                upgradeNow.setVisibility(
                        allowableRecipientDTO.relationship.freeFollow ? VISIBLE : INVISIBLE);
            }
            else
            {
                upgradeNow.setVisibility(INVISIBLE);
            }
        }
    }

    public void displayUserType()
    {
        if (userType != null)
        {
            userType.setText(getUserTypeText());
        }
    }

    protected String getUserTypeText()
    {
        if (allowableRecipientDTO == null || allowableRecipientDTO.relationship == null)
        {
            return getContext().getString(R.string.na);
        }

        int userTypeTextResId;
        if (allowableRecipientDTO.relationship.isFollower)
        {
            userTypeTextResId = R.string.relation_follower;
        }
        else if (allowableRecipientDTO.relationship.isHero)
        {
            userTypeTextResId = R.string.relation_following;
        }
        else
        {
            userTypeTextResId = R.string.relation_friend;
        }

        int subtitleresId = 0;

        if (allowableRecipientDTO.relationship.freeFollow)
        {
            subtitleresId = R.string.not_follow_subtitle2;
        }
        else if (allowableRecipientDTO.relationship.isHero)
        {
            subtitleresId = R.string.not_follow_premium_subtitle2;
        }

        if (subtitleresId > 0)
        {
            String userTypeText = getContext().getString(userTypeTextResId);
            String subTitle = getContext().getString(subtitleresId);
            return getContext().getString(
                    R.string.follower_item_with_subtitle,
                    userTypeText,
                    subTitle);
        }
        return getContext().getString(userTypeTextResId);
    }

    public void displayCountryLogo()
    {
        if (countryLogo != null &&
                allowableRecipientDTO != null &&
                allowableRecipientDTO.user != null &&
                allowableRecipientDTO.user.countryCode != null)
        {
            int imageResId = Country.getCountryLogo(R.drawable.default_image, allowableRecipientDTO.user.countryCode);
            countryLogo.setImageResource(imageResId);
        }
        else if (countryLogo != null)
        {
            countryLogo.setImageResource(R.drawable.default_image);
        }
    }

    protected void notifyFollowRequested()
    {
        OnPremiumFollowRequestedListener listener = premiumFollowRequestedListener;
        if (listener != null)
        {
            if (allowableRecipientDTO == null || allowableRecipientDTO.user == null)
            {
                THToast.show(R.string.error_incomplete_info_title);
            }
            else
            {
                listener.premiumFollowRequested(allowableRecipientDTO.user.getBaseKey());
            }
        }
    }
}

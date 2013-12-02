package com.tradehero.th.fragments.portfolio;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.graphics.RoundedShapeTransformation;
import com.tradehero.common.milestone.Milestone;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.portfolio.DisplayablePortfolioDTO;
import com.tradehero.th.api.portfolio.DisplayablePortfolioUtil;
import com.tradehero.th.api.users.CurrentUserBaseKeyHolder;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.portfolio.header.PortfolioHeaderView;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.persistence.user.UserProfileRetrievedMilestone;
import com.tradehero.th.utils.DaggerUtils;
import dagger.Lazy;
import java.lang.ref.WeakReference;
import javax.inject.Inject;

/** Created with IntelliJ IDEA. User: xavier Date: 10/14/13 Time: 12:28 PM To change this template use File | Settings | File Templates. */
public class PortfolioListItemView extends RelativeLayout implements DTOView<DisplayablePortfolioDTO>
{
    public static final String TAG = PortfolioListItemView.class.getName();

    private ImageView userIcon;
    private TextView title;
    private TextView description;
    private ImageView followingStamp;

    private DisplayablePortfolioDTO displayablePortfolioDTO;
    @Inject Lazy<Picasso> picasso;
    @Inject Lazy<CurrentUserBaseKeyHolder> currentUserBaseKeyHolder;
    @Inject Lazy<UserProfileCache> userProfileCache;

    private UserProfileRetrievedMilestone currentUserProfileRetrievedMilestone;
    private Milestone.OnCompleteListener currentUserProfileRetrievedMilestoneListener;

    //<editor-fold desc="Constructors">
    public PortfolioListItemView(Context context)
    {
        super(context);
    }

    public PortfolioListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PortfolioListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        initViews();
        DaggerUtils.inject(this);
        if (userIcon != null)
        {
            picasso.get().load(R.drawable.superman_facebook)
                    .transform(new RoundedShapeTransformation())
                    .into(userIcon);
        }
    }

    private void initViews()
    {
        userIcon = (ImageView) findViewById(R.id.user_icon);
        title = (TextView) findViewById(R.id.portfolio_title);
        description = (TextView) findViewById(R.id.portfolio_description);
        followingStamp = (ImageView) findViewById(R.id.following_image);
    }

    @Override protected void onAttachedToWindow()
    {
        THLog.d(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();
        currentUserProfileRetrievedMilestoneListener = new Milestone.OnCompleteListener()
        {
            @Override public void onComplete(Milestone milestone)
            {
                displayFollowingStamp();
            }

            @Override public void onFailed(Milestone milestone, Throwable throwable)
            {
                THLog.e(TAG, "Failed to fetch user profile", throwable);
                THToast.show(R.string.error_fetch_your_user_profile);
            }
        };
        UserProfileRetrievedMilestone milestone = new UserProfileRetrievedMilestone(currentUserBaseKeyHolder.get().getCurrentUserBaseKey());
        milestone.setOnCompleteListener(currentUserProfileRetrievedMilestoneListener);
        currentUserProfileRetrievedMilestone = milestone;
        milestone.launch();
    }

    @Override protected void onDetachedFromWindow()
    {
        currentUserProfileRetrievedMilestoneListener = null;
        Milestone milestoneCopy = currentUserProfileRetrievedMilestone;
        if (milestoneCopy != null)
        {
            milestoneCopy.setOnCompleteListener(null);
        }
        currentUserProfileRetrievedMilestone = null;
        super.onDetachedFromWindow();

    }

    public DisplayablePortfolioDTO getDisplayablePortfolioDTO()
    {
        return displayablePortfolioDTO;
    }

    public void display(DisplayablePortfolioDTO displayablePortfolioDTO)
    {
        linkWith(displayablePortfolioDTO, true);
    }

    public void linkWith(DisplayablePortfolioDTO displayablePortfolioDTO, boolean andDisplay)
    {
        this.displayablePortfolioDTO = displayablePortfolioDTO;
        if (andDisplay)
        {
            displayUserIcon();
            displayTitle();
            displayDescription();
            displayFollowingStamp();
        }
    }

    //<editor-fold desc="Display Methods">
    public void display()
    {
        displayUserIcon();
        displayTitle();
        displayDescription();
        displayFollowingStamp();
    }

    public void displayUserIcon()
    {
        if (userIcon != null)
        {
            if (displayablePortfolioDTO != null && displayablePortfolioDTO.userBaseDTO != null)
            {
                picasso.get().load(displayablePortfolioDTO.userBaseDTO.picture)
                             .transform(new RoundedShapeTransformation())
                             .into(userIcon);
            }
        }
    }

    public void displayTitle()
    {
        if (title != null)
        {
            title.setText(DisplayablePortfolioUtil.getLongTitle(getContext(), displayablePortfolioDTO));
        }
    }

    public void displayDescription()
    {
        if (description != null)
        {
            if (displayablePortfolioDTO != null && displayablePortfolioDTO.portfolioDTO != null)
            {
                description.setText(displayablePortfolioDTO.portfolioDTO.description);
            }
        }
    }

    public void displayFollowingStamp()
    {
        if (followingStamp != null)
        {
            if (isThisUserFollowed())
            {
                followingStamp.setVisibility(VISIBLE);
            }
            else
            {
                followingStamp.setVisibility(GONE);
            }
        }
    }

    public boolean isThisUserFollowed()
    {
        UserProfileDTO currentUserProfile = userProfileCache.get().get(currentUserBaseKeyHolder.get().getCurrentUserBaseKey());
        return currentUserProfile != null && displayablePortfolioDTO != null &&
                currentUserProfile.isFollowingUser(displayablePortfolioDTO.userBaseDTO);
    }
    //</editor-fold>
}

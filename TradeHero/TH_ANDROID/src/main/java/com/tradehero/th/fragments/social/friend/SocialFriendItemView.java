package com.tradehero.th.fragments.social.friend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.social.UserFriendsWeiboDTO;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.utils.DaggerUtils;
import javax.inject.Inject;
import timber.log.Timber;

public class SocialFriendItemView extends LinearLayout implements DTOView<SocialFriendListItemDTO>
{
    @InjectView(R.id.social_item_logo) ImageView friendLogo;
    @InjectView(R.id.social_item_title) TextView friendTitle;
    @InjectView(R.id.social_item_action_btn) TextView actionBtn;
    @InjectView(R.id.social_friend_item_ll) LinearLayout socialFriendItem;
    @InjectView(R.id.social_friend_headline) TextView headLine;
    @InjectView(R.id.social_item_action_cb) CheckBox actionCb;
    @Inject Picasso picasso;
    @Inject @ForUserPhoto Transformation peopleIconTransformation;

    private SocialFriendListItemDTO socialFriendListItemDTO;
    private OnElementClickListener onElementClickListener;

    //<editor-fold desc="Constructors">
    public SocialFriendItemView(Context context)
    {
        super(context);
    }

    public SocialFriendItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SocialFriendItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
        DaggerUtils.inject(this);
    }

    @OnClick(R.id.social_item_action_btn)
    public void onActionButtonClick(View v)
    {
        if (v.getId() == R.id.social_item_action_btn && onElementClickListener != null)
        {
            if (socialFriendListItemDTO instanceof SocialFriendListItemUserDTO)
            {
                if (((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.isTradeHeroUser())
                {
                    onElementClickListener.onFollowButtonClick(((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO);
                }
                else
                {
                    onElementClickListener.onInviteButtonClick(((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO);
                }
            }
            else
            {
                throw new IllegalArgumentException("Unhandled type " + socialFriendListItemDTO);
            }
        }
    }

    @OnClick(R.id.social_item_action_cb)
    public void onActionCheckBoxClick(View v)
    {
        if (v.getId() == R.id.social_item_action_cb && onElementClickListener != null)
        {
            if (socialFriendListItemDTO instanceof SocialFriendListItemUserDTO)
            {
                ((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.isInviteChecked = actionCb.isChecked();
                onElementClickListener.onCheckBoxClick(((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO);
            }
            else
            {
                throw new IllegalArgumentException("Unhandled type " + socialFriendListItemDTO);
            }
        }
    }

    public void setOnElementClickedListener(OnElementClickListener onElementClickListener)
    {
        this.onElementClickListener = onElementClickListener;
    }

    @Override
    public void display(SocialFriendListItemDTO dto)
    {
        this.socialFriendListItemDTO = dto;
        displayUserIcon();
        displayTitle();
        displayActionButton();
        displayHeadLine();
        displayByType();
    }

    private void displayHeadLine()
    {
        if (socialFriendListItemDTO instanceof SocialFriendListItemHeaderDTO)
        {
            headLine.setText(((SocialFriendListItemHeaderDTO) socialFriendListItemDTO).header);
        }
    }

    private void displayByType()
    {
        if (socialFriendListItemDTO instanceof SocialFriendListItemHeaderDTO)
        {
            socialFriendItem.setVisibility(View.GONE);
            headLine.setVisibility(View.VISIBLE);
        }
        else
        {
            socialFriendItem.setVisibility(View.VISIBLE);
            headLine.setVisibility(View.GONE);
        }
    }

    private void displayUserIcon()
    {
        if (socialFriendListItemDTO instanceof SocialFriendListItemUserDTO)
        {
            displayDefaultUserIcon();
            picasso.load(((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.getProfilePictureURL())
                    .placeholder(friendLogo.getDrawable())
                    .transform(peopleIconTransformation)
                    .error(R.drawable.superman_facebook)
                    .into(friendLogo, new Callback()
                    {
                        @Override public void onSuccess()
                        {
                            Timber.d("windy display User Icon success!");
                        }

                        @Override public void onError()
                        {
                            displayDefaultUserIcon();
                        }
                    });
        }
        else
        {
            displayDefaultUserIcon();
        }
    }

    private void displayDefaultUserIcon()
    {
        picasso.load(R.drawable.superman_facebook)
                .transform(peopleIconTransformation)
                .into(friendLogo);
    }

    private void displayTitle()
    {
        if (socialFriendListItemDTO instanceof SocialFriendListItemUserDTO)
        {
            friendTitle.setText(((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.name);
        }
    }

    private void displayActionButton()
    {
        int pL = actionBtn.getPaddingLeft();
        int pR = actionBtn.getPaddingRight();
        int pT = actionBtn.getPaddingTop();
        int pB = actionBtn.getPaddingBottom();

        if (socialFriendListItemDTO instanceof SocialFriendListItemUserDTO)
        {
            if (((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.isTradeHeroUser())
            {
                actionBtn.setText(R.string.follow);
                actionBtn.setBackgroundResource(R.drawable.leaderboard_user_item_follow_action_button);
                actionBtn.setEnabled(true);
            }
            else
            {
                actionBtn.setText(R.string.invite);
                actionBtn.setBackgroundResource(R.drawable.yellow_rounded_button_selector);
                actionBtn.setEnabled(!((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.alreadyInvited);
                setWeiboCheckBox();
            }
        }
        actionBtn.setPadding(pL, pT, pR, pB);
    }

    private void setWeiboCheckBox()
    {
        if (socialFriendListItemDTO instanceof SocialFriendListItemUserDTO)
        {
            actionCb.setChecked(((SocialFriendListItemUserDTO) socialFriendListItemDTO).userFriendsDTO.isInviteChecked);
        }

        // TODO change to be another test
        if (socialFriendListItemDTO instanceof UserFriendsWeiboDTO)
        {
            actionBtn.setVisibility(View.GONE);
            actionCb.setVisibility(View.VISIBLE);
        }
        else
        {
            actionBtn.setVisibility(View.VISIBLE);
            actionCb.setVisibility(View.GONE);
        }
    }

    public static interface OnElementClickListener
    {
        void onFollowButtonClick(UserFriendsDTO userFriendsDTO);
        void onInviteButtonClick(UserFriendsDTO userFriendsDTO);
        void onCheckBoxClick(UserFriendsDTO userFriendsDTO);
    }
}

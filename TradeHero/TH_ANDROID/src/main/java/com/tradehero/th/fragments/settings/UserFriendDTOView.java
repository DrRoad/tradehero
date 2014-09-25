package com.tradehero.th.fragments.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.social.UserFriendsContactEntryDTO;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.social.UserFriendsFacebookDTO;
import com.tradehero.th.api.social.UserFriendsLinkedinDTO;
import com.tradehero.th.models.graphics.ForUserPhoto;
import com.tradehero.th.utils.DaggerUtils;
import dagger.Lazy;
import javax.inject.Inject;

public class UserFriendDTOView extends RelativeLayout
        implements DTOView<UserFriendsDTO>, Checkable
{
    private ImageView userFriendAvatar;
    private TextView userFriendName;
    private TextView userFriendSourceFb;
    private TextView userFriendSourceLi;
    private TextView userFriendSourceContact;
    private UserFriendsDTO userFriendDTO;

    @Inject @ForUserPhoto protected Transformation peopleIconTransformation;
    @Inject protected Lazy<Picasso> picasso;

    //<editor-fold desc="Constructors">
    public UserFriendDTOView(Context context)
    {
        super(context);
    }

    public UserFriendDTOView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public UserFriendDTOView(Context context, AttributeSet attrs, int defStyle)
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
        DaggerUtils.inject(this);

        userFriendAvatar = (ImageView) findViewById(R.id.user_friend_avatar);
        userFriendName = (TextView) findViewById(R.id.user_friend_name);

        userFriendSourceFb = (TextView) findViewById(R.id.user_friend_source_facebook);
        userFriendSourceLi = (TextView) findViewById(R.id.user_friend_source_linkedin);
        userFriendSourceContact = (TextView) findViewById(R.id.user_friend_source_contact);
    }

    @Override public void display(UserFriendsDTO dto)
    {
        linkWith(dto, true);
    }

    private void linkWith(UserFriendsDTO userFriendDTO, boolean andDisplay)
    {
        if (userFriendDTO != null)
        {
            this.userFriendDTO = userFriendDTO;
        }
        if (andDisplay && userFriendDTO != null)
        {
            displayFriendAvatar();
            displayFriendName();
            displayFriendSource();
            displaySelectionState();
        }
    }

    private void displaySelectionState()
    {
        setBackgroundColor(!isChecked() ? getResources().getColor(R.color.white) : getResources().getColor(R.color.gray_normal));
    }

    private void displayFriendSource()
    {
        resetVisibilityOfSourceButtons();

        if (userFriendDTO instanceof UserFriendsFacebookDTO)
        {
            userFriendSourceFb.setVisibility(View.VISIBLE);
        }
        else if (userFriendDTO instanceof UserFriendsLinkedinDTO)
        {
            userFriendSourceLi.setVisibility(View.VISIBLE);
        }
        else if (userFriendDTO instanceof UserFriendsContactEntryDTO)
        {
            userFriendSourceContact.setVisibility(View.VISIBLE);
            userFriendSourceContact.setText(userFriendDTO.email);
        }
    }

    private void resetVisibilityOfSourceButtons()
    {
        userFriendSourceFb.setVisibility(View.INVISIBLE);
        userFriendSourceLi.setVisibility(View.INVISIBLE);
        userFriendSourceContact.setVisibility(View.INVISIBLE);
    }

    private void displayFriendName()
    {
        if (userFriendDTO.name != null)
        {
            userFriendName.setText(userFriendDTO.name);
        }
        else
        {
            userFriendName.setText("");
        }
    }

    private void displayFriendAvatar()
    {
        String avatarUrl = userFriendDTO.getProfilePictureURL();
        if (avatarUrl != null)
        {
            picasso.get().load(avatarUrl)
                    .transform(peopleIconTransformation)
                    .into(userFriendAvatar);
        }
        else
        {
            picasso.get().load(R.drawable.superman_facebook)
                    .transform(peopleIconTransformation)
                    .into(userFriendAvatar);
        }
    }

    //@Override public void invalidate()
    //{
    //    displaySelectionState();
    //    super.invalidate();
    //}

    @Override public void setChecked(boolean checked)
    {
        if (userFriendDTO != null && checked != isChecked())
        {
            userFriendDTO.selected = checked;
            displaySelectionState();
        }
    }

    @Override public boolean isChecked()
    {
        return userFriendDTO != null && userFriendDTO.selected;
    }

    @Override public void toggle()
    {
        setChecked(!isChecked());
    }
}

package com.tradehero.th.api.social;

import android.net.Uri;
import com.tradehero.th.R;
import com.tradehero.th.loaders.ContactEntry;
import android.support.annotation.NonNull;

public class UserFriendsContactEntryDTO extends UserFriendsDTO
{
    public Uri photoUri;

    //<editor-fold desc="Constructors">
    public UserFriendsContactEntryDTO()
    {
        super();
    }

    public UserFriendsContactEntryDTO(@NonNull ContactEntry contactEntry)
    {
        this.name = contactEntry.getName();
        this.email = contactEntry.getEmail();
        this.photoUri = contactEntry.getPhotoUri();
    }
    //</editor-fold>

    @Override public int getNetworkLabelImage()
    {
        return R.drawable.default_image;
    }

    @Override public String getProfilePictureURL()
    {
        // TODO do something here
        return super.getProfilePictureURL();
    }

    @Override public InviteDTO createInvite()
    {
        return new InviteContactEntryDTO(email);
    }

    @Override public int hashCode()
    {
        return super.hashCode() ^
                (email == null ? 0 : email.hashCode());
    }

    @Override protected boolean equals(@NonNull UserFriendsDTO other)
    {
        return super.equals(other) &&
                email == null ? other.email == null : email.equals(other.email);
    }
}

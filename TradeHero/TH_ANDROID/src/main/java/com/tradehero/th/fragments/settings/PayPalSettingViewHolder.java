package com.tradehero.th.fragments.settings;

import android.util.Pair;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import javax.inject.Inject;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class PayPalSettingViewHolder extends OneSettingViewHolder
{
    @NonNull private final CurrentUserId currentUserId;
    @NonNull private final UserProfileCacheRx userProfileCache;
    @Nullable private Subscription userProfileCacheSubscription;
    @Nullable private UserProfileDTO userProfileDTO;

    //<editor-fold desc="Constructors">
    @Inject public PayPalSettingViewHolder(
            @NonNull CurrentUserId currentUserId,
            @NonNull UserProfileCacheRx userProfileCache)
    {
        super();
        this.currentUserId = currentUserId;
        this.userProfileCache = userProfileCache;
    }
    //</editor-fold>

    @Override public void initViews(@NonNull DashboardPreferenceFragment preferenceFragment)
    {
        super.initViews(preferenceFragment);
        fetchUserProfile();
    }

    @Override public void destroyViews()
    {
        unsubscribe(userProfileCacheSubscription);
        super.destroyViews();
    }

    protected void fetchUserProfile()
    {
        unsubscribe(userProfileCacheSubscription);
        userProfileCacheSubscription = this.userProfileCache.get(currentUserId.toUserBaseKey())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createUserProfileCacheObserver());
    }

    @Override protected int getStringKeyResId()
    {
        return R.string.key_settings_primary_paypal;
    }

    @Override protected void handlePrefClicked()
    {
        DashboardPreferenceFragment preferenceFragmentCopy = preferenceFragment;
        if (preferenceFragmentCopy != null)
        {
            preferenceFragmentCopy.getNavigator().pushFragment(SettingsPayPalFragment.class);
        }
    }

    private Observer<Pair<UserBaseKey, UserProfileDTO>> createUserProfileCacheObserver()
    {
        return new Observer<Pair<UserBaseKey, UserProfileDTO>>()
        {
            @Override public void onNext(Pair<UserBaseKey, UserProfileDTO> pair)
            {
                setUserProfile(pair.second);
            }

            @Override public void onCompleted()
            {
            }

            @Override public void onError(Throwable e)
            {
                setUserProfile(null);
            }
        };
    }

    protected void setUserProfile(@Nullable UserProfileDTO userProfile)
    {
        this.userProfileDTO = userProfile;
        clickablePref.setSummary(getSubtitleText());
    }

    @Nullable protected String getSubtitleText()
    {
        if (userProfileDTO == null || userProfileDTO.paypalEmailAddress == null)
        {
            return null;
        }
        return userProfileDTO.paypalEmailAddress;
    }
}

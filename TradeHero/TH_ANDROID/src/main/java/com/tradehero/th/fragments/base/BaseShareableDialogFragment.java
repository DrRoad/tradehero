package com.tradehero.th.fragments.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.ToggleButton;
import butterknife.ButterKnife;
import butterknife.Bind;
import android.support.annotation.Nullable;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.SocialLinkToggleButton;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.UserProfileDTOUtil;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.share.SocialShareHelper;
import com.tradehero.th.models.share.preference.SocialSharePreferenceHelperNew;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.ToastOnErrorAction1;
import com.tradehero.th.rx.dialog.OnDialogClickEvent;
import com.tradehero.th.rx.view.ViewArrayObservable;
import com.tradehero.th.utils.SocialAlertDialogRxUtil;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class BaseShareableDialogFragment extends BaseDialogFragment
{
    @Inject SocialSharePreferenceHelperNew socialSharePreferenceHelperNew;
    @Inject protected CurrentUserId currentUserId;
    @Inject protected UserProfileCacheRx userProfileCache;
    @Inject protected SocialShareHelper socialShareHelper;

    @Bind(R.id.btn_share_wechat) public ToggleButton mBtnShareWeChat;
    Subscription weChatLinkingSubscription;
    @Nullable @Bind({
            R.id.btn_share_fb,
            R.id.btn_share_li,
            R.id.btn_share_tw,
            R.id.btn_share_wb})
    SocialLinkToggleButton[] socialLinkingButtons;
    Subscription socialLinkingSubscription;

    @Nullable protected UserProfileDTO userProfileDTO;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        socialSharePreferenceHelperNew.reload();
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        registerWeChatButton();
        registerSocialButtons();
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchUserProfile();
    }

    @Override public void onDestroyView()
    {
        unsubscribeWeChatButton();
        unsubscribeSocialLinkingButtons();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    //<editor-fold desc="User Profile">
    protected void fetchUserProfile()
    {
        onStopSubscriptions.add(AppObservable.bindSupportFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createUserProfileCacheObserver()));
    }

    protected Observer<Pair<UserBaseKey, UserProfileDTO>> createUserProfileCacheObserver()
    {
        return new BaseShareableDialogUserProfileCacheObserver();
    }

    protected class BaseShareableDialogUserProfileCacheObserver implements Observer<Pair<UserBaseKey, UserProfileDTO>>
    {
        @Override public void onNext(Pair<UserBaseKey, UserProfileDTO> pair)
        {
            linkWith(pair.second);
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            THToast.show(new THException(e));
        }
    }

    protected void linkWith(UserProfileDTO updatedUserProfileDTO)
    {
        this.userProfileDTO = updatedUserProfileDTO;
        for (ToggleButton toggleButton : socialLinkingButtons)
        {
            if (toggleButton != null)
            {
                toggleButton.setEnabled(true);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="WeChat button">
    private void unsubscribeWeChatButton()
    {
        Subscription subscription = weChatLinkingSubscription;
        if (subscription != null)
        {
            subscription.unsubscribe();
        }
    }

    private void registerWeChatButton()
    {
        unsubscribeWeChatButton();
        weChatLinkingSubscription = AppObservable.bindSupportFragment(
                this,
                initialShareButtonState(SocialNetworkEnum.WECHAT)
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(new Func1<Boolean, Observable<OnClickEvent>>()
                        {
                            @Override public Observable<OnClickEvent> call(Boolean checked)
                            {
                                mBtnShareWeChat.setChecked(checked);
                                return ViewObservable.clicks(mBtnShareWeChat, false);
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<OnClickEvent>()
                        {
                            @Override public void call(OnClickEvent event)
                            {
                                socialSharePreferenceHelperNew.updateSocialSharePreference(
                                        SocialNetworkEnum.WECHAT,
                                        ((ToggleButton) event.view()).isChecked());
                            }
                        },
                        new ToastOnErrorAction1());
    }
    //</editor-fold>

    //<editor-fold desc="Social buttons">
    protected void unsubscribeSocialLinkingButtons()
    {
        Subscription subscriptionCopy = socialLinkingSubscription;
        if (subscriptionCopy != null)
        {
            subscriptionCopy.unsubscribe();
        }
    }

    private void registerSocialButtons()
    {
        unsubscribeSocialLinkingButtons();
        socialLinkingSubscription = AppObservable.bindSupportFragment(
                this,
                Observable.from(socialLinkingButtons) // For initial status
                        .flatMap(new Func1<SocialLinkToggleButton, Observable<?>>()
                        {
                            @Override public Observable<?> call(final SocialLinkToggleButton toggleButton)
                            {
                                return initialShareButtonState(toggleButton.getSocialNetworkEnum())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .map(new Func1<Boolean, Boolean>()
                                        {
                                            @Override public Boolean call(Boolean checked)
                                            {
                                                toggleButton.setChecked(checked);
                                                return checked;
                                            }
                                        });
                            }
                        })
                        .toList()
                        .flatMap(new Func1<List<Object>, Observable<SocialLinkToggleButton>>()
                        {
                            @Override public Observable<SocialLinkToggleButton> call(List<Object> objects)
                            {
                                return createCheckedLinkingObservable();
                            }
                        }) // For updates
                        .flatMap(new Func1<SocialLinkToggleButton, Observable<? extends Pair<SocialLinkToggleButton, UserProfileDTO>>>()
                        {
                            @Override public Observable<? extends Pair<SocialLinkToggleButton, UserProfileDTO>> call(
                                    final SocialLinkToggleButton socialLinkToggleButton)
                            {
                                final SocialNetworkEnum socialNetwork = socialLinkToggleButton.getSocialNetworkEnum();
                                return isSocialLinked(socialNetwork)
                                        .flatMap(new Func1<Boolean, Observable<Pair<SocialLinkToggleButton, UserProfileDTO>>>()
                                        {
                                            @Override public Observable<Pair<SocialLinkToggleButton, UserProfileDTO>> call(Boolean socialLinked)
                                            {
                                                if (socialLinked != null && socialLinked)
                                                {
                                                    return Observable.just(Pair.create(socialLinkToggleButton, userProfileDTO));
                                                }
                                                return BaseShareableDialogFragment.this.createSocialAuthObservable(socialLinkToggleButton,
                                                        socialNetwork);
                                            }
                                        });
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .finallyDo(new Action0()
                {
                    @Override public void call()
                    {
                        registerSocialButtons();
                    }
                })
                .subscribe(new Action1<Pair<SocialLinkToggleButton, UserProfileDTO>>()
                           {
                               @Override public void call(Pair<SocialLinkToggleButton, UserProfileDTO> args)
                               {
                                   setPublishEnable(args.first.getSocialNetworkEnum());
                                   linkWith(args.second);
                               }
                           },
                        new Action1<Throwable>()
                        {
                            @Override public void call(Throwable e)
                            {
                                SocialAlertDialogRxUtil.popErrorSocialAuth(getActivity(), e).subscribe(
                                        new EmptyAction1<OnDialogClickEvent>(),
                                        new EmptyAction1<Throwable>());
                            }
                        }
                );
    }

    @NonNull protected Observable<SocialLinkToggleButton> createCheckedLinkingObservable()
    {
        return ViewArrayObservable.clicks(socialLinkingButtons, false)
                .flatMap(new Func1<OnClickEvent, Observable<SocialLinkToggleButton>>()
                {
                    @Override public Observable<SocialLinkToggleButton> call(OnClickEvent event)
                    {
                        SocialLinkToggleButton button = (SocialLinkToggleButton) event.view();
                        if (!button.isChecked())
                        {
                            socialSharePreferenceHelperNew.updateSocialSharePreference(
                                    button.getSocialNetworkEnum(),
                                    false);
                            return Observable.empty();
                        }
                        button.setChecked(false);
                        return Observable.just(button);
                    }
                });
    }

    @NonNull protected Observable<Pair<SocialLinkToggleButton, UserProfileDTO>> createSocialAuthObservable(
            @NonNull final SocialLinkToggleButton socialLinkToggleButton,
            @NonNull final SocialNetworkEnum socialNetwork)
    {
        return SocialAlertDialogRxUtil.popNeedToLinkSocial(getActivity(), socialNetwork)
                .flatMap(new Func1<OnDialogClickEvent, Observable<? extends UserProfileDTO>>()
                {
                    @Override public Observable<? extends UserProfileDTO> call(OnDialogClickEvent event)
                    {
                        return socialShareHelper.handleNeedToLink(event, socialNetwork);
                    }
                })
                .map(new Func1<UserProfileDTO, Pair<SocialLinkToggleButton, UserProfileDTO>>()
                {
                    @Override public Pair<SocialLinkToggleButton, UserProfileDTO> call(UserProfileDTO userProfileDTO)
                    {
                        return Pair.create(socialLinkToggleButton, userProfileDTO);
                    }
                });
    }
    //</editor-fold>

    @NonNull protected Observable<Boolean> initialShareButtonState(@NonNull final SocialNetworkEnum socialNetworkEnum)
    {
        return isSocialLinkedOr(socialNetworkEnum, false)
                .map(new Func1<Boolean, Boolean>()
                {
                    @Override public Boolean call(Boolean isLinked)
                    {
                        return socialSharePreferenceHelperNew.isShareEnabled(
                                socialNetworkEnum,
                                isLinked);
                    }
                });
    }

    @NonNull public Observable<Boolean> isSocialLinkedOr(SocialNetworkEnum socialNetwork, final boolean orElse)
    {
        return isSocialLinked(socialNetwork)
                .map(new Func1<Boolean, Boolean>()
                {
                    @Override public Boolean call(Boolean socialLinked)
                    {
                        return socialLinked != null ? socialLinked : orElse;
                    }
                });
    }

    @NonNull public Observable<Boolean> isSocialLinked(final SocialNetworkEnum socialNetwork)
    {
        final UserProfileDTO userProfileCopy = userProfileDTO;
        if (socialNetwork == SocialNetworkEnum.WECHAT || userProfileCopy == null)
        {
            return Observable.just(null);
        }
        return Observable.just(UserProfileDTOUtil.checkLinkedStatus(userProfileCopy, socialNetwork))
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override public Observable<Boolean> call(Boolean linked)
                    {
                        return linked
                                ? socialShareHelper.canShare(socialNetwork)
                                : Observable.just(false);
                    }
                })
                .map(new Func1<Boolean, Boolean>()
                {
                    @Override public Boolean call(Boolean canShare)
                    {
                        return canShare && UserProfileDTOUtil.checkLinkedStatus(userProfileCopy, socialNetwork);
                    }
                });
    }

    public void setPublishEnable(@NonNull SocialNetworkEnum socialNetwork)
    {
        socialSharePreferenceHelperNew.updateSocialSharePreference(socialNetwork, true);
        for (SocialLinkToggleButton toggleButton : socialLinkingButtons)
        {
            if (toggleButton != null && toggleButton.getSocialNetworkEnum().equals(socialNetwork))
            {
                toggleButton.setChecked(true);
            }
        }
    }

    protected boolean shareForTransaction(@NonNull SocialNetworkEnum socialNetworkEnum)
    {
        return socialSharePreferenceHelperNew.isShareEnabled(socialNetworkEnum, false);
    }

    protected void saveShareSettings()
    {
        socialSharePreferenceHelperNew.save();
    }

    @NonNull protected List<SocialNetworkEnum> getEnabledSharePreferences()
    {
        return socialSharePreferenceHelperNew.getAllEnabledSharePreferences();
    }
}

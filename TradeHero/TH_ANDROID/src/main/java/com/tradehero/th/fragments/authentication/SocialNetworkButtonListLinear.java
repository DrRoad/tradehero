package com.tradehero.th.fragments.authentication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import com.tradehero.th.R;
import com.tradehero.th.api.social.SocialNetworkEnum;
import rx.Observable;
import rx.subjects.PublishSubject;

public class SocialNetworkButtonListLinear extends LinearLayout
{
    @NonNull private final PublishSubject<SocialNetworkEnum> socialNetworkEnumSubject;

    //<editor-fold desc="Constructors">
    public SocialNetworkButtonListLinear(Context context)
    {
        super(context);
        socialNetworkEnumSubject = PublishSubject.create();
    }

    public SocialNetworkButtonListLinear(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        socialNetworkEnumSubject = PublishSubject.create();
    }

    public SocialNetworkButtonListLinear(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        socialNetworkEnumSubject = PublishSubject.create();
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ButterKnife.inject(this);
    }

    @Override protected void onDetachedFromWindow()
    {
        ButterKnife.reset(this);
        super.onDetachedFromWindow();
    }

    @SuppressWarnings({"unused"}) @OnClick({
            R.id.btn_linkedin_signin,
            R.id.btn_facebook_signin,
            R.id.btn_twitter_signin,
            R.id.btn_qq_signin,
            R.id.btn_weibo_signin,
    }) @Optional
    protected void onSignInButtonClicked(View view)
    {
        socialNetworkEnumSubject.onNext(((AuthenticationImageButton) view).getType());
    }

    @NonNull public Observable<SocialNetworkEnum> getSocialNetworkEnumObservable()
    {
        return socialNetworkEnumSubject.asObservable();
    }
}

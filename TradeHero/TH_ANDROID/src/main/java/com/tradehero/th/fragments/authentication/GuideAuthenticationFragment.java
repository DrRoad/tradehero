package com.tradehero.th.fragments.authentication;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.activities.AuthenticationActivity;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.fragments.DashboardNavigator;
import com.tradehero.th.inject.Injector;
import com.tradehero.th.rx.TimberOnErrorAction;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.GraphicUtil;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.MethodEvent;
import com.viewpagerindicator.PageIndicator;
import javax.inject.Inject;
import rx.Observable;
import rx.Observer;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GuideAuthenticationFragment extends Fragment
{
    private static final String BUNDLE_KEY_DEEP_LINK = GuideAuthenticationFragment.class.getName() + ".deepLink";

    private static final int PAGER_INITIAL_POSITION = 0;

    @Inject DashboardNavigator navigator;
    @Inject Analytics analytics;

    @InjectView(R.id.guide_page_indicator) PageIndicator guidePageIndicator;
    @InjectView(R.id.viewpager) ViewPager guidePager;

    @NonNull final int[] guideRes = new int[] {
            R.layout.guide_1,
            R.layout.guide_2,
            R.layout.guide_3,
            R.layout.guide_4,
    };
    @NonNull final int[] bgRes = new int[] {
            R.drawable.login_bg_1,
            R.drawable.login_bg_2,
            R.drawable.login_bg_3,
            R.drawable.login_bg_4,
    };
    @NonNull final boolean[] pagesSeen = new boolean[4];

    @Nullable protected Observer<SocialNetworkEnum> socialNetworkEnumObserver;
    protected SubscriptionList onViewSubscriptions;
    @Nullable Uri deepLink;

    public static void putDeepLink(@NonNull Bundle args, @NonNull Uri deepLink)
    {
        args.putString(BUNDLE_KEY_DEEP_LINK, deepLink.toString());
    }

    @Nullable private static Uri getDeepLink(@NonNull Bundle args)
    {
        String link = args.getString(BUNDLE_KEY_DEEP_LINK);
        return link == null ? null : Uri.parse(link);
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        ((Injector) activity).inject(this);
        socialNetworkEnumObserver = ((AuthenticationActivity) activity).getSelectedSocialNetworkObserver();
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        deepLink = getDeepLink(getArguments());
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_entry_authentication, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        onViewSubscriptions = new SubscriptionList();
        guidePager.setAdapter(new GuidePagerAdapter());
        guidePageIndicator.setViewPager(guidePager, PAGER_INITIAL_POSITION);
        guidePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override public void onPageSelected(int position)
            {
                onGuidePageSelected(position);
            }

            @Override public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    @Override public void onStart()
    {
        super.onStart();
        onGuidePageSelected(guidePager.getCurrentItem());
    }

    @Override public void onDestroyView()
    {
        guidePageIndicator.setOnPageChangeListener(null);
        onViewSubscriptions.unsubscribe();
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDetach()
    {
        this.socialNetworkEnumObserver = null;
        super.onDetach();
    }

    @SuppressWarnings({"unused", "UnusedParameters"})
    @OnClick({
            R.id.btn_facebook_signin,
            R.id.btn_linkedin_signin,
            R.id.btn_qq_signin,
            R.id.btn_twitter_signin,
            R.id.btn_weibo_signin,
    }) @Optional
    protected void mainSocialNetworkClicked(View view)
    {
        if (socialNetworkEnumObserver != null)
        {
            socialNetworkEnumObserver.onNext(((AuthenticationButton) view).getType());
        }
    }

    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    @OnClick(R.id.authentication_email_sign_in_link)
    protected void onSignInClicked(View view)
    {
        Bundle args = new Bundle();
        if (deepLink != null)
        {
            EmailSignInFragment.putDeepLink(args, deepLink);
        }
        navigator.pushFragment(EmailSignInFragment.class, args);
    }

    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    @OnClick(R.id.authentication_email_sign_up_link)
    protected void onSignUpClicked(View view)
    {
        Bundle args = new Bundle();
        if (deepLink != null)
        {
            EmailSignUpFragment.putDeepLink(args, deepLink);
        }
        navigator.pushFragment(EmailSignUpFragment.class);
    }

    protected void onGuidePageSelected(int position)
    {
        final View view = getView();
        if (view != null)
        {
            onViewSubscriptions.add(AppObservable.bindFragment(
                    this,
                    Observable.just(bgRes[position])
                            .subscribeOn(Schedulers.computation())
                            .flatMap(new Func1<Integer, Observable<Drawable>>()
                            {
                                @Override public Observable<Drawable> call(Integer drawableRes)
                                {
                                    try
                                    {
                                        return Observable.just(getResources().getDrawable(drawableRes));
                                    } catch (Throwable e)
                                    {
                                        return Observable.error(e);
                                    }
                                }
                            }))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<Drawable>()
                            {
                                @Override public void call(Drawable drawable)
                                {
                                    try
                                    {
                                        GraphicUtil.setBackground(view, drawable);
                                    } catch (Throwable e)
                                    {
                                        Timber.e(e, "Failed to set guide background");
                                        view.setBackgroundColor(getResources().getColor(R.color.authentication_guide_bg_color));
                                    }
                                }
                            },
                            new TimberOnErrorAction("Failed to set guide background")
                            {
                                @Override public void call(Throwable error)
                                {
                                    super.call(error);
                                    view.setBackgroundColor(getResources().getColor(R.color.authentication_guide_bg_color));
                                }
                            }));
        }
        if (!pagesSeen[position])
        {
            analytics.addEvent(new MethodEvent(AnalyticsConstants.SplashScreen, AnalyticsConstants.Screen + String.valueOf(position)));
            pagesSeen[position] = true;
        }
    }

    class GuidePagerAdapter extends PagerAdapter
    {
        @Override public int getCount()
        {
            return 4;
        }

        @Override public boolean isViewFromObject(View view, Object object)
        {
            return view == object;
        }

        @Override public Object instantiateItem(ViewGroup container, int position)
        {
            View view = LayoutInflater.from(getActivity()).inflate(guideRes[position], null);
            container.addView(view);
            view.setTag(R.id.txt_term_of_service_signin, new GuideViewHolder(view));
            return view;
        }

        @Override public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);
            ButterKnife.reset(((View) object).getTag(R.id.txt_term_of_service_signin));
        }
    }

    class GuideViewHolder
    {
        GuideViewHolder(@NonNull View view)
        {
            ButterKnife.inject(this, view);
        }

        @SuppressWarnings({"UnusedParameters", "unused"})
        @OnClick(R.id.txt_term_of_service_signin) @Optional
        void handleTermsServiceClicked(View view)
        {
            openWebPage(Constants.PRIVACY_TERMS_OF_SERVICE);
        }

        @SuppressWarnings({"UnusedParameters", "unused"})
        @OnClick(R.id.txt_term_of_service_termsofuse) @Optional
        void handlePrivacyClicked(View view)
        {
            openWebPage(Constants.PRIVACY_TERMS_OF_USE);
        }
    }

    void openWebPage(@NonNull String url)
    {
        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        try
        {
            startActivity(it);
        } catch (ActivityNotFoundException e)
        {
            THToast.show("Unable to open url: " + uri);
        }
    }
}
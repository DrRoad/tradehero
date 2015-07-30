package com.tradehero.th.fragments.live;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.common.SlidingTabLayout;
import com.tradehero.th.R;
import com.tradehero.th.api.kyc.StepStatus;
import com.tradehero.th.api.kyc.StepStatusesDTO;
import com.tradehero.th.api.live.LiveBrokerSituationDTO;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.network.service.LiveServiceWrapper;
import com.tradehero.th.persistence.prefs.LiveBrokerSituationPreference;
import com.tradehero.th.rx.TimberOnErrorAction1;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

public class LiveSignUpMainFragment extends BaseFragment
{
    @Inject SignUpLivePagerAdapterFactory signUpLivePagerAdapterFactory;
    @Inject Toolbar toolbar;
    @Inject LiveBrokerSituationPreference liveBrokerSituationPreference;
    @Inject LiveServiceWrapper liveServiceWrapper;

    @Bind(R.id.android_tabs) protected SlidingTabLayout tabLayout;
    @Bind(R.id.pager) protected ViewPager viewPager;

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu, menu);
        actionBarOwnerMixin.setCustomView(LayoutInflater.from(getActivity()).inflate(R.layout.sign_up_custom_actionbar, toolbar, false));
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_sign_up_live_main, container, false);
    }

    @Override public void onViewCreated(final View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        tabLayout.setCustomTabView(R.layout.th_sign_up_tab_indicator, android.R.id.title);
        tabLayout.setDistributeEvenly(true);
        tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.transparent));

        ConnectableObservable<PagerAdapter> pagerAdapterObservable = signUpLivePagerAdapterFactory.create(
                getChildFragmentManager(),
                getArguments()).publish();

        onDestroyViewSubscriptions.add(
                pagerAdapterObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Action1<PagerAdapter>()
                        {
                            @Override public void call(PagerAdapter pagerAdapter)
                            {

                                viewPager.setAdapter(pagerAdapter);
                                tabLayout.setViewPager(viewPager);
                            }
                        })
                        .flatMap(new Func1<PagerAdapter, Observable<LiveBrokerSituationDTO>>()
                        {
                            @Override public Observable<LiveBrokerSituationDTO> call(PagerAdapter pagerAdapter)
                            {
                                return liveBrokerSituationPreference.getLiveBrokerSituationDTOObservable()
                                        .distinctUntilChanged();
                            }
                        })
                        .filter(new Func1<LiveBrokerSituationDTO, Boolean>()
                        {
                            @Override public Boolean call(LiveBrokerSituationDTO situationDTO)
                            {
                                return situationDTO.kycForm != null;
                            }
                        })
                        .throttleLast(1, TimeUnit.SECONDS)
                        .flatMap(new Func1<LiveBrokerSituationDTO, Observable<StepStatusesDTO>>()
                        {
                            @Override public Observable<StepStatusesDTO> call(final LiveBrokerSituationDTO situationDTO)
                            {
                                //noinspection ConstantConditions
                                return liveServiceWrapper.applyToLiveBroker(situationDTO.broker.id, situationDTO.kycForm)
                                        .doOnNext(new Action1<StepStatusesDTO>()
                                        {
                                            @Override public void call(StepStatusesDTO stepStatusesDTO)
                                            {
                                                situationDTO.kycForm.setStepStatuses(stepStatusesDTO.stepStatuses);
                                                liveBrokerSituationPreference.set(situationDTO);
                                            }
                                        });
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Action1<StepStatusesDTO>()
                                {
                                    @Override public void call(StepStatusesDTO updatedSteps)
                                    {
                                        updatePageIndicator(updatedSteps.stepStatuses);
                                    }
                                },
                                new TimberOnErrorAction1("Error on updating step status")));

        onDestroyViewSubscriptions.add(pagerAdapterObservable
                .flatMap(new Func1<PagerAdapter, Observable<Boolean>>()
                {
                    @Override public Observable<Boolean> call(PagerAdapter pagerAdapter)
                    {
                        if (pagerAdapter instanceof PrevNextObservable)
                        {
                            return ((PrevNextObservable) pagerAdapter).getPrevNextObservable();
                        }
                        return Observable.empty();
                    }
                })
                .subscribe(
                        new Action1<Boolean>()
                        {
                            @Override public void call(Boolean next)
                            {
                                viewPager.setCurrentItem(viewPager.getCurrentItem() + (next ? 1 : -1));
                            }
                        },
                        new TimberOnErrorAction1("Failed to listen to prev / next buttons")));

        pagerAdapterObservable.connect();
    }

    @Override public void onDestroyView()
    {
        super.onDestroyView();
        Timber.d("on destroy view");
    }

    private void updatePageIndicator(List<StepStatus> stepStatusList)
    {
        int childCount = tabLayout.getTabStrip().getChildCount();
        int stepSize = stepStatusList.size();
        for (int i = 0; i < childCount && i < stepSize; i++)
        {
            Checkable textView = (Checkable) tabLayout.getTabStrip().getChildAt(i);
            StepStatus step = stepStatusList.get(i);
            textView.setChecked(step.equals(StepStatus.COMPLETE));
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null)
        {
            for (Fragment fragment : fragments)
            {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
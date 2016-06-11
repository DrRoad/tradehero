package com.androidth.general.fragments.live;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Spinner;

import com.androidth.general.api.kyc.KYCFormOptionsDTO;
import com.androidth.general.api.kyc.KYCFormOptionsId;
import com.androidth.general.api.live.LiveBrokerDTO;
import com.androidth.general.api.live.LiveBrokerId;
import com.androidth.general.api.live.LiveBrokerSituationDTO;
import com.androidth.general.common.rx.PairGetSecond;
import com.androidth.general.fragments.base.BaseFragment;
import com.androidth.general.persistence.kyc.KYCFormOptionsCache;
import com.androidth.general.persistence.prefs.LiveBrokerSituationPreference;
import com.androidth.general.R;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

abstract public class LiveSignUpStepBaseFragment extends BaseFragment
{
    @Inject LiveBrokerSituationPreference liveBrokerSituationPreference;
    @Inject protected KYCFormOptionsCache kycFormOptionsCache;

    @Bind(R.id.btn_prev) @Nullable protected View btnPrev;
    @Bind(R.id.btn_next) @Nullable protected View btnNext;

    @NonNull protected PublishSubject<Boolean> prevNextSubject;
    @NonNull private final BehaviorSubject<LiveBrokerSituationDTO> brokerSituationSubject;
    @Nullable private ConnectableObservable<LiveBrokerSituationDTO> brokerSituationObservable;
    @Nullable private ConnectableObservable<KYCFormOptionsDTO> kycOptionsObservable;
    private Subscription kycOptionsSubscription;
    private Subscription brokerSituationSubscription;

    public LiveSignUpStepBaseFragment()
    {
        this.prevNextSubject = PublishSubject.create();
        this.brokerSituationSubject = BehaviorSubject.create();
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override public void onDestroy()
    {
        super.onDestroy();
    }

    /**
     * You should only pass KYCForms that have values that need to be changed.
     */
    @CallSuper public void onNext(@NonNull LiveBrokerSituationDTO situationDTO)
    {
        liveBrokerSituationPreference.set(situationDTO);
    }

    @NonNull public Observable<Boolean> getPrevNextObservabel()
    {
        return prevNextSubject.asObservable();
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        brokerSituationObservable = createBrokerSituationObservable().publish();
        Observable<LiveBrokerDTO> brokerObservable = createBrokerObservable(brokerSituationObservable);
        kycOptionsObservable = createKYCFormOptionsObservable(brokerSituationObservable).publish();

        List<Subscription> list = onInitSubscription(brokerObservable, brokerSituationObservable, kycOptionsObservable);
        if (!list.isEmpty())
        {
            for (Subscription sub :
                    list)
            {
                onDestroyViewSubscriptions.add(sub);
            }
        }
    }

    @Override public void onResume()
    {
        super.onResume();
        onConnectObservables();
    }

    @Override public void onPause()
    {
        super.onPause();
        onDisconnectObservables();
    }

    @CallSuper protected void onConnectObservables()
    {
        if (kycOptionsObservable != null)
        {
            kycOptionsSubscription = kycOptionsObservable.connect();
        }
        if (brokerSituationObservable != null)
        {
            brokerSituationSubscription = brokerSituationObservable.connect();
        }
    }

    @CallSuper protected void onDisconnectObservables()
    {
        if(kycOptionsSubscription != null)
        {
            kycOptionsSubscription.unsubscribe();
        }
        if(brokerSituationSubscription != null)
        {
            brokerSituationSubscription.unsubscribe();
        }
    }

    @NonNull protected List<Subscription> onInitSubscription(Observable<LiveBrokerDTO> brokerDTOObservable,
            Observable<LiveBrokerSituationDTO> liveBrokerSituationDTOObservable, Observable<KYCFormOptionsDTO> kycFormOptionsDTOObservable)
    {
        return Collections.emptyList();
    }

    protected Observable<LiveBrokerSituationDTO> getLiveBrokerSituationObservable()
    {
        return brokerSituationObservable;
    }

    @Override public void onDestroyView()
    {
        brokerSituationObservable = null;
        kycOptionsObservable = null;
        super.onDestroyView();
    }

    @NonNull protected Observable<LiveBrokerSituationDTO> createBrokerSituationObservable()
    {
        return brokerSituationSubject
                .mergeWith(liveBrokerSituationPreference.getLiveBrokerSituationDTOObservable())
                .distinctUntilChanged();
    }

    private Observable<LiveBrokerDTO> createBrokerObservable(Observable<LiveBrokerSituationDTO> brokerSituationObservable)
    {
        return brokerSituationObservable.map(new Func1<LiveBrokerSituationDTO, LiveBrokerDTO>()
        {
            @Override public LiveBrokerDTO call(LiveBrokerSituationDTO liveBrokerSituationDTO)
            {
                return liveBrokerSituationDTO.broker;
            }
        });
    }

    @NonNull protected Observable<KYCFormOptionsDTO> createKYCFormOptionsObservable(Observable<LiveBrokerSituationDTO> brokerSituationObservable)
    {
        return brokerSituationObservable
                .distinctUntilChanged(new Func1<LiveBrokerSituationDTO, LiveBrokerId>()
                {
                    @Override public LiveBrokerId call(LiveBrokerSituationDTO situationDTO)
                    {
                        return situationDTO.broker.id;
                    }
                })
                .flatMap(new Func1<LiveBrokerSituationDTO, Observable<KYCFormOptionsDTO>>()
                {
                    @Override public Observable<KYCFormOptionsDTO> call(LiveBrokerSituationDTO situationDTO)
                    {
                        return kycFormOptionsCache.getOne(new KYCFormOptionsId(situationDTO.broker.id))
                                .map(new PairGetSecond<KYCFormOptionsId, KYCFormOptionsDTO>());
                    }
                });
    }

    @Nullable protected <T> Integer setSpinnerOnFirst(
            @NonNull Spinner spinner,
            @NonNull List<T> candidates,
            @NonNull List<T> values)
    {
        Integer index = null;
        int itemIndex;
        for (T candidate : candidates)
        {
            itemIndex = values.indexOf(candidate);
            if (itemIndex >= 0)
            {
                index = itemIndex;
                break;
            }
        }

        if (index != null)
        {
            spinner.setSelection(index);
        }
        return index;
    }

    @Nullable protected <T> Integer populateSpinner(@NonNull Spinner spinner, @Nullable T value, @NonNull List<T> list)
    {
        if (value != null)
        {
            int index = list.indexOf(value);
            if (index >= 0)
            {
                spinner.setSelection(index);
            }
            return index;
        }
        return null;
    }
}
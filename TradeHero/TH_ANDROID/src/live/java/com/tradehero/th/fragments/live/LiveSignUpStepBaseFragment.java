package com.tradehero.th.fragments.live;

import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.th.api.kyc.KYCFormOptionsDTO;
import com.tradehero.th.api.kyc.KYCFormOptionsId;
import com.tradehero.th.api.live.LiveBrokerSituationDTO;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.persistence.live.KYCFormOptionsCache;
import com.tradehero.th.persistence.prefs.LiveBrokerSituationPreference;
import javax.inject.Inject;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

abstract public class LiveSignUpStepBaseFragment extends BaseFragment
{
    @Inject LiveBrokerSituationPreference liveBrokerSituationPreference;
    @Inject protected KYCFormOptionsCache kycFormOptionsCache;

    @NonNull private final BehaviorSubject<LiveBrokerSituationDTO> brokerSituationSubject;
    @Nullable private Observable<LiveBrokerSituationDTO> brokerSituationObservable;
    @Nullable private Observable<KYCFormOptionsDTO> kycOptionsObservable;

    public LiveSignUpStepBaseFragment()
    {
        this.brokerSituationSubject = BehaviorSubject.create();
    }

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        brokerSituationSubject.onNext(liveBrokerSituationPreference.get());
    }

    @CallSuper public void onNext(@NonNull LiveBrokerSituationDTO situationDTO)
    {
        LiveBrokerSituationDTO previous = liveBrokerSituationPreference.get();
        liveBrokerSituationPreference.set(situationDTO);
        if (!previous.hasSameFields(situationDTO))
        {
            brokerSituationSubject.onNext(situationDTO);
        }
    }

    @NonNull public Observable<LiveBrokerSituationDTO> getBrokerSituationObservable()
    {
        Observable<LiveBrokerSituationDTO> copy = brokerSituationObservable;
        if (copy == null)
        {
            copy = createBrokerSituationObservable().share().cache(1);
            brokerSituationObservable = copy;
        }
        return copy;
    }

    @NonNull protected Observable<LiveBrokerSituationDTO> createBrokerSituationObservable()
    {
        return brokerSituationSubject.asObservable();
    }

    @NonNull public Observable<KYCFormOptionsDTO> getKYCFormOptionsObservable()
    {
        Observable<KYCFormOptionsDTO> copy = kycOptionsObservable;
        if (copy == null)
        {
            copy = createKYCFormOptionsObservable().share().cache(1);
            kycOptionsObservable = copy;
        }
        return copy;
    }

    @NonNull protected Observable<KYCFormOptionsDTO> createKYCFormOptionsObservable()
    {
        return getBrokerSituationObservable()
                .flatMap(new Func1<LiveBrokerSituationDTO, Observable<KYCFormOptionsDTO>>()
                {
                    @Override public Observable<KYCFormOptionsDTO> call(LiveBrokerSituationDTO situationDTO)
                    {
                        return kycFormOptionsCache.getOne(new KYCFormOptionsId(situationDTO.broker.id))
                                .map(new PairGetSecond<KYCFormOptionsId, KYCFormOptionsDTO>());
                    }
                });
    }
}

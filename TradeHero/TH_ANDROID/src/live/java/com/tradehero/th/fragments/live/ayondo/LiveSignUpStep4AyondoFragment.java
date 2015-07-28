package com.tradehero.th.fragments.live.ayondo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.tradehero.th.R;
import com.tradehero.th.api.kyc.KYCAddress;
import com.tradehero.th.api.kyc.ayondo.KYCAyondoForm;
import com.tradehero.th.api.live.LiveBrokerDTO;
import com.tradehero.th.api.live.LiveBrokerSituationDTO;
import com.tradehero.th.rx.EmptyAction1;
import com.tradehero.th.rx.TimberOnErrorAction1;
import com.tradehero.th.widget.KYCAddressWidget;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class LiveSignUpStep4AyondoFragment extends LiveSignUpStepBaseAyondoFragment
{
    private static final int PICK_LOCATION_REQUEST_PRIMARY = 2513;
    private static final int PICK_LOCATION_REQUEST_SECONDARY = 2514;
    private Geocoder mGeocoder;

    @Bind(R.id.info_address_pri) KYCAddressWidget primaryWidget;
    @Bind(R.id.info_address_sec) KYCAddressWidget secondaryWidget;
    @Bind(R.id.info_address_sec_title) TextView secondaryAddressWidget;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_sign_up_live_ayondo_step_4, container, false);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mGeocoder = new Geocoder(getActivity());
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        onDestroyViewSubscriptions.add(primaryWidget.getPickLocationClickedObservable()
                .subscribe(new Action1<OnClickEvent>()
                {
                    @Override public void call(OnClickEvent onClickEvent)
                    {
                        pickLocation(PICK_LOCATION_REQUEST_PRIMARY);
                    }
                }));

        onDestroyViewSubscriptions.add(secondaryWidget.getPickLocationClickedObservable()
                .subscribe(new Action1<OnClickEvent>()
                {
                    @Override public void call(OnClickEvent onClickEvent)
                    {
                        pickLocation(PICK_LOCATION_REQUEST_SECONDARY);
                    }
                }));

        onDestroyViewSubscriptions.add(
                getBrokerSituationObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Func1<LiveBrokerSituationDTO, LiveBrokerDTO>()
                        {
                            @Override public LiveBrokerDTO call(LiveBrokerSituationDTO situationDTO)
                            {
                                //noinspection ConstantConditions
                                List<KYCAddress> addresses = ((KYCAyondoForm) situationDTO.kycForm).getAddresses();
                                if (addresses != null)
                                {
                                    if (addresses.size() > 0)
                                    {
                                        KYCAddress address = addresses.get(0);

                                        primaryWidget.setKYCAddress(address);
                                        if (addresses.size() > 1)
                                        {
                                            KYCAddress address1 = addresses.get(1);
                                            secondaryWidget.setKYCAddress(address1);
                                        }
                                    }
                                }
                                return situationDTO.broker;
                            }
                        })
                        .distinctUntilChanged(new Func1<LiveBrokerDTO, String>()
                        {
                            @Override public String call(LiveBrokerDTO liveBrokerDTO)
                            {
                                return liveBrokerDTO.id + ":" + liveBrokerDTO.name;
                            }
                        })
                        .flatMap(new Func1<LiveBrokerDTO, Observable<LiveBrokerSituationDTO>>()
                        {
                            @Override public Observable<LiveBrokerSituationDTO> call(final LiveBrokerDTO liveBrokerDTO)
                            {
                                return Observable.combineLatest(
                                        primaryWidget.getKYCAddressObservable()
                                                .doOnNext(new Action1<KYCAddress>()
                                                {
                                                    @Override public void call(KYCAddress kycAddress)
                                                    {
                                                        int visibility = kycAddress.lessThanAYear ? View.VISIBLE : View.GONE;
                                                        secondaryAddressWidget.setVisibility(visibility);
                                                        secondaryWidget.setVisibility(visibility);
                                                    }
                                                }),
                                        secondaryWidget.getKYCAddressObservable(),
                                        new Func2<KYCAddress, KYCAddress, List<KYCAddress>>()
                                        {
                                            @Override public List<KYCAddress> call(KYCAddress primaryAddress, KYCAddress secondaryAddress)
                                            {
                                                ArrayList<KYCAddress> addresses = new ArrayList<>(2);
                                                addresses.add(primaryAddress);
                                                if (primaryAddress.lessThanAYear)
                                                {
                                                    addresses.add(secondaryAddress);
                                                }
                                                return addresses;
                                            }
                                        })
                                        .map(new Func1<List<KYCAddress>, LiveBrokerSituationDTO>()
                                        {
                                            @Override public LiveBrokerSituationDTO call(List<KYCAddress> kycAddresses)
                                            {
                                                KYCAyondoForm update = new KYCAyondoForm();
                                                update.setAddresses(kycAddresses);
                                                return new LiveBrokerSituationDTO(liveBrokerDTO, update);
                                            }
                                        });
                            }
                        })
                        .subscribe(
                                new Action1<LiveBrokerSituationDTO>()
                                {
                                    @Override public void call(LiveBrokerSituationDTO update)
                                    {
                                        onNext(update);
                                    }
                                },
                                new TimberOnErrorAction1("Failed to set or listen to address widget")));
    }

    public void pickLocation(int requestCode)
    {
        try
        {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Context context = getActivity().getApplicationContext();
            getParentFragment().startActivityForResult(builder.build(context), requestCode);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e)
        {
            e.printStackTrace();
        }
    }

    @Override public void onDestroyView()
    {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_LOCATION_REQUEST_PRIMARY || requestCode == PICK_LOCATION_REQUEST_SECONDARY) && resultCode == Activity.RESULT_OK)
        {
            LatLng latLng = PlacePicker.getPlace(data, getActivity()).getLatLng();
            Observable.combineLatest(
                    Observable.just(latLng)
                            .subscribeOn(Schedulers.io())
                            .map(new Func1<LatLng, List<Address>>()
                            {
                                @Override public List<Address> call(LatLng latLng)
                                {
                                    try
                                    {
                                        return mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                    } catch (IOException e)
                                    {
                                        throw new RuntimeException(e);
                                    }
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .filter(new Func1<List<Address>, Boolean>()
                            {
                                @Override public Boolean call(List<Address> addresses)
                                {
                                    return addresses.size() == 1;
                                }
                            })
                            .map(new Func1<List<Address>, KYCAddress>()
                            {
                                @Override public KYCAddress call(List<Address> addresses)
                                {
                                    Address addr = addresses.get(0);

                                    String add1 = addr.getAddressLine(0);
                                    String add2 = addr.getAddressLine(1);
                                    String city = addr.getAdminArea();
                                    String postal = addr.getPostalCode();

                                    return new KYCAddress(add1, add2, city, postal);
                                }
                            }),
                    Observable.just(requestCode == PICK_LOCATION_REQUEST_PRIMARY ? primaryWidget : secondaryWidget)
                            .doOnNext(new Action1<KYCAddressWidget>()
                            {
                                @Override public void call(KYCAddressWidget kycAddressWidget)
                                {
                                    kycAddressWidget.setLoading(true);
                                }
                            }),
                    new Func2<KYCAddress, KYCAddressWidget, Object>()
                    {
                        @Override public Object call(KYCAddress kycAddress, KYCAddressWidget kycAddressWidget)
                        {
                            kycAddressWidget.setKYCAddress(kycAddress);
                            kycAddressWidget.setLoading(false);
                            return null;
                        }
                    }
            ).subscribe(new EmptyAction1<>());
        }
    }
}

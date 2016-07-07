package com.androidth.general.fragments.live.ayondo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidth.general.R;
import com.androidth.general.activities.ActivityHelper;
import com.androidth.general.api.competition.EmailVerifiedDTO;
import com.androidth.general.api.competition.ProviderDTO;
import com.androidth.general.api.competition.ProviderDTOList;
import com.androidth.general.api.competition.ProviderId;
import com.androidth.general.api.competition.ProviderUtil;
import com.androidth.general.api.competition.key.ProviderListKey;
import com.androidth.general.api.kyc.CountryDocumentTypes;
import com.androidth.general.api.kyc.EmptyKYCForm;
import com.androidth.general.api.kyc.KYCForm;
import com.androidth.general.api.kyc.PhoneNumberVerifiedStatusDTO;
import com.androidth.general.api.kyc.ayondo.KYCAyondoForm;
import com.androidth.general.api.kyc.ayondo.KYCAyondoFormOptionsDTO;
import com.androidth.general.api.live.CountryUtil;
import com.androidth.general.api.live.LiveBrokerDTO;
import com.androidth.general.api.live.LiveBrokerSituationDTO;
import com.androidth.general.api.market.Country;
import com.androidth.general.api.users.CurrentUserId;
import com.androidth.general.api.users.UserBaseKey;
import com.androidth.general.api.users.UserProfileDTO;
import com.androidth.general.common.rx.PairGetSecond;
import com.androidth.general.common.utils.THToast;
import com.androidth.general.fragments.base.LollipopArrayAdapter;
import com.androidth.general.fragments.live.CountrySpinnerAdapter;
import com.androidth.general.fragments.live.DatePickerDialogFragment;
import com.androidth.general.fragments.live.VerifyEmailDialogFragment;
import com.androidth.general.fragments.live.VerifyPhoneDialogFragment;
import com.androidth.general.fragments.web.BaseWebViewFragment;
import com.androidth.general.models.fastfill.Gender;
import com.androidth.general.network.LiveNetworkConstants;
import com.androidth.general.network.retrofit.RequestHeaders;
import com.androidth.general.network.service.KycServicesRx;
import com.androidth.general.network.service.LiveServiceWrapper;
import com.androidth.general.network.service.SignalRManager;
import com.androidth.general.persistence.competition.ProviderCacheRx;
import com.androidth.general.persistence.competition.ProviderListCacheRx;
import com.androidth.general.persistence.user.UserProfileCacheRx;
import com.androidth.general.rx.EmptyAction1;
import com.androidth.general.rx.TimberOnErrorAction1;
import com.androidth.general.rx.view.adapter.AdapterViewObservable;
import com.androidth.general.rx.view.adapter.OnItemSelectedEvent;
import com.androidth.general.rx.view.adapter.OnSelectedEvent;
import com.androidth.general.utils.DateUtils;
import com.androidth.general.utils.metrics.appsflyer.AppsFlyerConstants;
import com.androidth.general.utils.metrics.appsflyer.THAppsFlyer;
import com.androidth.general.utils.route.THRouter;
import com.androidth.general.widget.validation.KYCVerifyButton;
import com.androidth.general.widget.validation.VerifyButtonState;
import com.neovisionaries.i18n.CountryCode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

//@Routable({
//        "enrollchallenge/:enrollProviderId"
//})
public class LiveSignUpStep1AyondoFragment extends LiveSignUpStepBaseAyondoFragment
{
    //@RouteProperty("enrollProviderId") protected Integer enrollProviderId;
    @Inject THRouter thRouter;
    @Inject KycServicesRx kycServices;
    @Inject ProviderUtil providerUtil;
    private static final int PHONE_NUM_MIN_LENGTH = 7;

    @LayoutRes private static final int LAYOUT_COUNTRY = R.layout.spinner_live_country_dropdown_item;
    @LayoutRes private static final int LAYOUT_COUNTRY_SELECTED_FLAG = R.layout.spinner_live_country_dropdown_item_selected;
    @LayoutRes private static final int LAYOUT_PHONE_COUNTRY = R.layout.spinner_live_phone_country_dropdown_item;
    @LayoutRes private static final int LAYOUT_PHONE_SELECTED_FLAG = R.layout.spinner_live_phone_country_dropdown_item_selected;
    private static final int REQUEST_PICK_DATE = 2805;
    private static final int REQUEST_VERIFY_PHONE_NUMBER_CODE = 2808;
    private static final int REQUEST_VERIFY_EMAIL_CODE = 2809;
    private static final String KEY_EXPECTED_SMS_CODE = LiveSignUpStep1AyondoFragment.class.getName() + ".expectedCode";
    private static final String KEY_SMS_ID = LiveSignUpStep1AyondoFragment.class.getName() + ".smsId";

    @Bind(R.id.nric_number) EditText nricNumber;
    @Bind(R.id.info_title) Spinner title;
    @Bind(R.id.info_first_name) TextView firstName;
    @Bind(R.id.info_last_name) TextView lastName;
    @Bind(R.id.sign_up_email) EditText email;
    @Bind(R.id.country_code_spinner) Spinner spinnerPhoneCountryCode;
    @Bind(R.id.info_phone_number) EditText phoneNumber;
    @Bind(R.id.info_dob) TextView dob;
    @Bind(R.id.step_1_tnc_checkbox) CheckBox tncCheckbox;
    @Bind(R.id.step_1_tnc) TextView termsCond;
    //@Bind(R.id.btn_verify_phone) TextView buttonVerifyPhone;
    //@Bind(R.id.info_nationality) Spinner spinnerNationality;
    //@Bind(R.id.info_residency) Spinner spinnerResidency;

    @Bind(R.id.email_verify_button) KYCVerifyButton emailVerifybutton;
    @Bind(R.id.nric_verify_button) KYCVerifyButton nricVerifyButton;
    @Bind(R.id.phone_verify_button) KYCVerifyButton phoneVerifyButton;

    @Bind(R.id.residence_state) Spinner spinnerResidenceState;
    @Bind(R.id.how_you_know_th) Spinner spinnerHowYouKnowTH;
    @Bind(R.id.btn_join_competition) Button joinCompetitionButton;

    @Inject ProviderCacheRx providerCache;
    @Inject ProviderListCacheRx providerListCache;
    @Inject CurrentUserId currentUserId;
    @Inject UserProfileCacheRx userProfileCache;
    @Inject LiveServiceWrapper liveServiceWrapper;
    @Inject protected RequestHeaders requestHeaders;
    SignalRManager signalRManager;

    private Pattern emailPattern;
    private String expectedCode;
    private String smsId;
    private ProviderId providerId;
    private PublishSubject<Pair<Integer, String>> verifiedPublishSubject;
    private static PublishSubject<String> verifiedPublishEmail;
    private Drawable noErrorIconDrawable;
    private int providerIdInt = 0;
    private VerifyEmailDialogFragment vedf;

    HubProxy proxy;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        thRouter.inject(this);
        verifiedPublishSubject = PublishSubject.create();
        verifiedPublishEmail = PublishSubject.create();
        if (savedInstanceState != null)
        {
            expectedCode = savedInstanceState.getString(KEY_EXPECTED_SMS_CODE, null);
            smsId = savedInstanceState.getString(KEY_SMS_ID, null);
        }

        emailPattern = Pattern.compile(getString(R.string.regex_email_validator));
        noErrorIconDrawable = getResources().getDrawable(R.drawable.red_alert);
        if (noErrorIconDrawable != null)
        {
            noErrorIconDrawable.setBounds(0,0,0,0);
        }
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_sign_up_live_ayondo_step_1, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(getActivity());
        termsCond.setOnClickListener(click->{
            Bundle args = new Bundle();
            BaseWebViewFragment.putUrl(args, providerUtil.getTermsPage(providerId));

            if (navigator != null)
            {
                navigator.get().pushFragment(BaseWebViewFragment.class, args);
            }
        });

        phoneNumber.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && phoneVerifyButton.getState() == VerifyButtonState.PENDING)
            {
                offerToEnterCode();
            }

            return false;
        });

        providerIdInt = getProviderId(getArguments());

        if (providerIdInt != 0) {
            this.providerId = new ProviderId(providerIdInt);

            providerCache.get(providerId).subscribe(providerIdProviderDTOPair -> {
                ProviderDTO providerDTO = providerIdProviderDTOPair.second;

                if (providerDTO != null) {
                    if (btnNext != null)
                    {
                        if(providerDTO.isUserEnrolled){
                            btnNext.setVisibility(View.VISIBLE);
                            joinCompetitionButton.setVisibility(View.GONE);
                        }else{
                            btnNext.setVisibility(View.GONE);
                            joinCompetitionButton.setVisibility(View.VISIBLE);
                            joinCompetitionButton.setEnabled(true);
                        }
                    }

                    if (btnPrev != null)
                    {
                        btnPrev.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override protected List<Subscription> onInitAyondoSubscription(Observable<LiveBrokerDTO> brokerDTOObservable,
            final Observable<LiveBrokerSituationDTO> liveBrokerSituationDTOObservable,
            Observable<KYCAyondoFormOptionsDTO> kycAyondoFormOptionsDTOObservable)
    {
        List<Subscription> subscriptions = new ArrayList<>();
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getContext(),R.array.live_title_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        title.setAdapter(adapter);


        WidgetObservable.text(firstName).withLatestFrom(liveBrokerSituationDTOObservable,
                (onTextChangeEvent, liveBrokerSituationDTO) -> {
                    KYCAyondoForm updated = KYCAyondoFormFactory.fromFirstNameEvent(onTextChangeEvent);

                    return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, updated);
                }).subscribe(this::onNext);

        WidgetObservable.text(lastName).withLatestFrom(liveBrokerSituationDTOObservable,
                (onTextChangeEvent, liveBrokerSituationDTO) -> {
                    KYCAyondoForm updated = KYCAyondoFormFactory.fromLastNameEvent(onTextChangeEvent);

                    return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, updated);
                }).subscribe(this::onNext);

        WidgetObservable.text(nricNumber)
                .doOnNext(onTextChangeEvent -> {
                    if (onTextChangeEvent.text().length() != 12) {
                        nricVerifyButton.setState(VerifyButtonState.BEGIN);
                    } else if (onTextChangeEvent.text().length() == 12) {
                        nricVerifyButton.setState(VerifyButtonState.PENDING);
                    }
                })
                .withLatestFrom(liveBrokerSituationDTOObservable,
                (onTextChangeEvent, liveBrokerSituationDTO) -> {
                    KYCAyondoForm updated = KYCAyondoFormFactory.fromIdentificationNumber(onTextChangeEvent);

                    return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, updated);
                }).subscribe(this::onNext);

        WidgetObservable.text(email)
                .doOnNext(onTextChangeEvent -> {
                    if (isValidEmail(onTextChangeEvent.text().toString())) {
                        emailVerifybutton.setState(VerifyButtonState.PENDING);
                    } else {
                        emailVerifybutton.setState(VerifyButtonState.BEGIN);
                    }
                })
                .withLatestFrom(liveBrokerSituationDTOObservable,
                (onTextChangeEvent, liveBrokerSituationDTO) -> {
                    KYCAyondoForm updated = KYCAyondoFormFactory.fromEmailEvent(onTextChangeEvent);

                    return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, updated);
                }).subscribe(this::onNext);

        WidgetObservable.text(dob).withLatestFrom(liveBrokerSituationDTOObservable,
                (onTextChangeEvent, liveBrokerSituationDTO) -> {
                    KYCAyondoForm updated = KYCAyondoFormFactory.fromDobEvent(onTextChangeEvent);

                    return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, updated);
                }).subscribe(this::onNext);

        AdapterViewObservable.selects(spinnerPhoneCountryCode).withLatestFrom(liveBrokerSituationDTOObservable,
                new Func2<OnSelectedEvent, LiveBrokerSituationDTO, LiveBrokerSituationDTO>()
                {
                    public LiveBrokerSituationDTO call(OnSelectedEvent onSelectedEvent, LiveBrokerSituationDTO liveBrokerSituationDTO) {
                        KYCAyondoForm updated = KYCAyondoFormFactory.fromPhoneCountryCode(onSelectedEvent);

                        return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, updated);
                    }
                }).subscribe(this::onNext);


        // clicks observable
        ViewObservable.clicks(nricVerifyButton)
                .withLatestFrom(liveBrokerSituationDTOObservable, (onClickEvent, liveBrokerSituationDTO) -> liveBrokerSituationDTO)
                .doOnError(throwable1 -> {
                    Log.v(getTag(), "NRIC ERROR");
                })
                .subscribe((LiveBrokerSituationDTO liveBrokerSituationDTO) -> {
                    switch (nricVerifyButton.getState()) {
                        case BEGIN:
                            nricNumber.setError("NRIC must be 12 digits.", noErrorIconDrawable);
                            nricVerifyButton.setState(VerifyButtonState.ERROR);
                            break;
                        case PENDING:
                        case VALIDATE:
                            if (liveBrokerSituationDTO.kycForm instanceof KYCAyondoForm) {
                                KYCAyondoForm form = (KYCAyondoForm)liveBrokerSituationDTO.kycForm;

                                ProgressDialog progress = new ProgressDialog(getContext());
                                progress.setMessage("Loading...");
                                progress.show();

                                liveServiceWrapper.documentsForCountry(form.getNationality().getAlpha2()).subscribe(
                                        countryDocumentTypes -> {
                                            // possible have multiple items, currently UI is hardcoded for NRIC, I(James) still not sure how to handle this things because the form is not totally dynamic...
                                            // need to thing some ways to handle all the cases and make it dynamic.

                                            for (CountryDocumentTypes countryDocumentType : countryDocumentTypes)
                                            {
                                                if (countryDocumentType.validation != null)
                                                {
                                                    Map queryParameters = new HashMap<String, String>();
                                                    queryParameters.put(LiveServiceWrapper.PROVIDER_ID, Integer.toString(getProviderId(getArguments())));
                                                    queryParameters.put(LiveServiceWrapper.INPUT, nricNumber.getText().toString());
                                                    liveServiceWrapper.validateData(countryDocumentType.validation, queryParameters)
                                                            .subscribeOn(Schedulers.newThread())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(aBoolean -> {
//                                                                if (aBoolean)
//                                                                {
                                                                    nricVerifyButton.setState(VerifyButtonState.FINISH);
//                                                                }

                                                                progress.dismiss();
                                                            }, throwable -> {
//                                                                THToast.show(throwable.getMessage());
                                                                nricVerifyButton.setState(VerifyButtonState.ERROR);
                                                                progress.dismiss();
                                                            });
                                                }
                                            }
                                        }, throwable -> {
                                            THToast.show(throwable.getMessage());
                                            progress.dismiss();
                                        });
                            }
                            break;
                    }
                });

        ViewObservable.clicks(emailVerifybutton)
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {switch (emailVerifybutton.getState()) {
                            case BEGIN:
                                email.setError(LiveSignUpStep1AyondoFragment.this.getString(R.string.validation_incorrect_pattern_email), noErrorIconDrawable);
                                emailVerifybutton.setState(VerifyButtonState.ERROR);
                                break;
                            case PENDING:
                            case VALIDATE:
                                LiveSignUpStep1AyondoFragment.this.validateEmail();
                                break;
                        }
                    }
                });

        ViewObservable.clicks(phoneVerifyButton).subscribe(onClickEvent -> {
            switch (phoneVerifyButton.getState()) {
                case BEGIN:
                    phoneNumber.setError("Mobile number cannot less than 7 digits.", noErrorIconDrawable);
                    phoneVerifyButton.setState(VerifyButtonState.ERROR);
                    break;
                case PENDING:
                case VALIDATE:
                    offerToEnterCode();
            }
        });

        //AdapterViewObservable.selects(title).subscribe(KYCAyondoFormFactory::fromTitleEvent);
        //AdapterViewObservable.selects(spinnerNationality).subscribe(KYCAyondoFormFactory::fromNationalityEvent);
        //AdapterViewObservable.selects(spinnerResidency).subscribe(KYCAyondoFormFactory::fromResidencyEvent);

        subscriptions.add(Observable.combineLatest(
                liveBrokerSituationDTOObservable
                        .take(1)
                        .observeOn(AndroidSchedulers.mainThread()),
                kycAyondoFormOptionsDTOObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Func1<KYCAyondoFormOptionsDTO, CountryDTOForSpinner>()
                        {
                            @Override public CountryDTOForSpinner call(KYCAyondoFormOptionsDTO kycAyondoFormOptionsDTO)
                            {
                                return new CountryDTOForSpinner(getActivity(), kycAyondoFormOptionsDTO);
                            }
                        })
                        .distinctUntilChanged()
                        .doOnNext(new Action1<CountryDTOForSpinner>()
                        {
                            @Override public void call(CountryDTOForSpinner options)
                            {
                                LollipopArrayAdapter<GenderDTO> genderAdapter = new LollipopArrayAdapter<>(
                                        getActivity(),
                                        GenderDTO.createList(getResources(), options.genders));
                                title.setAdapter(genderAdapter);
                                title.setEnabled(options.genders.size() > 1);

                                CountrySpinnerAdapter phoneCountryCodeAdapter =
                                        new CountrySpinnerAdapter(getActivity(), LAYOUT_PHONE_SELECTED_FLAG, LAYOUT_PHONE_COUNTRY);
                                phoneCountryCodeAdapter.addAll(options.allowedMobilePhoneCountryDTOs);
                                spinnerPhoneCountryCode.setAdapter(phoneCountryCodeAdapter);
                                spinnerPhoneCountryCode.setEnabled(options.allowedMobilePhoneCountryDTOs.size() > 1);

                                //CountrySpinnerAdapter residencyAdapter =
                                //        new CountrySpinnerAdapter(getActivity(), LAYOUT_COUNTRY_SELECTED_FLAG, LAYOUT_COUNTRY);
                                //residencyAdapter.addAll(options.allowedResidencyCountryDTOs);
                                //spinnerResidency.setAdapter(residencyAdapter);
                                //spinnerResidency.setEnabled(options.allowedResidencyCountryDTOs.size() > 1);
                                //
                                //CountrySpinnerAdapter nationalityAdapter =
                                //        new CountrySpinnerAdapter(getActivity(), LAYOUT_COUNTRY_SELECTED_FLAG, LAYOUT_COUNTRY);
                                //nationalityAdapter.addAll(options.allowedNationalityCountryDTOs);
                                //spinnerNationality.setAdapter(nationalityAdapter);
                                //spinnerNationality.setEnabled(options.allowedNationalityCountryDTOs.size() > 1);

                                LollipopArrayAdapter<String> residenceStateAdapter = new LollipopArrayAdapter<>(
                                        getActivity(), options.residenceStateList);
                                spinnerResidenceState.setAdapter(residenceStateAdapter);
                                spinnerResidenceState.setEnabled(options.residenceStateList.size() > 1);

                                LollipopArrayAdapter<String> howYouKnowTHAdapter = new LollipopArrayAdapter<>(
                                        getActivity(), options.howYouKnowTHList);
                                spinnerHowYouKnowTH.setAdapter(howYouKnowTHAdapter);
                                spinnerHowYouKnowTH.setEnabled(options.howYouKnowTHList.size() > 1);
                            }
                        }),
                userProfileCache.getOne(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<UserBaseKey, UserProfileDTO>())
                        .observeOn(AndroidSchedulers.mainThread()),
                new Func3<LiveBrokerSituationDTO, CountryDTOForSpinner, UserProfileDTO, LiveBrokerSituationDTO>()
                {
                    @Override public LiveBrokerSituationDTO call(LiveBrokerSituationDTO situation,
                            CountryDTOForSpinner options,
                            UserProfileDTO currentUserProfile)
                    {
                        ////noinspection ConstantConditions
                        LiveBrokerSituationDTO latestDTO = situation;

                        if (situation.kycForm instanceof EmptyKYCForm) {
                            KYCAyondoForm defaultForm = new KYCAyondoForm();
                            defaultForm.pickFromWithDefaultValues(currentUserProfile);

                            latestDTO = new LiveBrokerSituationDTO(situation.broker, defaultForm);
                        }

                        if ((KYCAyondoForm) latestDTO.kycForm != null)
                        {
                            ProviderDTO providerDTO = providerCache.getCachedValue(new ProviderId(getProviderId(getArguments())));

                            if (providerDTO != null) {
                                if (providerDTO.isStrictlyForProviderCountry && providerDTO.providerCountries.length == 1) {
                                    KYCAyondoForm updated = new KYCAyondoForm();
                                    updated.setNationality(CountryCode.getByCode(providerDTO.providerCountries[0]));
                                    updated.setResidency(CountryCode.getByCode(providerDTO.providerCountries[0]));

                                    //latestDTO = new LiveBrokerSituationDTO(latestDTO.broker, updated);
                                    onNext(new LiveBrokerSituationDTO(latestDTO.broker, updated));
                                }
                            }

                            populate((KYCAyondoForm) latestDTO.kycForm);
                            populateGender((KYCAyondoForm) latestDTO.kycForm, options.genders);
                            populateMobileCountryCode((KYCAyondoForm) latestDTO.kycForm, currentUserProfile,
                                    options.allowedMobilePhoneCountryDTOs);
                            populateNationality((KYCAyondoForm) latestDTO.kycForm, currentUserProfile, options.allowedNationalityCountryDTOs);
                            populateResidency((KYCAyondoForm) latestDTO.kycForm, currentUserProfile, options.allowedResidencyCountryDTOs);
                        }

                        return latestDTO;
                    }
                })
                .subscribe(
                        new EmptyAction1<LiveBrokerSituationDTO>(),
                        new TimberOnErrorAction1("Failed to load phone drop down lists")));

        subscriptions.add(
                Observable.combineLatest(
                        AdapterViewObservable.selects(spinnerPhoneCountryCode)
                                .filter(new Func1<OnSelectedEvent, Boolean>()
                                {
                                    @Override public Boolean call(OnSelectedEvent onSelectedEvent)
                                    {
                                        return onSelectedEvent instanceof OnItemSelectedEvent;
                                    }
                                })
                                .cast(OnItemSelectedEvent.class),
                        WidgetObservable.text(phoneNumber),
                        new Func2<OnItemSelectedEvent, OnTextChangeEvent, PhoneNumberDTO>()
                        {
                            @Override public PhoneNumberDTO call(OnItemSelectedEvent onSelectedEvent, OnTextChangeEvent onTextChangeEvent)
                            {
                                CountrySpinnerAdapter.DTO selectedDTO = (CountrySpinnerAdapter.DTO) onSelectedEvent.parent.getItemAtPosition(
                                        onSelectedEvent.position);
                                return new PhoneNumberDTO(
                                        selectedDTO.country,
                                        selectedDTO.phoneCountryCode,
                                        onTextChangeEvent.text().toString());
                            }
                        })
                        .doOnNext(new Action1<PhoneNumberDTO>()
                        {
                            @Override public void call(PhoneNumberDTO phoneNumberDTO)
                            {
                                //buttonVerifyPhone.setText(R.string.verify);
                                //buttonVerifyPhone.setEnabled(false);
                                //if (isValidPhoneNumber(phoneNumberDTO))
                                //{
                                //    buttonVerifyPhone.setBackgroundResource(R.drawable.basic_green_selector);
                                //    buttonVerifyPhone.setEnabled(true);
                                //}

                                if (isValidPhoneNumber(phoneNumberDTO)) {
                                    phoneVerifyButton.setState(VerifyButtonState.PENDING);
                                } else {
                                    phoneVerifyButton.setState(VerifyButtonState.BEGIN);
                                }
                            }
                        })
                        .distinctUntilChanged()
                        .doOnNext(new Action1<PhoneNumberDTO>()
                        {
                            @Override public void call(PhoneNumberDTO phoneNumberDTO)
                            {
                                smsId = null;
                                expectedCode = null;
                            }
                        })
                        .withLatestFrom(liveBrokerSituationDTOObservable, new Func2<PhoneNumberDTO, LiveBrokerSituationDTO, PhoneNumberDTO>()
                        {
                            @Override public PhoneNumberDTO call(PhoneNumberDTO phoneNumberDTO, LiveBrokerSituationDTO liveBrokerSituationDTO)
                            {
                                KYCAyondoForm update = new KYCAyondoForm();

                                String newNumber = phoneNumberDTO.typedNumber;

                                update.setPhonePrimaryCountryCode(phoneNumberDTO.dialingCountry);
                                update.setMobileNumber(newNumber);

                                onNext(new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, update));

                                return phoneNumberDTO;
                            }
                        })
                        .filter(new Func1<PhoneNumberDTO, Boolean>()
                        {
                            @Override public Boolean call(PhoneNumberDTO phoneNumberDTO)
                            {
                                return isValidPhoneNumber(phoneNumberDTO);
                            }
                        })
                        .flatMap(new Func1<PhoneNumberDTO, Observable<PhoneNumberAndVerifiedDTO>>()
                        {
                            @Override public Observable<PhoneNumberAndVerifiedDTO> call(
                                    final PhoneNumberDTO numberDTO)
                            {
                                String numberText = VerifyPhoneDialogFragment.getFormattedPhoneNumber(numberDTO.dialingPrefix, numberDTO.typedNumber);
                                return liveServiceWrapper.getPhoneNumberVerifiedStatus(numberText)
                                        .map(new Func1<PhoneNumberVerifiedStatusDTO, PhoneNumberAndVerifiedDTO>()
                                        {
                                            @Override public PhoneNumberAndVerifiedDTO call(
                                                    PhoneNumberVerifiedStatusDTO verifiedStatusDTO)
                                            {
                                                return new PhoneNumberAndVerifiedDTO(
                                                        numberDTO.dialingCountry,
                                                        numberDTO.dialingPrefix,
                                                        numberDTO.typedNumber,
                                                        verifiedStatusDTO.verified);
                                            }
                                        });
                            }
                        })
                        .withLatestFrom(liveBrokerSituationDTOObservable,
                                new Func2<PhoneNumberAndVerifiedDTO, LiveBrokerSituationDTO, LiveBrokerSituationDTO>()
                                {
                                    @Override public LiveBrokerSituationDTO call(PhoneNumberAndVerifiedDTO phoneNumberAndVerifiedDTO,
                                            LiveBrokerSituationDTO liveBrokerSituationDTO)
                                    {
                                        KYCAyondoForm update = new KYCAyondoForm();

                                        int dialingPrefix = phoneNumberAndVerifiedDTO.dialingPrefix;
                                        String newNumber = phoneNumberAndVerifiedDTO.typedNumber;

                                        if (phoneNumberAndVerifiedDTO.verified)
                                        {
                                            update.setVerifiedMobileNumberDialingPrefix(dialingPrefix);
                                            update.setVerifiedMobileNumber(newNumber);
                                        }

                                        //noinspection ConstantConditions
                                        liveBrokerSituationDTO.kycForm.pickFrom(update);
                                        populateVerifyMobile((KYCAyondoForm) liveBrokerSituationDTO.kycForm, phoneNumberAndVerifiedDTO);

                                        return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, update);
                                    }
                                })
                        .subscribe(new Action1<LiveBrokerSituationDTO>()
                        {
                            @Override public void call(LiveBrokerSituationDTO liveBrokerSituationDTO)
                            {
                                onNext(liveBrokerSituationDTO);
                            }
                        }, new Action1<Throwable>()
                        {
                            @Override public void call(Throwable throwable)
                            {

                            }
                        }));

        subscriptions.add(ViewObservable.clicks(dob)
                .withLatestFrom(kycAyondoFormOptionsDTOObservable, new Func2<OnClickEvent, KYCAyondoFormOptionsDTO, KYCAyondoFormOptionsDTO>()
                {
                    @Override public KYCAyondoFormOptionsDTO call(OnClickEvent onClickEvent, KYCAyondoFormOptionsDTO kycAyondoFormOptionsDTO)
                    {
                        return kycAyondoFormOptionsDTO;
                    }
                })
                .map(new Func1<KYCAyondoFormOptionsDTO, Calendar>()
                {
                    @Override public Calendar call(KYCAyondoFormOptionsDTO kycAyondoFormOptionsDTO)
                    {
                        Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR) - kycAyondoFormOptionsDTO.minAge;
                        c.set(Calendar.YEAR, year);
                        return c;
                    }
                })
                .subscribe(new Action1<Calendar>()
                {
                    @Override public void call(Calendar calendar)
                    {
                        Calendar selected = null;
                        if (!TextUtils.isEmpty(dob.getText()))
                        {
                            Date d = DateUtils.parseString(dob.getText().toString(), KYCAyondoForm.DATE_FORMAT_AYONDO);
                            if (d != null)
                            {
                                selected = Calendar.getInstance();
                                selected.setTime(d);
                            }
                        }
                        DatePickerDialogFragment dpf = DatePickerDialogFragment.newInstance(calendar, selected);
                        dpf.setTargetFragment(LiveSignUpStep1AyondoFragment.this, REQUEST_PICK_DATE);
                        dpf.show(getChildFragmentManager(), dpf.getClass().getName());
                    }
                }, new TimberOnErrorAction1("Failed to listen to DOB clicks")));

        //subscriptions.add(ViewObservable.clicks(buttonVerifyPhone)
        //        .withLatestFrom(liveBrokerSituationDTOObservable, new Func2<OnClickEvent, LiveBrokerSituationDTO, LiveBrokerSituationDTO>()
        //        {
        //            @Override public LiveBrokerSituationDTO call(OnClickEvent onClickEvent, LiveBrokerSituationDTO liveBrokerSituationDTO)
        //            {
        //                return liveBrokerSituationDTO;
        //            }
        //        })
        //        .subscribe(new Action1<LiveBrokerSituationDTO>()
        //        {
        //            @Override public void call(LiveBrokerSituationDTO liveBrokerSituationDTO)
        //            {
        //                offerToEnterCode();
        //            }
        //        }, new TimberOnErrorAction1("Failed to present verify phone dialog")));

        subscriptions.add(verifiedPublishSubject.withLatestFrom(liveBrokerSituationDTOObservable,
                new Func2<Pair<Integer, String>, LiveBrokerSituationDTO, LiveBrokerSituationDTO>()
                {
                    @Override
                    public LiveBrokerSituationDTO call(Pair<Integer, String> verifiedPhonePair, LiveBrokerSituationDTO liveBrokerSituationDTO)
                    {
                        KYCAyondoForm update = new KYCAyondoForm();
                        update.setVerifiedMobileNumberDialingPrefix(verifiedPhonePair.first);
                        update.setVerifiedMobileNumber(verifiedPhonePair.second);
                        //noinspection ConstantConditions

                        liveServiceWrapper.submitPhoneNumberVerifiedStatus(
                                VerifyPhoneDialogFragment.getFormattedPhoneNumber(verifiedPhonePair.first, verifiedPhonePair.second));
                        //buttonVerifyPhone.setEnabled(false);
                        //buttonVerifyPhone.setText(R.string.verified);
                        phoneVerifyButton.setState(VerifyButtonState.FINISH);
                        return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, update);
                    }
                }).subscribe(
                new Action1<LiveBrokerSituationDTO>()
                {
                    @Override public void call(LiveBrokerSituationDTO liveBrokerSituationDTO)
                    {
                        onNext(liveBrokerSituationDTO);
                    }
                }, new TimberOnErrorAction1("Failed to update verified mobile number")));

        subscriptions.add(verifiedPublishEmail.withLatestFrom(liveBrokerSituationDTOObservable,
                new Func2<String, LiveBrokerSituationDTO, LiveBrokerSituationDTO>()
                {
                    @Override
                    public LiveBrokerSituationDTO call(String verifiedEmail, LiveBrokerSituationDTO liveBrokerSituationDTO)
                    {
                        KYCAyondoForm update = new KYCAyondoForm();
                        update.setVerifiedEmailAddress(verifiedEmail);
                        return new LiveBrokerSituationDTO(liveBrokerSituationDTO.broker, update);
                    }
                }).subscribe(
                new Action1<LiveBrokerSituationDTO>()
                {
                    @Override public void call(LiveBrokerSituationDTO liveBrokerSituationDTO)
                    {
                        onNext(liveBrokerSituationDTO);
                    }
                }, new TimberOnErrorAction1("Failed to update email address")));

        Log.v("ayondoStep1", "Subscriptions final "+subscriptions.size());
        return subscriptions;
    }

    private boolean isValidPhoneNumber(PhoneNumberDTO phoneNumberDTO)
    {
        return phoneNumberDTO.dialingPrefix > 0 && phoneNumberDTO.typedNumber.length() > PHONE_NUM_MIN_LENGTH;
    }

    private boolean isValidEmail(String email)
    {
        return emailPattern.matcher(email).matches();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        super.onDestroy();
        verifiedPublishSubject = null;
        verifiedPublishEmail = null;
    }

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_EXPECTED_SMS_CODE, expectedCode);
        outState.putString(KEY_SMS_ID, smsId);
    }

    @Override public void onNext(@NonNull LiveBrokerSituationDTO situationDTO)
    {
        super.onNext(situationDTO);
        if (!(situationDTO.kycForm instanceof KYCAyondoForm))
        {
            Timber.e(new IllegalArgumentException(), "Should not submit a situation.KYC of type: %s", situationDTO.kycForm);
        }
        // TODO
    }

    @MainThread
    protected void populate(@NonNull KYCAyondoForm kycForm)
    {
        String firstNameText = kycForm.getFirstName();
        if (firstName != null && firstNameText != null && !firstNameText.equals(firstName.getText().toString()))
        {
            firstName.setText(firstNameText);
        }

        String lastNameText = kycForm.getLastName();
        if (lastName != null && lastNameText != null && !lastNameText.equals(lastName.getText().toString()))
        {
            lastName.setText(lastNameText);
        }

        String emailText = kycForm.getEmail();
        if (email != null && emailText != null && !emailText.equals(email.getText().toString()))
        {
            email.setText(emailText);
            String currentVerifiedEmail = kycForm.getVerifiedEmailAddress();
            if(currentVerifiedEmail!=null && currentVerifiedEmail.equals(emailText)){
                emailVerifybutton.setState(VerifyButtonState.FINISH);
            }
        }

        String nricNumberText = kycForm.getIdentificationNumber();
        if (this.nricNumber != null && nricNumberText != null && !nricNumberText.equals(nricNumber.getText().toString()))
        {
            nricNumber.setText(nricNumberText);
        }

        String mobileNumberText = kycForm.getMobileNumber();
        if (phoneNumber != null && mobileNumberText != null && !mobileNumberText.equals(phoneNumber.getText().toString()))
        {
            phoneNumber.setText(mobileNumberText);
        }

        String dobText = kycForm.getDob();
        if (dob != null && dobText != null && !dobText.equals(dob.getText().toString()))
        {
            dob.setText(dobText);
        }
    }

    @MainThread
    protected void populateVerifyMobile(@NonNull KYCAyondoForm kycForm, PhoneNumberDTO phoneNumberDTO)
    {
        //if (buttonVerifyPhone != null)
        //{
        //    boolean verified = Integer.valueOf(phoneNumberDTO.dialingPrefix).equals(kycForm.getVerifiedMobileNumberDialingPrefix())
        //            && phoneNumberDTO.typedNumber.equals(kycForm.getVerifiedMobileNumber());
        //    buttonVerifyPhone.setEnabled(!verified && isValidPhoneNumber(phoneNumberDTO));
        //    buttonVerifyPhone.setText(verified ? R.string.verified : R.string.verify);
        //}

        boolean verified = Integer.valueOf(phoneNumberDTO.dialingPrefix).equals(kycForm.getVerifiedMobileNumberDialingPrefix())
                    && phoneNumberDTO.typedNumber.equals(kycForm.getVerifiedMobileNumber());

        if (verified) {
            phoneVerifyButton.setState(VerifyButtonState.FINISH);
        } else {
            if (isValidPhoneNumber(phoneNumberDTO)) {
                phoneVerifyButton.setState(VerifyButtonState.PENDING);
            } else {
                phoneVerifyButton.setState(VerifyButtonState.BEGIN);
            }
        }
    }

    @MainThread
    @NonNull protected KYCAyondoForm populateGender(
            @NonNull final KYCAyondoForm kycForm,
            @NonNull List<Gender> genders)
    {
        KYCAyondoForm update = new KYCAyondoForm();
        Gender savedGender = kycForm.getGender();
        Integer genderIndex = populateSpinner(title, savedGender, genders);
        if (savedGender == null)
        {
            Gender chosenGender;
            if (genderIndex != null)
            {
                chosenGender = genders.get(genderIndex);
            }
            else
            {
                chosenGender = ((GenderDTO) title.getSelectedItem()).gender;
            }

            if (chosenGender != null)
            {
                update.setGender(chosenGender);
            }
        }
        return update;
    }

    @MainThread
    @NonNull protected KYCAyondoForm populateMobileCountryCode(
            @NonNull final KYCAyondoForm kycForm,
            @NonNull UserProfileDTO currentUserProfile,
            @NonNull List<CountrySpinnerAdapter.DTO> liveCountryDTOs)
    {
        Integer savedMobileNumberDialingPrefix = null;
        if (kycForm.getPhonePrimaryCountryCode() != null)
        {
            savedMobileNumberDialingPrefix = CountryUtil.getPhoneCodePlusLeadingDigits(kycForm.getPhonePrimaryCountryCode());
        }
        final List<CountrySpinnerAdapter.DTO> candidates;
        if (savedMobileNumberDialingPrefix != null)
        {
            candidates = CountrySpinnerAdapter.getFilterByPhoneCountryCode(liveCountryDTOs, savedMobileNumberDialingPrefix);
        }
        else
        {
            candidates = new ArrayList<>();
        }

        KYCAyondoForm update = new KYCAyondoForm();
        candidates.addAll(getFilteredByCountries(liveCountryDTOs, kycForm, currentUserProfile));
        Integer index = setSpinnerOnFirst(spinnerPhoneCountryCode, candidates, liveCountryDTOs);
        if (savedMobileNumberDialingPrefix == null)
        {
            CountrySpinnerAdapter.DTO chosenDTO;
            if (index != null)
            {
                chosenDTO = liveCountryDTOs.get(index);
            }
            else
            {
                chosenDTO = (CountrySpinnerAdapter.DTO) spinnerPhoneCountryCode.getSelectedItem();
            }

            if (chosenDTO != null)
            {
                update.setPhonePrimaryCountryCode(chosenDTO.country);
            }
        }
        return update;
    }

    @MainThread
    @NonNull protected KYCAyondoForm populateNationality(
            @NonNull final KYCAyondoForm kycForm,
            @NonNull UserProfileDTO currentUserProfile,
            @NonNull List<CountrySpinnerAdapter.DTO> liveCountryDTOs)
    {
        CountryCode savedNationality = kycForm.getNationality();
        List<CountrySpinnerAdapter.DTO> candidates;
        if (savedNationality != null)
        {
            try
            {
                candidates = CountrySpinnerAdapter.getFilterByCountry(liveCountryDTOs,
                        Collections.singletonList(Enum.valueOf(Country.class, savedNationality.getAlpha2())));
            }
            catch (Exception e)
            {
                candidates = new ArrayList<>();
            }
        }
        else
        {
            candidates = new ArrayList<>();
        }

        KYCAyondoForm update = new KYCAyondoForm();
        candidates.addAll(getFilteredByCountries(liveCountryDTOs, kycForm, currentUserProfile));
        //Integer index = setSpinnerOnFirst(spinnerNationality, candidates, liveCountryDTOs);
        //if (savedNationality == null)
        //{
        //    CountrySpinnerAdapter.DTO chosenDTO;
        //    if (index != null)
        //    {
        //        chosenDTO = liveCountryDTOs.get(index);
        //    }
        //    else
        //    {
        //        chosenDTO = (CountrySpinnerAdapter.DTO) spinnerNationality.getSelectedItem();
        //    }
        //
        //    if (chosenDTO != null)
        //    {
        //        update.setNationality(CountryCode.getByCode(chosenDTO.country.name()));
        //    }
        //}
        return update;
    }

    @MainThread
    @NonNull protected KYCAyondoForm populateResidency(
            @NonNull final KYCAyondoForm kycForm,
            @NonNull UserProfileDTO currentUserProfile,
            @NonNull List<CountrySpinnerAdapter.DTO> liveCountryDTOs)
    {
        CountryCode savedResidency = kycForm.getResidency();
        List<CountrySpinnerAdapter.DTO> candidates;
        if (savedResidency != null)
        {
            try
            {
                candidates = CountrySpinnerAdapter.getFilterByCountry(liveCountryDTOs,
                        Collections.singletonList(Enum.valueOf(Country.class, savedResidency.getAlpha2())));
            }
            catch (Exception e)
            {
                candidates = new ArrayList<>();
            }
        }
        else
        {
            candidates = new ArrayList<>();
        }

        KYCAyondoForm update = new KYCAyondoForm();
        candidates.addAll(getFilteredByCountries(liveCountryDTOs, kycForm, currentUserProfile));
        //Integer index = setSpinnerOnFirst(spinnerResidency, candidates, liveCountryDTOs);
        //if (savedResidency == null)
        //{
        //    CountrySpinnerAdapter.DTO chosenDTO;
        //    if (index != null)
        //    {
        //        chosenDTO = liveCountryDTOs.get(index);
        //    }
        //    else
        //    {
        //        chosenDTO = (CountrySpinnerAdapter.DTO) spinnerResidency.getSelectedItem();
        //    }
        //
        //    if (chosenDTO != null)
        //    {
        //        update.setResidency(CountryCode.getByCode(chosenDTO.country.name()));
        //    }
        //}
        return update;
    }

    @NonNull protected List<CountrySpinnerAdapter.DTO> getFilteredByCountries(
            @NonNull List<CountrySpinnerAdapter.DTO> liveCountryDTOs,
            @NonNull final KYCAyondoForm kycForm,
            @NonNull UserProfileDTO currentUserProfile)
    {
        List<CountrySpinnerAdapter.DTO> defaultOnes = new ArrayList<>();
        Country formCountry = kycForm.getCountry();
        if (formCountry == null)
        {
            throw new NullPointerException("Country should not be null at this stage");
        }
        defaultOnes.addAll(CountrySpinnerAdapter.getFilterByCountry(liveCountryDTOs, Collections.singletonList(formCountry)));
        Country userCountry = currentUserProfile.getCountry();
        if (userCountry != null)
        {
            defaultOnes.addAll(CountrySpinnerAdapter.getFilterByCountry(liveCountryDTOs, Collections.singletonList(userCountry)));
        }
        return defaultOnes;
    }

    @MainThread
    protected void offerToEnterCode()
    {
       final int phoneCountryCode =
                ((CountrySpinnerAdapter.DTO) spinnerPhoneCountryCode.getSelectedItem()).phoneCountryCode;
        final String phoneNumberInt = phoneNumber.getText().toString();

        if (phoneCountryCode > 0 && phoneNumberInt.length() > PHONE_NUM_MIN_LENGTH)
        {
            if (expectedCode == null)
            {
                expectedCode = String.format("%04d", Math.abs(new Random(System.nanoTime()).nextInt() % 10000));
            }

            VerifyPhoneDialogFragment.show(REQUEST_VERIFY_PHONE_NUMBER_CODE, this, phoneCountryCode, phoneNumberInt, expectedCode);
            //buttonVerifyPhone.setText(R.string.enter_code);
            //buttonVerifyPhone.setBackgroundResource(R.drawable.basic_red_selector);
        }
    }

    @MainThread
    protected void validateEmail()
    {
        final String email = this.email.getText().toString();

//        liveServiceWrapper.verifyEmail(currentUserId.get(), email).subscribe();

        vedf = VerifyEmailDialogFragment.show(REQUEST_VERIFY_EMAIL_CODE, this, currentUserId.get(), email, this.providerIdInt);
        setupSignalR(email);
    }

    //for email subscription pop up box
    protected void checkEmailSubscription(Integer userId) {
        Subscription subs = kycServices.validatedEmail(userId ,email.getText().toString()).subscribe(kycEmailIsValid -> {
            if (kycEmailIsValid) {
                Log.i("Valid", "Cool");
                //Pop up dialog
            } else {
                Log.i("InValid", "James lied");
                //Make tick sign green
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.i("Error",throwable.getMessage());
            }
        });
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_DATE && resultCode == Activity.RESULT_OK)
        {
            Calendar c = DatePickerDialogFragment.getCalendarFromIntent(data);
            dob.setText(DateUtils.getDisplayableDate(c.getTime(), KYCAyondoForm.DATE_FORMAT_AYONDO));
        }
        else if (requestCode == REQUEST_VERIFY_PHONE_NUMBER_CODE && resultCode == Activity.RESULT_OK)
        {
            Pair<Integer, String> verifiedPhoneNumberPair = VerifyPhoneDialogFragment.getVerifiedFromIntent(data);
            if (verifiedPhoneNumberPair != null)
            {
                verifiedPublishSubject.onNext(verifiedPhoneNumberPair);
            }
        }else if (requestCode == REQUEST_VERIFY_EMAIL_CODE && resultCode == Activity.RESULT_OK)
        {
            Log.v(getTag(), "Jeff email ok");
            String verifiedEmail = VerifyEmailDialogFragment.getVerifiedFromIntent(data);
            if (verifiedEmail != null)
            {
                verifiedPublishEmail.onNext(verifiedEmail);
            }
            updateEmailVerification(data.getStringExtra("VerifiedEmailAddress"));
        }
    }

    public String getSmsId()
    {
        return smsId;
    }

    public void setSmsId(String smsId)
    {
        this.smsId = smsId;
    }

    private Boolean isAllInputValidated() {
        boolean resultFlag = true;
        if (nricNumber.length() != 12) {
            nricNumber.setError("NRIC must be 12 digits.", noErrorIconDrawable);
            resultFlag = false;
        }
        if (firstName.length() == 0) {
            firstName.setError("Must not be empty", noErrorIconDrawable);
            resultFlag = false;
        }
        if(lastName.length() == 0){
            lastName.setError("Must not be empty", noErrorIconDrawable);
            resultFlag = false;
        }

        if(nricVerifyButton.getState() != VerifyButtonState.FINISH){
            nricNumber.setError("Click right button to verify", noErrorIconDrawable);
            resultFlag = false;
        }else if(emailVerifybutton.getState() != VerifyButtonState.FINISH){
            email.setError("Click right button to verify", noErrorIconDrawable);
            if(!emailPattern.matcher(email.getText()).matches()) {
                email.setError("Invalid email address", noErrorIconDrawable);
            }
            resultFlag = false;
        }else if(phoneVerifyButton.getState() != VerifyButtonState.FINISH){
            phoneNumber.setError("Click right button to verify", noErrorIconDrawable);
            resultFlag = false;
        }

        if(dob.length() == 0){
            dob.setError("Must not be empty", noErrorIconDrawable);
            resultFlag = false;
        }
        if(!tncCheckbox.isChecked()){
            tncCheckbox.setError("Please check to agree", noErrorIconDrawable);
            resultFlag = false;
        }

        return resultFlag;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_join_competition)
    public void onClickedJoinButton() {

        if (!isAllInputValidated()) {
            return;
        }

        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setMessage("Loading...");
        progress.show();

        KYCForm kycForm = liveBrokerSituationPreference.get().kycForm;

        liveServiceWrapper.createOrUpdateLead(getProviderId(getArguments()), kycForm).subscribe(
                brokerApplicationDTO -> {
                    liveServiceWrapper.enrollCompetition(providerId.key, currentUserId.get())
                            .subscribe(aBoolean -> {
                                if (aBoolean) {
                                    ProviderListKey key = new ProviderListKey();
                                    providerListCache.invalidate(key);
                                    providerListCache.get(key).subscribe(new Action1<android.util.Pair<ProviderListKey, ProviderDTOList>>() {
                                        @Override
                                        public void call(android.util.Pair<ProviderListKey, ProviderDTOList> providerListKeyProviderDTOListPair) {
                                            ActivityHelper.launchDashboard(LiveSignUpStep1AyondoFragment.this.getActivity(), Uri.parse("tradehero://providers/" + providerId.key));
                                            THAppsFlyer.sendTrackingWithEvent(LiveSignUpStep1AyondoFragment.this.getActivity(), AppsFlyerConstants.KYC_1_SUBMIT, null);
                                        }
                                    }, throwable -> progress.dismiss());
//hacky way of doing it, TODO review jeff
                                    ActivityHelper.launchDashboard(LiveSignUpStep1AyondoFragment.this.getActivity(), Uri.parse("tradehero://providers/" + providerId.key));
                                    progress.dismiss();
                                }
                            }, throwable -> progress.dismiss());
                }, throwable -> {
                    THToast.show(throwable.getMessage());
                    progress.dismiss();
                });
    }

    @OnCheckedChanged(R.id.step_1_tnc_checkbox)
    public void tncCheckboxClicked(CheckBox checkBox) {
        Log.v(getTag(), "Jeff checkbox "+checkBox.isChecked());
    }

    @MainThread
    public void updateEmailVerification(String emailAddress){
        emailVerifybutton.setState(VerifyButtonState.FINISH);

        if(vedf.isVisible()){
            try{
                vedf.dismiss();
            }catch (Exception e){
                //might be closed or not in view
            }
        }
        verifiedPublishEmail.onNext(emailAddress);

    }

    public void setupSignalR(String emailAddress) {

        signalRManager = new SignalRManager(requestHeaders, currentUserId);
        signalRManager.initWithEvent(LiveNetworkConstants.HUB_NAME,
                "SetValidationStatus",
                new String[]{emailAddress},
                emailVerifybutton, emailVerifiedDTO ->{
                    if(((EmailVerifiedDTO)emailVerifiedDTO).isValidated()){
                        updateEmailVerification(emailAddress);
                    }
                }, EmailVerifiedDTO.class);

//        HubConnection connection = setConnection(LiveNetworkConstants.TRADEHERO_LIVE_ENDPOINT);
//        connection.setCredentials(new Credentials() {
//            @Override
//            public void prepareRequest(Request request) {
//                request.addHeader(Constants.AUTHORIZATION, requestHeaders.headerTokenLive());
//                request.addHeader(Constants.USER_ID, currentUserId.get().toString());
//            }
//        });
//        try {
//            proxy = setProxy(LiveNetworkConstants.HUB_NAME, connection);
//            connection.start().done(aVoid -> {
////                SignalRFuture<Void> signalProxy = proxy.invoke(LiveNetworkConstants.PROXY_METHOD_ADD_TO_GROUPS);
////                signalProxy.done(req -> com.tencent.mm.sdk.platformtools.Log.i("Yay", "Nayy"));
//            });
//            connection.connected(new Runnable() {
//                @Override
//                public void run() {
//                    Log.v(getTag(), "Jeff signalR connected");
////                    com.tencent.mm.sdk.platformtools.Log.i("SD", "cONNECTED");
//                }
//            });
//            connection.connectionSlow(new Runnable() {
//                @Override
//                public void run() {
////                    com.tencent.mm.sdk.platformtools.Log.i("Slow", "Slow Connection");
//                }
//            });
//            connection.reconnected(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
//            connection.closed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.v(getTag(), "Jeff signalR closed");
//                }
//            });
//            proxy.on("SetValidationStatus", emailVerifiedDTO-> {
//                Log.v(getTag(), "Jeff signalR Received "+emailVerifiedDTO.getMessage()+"::"+emailVerifiedDTO.isValidated());
//                updateVerifyEmailButton();
//            }, EmailVerifiedDTO.class);
//
//            proxy.subscribe(this);
//        } catch (Exception e) {
////            com.tencent.mm.sdk.platformtools.Log.e("Error", "Could not connect to Hub Name");
//        }
    }
//
//    public HubConnection setConnection(String url) {
//        return new HubConnection(url);
//    }
//
//    public HubProxy setProxy(String hubName, HubConnection connection) { return connection.createHubProxy(hubName); }
}

package com.tradehero.th.fragments.location;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnItemClick;
import com.tradehero.common.rx.PairGetSecond;
import com.tradehero.th.R;
import com.tradehero.th.api.market.Country;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UpdateCountryCodeDTO;
import com.tradehero.th.api.users.UpdateCountryCodeFormDTO;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.network.service.UserServiceWrapper;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.ToastAction;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.rx.view.DismissDialogAction0;
import dagger.Lazy;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

public class LocationListFragment extends BaseFragment
{
    private LocationAdapter mListAdapter;
    @Nullable private Subscription updateCountryCodeSubscription;
    protected UserProfileDTO currentUserProfile;

    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserServiceWrapper> userServiceWrapperLazy;
    @Inject UserProfileCacheRx userProfileCache;

    @Bind(android.R.id.list) ListView listView;

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mListAdapter = new LocationAdapter(
                activity,
                R.layout.settings_location_list_item);
        mListAdapter.addAll(ListedLocationDTOFactory.createListToShow());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings_location_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        listView.setAdapter(mListAdapter);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        setActionBarTitle(R.string.location_fragment_title);
        setActionBarSubtitle(R.string.location_fragment_subtitle);
    }

    @Override public void onResume()
    {
        super.onResume();
        fetchUserProfile();
    }

    @Override public void onStop()
    {
        unsubscribe(updateCountryCodeSubscription);
        updateCountryCodeSubscription = null;
        super.onStop();
    }

    @Override public void onDestroyOptionsMenu()
    {
        setActionBarSubtitle(null);
        super.onDestroyOptionsMenu();
    }

    @Override public void onDestroyView()
    {
        listView.setOnScrollListener(null);
        listView.setEmptyView(null);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override public void onDetach()
    {
        mListAdapter = null;
        super.onDetach();
    }

    protected void fetchUserProfile()
    {
        AppObservable.bindFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey())
                        .map(new PairGetSecond<UserBaseKey, UserProfileDTO>()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<UserProfileDTO>()
                        {
                            @Override public void call(UserProfileDTO profile)
                            {
                                LocationListFragment.this.linkWith(profile);
                            }
                        },
                        new ToastAction<Throwable>(getString(R.string.error_fetch_your_user_profile)));
    }

    protected void linkWith(UserProfileDTO userProfileDTO)
    {
        this.currentUserProfile = userProfileDTO;
        if (userProfileDTO != null && userProfileDTO.countryCode != null)
        {
            try
            {
                Country currentCountry = Country.valueOf(userProfileDTO.countryCode);
                mListAdapter.setCurrentCountry(currentCountry);
                listView.smoothScrollToPosition(mListAdapter.getPosition(new ListedLocationDTO(currentCountry)));
            } catch (IllegalArgumentException e)
            {
                Timber.e(e, "Does not have country code for ", userProfileDTO.countryCode);
                mListAdapter.setCurrentCountry(null);
            }
        }
        else
        {
            mListAdapter.setCurrentCountry(null);
        }
        mListAdapter.notifyDataSetChanged();
    }

    @OnItemClick(android.R.id.list)
    public void onItemClick(
            @NonNull AdapterView<?> adapterView,
            @SuppressWarnings("UnusedParameters") View view,
            int position,
            @SuppressWarnings("UnusedParameters") long l)
    {
        updateCountryCode(((ListedLocationDTO) adapterView.getItemAtPosition(position)).country.name());
    }

    protected void updateCountryCode(@NonNull String countryCode)
    {
        String currentCountryCode = currentUserProfile != null ? currentUserProfile.countryCode : null;
        if (currentCountryCode != null &&
                countryCode.equals(currentCountryCode))
        {
            // Nothing to do
            backToSettings();
            return;
        }

        final ProgressDialog progressDialog = getProgressDialogOld();

        UpdateCountryCodeFormDTO updateCountryCodeFormDTO = new UpdateCountryCodeFormDTO(countryCode);
        unsubscribe(updateCountryCodeSubscription);
        updateCountryCodeSubscription = AppObservable.bindFragment(
                this,
                userServiceWrapperLazy.get().updateCountryCodeRx(
                        currentUserId.toUserBaseKey(), updateCountryCodeFormDTO))
                .observeOn(AndroidSchedulers.mainThread())
                .finallyDo(new DismissDialogAction0(progressDialog))
                .subscribe(
                        new Action1<UpdateCountryCodeDTO>()
                        {
                            @Override public void call(UpdateCountryCodeDTO args)
                            {
                                LocationListFragment.this.backToSettings();
                            }
                        },
                        new ToastOnErrorAction());
    }

    private void backToSettings()
    {
        navigator.get().popFragment();
    }

    private ProgressDialog getProgressDialogOld()
    {
        return ProgressDialog.show(
                getActivity(),
                getString(R.string.loading_loading),
                getString(R.string.alert_dialog_please_wait),
                true);
    }
}

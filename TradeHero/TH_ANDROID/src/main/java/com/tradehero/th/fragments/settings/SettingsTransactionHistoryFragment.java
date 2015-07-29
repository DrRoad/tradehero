package com.tradehero.th.fragments.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.Bind;
import com.tradehero.common.utils.THToast;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserTransactionHistoryDTOList;
import com.tradehero.th.api.users.UserTransactionHistoryListType;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.persistence.user.UserTransactionHistoryListCacheRx;
import com.tradehero.th.rx.view.DismissDialogAction0;
import com.tradehero.th.rx.view.DismissDialogAction1;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import javax.inject.Inject;
import rx.Observer;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;

public class SettingsTransactionHistoryFragment extends BaseFragment
{
    @Bind(R.id.transaction_list) ListView transactionListView;
    private SettingsTransactionHistoryAdapter transactionListViewAdapter;

    @Inject UserTransactionHistoryListCacheRx userTransactionHistoryListCache;
    @Inject CurrentUserId currentUserId;
    @Inject Analytics analytics;

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        transactionListViewAdapter = new SettingsTransactionHistoryAdapter(
                activity,
                R.layout.fragment_settings_transaction_history_adapter);
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        setActionBarTitle(getResources().getString(R.string.settings_transaction_header));
        super.onCreateOptionsMenu(menu, inflater);
    }
    //</editor-fold>

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_settings_transaction_history, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        transactionListView.setAdapter(transactionListViewAdapter);
    }

    @Override public void onStart()
    {
        super.onStart();
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.Settings_TransactionHistory));
        fetchTransactionList();
    }

    @Override public void onDetach()
    {
        transactionListViewAdapter = null;
        super.onDetach();
    }

    protected void fetchTransactionList()
    {
        final ProgressDialog progressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.alert_dialog_please_wait),
                getString(R.string.authentication_connecting_tradehero_only),
                true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);

        UserTransactionHistoryListType key = new UserTransactionHistoryListType(currentUserId.toUserBaseKey());
        onStopSubscriptions.add(AppObservable.bindSupportFragment(
                this,
                userTransactionHistoryListCache.get(key))
                .observeOn(AndroidSchedulers.mainThread())
                .finallyDo(new DismissDialogAction0(progressDialog))
                .doOnUnsubscribe(new DismissDialogAction0(progressDialog))
                .doOnNext(new DismissDialogAction1<Pair<UserTransactionHistoryListType, UserTransactionHistoryDTOList>>(progressDialog))
                .subscribe(new SettingsTransactionHistoryListObserver()));
    }

    protected class SettingsTransactionHistoryListObserver implements Observer<Pair<UserTransactionHistoryListType, UserTransactionHistoryDTOList>>
    {
        @Override public void onNext(
                Pair<UserTransactionHistoryListType, UserTransactionHistoryDTOList> pair)
        {
            transactionListViewAdapter.setItems(pair.second);
            transactionListViewAdapter.notifyDataSetChanged();
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            THToast.show("Unable to fetch transaction history. Please try again later.");
        }
    }
}


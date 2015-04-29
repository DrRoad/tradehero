package com.tradehero.th.fragments.alert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClickSticky;
import com.tradehero.common.billing.BillingConstants;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.R;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.alert.AlertCompactDTOList;
import com.tradehero.th.api.system.SystemStatusDTO;
import com.tradehero.th.api.system.SystemStatusKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.billing.ProductIdentifierDomain;
import com.tradehero.th.billing.SecurityAlertKnowledge;
import com.tradehero.th.billing.THBillingInteractorRx;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.persistence.alert.AlertCompactListCacheRx;
import com.tradehero.th.persistence.system.SystemStatusCache;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.rx.TimberOnErrorAction;
import com.tradehero.th.rx.ToastOnErrorAction;
import com.tradehero.th.widget.list.BaseListHeaderView;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class AlertManagerFragment extends BaseFragment
{
    public static final String BUNDLE_KEY_USER_ID = AlertManagerFragment.class.getName() + ".userId";

    @InjectView(R.id.manage_alerts_header) View planHeader;
    @InjectView(R.id.manage_alerts_count) TextView alertPlanCount;
    @InjectView(R.id.icn_manage_alert_count) ImageView alertPlanCountIcon;
    @InjectView(R.id.progress_animator) BetterViewAnimator progressAnimator;
    @InjectView(R.id.btn_upgrade_plan) ImageButton btnPlanUpgrade;
    @InjectView(R.id.alerts_list) StickyListHeadersListView alertListView;
    protected BaseListHeaderView footerView;

    @Inject CurrentUserId currentUserId;
    @Inject THBillingInteractorRx userInteractorRx;
    @Inject SystemStatusCache systemStatusCache;
    @Inject AlertCompactListCacheRx alertCompactListCache;
    @Inject UserProfileCacheRx userProfileCache;

    protected UserProfileDTO currentUserProfile;
    private AlertListItemAdapter alertListItemAdapter;

    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        HierarchyInjector.inject(this);
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        alertListItemAdapter = new AlertListItemAdapter(getActivity(), currentUserId, R.layout.alert_list_item);
    }

    @SuppressLint("InflateParams")
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_store_manage_alerts, container, false);
        footerView = (BaseListHeaderView) inflater.inflate(R.layout.alert_manage_subscription_view, null);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        alertListView.addFooterView(footerView);
        alertListView.setAdapter(alertListItemAdapter);

        displayAlertCount();
        displayAlertCountIcon();

        footerView.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                handleManageSubscriptionClicked();
            }
        });
    }

    @Override public void onStart()
    {
        super.onStart();
        fetchSystemStatus();
        fetchUserProfile();
        fetchAlertCompactList();
    }

    @Override public void onDestroyView()
    {
        if (alertListView != null)
        {
            alertListView.setOnScrollListener(null);
        }
        alertListView = null;

        if (footerView != null)
        {
            footerView.setOnClickListener(null);
        }
        footerView = null;
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        alertListItemAdapter = null;
        super.onDestroy();
    }

    protected void fetchSystemStatus()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                systemStatusCache.getOne(new SystemStatusKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Pair<SystemStatusKey, SystemStatusDTO>>()
                        {
                            @Override public void call(Pair<SystemStatusKey, SystemStatusDTO> statusPair)
                            {
                                linkWith(statusPair.second);
                            }
                        },
                        new TimberOnErrorAction("Failed to fetch system status")));
    }

    protected void linkWith(@NonNull SystemStatusDTO status)
    {
        planHeader.setVisibility(status.alertsAreFree ? View.GONE : View.VISIBLE);
        if (status.alertsAreFree)
        {
            alertListView.removeFooterView(footerView);
        }
    }

    protected void fetchUserProfile()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                userProfileCache.get(currentUserId.toUserBaseKey()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Pair<UserBaseKey, UserProfileDTO>>()
                        {
                            @Override public void call(Pair<UserBaseKey, UserProfileDTO> pair)
                            {
                                linkWith(pair.second);
                            }
                        },
                        new ToastOnErrorAction()
                ));
    }

    public void linkWith(UserProfileDTO userProfileDTO)
    {
        this.currentUserProfile = userProfileDTO;
        displayAlertCount();
        displayAlertCountIcon();
    }

    protected void fetchAlertCompactList()
    {
        onStopSubscriptions.add(AppObservable.bindFragment(
                this,
                alertCompactListCache.get(currentUserId.toUserBaseKey())
                        .subscribeOn(Schedulers.computation())
                        .flatMap(new Func1<Pair<UserBaseKey, AlertCompactDTOList>, Observable<List<AlertItemView.DTO>>>()
                        {
                            @Override public Observable<List<AlertItemView.DTO>> call(
                                    Pair<UserBaseKey, AlertCompactDTOList> alertCompactDTOListPair)
                            {
                                return Observable.from(alertCompactDTOListPair.second)
                                        .map(new Func1<AlertCompactDTO, AlertItemView.DTO>()
                                        {
                                            @Override public AlertItemView.DTO call(AlertCompactDTO alertCompactDTO)
                                            {
                                                return new AlertItemView.DTO(getResources(), alertCompactDTO);
                                            }
                                        })
                                        .toList();
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<? extends AlertItemView.DTO>>()
                        {
                            @Override public void call(List<? extends AlertItemView.DTO> pair)
                            {
                                linkWith(pair);
                            }
                        },
                        new ToastOnErrorAction()
                ));
    }

    protected void linkWith(@NonNull List<? extends AlertItemView.DTO> alertCompactDTOs)
    {
        alertListItemAdapter.clear();
        alertListItemAdapter.appendTail(alertCompactDTOs);
        alertListItemAdapter.notifyDataSetChanged();
        if (alertListItemAdapter.getCount() == 0)
        {
            progressAnimator.setDisplayedChildByLayoutId(R.id.empty_item);
        }
        else
        {
            progressAnimator.setDisplayedChildByLayoutId(R.id.alerts_list);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.btn_upgrade_plan)
    protected void handleBtnPlanUpgradeClicked(@SuppressWarnings("UnusedParameters") View view)
    {
        //noinspection unchecked
        onStopSubscriptions.add(userInteractorRx.purchaseAndClear(ProductIdentifierDomain.DOMAIN_STOCK_ALERTS)
                .subscribe(
                        new Action1()
                        {
                            @Override public void call(Object result)
                            {
                                AlertManagerFragment.this.displayAlertCount();
                                AlertManagerFragment.this.displayAlertCountIcon();
                            }
                        },
                        new ToastOnErrorAction()
                ));
    }

    private void displayAlertCount()
    {
        if (currentUserProfile != null)
        {
            int count = currentUserProfile.getUserAlertPlansAlertCount();
            if (count == 0)
            {
                alertPlanCount.setText(R.string.stock_alerts_no_alerts);
                btnPlanUpgrade.setVisibility(View.VISIBLE);
            }
            else if (count < BillingConstants.ALERT_PLAN_UNLIMITED)
            {
                alertPlanCount.setText(String.format(getString(R.string.stock_alert_count_alert_format), count));
                btnPlanUpgrade.setVisibility(View.VISIBLE);
            }
            else
            {
                alertPlanCount.setText(R.string.stock_alert_plan_unlimited);
                btnPlanUpgrade.setVisibility(View.GONE);
            }
        }
    }

    private void displayAlertCountIcon()
    {
        if (currentUserProfile != null)
        {
            int count = currentUserProfile.getUserAlertPlansAlertCount();
            alertPlanCountIcon.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
            alertPlanCountIcon.setImageResource(SecurityAlertKnowledge.getStockAlertIcon(count));
        }
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @OnItemClickSticky(R.id.alerts_list)
    protected void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        AlertItemView.DTO viewDTO = (AlertItemView.DTO) parent.getItemAtPosition(position);
        if (viewDTO != null)
        {
            handleAlertItemClicked(viewDTO.alertCompactDTO);
            alertListItemAdapter.notifyDataSetChanged();
        }
    }

    private void handleAlertItemClicked(@NonNull AlertCompactDTO alertCompactDTO)
    {
        AlertEditDialogFragment.newInstance(alertCompactDTO.getAlertId(currentUserId.toUserBaseKey()))
                .show(
                        getFragmentManager(),
                        BaseAlertEditDialogFragment.class.getName());
    }

    private void handleManageSubscriptionClicked()
    {
        userInteractorRx.manageSubscriptions();
    }
}

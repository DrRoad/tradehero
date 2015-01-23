package com.tradehero.th.fragments.alert;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import com.tradehero.common.graphics.WhiteToTransparentTransformation;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.alert.AlertCompactDTO;
import com.tradehero.th.api.alert.AlertDTO;
import com.tradehero.th.api.alert.AlertFormDTO;
import com.tradehero.th.api.alert.AlertId;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.fragments.billing.BasePurchaseManagerFragment;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.models.number.THSignedMoney;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.network.retrofit.MiddleCallback;
import com.tradehero.th.network.service.AlertServiceWrapper;
import com.tradehero.th.persistence.alert.AlertCache;
import com.tradehero.th.persistence.user.UserProfileCache;
import com.tradehero.th.utils.DateUtils;
import com.tradehero.th.utils.ProgressDialogUtil;
import dagger.Lazy;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;

import javax.inject.Inject;

public class AlertViewFragment extends BasePurchaseManagerFragment
{
    private static final String BUNDLE_KEY_ALERT_ID_BUNDLE = AlertViewFragment.class.getName() + ".alertId";

    @InjectView(R.id.stock_logo) ImageView stockLogo;
    @InjectView(R.id.stock_symbol) TextView stockSymbol;
    @InjectView(R.id.company_name) TextView companyName;
    @InjectView(R.id.target_price) TextView targetPrice;
    @InjectView(R.id.target_price_label) TextView targetPriceLabel;
    @InjectView(R.id.current_price) TextView currentPrice;
    @InjectView(R.id.active_until) TextView activeUntil;
    @InjectView(R.id.alert_toggle) Switch alertToggle;

    private StickyListHeadersListView priceChangeHistoryList;

    @Inject protected Lazy<AlertCache> alertCache;
    protected DTOCacheNew.Listener<AlertId, AlertDTO> alertCacheListener;
    @Inject protected Lazy<AlertServiceWrapper> alertServiceWrapper;
    @Inject protected Lazy<Picasso> picasso;
    @Inject protected Lazy<PrettyTime> prettyTime;
    @Inject protected ProgressDialogUtil progressDialogUtil;
    @Inject protected Lazy<UserProfileCache> userProfileCache;
    @Inject protected CurrentUserId currentUserId;

    private View headerView;
    protected AlertDTO alertDTO;
    private SecurityCompactDTO securityCompactDTO;
    private AlertEventAdapter alertEventAdapter;
    private AlertId alertId;
    private ProgressDialog progressDialog;

    private CompoundButton.OnCheckedChangeListener alertToggleCheckedChangeListener;
    private THCallback<AlertCompactDTO> alertUpdateCallback;
    protected MiddleCallback<AlertCompactDTO> alertUpdateMiddleCallback;

    public static void putAlertId(@NotNull Bundle args, @NotNull AlertId alertId)
    {
        args.putBundle(BUNDLE_KEY_ALERT_ID_BUNDLE, alertId.getArgs());
    }

    @NotNull public static AlertId getAlertId(@NotNull Bundle args)
    {
        return new AlertId(args.getBundle(BUNDLE_KEY_ALERT_ID_BUNDLE));
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        alertUpdateCallback = new THCallback<AlertCompactDTO>()
        {
            @Override protected void finish()
            {
                progressDialog.hide();
            }

            @Override protected void success(AlertCompactDTO alertCompactDTO, THResponse thResponse)
            {
                displayActiveUntil();
            }

            @Override protected void failure(THException ex)
            {
                THToast.show(ex);
            }
        };
        alertToggleCheckedChangeListener = new CompoundButton.OnCheckedChangeListener()
        {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                handleAlertToggleChanged(isChecked);
            }
        };
        alertCacheListener = createAlertCacheListener();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        priceChangeHistoryList = (StickyListHeadersListView) inflater.inflate(R.layout.alert_view_fragment, container, false);
        headerView = inflater.inflate(R.layout.alert_security_profile, null);
        ButterKnife.inject(this, headerView);
        return priceChangeHistoryList;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        priceChangeHistoryList.addHeaderView(headerView);
        alertEventAdapter = new AlertEventAdapter(getActivity(),
                R.layout.alert_event_item_view);
        priceChangeHistoryList.setAdapter(alertEventAdapter);
    }

    @Override protected void initViews(View view)
    {
    }

    @Override public void onResume()
    {
        super.onResume();
        linkWith(getAlertId(getArguments()), true);
    }

    @Override public void onDestroyView()
    {
        detachAlertFetchTask();
        detachAlertUpdateMiddleCallback();
        priceChangeHistoryList.removeHeaderView(headerView);
        alertToggle.setOnCheckedChangeListener(null);
        alertToggle.setOnClickListener(null);
        super.onDestroyView();
    }

    protected void detachAlertUpdateMiddleCallback()
    {
        if (alertUpdateMiddleCallback != null)
        {
            alertUpdateMiddleCallback.setPrimaryCallback(null);
        }
        alertUpdateMiddleCallback = null;
    }

    protected void detachAlertFetchTask()
    {
        alertCache.get().unregister(alertCacheListener);
    }

    @Override public void onDestroy()
    {
        alertToggleCheckedChangeListener = null;
        alertUpdateCallback = null;
        alertCacheListener = null;
        super.onDestroy();
    }

    private void linkWith(AlertId alertId, boolean andDisplay)
    {
        this.alertId = alertId;
        if (alertId != null)
        {
            detachAlertFetchTask();
            progressDialog = progressDialogUtil.show(getActivity(), R.string.loading_loading, R.string.alert_dialog_please_wait);
            progressDialog.setCanceledOnTouchOutside(true);
            alertCache.get().register(alertId, alertCacheListener);
            alertCache.get().getOrFetchAsync(alertId);
        }
    }

    private void linkWith(AlertDTO alertDTO, boolean andDisplay)
    {
        this.alertDTO = alertDTO;

        alertEventAdapter.clear();
        if (alertDTO.alertEvents != null)
        {
            alertEventAdapter.addAll(alertDTO.alertEvents);
        }
        alertEventAdapter.notifyDataSetChanged();

        if (alertDTO.security != null)
        {
            linkWith(alertDTO.security, andDisplay);
        }

        if (andDisplay)
        {
            displayCurrentPrice();

            displayTargetPrice();

            displayToggle();

            displayActiveUntil();
        }
    }

    private void displayToggle()
    {
        alertToggle.setChecked(alertDTO.active);
        alertToggle.setOnCheckedChangeListener(alertToggleCheckedChangeListener);
        alertToggle.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                UserProfileDTO userProfileDTO = userProfileCache.get().get(currentUserId.toUserBaseKey());

                if (alertToggle.isChecked()
                        && userProfileDTO != null
                        && userProfileDTO.alertCount >= userProfileDTO.getUserAlertPlansAlertCount())
                {
                    // TODO
                    //userInteractor.conditionalPopBuyStockAlerts();
                    alertToggle.setChecked(false);
                }
            }
        });
    }

    private void linkWith(SecurityCompactDTO security, boolean andDisplay)
    {
        this.securityCompactDTO = security;

        if (andDisplay)
        {
            displayStockLogo();

            displayStockSymbol();

            displayCompanyName();
        }
    }

    private void displayActiveUntil()
    {
        if (!alertToggle.isChecked())
        {
            activeUntil.setText("-");
        }
        else if (alertDTO.activeUntilDate != null)
        {
            activeUntil.setText(DateUtils.getDisplayableDate(getResources(), alertDTO.activeUntilDate));
        }
        else
        {
            activeUntil.setText("");
        }
    }

    private void displayTargetPrice()
    {
        if (alertDTO.priceMovement == null)
        {
            THSignedNumber thTargetPrice = THSignedMoney
                    .builder(alertDTO.targetPrice)
                    .withOutSign()
                    .build();
            targetPrice.setText(thTargetPrice.toString());
            targetPriceLabel.setText(getString(R.string.stock_alert_target_price));
        }
        else
        {
            THSignedNumber thPriceMovement = THSignedPercentage
                    .builder(alertDTO.priceMovement * 100)
                    .build();
            targetPrice.setText(thPriceMovement.toString());
            targetPriceLabel.setText(getString(R.string.stock_alert_percentage_movement));
        }
    }

    private void displayCurrentPrice()
    {
        THSignedNumber thCurrentPrice = THSignedMoney
                .builder(alertDTO.security.lastPrice)
                .withOutSign()
                .build();
        currentPrice.setText(thCurrentPrice.toString());
    }

    private void displayCompanyName()
    {
        companyName.setText(securityCompactDTO.name);
    }

    private void displayStockSymbol()
    {
        stockSymbol.setText(securityCompactDTO.getExchangeSymbol());
        setActionBarTitle(securityCompactDTO.getExchangeSymbol());
    }

    private void displayStockLogo()
    {
        if (securityCompactDTO != null && securityCompactDTO.imageBlobUrl != null)
        {
            picasso.get()
                    .load(securityCompactDTO.imageBlobUrl)
                    .transform(new WhiteToTransparentTransformation())
                    .into(stockLogo);
        }
        else if (securityCompactDTO != null)
        {
            picasso.get()
                    .load(securityCompactDTO.getExchangeLogoId())
                    .into(stockLogo);
        }
        else
        {
            stockLogo.setImageResource(R.drawable.default_image);
        }
    }

    private void handleAlertToggleChanged(boolean alertActive)
    {
        progressDialog = progressDialogUtil.show(getActivity(), R.string.loading_loading, R.string.alert_dialog_please_wait);
        progressDialog.setCanceledOnTouchOutside(true);

        if (alertDTO != null)
        {
            AlertFormDTO alertFormDTO = new AlertFormDTO();
            alertFormDTO.securityId = alertDTO.security.id;
            alertFormDTO.targetPrice = alertDTO.targetPrice;
            alertFormDTO.upOrDown = alertDTO.upOrDown;
            alertFormDTO.priceMovement = alertDTO.priceMovement;
            alertFormDTO.active = alertActive;
            detachAlertUpdateMiddleCallback();
            alertUpdateMiddleCallback = alertServiceWrapper.get().updateAlert(alertId, alertFormDTO, alertUpdateCallback);
        }
    }

    protected DTOCacheNew.Listener<AlertId, AlertDTO> createAlertCacheListener()
    {
        return new AlertViewFragmentAlertCacheListener();
    }

    protected class AlertViewFragmentAlertCacheListener implements DTOCacheNew.Listener<AlertId, AlertDTO>
    {
        private void finish()
        {
            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }
        }

        @Override public void onDTOReceived(@NotNull AlertId key, @NotNull AlertDTO value)
        {
            linkWith(value, true);
            finish();
        }

        @Override public void onErrorThrown(@NotNull AlertId key, @NotNull Throwable error)
        {
            THToast.show(R.string.error_fetch_alert);
            Timber.e(error, "Failed fetching alert " + key);
            finish();
        }
    }
}

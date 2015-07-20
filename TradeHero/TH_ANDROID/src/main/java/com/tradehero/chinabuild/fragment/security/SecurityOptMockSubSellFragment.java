package com.tradehero.chinabuild.fragment.security;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tradehero.chinabuild.data.QuoteDetail;
import com.tradehero.chinabuild.data.SignedQuote;
import com.tradehero.chinabuild.fragment.competition.CompetitionSecuritySearchFragment;
import com.tradehero.chinabuild.fragment.search.SearchUnitFragment;
import com.tradehero.common.utils.IOUtils;
import com.tradehero.common.utils.THToast;
import com.tradehero.firmbargain.DataUtils;
import com.tradehero.th.R;
import com.tradehero.th.activities.ActivityHelper;
import com.tradehero.th.activities.SecurityOptActivity;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.portfolio.PortfolioId;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.quote.QuoteDTO;
import com.tradehero.th.api.security.TransactionFormDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.network.service.PortfolioServiceWrapper;
import com.tradehero.th.network.service.QuoteServiceWrapper;
import com.tradehero.th.network.service.SecurityServiceWrapper;
import com.tradehero.th.utils.DaggerUtils;

import java.text.DecimalFormat;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.Converter;
import retrofit.mime.TypedByteArray;

/**
 * Sell Page
 * <p/>
 * Created by palmer on 15/7/6.
 */
public class SecurityOptMockSubSellFragment extends Fragment implements View.OnClickListener {

    private Button buySellBtn;
    private ListView positionsLV;
    private TextView securityCodeTV;
    private EditText priceET;
    private TextView addOneTV;
    private TextView reduceOneTV;
    private LinearLayout availableLayout;
    private LinearLayout sharesLayout;
    private TextView availableSellTV;
    private TextView totalSellTV;

    private EditText decisionET;
    private ImageView oneFourIV;
    private ImageView oneThirdIV;
    private ImageView halfIV;
    private ImageView allIV;

    //Dialog
    private Dialog sellConfirmDialog;
    private TextView dlgStockNameTV;
    private TextView dlgStockCodeTV;
    private TextView dlgStockPriceTV;
    private TextView dlgStockAmountTV;
    private TextView dlgStockTotalTV;
    private TextView dlgConfirmTV;
    private TextView dlgCancelTV;

    private String securityExchange = "";
    private String securitySymbol = "";
    private String securityName;
    @Inject QuoteServiceWrapper quoteServiceWrapper;
    @Inject SecurityServiceWrapper securityServiceWrapper;
    @Inject Converter converter;
    @Inject PortfolioServiceWrapper portfolioServiceWrapper;
    @Inject CurrentUserId currentUserId;

    private QuoteDetail quoteDetail;
    private SecurityOptPositionsList securityOptPositionDTOs;
    private int portfolioId = 0;
    private int competitionId;

    private RefreshPositionsHandler refreshPositionsHandler = new RefreshPositionsHandler();
    private RefreshBuySellHandler refreshBuySellHandler = new RefreshBuySellHandler();
    private RefreshQuoteHandler refreshQuoteHandler = new RefreshQuoteHandler();

    private QuoteDTO quoteDTO;

    //Buy Sell Layout
    private TextView sell5Price;
    private TextView sell5Amount;
    private TextView sell4Price;
    private TextView sell4Amount;
    private TextView sell3Price;
    private TextView sell3Amount;
    private TextView sell2Price;
    private TextView sell2Amount;
    private TextView sell1Price;
    private TextView sell1Amount;

    private TextView buy5Price;
    private TextView buy5Amount;
    private TextView buy4Price;
    private TextView buy4Amount;
    private TextView buy3Price;
    private TextView buy3Amount;
    private TextView buy2Price;
    private TextView buy2Amount;
    private TextView buy1Price;
    private TextView buy1Amount;

    private int color_up;
    private int color_down;

    private int availableSells = 0;

    private boolean isRefresh = true;

    private SecurityOptPositionMockAdapter securityOptMockPositionAdapter;

    private PortfolioDTO portfolioDTO;
    private PortfolioId portfolioIdObj;


    public final static String INTENT_REFRESH_POSITION_REQUIRED = "INTENT_REFRESH_POSITION_REQUIRED";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(INTENT_REFRESH_POSITION_REQUIRED)){
                if (portfolioId == 0) {
                    retrieveMainPositionsNoRepeat();
                } else {
                    retrieveCompetitionPositionsNoRepeat();
                }
            }
        }
    };
    private IntentFilter intentFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        securityOptMockPositionAdapter = new SecurityOptPositionMockAdapter(getActivity());
        color_up = getResources().getColor(R.color.number_up);
        color_down = getResources().getColor(R.color.number_down);
        securitySymbol = getArguments().getString(SecurityOptActivity.KEY_SECURITY_SYMBOL, "");
        securityExchange = getArguments().getString(SecurityOptActivity.KEY_SECURITY_EXCHANGE, "");
        securityName = getArguments().getString(SecurityDetailFragment.BUNDLE_KEY_SECURITY_NAME, "");
        competitionId = getArguments().getInt(CompetitionSecuritySearchFragment.BUNLDE_COMPETITION_ID, 0);
        if(getArguments().containsKey(SecurityOptActivity.KEY_PORTFOLIO_ID)) {
            portfolioIdObj = getPortfolioId();
            if (competitionId != 0) {
                portfolioId = portfolioIdObj.key;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DaggerUtils.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.security_opt_sub_buysell, container, false);
        initViews(view);
        isRefresh = true;
        if (!TextUtils.isEmpty(securitySymbol) && !TextUtils.isEmpty(securityExchange)) {
            if(isSHASHE()){
                quoteServiceWrapper.getQuoteDetails(securityExchange, securitySymbol, new RefreshBUYSELLCallback());
            } else {
                priceET.setEnabled(false);
                addOneTV.setEnabled(false);
                reduceOneTV.setEnabled(false);
            }
            retrieveQuoteDTO();
        }
        if(portfolioId == 0) {
            retrieveMainPositions();
        } else {
            retrieveCompetitionPositions();
        }

        //Retrieve user portfolio
        retrieveUserInformation();

        intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_REFRESH_POSITION_REQUIRED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver ,intentFilter);

        return view;
    }

    @Override
    public void onDestroyView(){
        isRefresh = false;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
    }


    private void initViews(View view) {
        initSellBuyViews(view);
        availableLayout = (LinearLayout)view.findViewById(R.id.layout_available_money);
        sharesLayout = (LinearLayout)view.findViewById(R.id.layout_shares);
        sharesLayout.setVisibility(View.VISIBLE);
        availableLayout.setVisibility(View.GONE);
        availableSellTV = (TextView)view.findViewById(R.id.textview_available_sells);
        totalSellTV = (TextView)view.findViewById(R.id.textview_all_sells);
        buySellBtn = (Button) view.findViewById(R.id.button_security_opt_buy_sell);
        buySellBtn.setText(R.string.security_opt_sell);
        buySellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSellConfirmDialog();
            }
        });
        positionsLV = (ListView) view.findViewById(R.id.listview_security_opt_positions);
        if (securityOptMockPositionAdapter == null) {
            securityOptMockPositionAdapter = new SecurityOptPositionMockAdapter(getActivity());
        }
        positionsLV.setAdapter(securityOptMockPositionAdapter);
        securityCodeTV = (TextView)view.findViewById(R.id.textview_security_code);
        securityCodeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterSearchPage();
            }
        });
        if(!TextUtils.isEmpty(securitySymbol)){
            securityCodeTV.setText(securitySymbol + " " + securityName);
        }
        priceET = (EditText)view.findViewById(R.id.edittext_security_price);
        addOneTV = (TextView)view.findViewById(R.id.textview_security_opt_add);
        reduceOneTV = (TextView)view.findViewById(R.id.textview_security_opt_minus);
        addOneTV.setOnClickListener(this);
        reduceOneTV.setOnClickListener(this);

        decisionET = (EditText) view.findViewById(R.id.edittext_security_decision);
        oneFourIV = (ImageView) view.findViewById(R.id.security_opt_one_fourth);
        oneThirdIV = (ImageView) view.findViewById(R.id.security_opt_one_third);
        halfIV = (ImageView) view.findViewById(R.id.security_opt_half);
        allIV = (ImageView) view.findViewById(R.id.security_opt_all);
        oneFourIV.setOnClickListener(this);
        oneThirdIV.setOnClickListener(this);
        halfIV.setOnClickListener(this);
        allIV.setOnClickListener(this);
    }

    private void showSellConfirmDialog() {
        if (getActivity() == null) {
            return;
        }
        if (sellConfirmDialog == null) {
            sellConfirmDialog = new Dialog(getActivity());
            sellConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            sellConfirmDialog.setCanceledOnTouchOutside(false);
            sellConfirmDialog.setCancelable(true);
            sellConfirmDialog.setContentView(R.layout.dialog_security_opt_sell);
            dlgStockNameTV = (TextView)sellConfirmDialog.findViewById(R.id.dialog_security_name);
            dlgStockCodeTV = (TextView)sellConfirmDialog.findViewById(R.id.dialog_security_code);
            dlgStockPriceTV = (TextView)sellConfirmDialog.findViewById(R.id.dialog_security_price);
            dlgStockAmountTV = (TextView)sellConfirmDialog.findViewById(R.id.dialog_security_amount);
            dlgStockTotalTV = (TextView)sellConfirmDialog.findViewById(R.id.dialog_security_total);
            dlgConfirmTV = (TextView) sellConfirmDialog.findViewById(R.id.dialog_confirm);
            dlgCancelTV = (TextView) sellConfirmDialog.findViewById(R.id.dialog_cancel);
            dlgCancelTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sellConfirmDialog != null) {
                        sellConfirmDialog.dismiss();
                    }
                }
            });
            dlgConfirmTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sellConfirmDialog.dismiss();
                    if(decisionET.getText() == null){
                        return;
                    }
                    if(TextUtils.isEmpty(decisionET.getText().toString())){
                        return;
                    }
                    if(priceET.getText() == null){
                        return;
                    }
                    if(TextUtils.isEmpty(priceET.getText().toString())){
                        return;
                    }
                    int quantity = 0 - Integer.valueOf(decisionET.getText().toString());
                    double price = Double.valueOf(priceET.getText().toString());
                    if(price <= 0){
                        THToast.show("股票价格错误");
                        return;
                    }
                    if(quantity >= 0){
                        THToast.show("股票交易数量错误");
                        return;
                    }

                    if(isSHASHE()){
                        if(quoteDetail!=null && quoteDetail.prec !=null){
                            if(price > (quoteDetail.prec*1.11) || price < (quoteDetail.prec*0.89)){
                                THToast.show("股票价格错误");
                                return;
                            }
                        }
                        securityServiceWrapper.order(portfolioId, securityExchange, securitySymbol, quantity, price, new Callback<Response>() {
                            @Override
                            public void success(Response value, Response response) {
                                if(isSHASHE()){
                                    THToast.show("委托成功");
                                } else {
                                    THToast.show("交易成功");
                                }
                                if(portfolioId == 0) {
                                    retrieveMainPositionsNoRepeat();
                                }else {
                                    retrieveCompetitionPositionsNoRepeat();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                THException thException = new THException(error);
                                THToast.show(thException.toString());
                            }
                        });
                    } else {
                        if(quoteDTO == null) {
                            return;
                        }
                        TransactionFormDTO transactionFormDTO = buildTransactionFormDTO();
                        if (transactionFormDTO == null) {
                            return;
                        }
                        securityServiceWrapper.sell(securityExchange, securitySymbol, buildTransactionFormDTO(), new Callback<SecurityPositionDetailDTO>() {
                            @Override
                            public void success(SecurityPositionDetailDTO securityPositionDetailDTO, Response response) {
                                if(isSHASHE()){
                                    THToast.show("委托成功");
                                } else {
                                    THToast.show("交易成功");
                                }
                                if (portfolioId == 0) {
                                    retrieveMainPositionsNoRepeat();
                                } else {
                                    retrieveCompetitionPositionsNoRepeat();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                THException thException = new THException(error);
                                THToast.show(thException.getMessage());
                            }
                        });
                    }
                }
            });

        }
        if(TextUtils.isEmpty(securityName) || TextUtils.isEmpty(securitySymbol)){
            return;
        }
        if(priceET.getText()==null){
            return;
        }
        if(TextUtils.isEmpty(priceET.getText().toString())){
            return;
        }
        if(decisionET.getText()==null){
            return;
        }
        if(TextUtils.isEmpty(decisionET.getText().toString())){
            return;
        }
        dlgStockNameTV.setText(securityName);
        dlgStockCodeTV.setText(securitySymbol);
        dlgStockPriceTV.setText(priceET.getText());
        dlgStockAmountTV.setText(decisionET.getText());
        int price = (int)(Double.valueOf(priceET.getText().toString()) * Integer.valueOf(decisionET.getText().toString()));
        dlgStockTotalTV.setText(String.valueOf(price));
        sellConfirmDialog.show();
    }

    public class RefreshBUYSELLCallback implements Callback<QuoteDetail> {

        @Override
        public void success(QuoteDetail quoteDetail, Response response) {
            SecurityOptMockSubSellFragment.this.quoteDetail = quoteDetail;
            if(securitySymbol.equals(quoteDetail.symb)){
                setSellBuyData(quoteDetail);
            }
            onFinish();
        }

        @Override
        public void failure(RetrofitError error) {
            onFinish();
        }

        private void onFinish() {
            if (isNeedToRefresh()) {
                if(isRefresh){
                    refreshBuySellHandler.sendEmptyMessageDelayed(-1, 5000);
                }
            }
        }
    }

    public class RefreshBuySellHandler extends Handler {
        public void handleMessage(Message msg) {
            quoteServiceWrapper.getQuoteDetails(securityExchange, securitySymbol, new RefreshBUYSELLCallback());
        }
    }


    private void initSellBuyViews(View view) {
        sell5Price = (TextView) view.findViewById(R.id.textview_sell_price_a);
        sell5Amount = (TextView) view.findViewById(R.id.textview_sell_amount_a);
        sell4Price = (TextView) view.findViewById(R.id.textview_sell_price_b);
        sell4Amount = (TextView) view.findViewById(R.id.textview_sell_amount_b);
        sell3Price = (TextView) view.findViewById(R.id.textview_sell_price_c);
        sell3Amount = (TextView) view.findViewById(R.id.textview_sell_amount_c);
        sell2Price = (TextView) view.findViewById(R.id.textview_sell_price_d);
        sell2Amount = (TextView) view.findViewById(R.id.textview_sell_amount_d);
        sell1Price = (TextView) view.findViewById(R.id.textview_sell_price_e);
        sell1Amount = (TextView) view.findViewById(R.id.textview_sell_amount_e);

        buy1Price = (TextView) view.findViewById(R.id.textview_buy_price_a);
        buy1Amount = (TextView) view.findViewById(R.id.textview_buy_amount_a);
        buy2Price = (TextView) view.findViewById(R.id.textview_buy_price_b);
        buy2Amount = (TextView) view.findViewById(R.id.textview_buy_amount_b);
        buy3Price = (TextView) view.findViewById(R.id.textview_buy_price_c);
        buy3Amount = (TextView) view.findViewById(R.id.textview_buy_amount_c);
        buy4Price = (TextView) view.findViewById(R.id.textview_buy_price_d);
        buy4Amount = (TextView) view.findViewById(R.id.textview_buy_amount_d);
        buy5Price = (TextView) view.findViewById(R.id.textview_buy_price_e);
        buy5Amount = (TextView) view.findViewById(R.id.textview_buy_amount_e);
    }

    private void clearAllSellBuy(){
        buy1Price.setText("- -");
        buy1Amount.setText("- -");
        buy2Price.setText("- -");
        buy2Amount.setText("- -");
        buy3Price.setText("- -");
        buy3Amount.setText("- -");
        buy4Price.setText("- -");
        buy4Amount.setText("- -");
        buy5Price.setText("- -");
        buy5Amount.setText("- -");

        sell1Price.setText("- -");
        sell1Amount.setText("- -");
        sell2Price.setText("- -");
        sell2Amount.setText("- -");
        sell3Price.setText("- -");
        sell3Amount.setText("- -");
        sell4Price.setText("- -");
        sell4Amount.setText("- -");
        sell5Price.setText("- -");
        sell5Amount.setText("- -");

    }

    private void setSellBuyData(QuoteDetail quoteDetail) {
        if (quoteDetail == null || buy1Price == null || quoteDetail.open == null) {
            return;
        }
        if (quoteDetail.bp1 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.bp1, buy1Price);
        }
        if (quoteDetail.bv1 != null) {
            buy1Amount.setText(convertAmountDoubleToString(quoteDetail.bv1));
        }
        if (quoteDetail.bp2 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.bp2, buy2Price);
        }
        if (quoteDetail.bv2 != null) {
            buy2Amount.setText(convertAmountDoubleToString(quoteDetail.bv2));
        }
        if (quoteDetail.bp3 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.bp3, buy3Price);
        }
        if (quoteDetail.bv3 != null) {
            buy3Amount.setText(convertAmountDoubleToString(quoteDetail.bv3));
        }
        if (quoteDetail.bp4 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.bp4, buy4Price);
        }
        if (quoteDetail.bv4 != null) {
            buy4Amount.setText(convertAmountDoubleToString(quoteDetail.bv4));
        }
        if (quoteDetail.bp5 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.bp5, buy5Price);
        }
        if (quoteDetail.bv5 != null) {
            buy5Amount.setText(convertAmountDoubleToString(quoteDetail.bv5));
        }

        if (quoteDetail.sp1 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.sp1, sell1Price);
        }
        if (quoteDetail.sv1 != null) {
            sell1Amount.setText(convertAmountDoubleToString(quoteDetail.sv1));
        }
        if (quoteDetail.sp2 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.sp2, sell2Price);
        }
        if (quoteDetail.sv2 != null) {
            sell2Amount.setText(convertAmountDoubleToString(quoteDetail.sv2));
        }
        if (quoteDetail.sp3 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.sp3, sell3Price);
        }
        if (quoteDetail.sv3 != null) {
            sell3Amount.setText(convertAmountDoubleToString(quoteDetail.sv3));
        }
        if (quoteDetail.sp4 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.sp4, sell4Price);
        }
        if (quoteDetail.sv4 != null) {
            sell4Amount.setText(convertAmountDoubleToString(quoteDetail.sv4));
        }
        if (quoteDetail.sp5 != null) {
            setStockPrice(quoteDetail.open, quoteDetail.sp5, sell5Price);
        }
        if (quoteDetail.sv5 != null) {
            sell5Amount.setText(convertAmountDoubleToString(quoteDetail.sv5));
        }
        if(TextUtils.isEmpty(priceET.getText())){
            if(quoteDetail.bp1!=null) {
                priceET.setText(String.valueOf(quoteDetail.bp1));
            } else if (quoteDetail.sp1 != null){
                priceET.setText(String.valueOf(quoteDetail.sp1));
            }
        }
    }

    private String convertAmountDoubleToString(Integer value){
        int valueNew = value/100;
        if(valueNew > 10000){
            double valueNewD = (double)valueNew/10000.0;
            DecimalFormat df =new DecimalFormat("#.0");
            return df.format(valueNewD) + "万";
        } else{
            return String.valueOf(valueNew);
        }
    }

    private void setStockPrice(double open, double data, TextView textView){
        textView.setText(String.valueOf(data));
        if(data >= open){
            textView.setTextColor(color_up);
        } else {
            textView.setTextColor(color_down);
        }
    }

    private boolean isNeedToRefresh(){
        if(TextUtils.isEmpty(securityExchange) || TextUtils.isEmpty(securitySymbol)){
            return false;
        }
        if(securityExchange.equalsIgnoreCase("SHA") || securityExchange.equalsIgnoreCase("SHE")){
            return true;
        }
        return false;
    }

    private boolean isSHASHE(){
        if (securityExchange.equalsIgnoreCase("SHA") || securityExchange.equalsIgnoreCase("SHE")) {
            return true;
        }
        return false;
    }

    private void gotoDashboard(String strFragment,Bundle bundle) {
        bundle.putString(DashboardFragment.BUNDLE_OPEN_CLASS_NAME,strFragment);
        ActivityHelper.launchDashboard(getActivity(), bundle);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId){
            case R.id.textview_security_opt_minus:
                reduceOne();
                break;
            case R.id.textview_security_opt_add:
                addOne();
                break;
            case R.id.security_opt_one_fourth:
                setSellAmount(4);
                break;
            case R.id.security_opt_one_third:
                setSellAmount(3);
                break;
            case R.id.security_opt_half:
                setSellAmount(2);
                break;
            case R.id.security_opt_all:
                setSellAmount(1);
                break;
        }
    }

    private void setSellAmount(int percent){
        if(availableSells <= 0){
            return;
        }

        decisionET.setText(String.valueOf(availableSells/percent));
        switch (percent){
            case 1:
                allIV.setImageResource(R.drawable.all);
                halfIV.setImageResource(R.drawable.half_normal);
                oneThirdIV.setImageResource(R.drawable.one_third_normal);
                oneFourIV.setImageResource(R.drawable.one_fourth_normal);
                break;
            case 2:
                allIV.setImageResource(R.drawable.all_normal);
                halfIV.setImageResource(R.drawable.half);
                oneThirdIV.setImageResource(R.drawable.one_third_normal);
                oneFourIV.setImageResource(R.drawable.one_fourth_normal);
                break;
            case 3:
                allIV.setImageResource(R.drawable.all_normal);
                halfIV.setImageResource(R.drawable.half_normal);
                oneThirdIV.setImageResource(R.drawable.one_third);
                oneFourIV.setImageResource(R.drawable.one_fourth_normal);
                break;
            case 4:
                allIV.setImageResource(R.drawable.all_normal);
                halfIV.setImageResource(R.drawable.half_normal);
                oneThirdIV.setImageResource(R.drawable.one_third_normal);
                oneFourIV.setImageResource(R.drawable.one_fourth);
                break;
        }
    }

    private void addOne(){
        if(priceET.getText() == null){
            return;
        }
        String valueStr = priceET.getText().toString();
        if(TextUtils.isEmpty(valueStr)){
            return;
        }
        double value = Double.valueOf(valueStr) + 0.01;
        if(quoteDetail==null){
            return;
        }
        if((quoteDetail.prec * 1.1) < value){
            return;
        }
        DecimalFormat df =new DecimalFormat("#.00");
        priceET.setText(df.format(value));
    }

    private void reduceOne(){
        if(priceET.getText() == null){
            return;
        }
        String valueStr = priceET.getText().toString();
        if(TextUtils.isEmpty(valueStr)){
            return;
        }
        double value = Double.valueOf(valueStr);
        if(value<=0.01){
            return;
        }
        value = value - 0.01;
        if(quoteDetail==null){
            return;
        }
        if((quoteDetail.prec * 0.9) > value){
            return;
        }
        DecimalFormat df =new DecimalFormat("#.00");
        priceET.setText(df.format(value));
    }

    private void retrieveMainPositions(){
        quoteServiceWrapper.retrieveMainPositions(new RetrievePositionsCallback());
    }

    private void retrieveMainPositionsNoRepeat(){
        quoteServiceWrapper.retrieveMainPositions(new Callback<SecurityOptPositionsList>() {
            @Override
            public void success(SecurityOptPositionsList securityOptPositionDTOs, Response response) {
                SecurityOptMockSubSellFragment.this.securityOptPositionDTOs = securityOptPositionDTOs;
                securityOptMockPositionAdapter.addData(securityOptPositionDTOs);
                displaySells(securityOptPositionDTOs);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void retrieveCompetitionPositions(){
        quoteServiceWrapper.retrieveCompetitionPositions(portfolioId, new RetrievePositionsCallback());
    }

    private void retrieveCompetitionPositionsNoRepeat() {
        quoteServiceWrapper.retrieveCompetitionPositions(portfolioId, new Callback<SecurityOptPositionsList>() {
            @Override
            public void success(SecurityOptPositionsList securityOptPositionDTOs, Response response) {
                SecurityOptMockSubSellFragment.this.securityOptPositionDTOs = securityOptPositionDTOs;
                securityOptMockPositionAdapter.addData(securityOptPositionDTOs);
                displaySells(securityOptPositionDTOs);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    class RetrievePositionsCallback implements Callback<SecurityOptPositionsList> {

        @Override
        public void success(SecurityOptPositionsList securityOptPositionDTOs, Response response) {
            SecurityOptMockSubSellFragment.this.securityOptPositionDTOs = securityOptPositionDTOs;
            securityOptMockPositionAdapter.addData(securityOptPositionDTOs);
            displaySells(securityOptPositionDTOs);

            onFinish();
        }

        @Override
        public void failure(RetrofitError error) {
            onFinish();
        }

        private void onFinish() {
            if(isRefresh) {
                refreshPositionsHandler.sendEmptyMessageDelayed(-1, 60000);
            }
        }
    }

    class RefreshPositionsHandler extends Handler {
        public void handleMessage(Message msg) {
            if (portfolioId == 0) {
                retrieveMainPositions();
            }
        }
    }

    private void displaySells(SecurityOptPositionsList securityOptPositionDTOs){
        if(securityOptPositionDTOs == null){
            totalSellTV.setText("0");
            availableSellTV.setText("0");
            return;
        }
        if(TextUtils.isEmpty(securitySymbol) || TextUtils.isEmpty(securityExchange)){
            totalSellTV.setText("0");
            availableSellTV.setText("0");
            return;
        }
        for(SecurityOptPositionMockDTO securityOptPositionDTO : securityOptPositionDTOs){
            if(securityOptPositionDTO.symbol.equals(securitySymbol) && securityOptPositionDTO.exchange.equals(securityExchange)){
                totalSellTV.setText(String.valueOf(securityOptPositionDTO.shares));
                availableSellTV.setText(String.valueOf(securityOptPositionDTO.sellableShares));
                availableSells = securityOptPositionDTO.sellableShares;
                decisionET.setHint("可委托数量： " + DataUtils.keepInteger(availableSells));
            }
        }
    }

    class RefreshQuoteHandler extends Handler {
        public void handleMessage(Message msg) {
            retrieveQuoteDTO();
        }
    }
    class QuoteNoSHASHECallback implements Callback<Response> {

        @Override
        public void success(Response rawResponse, Response response) {
            try {
                byte[] bytes = IOUtils.streamToBytes(rawResponse.getBody().in());
                SignedQuote signedQuote = (SignedQuote) converter.fromBody(new TypedByteArray(rawResponse.getBody().mimeType(), bytes), SignedQuote.class);
                QuoteDTO quoteDTO = signedQuote.signedObject;
                quoteDTO.rawResponse = new String(bytes);
                SecurityOptMockSubSellFragment.this.quoteDTO = quoteDTO;
                if (!isSHASHE() && quoteDTO.ask != null) {
                    sell1Price.setText(DataUtils.keepTwoDecimal(quoteDTO.ask));
                    buy1Price.setText(DataUtils.keepTwoDecimal(quoteDTO.ask));
                    priceET.setText(DataUtils.keepTwoDecimal(quoteDTO.ask));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            onFinish();
        }

        @Override
        public void failure(RetrofitError error) {
            onFinish();
        }

        private void onFinish() {
            if (isRefresh && !TextUtils.isEmpty(securitySymbol) && !TextUtils.isEmpty(securityExchange)) {
                refreshQuoteHandler.sendEmptyMessageDelayed(-1, 10000);
            }

        }
    }

    private void retrieveQuoteDTO(){
        quoteServiceWrapper.getQuote(securityExchange, securitySymbol, new QuoteNoSHASHECallback());
    }

    private TransactionFormDTO buildTransactionFormDTO(){
        if (quoteDTO == null) {
            return null;
        }
        if(decisionET == null || decisionET.getText() == null || TextUtils.isEmpty(decisionET.getText().toString())){
            return null;
        }
        if(portfolioId == 0) {
            if(portfolioDTO == null) {
                return null;
            }
            int mTransactionQuantity = Integer.valueOf(decisionET.getText().toString());
            return new TransactionFormDTO(null, null, null, null, null, null, null, false, null,
                    quoteDTO.rawResponse, mTransactionQuantity, portfolioDTO.id);
        } else {
            if(portfolioIdObj == null || portfolioIdObj.key == null){
                return null;
            }
            int mTransactionQuantity = Integer.valueOf(decisionET.getText().toString());
            return new TransactionFormDTO(null, null, null, null, null, null, null, false, null,
                    quoteDTO.rawResponse, mTransactionQuantity, portfolioIdObj.key);
        }
    }

    protected PortfolioId getPortfolioId() {
        if (this.portfolioIdObj == null) {
            this.portfolioIdObj = new PortfolioId(getArguments().getBundle(SecurityOptActivity.KEY_PORTFOLIO_ID));
        }
        return portfolioIdObj;
    }

    private void retrieveUserInformation(){
        if(portfolioId == 0){
            portfolioServiceWrapper.getMainPortfolio(currentUserId.toUserBaseKey().getUserId(), new Callback<PortfolioDTO>() {
                @Override
                public void success(PortfolioDTO portfolioDTO, Response response) {
                    SecurityOptMockSubSellFragment.this.portfolioDTO = portfolioDTO;
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    private void enterSearchPage(){
        if(getActivity()!=null){
            getActivity().finish();
        }
        Bundle bundle = new Bundle();
        if(competitionId!=0){
            bundle.putInt(CompetitionSecuritySearchFragment.BUNLDE_COMPETITION_ID, competitionId);
            gotoDashboard(CompetitionSecuritySearchFragment.class.getName(), bundle);
            getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else {
            gotoDashboard(SearchUnitFragment.class.getName(), new Bundle());
            getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
    }
}
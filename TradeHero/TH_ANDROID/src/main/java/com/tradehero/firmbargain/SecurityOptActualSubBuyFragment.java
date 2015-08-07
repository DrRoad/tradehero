package com.tradehero.firmbargain;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tradehero.chinabuild.data.QuoteDetail;
import com.tradehero.chinabuild.fragment.security.SecurityDetailFragment;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.activities.SearchSecurityActualActivity;
import com.tradehero.th.activities.SecurityOptActivity;
import com.tradehero.th.network.service.QuoteServiceWrapper;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.SecurityUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import cn.htsec.data.SecAccountInfo;
import cn.htsec.data.pkg.trade.IPackageProxy;
import cn.htsec.data.pkg.trade.TradeDataHelper;
import cn.htsec.data.pkg.trade.TradeInterface;
import cn.htsec.data.pkg.trade.TradeManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Actual Security Buy Page
 *
 * Created by palmer on 15/7/16.
 */
public class SecurityOptActualSubBuyFragment extends Fragment implements View.OnClickListener{

    private TradeManager tradeManager;

    private Button buySellBtn;
    private ListView positionsLV;
    private TextView securityCodeTV;
    private EditText priceET;
    private TextView addOneTV;
    private TextView reduceOneTV;
    private LinearLayout availableLayout;
    private LinearLayout sharesLayout;
    private TextView availableCashTV;

    private EditText decisionET;
    private ImageView oneFourIV;
    private ImageView oneThirdIV;
    private ImageView halfIV;
    private ImageView allIV;

    //Dialog
    private Dialog buyConfirmDialog;
    private TextView dlgStockNameTV;
    private TextView dlgStockCodeTV;
    private TextView dlgStockPriceTV;
    private TextView dlgStockAmountTV;
    private TextView dlgStockTotalTV;
    private TextView dlgConfirmTV;
    private TextView dlgCancelTV;

    private SecurityOptPositionActualAdapter securityOptPositionActualAdapter;

    private int color_up;
    private int color_down;

    private String securityExchange = "";
    private String securitySymbol = "";
    private String securityName = "";
    private double balance = 0.0;

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

    @Inject QuoteServiceWrapper quoteServiceWrapper;
    private QuoteDetail quoteDetail;

    private boolean isRefresh = true;

    private ArrayList<SecurityOptPositionActualDTO> securityOptPositionActualDTOs = new ArrayList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tradeManager = TradeManager.getInstance(getActivity());

        color_up = getResources().getColor(R.color.number_up);
        color_down = getResources().getColor(R.color.number_down);
        securitySymbol = getArguments().getString(SecurityOptActivity.KEY_SECURITY_SYMBOL, "");
        securityExchange = getArguments().getString(SecurityOptActivity.KEY_SECURITY_EXCHANGE, "");
        securityName = getArguments().getString(SecurityDetailFragment.BUNDLE_KEY_SECURITY_NAME, "");

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
        if (!TextUtils.isEmpty(securitySymbol) && !TextUtils.isEmpty(securityExchange)) {
            if (isSHASHE()) {
                quoteServiceWrapper.getQuoteDetails(securityExchange, securitySymbol, new RefreshBUYSELLCallback());
            }
        }
        queryPositionsRepeat();
        queryBalance();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isRefresh = false;
    }

    private void initViews(View view) {
        initSellBuyViews(view);
        availableLayout = (LinearLayout) view.findViewById(R.id.layout_available_money);
        sharesLayout = (LinearLayout) view.findViewById(R.id.layout_shares);
        sharesLayout.setVisibility(View.GONE);
        availableLayout.setVisibility(View.VISIBLE);
        availableCashTV = (TextView)view.findViewById(R.id.textview_available_cash);
        buySellBtn = (Button) view.findViewById(R.id.button_security_opt_buy_sell);
        buySellBtn.setText(R.string.security_opt_buy);
        buySellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBuyConfirmDialog();
            }
        });
        positionsLV = (ListView) view.findViewById(R.id.listview_security_opt_positions);
        if(securityOptPositionActualAdapter==null) {
            securityOptPositionActualAdapter = new SecurityOptPositionActualAdapter(getActivity());
        }
        positionsLV.setAdapter(securityOptPositionActualAdapter);
        positionsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SecurityOptPositionActualDTO securityOptPositionActualDTO = securityOptPositionActualAdapter.getItem(position);
                if(securityOptPositionActualDTO ==null || !isSHASHE(securityOptPositionActualDTO.market_name)
                        || TextUtils.isEmpty(securityOptPositionActualDTO.sec_code) || TextUtils.isEmpty(securityOptPositionActualDTO.sec_name)){
                    return;
                }
                boolean isEmptyBefore = false;
                if(TextUtils.isEmpty(securityExchange) && TextUtils.isEmpty(securitySymbol)){
                    isEmptyBefore = true;
                }
                setExchange(securityOptPositionActualDTO.market_name);
                securitySymbol = securityOptPositionActualDTO.sec_code;
                securityName = securityOptPositionActualDTO.sec_name;
                priceET.setText("");
                decisionET.setText("");
                securityCodeTV.setText(securitySymbol + " " + securityName);
                clearAllSellBuy();
                if(isEmptyBefore){
                    quoteServiceWrapper.getQuoteDetails(securityExchange, securitySymbol, new RefreshBUYSELLCallback());
                } else {
                    quoteServiceWrapper.getQuoteDetails(securityExchange, securitySymbol, new RefreshBUYSELLNoRepeatCallback());
                }
                enableIfNoSHASHE();
            }
        });
        securityCodeTV = (TextView) view.findViewById(R.id.textview_security_code);
        securityCodeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterSearchPage();
            }
        });
        if (!TextUtils.isEmpty(securitySymbol)) {
            securityCodeTV.setText(securitySymbol + " " + securityName);
        }
        priceET = (EditText) view.findViewById(R.id.edittext_security_price);
        addOneTV = (TextView) view.findViewById(R.id.textview_security_opt_add);
        reduceOneTV = (TextView) view.findViewById(R.id.textview_security_opt_minus);
        addOneTV.setOnClickListener(this);
        reduceOneTV.setOnClickListener(this);
        decisionET = (EditText) view.findViewById(R.id.edittext_security_decision);
        decisionET.setHint("请输入100的整数倍");
        oneFourIV = (ImageView) view.findViewById(R.id.security_opt_one_fourth);
        oneThirdIV = (ImageView) view.findViewById(R.id.security_opt_one_third);
        halfIV = (ImageView) view.findViewById(R.id.security_opt_half);
        allIV = (ImageView) view.findViewById(R.id.security_opt_all);
        oneFourIV.setOnClickListener(this);
        oneThirdIV.setOnClickListener(this);
        halfIV.setOnClickListener(this);
        allIV.setOnClickListener(this);

        if(!isSHASHE()){
            disableIfNoSHASHE();
        }
    }

    private void disableIfNoSHASHE(){
        decisionET.setEnabled(false);
        oneFourIV.setClickable(false);
        oneThirdIV.setClickable(false);
        halfIV.setClickable(false);
        allIV.setClickable(false);
        reduceOneTV.setClickable(false);
        addOneTV.setClickable(false);
        priceET.setEnabled(false);
    }

    private void enableIfNoSHASHE(){
        decisionET.setEnabled(true);
        oneFourIV.setClickable(true);
        oneThirdIV.setClickable(true);
        halfIV.setClickable(true);
        allIV.setClickable(true);
        reduceOneTV.setClickable(true);
        addOneTV.setClickable(true);
        priceET.setEnabled(true);
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

    private void showBuyConfirmDialog() {
        if (getActivity() == null) {
            return;
        }
        if(!isSHASHE()){
            return;
        }
        if (buyConfirmDialog == null) {
            buyConfirmDialog = new Dialog(getActivity());
            buyConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            buyConfirmDialog.setCanceledOnTouchOutside(false);
            buyConfirmDialog.setCancelable(true);
            buyConfirmDialog.setContentView(R.layout.dialog_security_opt_buy);
            dlgStockNameTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_security_name);
            dlgStockCodeTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_security_code);
            dlgStockPriceTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_security_price);
            dlgStockAmountTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_security_amount);
            dlgStockTotalTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_security_total);

            dlgCancelTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_cancel);
            dlgCancelTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (buyConfirmDialog != null) {
                        buyConfirmDialog.dismiss();
                    }
                }
            });
            dlgConfirmTV = (TextView) buyConfirmDialog.findViewById(R.id.dialog_confirm);

            dlgConfirmTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buyConfirmDialog.dismiss();
                    tradeStock();
                }

            });
        }
        if (TextUtils.isEmpty(securityName) || TextUtils.isEmpty(securitySymbol)) {
            return;
        }
        if (priceET.getText() == null || TextUtils.isEmpty(priceET.getText().toString())) {
            return;
        }
        if (decisionET.getText() == null || TextUtils.isEmpty(decisionET.getText().toString())) {
            return;
        }
        double amount = Double.valueOf(decisionET.getText().toString());
        double plus = amount%100;
        if(plus > 0){
            THToast.show("购买股票数量必须是100的整数倍哦");
            return;
        }
        dlgStockNameTV.setText(securityName);
        dlgStockCodeTV.setText(securitySymbol);
        dlgStockPriceTV.setText(priceET.getText());

        dlgStockAmountTV.setText(decisionET.getText());
        int price = (int) (Double.valueOf(priceET.getText().toString()) * Double.valueOf(decisionET.getText().toString()));
        dlgStockTotalTV.setText(String.valueOf(price));
        buyConfirmDialog.show();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.textview_security_opt_minus:
                reduceOne();
                break;
            case R.id.textview_security_opt_add:
                addOne();
                break;
            case R.id.security_opt_one_fourth:
                setBuyAmount(4);
                break;
            case R.id.security_opt_one_third:
                setBuyAmount(3);
                break;
            case R.id.security_opt_half:
                setBuyAmount(2);
                break;
            case R.id.security_opt_all:
                setBuyAmount(1);
                break;
        }
    }

    private void addOne() {
        if (priceET.getText() == null) {
            return;
        }
        String valueStr = priceET.getText().toString();
        if (TextUtils.isEmpty(valueStr)) {
            return;
        }
        double value = Double.valueOf(valueStr) + 0.01;
        if (quoteDetail == null) {
            return;
        }
        if ((quoteDetail.prec * 1.1) < value) {
            return;
        }
        priceET.setText(DataUtils.keepTwoDecimal(value));
    }

    private void reduceOne() {
        if (priceET.getText() == null) {
            return;
        }
        String valueStr = priceET.getText().toString();
        if (TextUtils.isEmpty(valueStr)) {
            return;
        }
        double value = Double.valueOf(valueStr);
        if (value <= 0.01) {
            return;
        }
        value = value - 0.01;
        if (quoteDetail == null) {
            return;
        }
        if ((quoteDetail.prec * 0.9) > value) {
            return;
        }
        priceET.setText(DataUtils.keepTwoDecimal(value));
    }

    private void setBuyAmount(int percent){
        if(balance <=0 ){
            return;
        }
        if(quoteDetail == null){
            return;
        }
        double amount = balance/(quoteDetail.prec * percent);
        if(amount < 1){
            return;
        }
        decisionET.setText(DataUtils.keepInteger(amount));
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
        if (priceET.getText() == null || TextUtils.isEmpty(priceET.getText().toString())) {
            if (quoteDetail.sp1 != null) {
                priceET.setText(String.valueOf(quoteDetail.sp1));
            } else if (quoteDetail.bp1 != null) {
                priceET.setText(String.valueOf(quoteDetail.bp1));
            }
        }
    }

    private void setStockPrice(double open, double data, TextView textView) {
        textView.setText(String.valueOf(data));
        if (data >= open) {
            textView.setTextColor(color_up);
        } else {
            textView.setTextColor(color_down);
        }
    }

    private String convertAmountDoubleToString(Integer value) {
        int valueNew = value / 100;
        if (valueNew > 10000) {
            double valueNewD = (double) valueNew / 10000.0;
            return DataUtils.keepTwoDecimal(valueNewD) + "万";
        } else {
            return String.valueOf(valueNew);
        }
    }

    private void enterSearchPage(){
        if(getActivity()!=null){
            getActivity().finish();
        }
        Intent intent = new Intent(getActivity(), SearchSecurityActualActivity.class);
        getActivity().startActivity(intent);
    }

    private boolean isSHASHE(){
        if (securityExchange.equalsIgnoreCase("SHA") || securityExchange.equalsIgnoreCase("SHE")) {
            return true;
        }
        return false;
    }

    private boolean isSHASHE(String marketName){
        if(TextUtils.isEmpty(marketName)){
            return false;
        }
        if(marketName.equals("沪A")){
            return true;
        }
        if(marketName.equals("深A")){
            return true;
        }
        return false;
    }

    private boolean isNeedToRefresh() {
        if (TextUtils.isEmpty(securityExchange) || TextUtils.isEmpty(securitySymbol)) {
            return false;
        }
        if (securityExchange.equalsIgnoreCase("SHA") || securityExchange.equalsIgnoreCase("SHE")) {
            return true;
        }
        return false;
    }

    class RefreshBUYSELLCallback implements Callback<QuoteDetail> {

        @Override
        public void success(QuoteDetail quoteDetail, Response response) {
            SecurityOptActualSubBuyFragment.this.quoteDetail = quoteDetail;
            if(quoteDetail == null || quoteDetail.symb == null){
                securitySymbol = "";
                securityName = "";
                securityExchange = "";
                return;
            }
            if (securitySymbol.equals(quoteDetail.symb)) {
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
                if(isRefresh) {
                    RefreshBuySellHandler refreshBuySellHandler = new RefreshBuySellHandler();
                    refreshBuySellHandler.sendEmptyMessageDelayed(-1, 5000);
                }
            }
        }
    }

    class RefreshBUYSELLNoRepeatCallback implements  Callback<QuoteDetail> {
        @Override
        public void success(QuoteDetail quoteDetail, Response response) {
            SecurityOptActualSubBuyFragment.this.quoteDetail = quoteDetail;
            if(quoteDetail == null || quoteDetail.symb == null){
                securitySymbol = "";
                securityName = "";
                securityExchange = "";
                return;
            }
            if (securitySymbol.equals(quoteDetail.symb)) {
                setSellBuyData(quoteDetail);
            }
        }

        @Override
        public void failure(RetrofitError error) {
        }
    }

    class RefreshBuySellHandler extends Handler {
        public void handleMessage(Message msg) {
            THLog.d("Actual buy refresh buy5sell5 per 5 seconds " + securityExchange + " " + securityName + " " + securitySymbol);
            quoteServiceWrapper.getQuoteDetails(securityExchange, securitySymbol, new RefreshBUYSELLCallback());
        }
    }

    private void queryPositionsNoRepeat(){
        if(tradeManager==null || !tradeManager.isLogined() ){
            if(getActivity()!=null) {
                getActivity().finish();
            }
            return;
        }
        tradeManager.sendData(TradeInterface.ID_QUERY_POSITION, new IPackageProxy(){
            @Override
            public void onSend(TradeDataHelper helper) {
                helper.setStartPosition(0);
            }


            @Override
            public void onReceive(TradeDataHelper helper) {
                securityOptPositionActualDTOs.clear();
                int rowCount = helper.getRowCount();
                for(int i = 0; i < rowCount; i++) {
                    SecurityOptPositionActualDTO securityOptPositionActualDTO = new SecurityOptPositionActualDTO();
                    securityOptPositionActualDTO.sec_name = helper.get(i, "sec_name", "");
                    securityOptPositionActualDTO.sec_code = helper.get(i, "sec_code", null);
                    securityOptPositionActualDTO.profit = helper.get(i, "profit", 0.0);
                    securityOptPositionActualDTO.profit_ratio = helper.get(i, "profit_ratio", 0.0);
                    securityOptPositionActualDTO.buy_money = helper.get(i, "buy_money", 0.0);
                    securityOptPositionActualDTO.cost_price = helper.get(i, "cost_price", 0.0);
                    securityOptPositionActualDTO.current_amt = helper.get(i, "current_amt", 0.0);
                    securityOptPositionActualDTO.enable_amt = helper.get(i, "enable_amt", 0.0);
                    securityOptPositionActualDTO.market_name = helper.get(i, "market_name", "");
                    securityOptPositionActualDTOs.add(securityOptPositionActualDTO);
                }
                securityOptPositionActualAdapter.addData(securityOptPositionActualDTOs);
            }

        });
    }

    private void queryPositionsRepeat(){
        if(tradeManager==null || !tradeManager.isLogined() ){
            if(getActivity()!=null) {
                getActivity().finish();
            }
            return;
        }
        tradeManager.sendData(TradeInterface.ID_QUERY_POSITION, new IPackageProxy(){
            @Override
            public void onSend(TradeDataHelper helper) {
                helper.setStartPosition(0);
            }


            @Override
            public void onReceive(TradeDataHelper helper) {
                securityOptPositionActualDTOs.clear();
                int rowCount = helper.getRowCount();
                for(int i = 0; i < rowCount; i++) {
                    SecurityOptPositionActualDTO securityOptPositionActualDTO = new SecurityOptPositionActualDTO();
                    securityOptPositionActualDTO.sec_name = helper.get(i, "sec_name", "");
                    securityOptPositionActualDTO.sec_code = helper.get(i, "sec_code", null);
                    securityOptPositionActualDTO.profit = helper.get(i, "profit", 0.0);
                    securityOptPositionActualDTO.profit_ratio = helper.get(i, "profit_ratio", 0.0);
                    securityOptPositionActualDTO.buy_money = helper.get(i, "buy_money", 0.0);
                    securityOptPositionActualDTO.cost_price = helper.get(i, "cost_price", 0.0);
                    securityOptPositionActualDTO.current_amt = helper.get(i, "current_amt", 0.0);
                    securityOptPositionActualDTO.enable_amt = helper.get(i, "enable_amt", 0.0);
                    securityOptPositionActualDTO.market_name = helper.get(i, "market_name", "");
                    securityOptPositionActualDTOs.add(securityOptPositionActualDTO);
                }
                securityOptPositionActualAdapter.addData(securityOptPositionActualDTOs);
                if (isNeedToRefresh()) {
                    if(isRefresh) {
                        RefreshQueryPositionHandler refreshQueryPositionHandler = new RefreshQueryPositionHandler();
                        refreshQueryPositionHandler.sendEmptyMessageDelayed(-1, 200000);
                    }
                }
            }

        });

    }

    class RefreshQueryPositionHandler extends Handler{
        public void handleMessage(Message msg) {
            queryPositionsRepeat();
        }
    }


    private void queryBalance(){
        if(tradeManager==null || !tradeManager.isLogined() ){
            if(getActivity()!=null) {
                getActivity().finish();
            }
            return;
        }
        tradeManager.sendData(TradeInterface.ID_QUERY_MONEY_BALANCE, new IPackageProxy(){
            @Override
            public void onSend(TradeDataHelper helper) {

            }

            @Override
            public void onReceive(TradeDataHelper helper) {
                int rowCount = helper.getRowCount();
                for(int i = 0; i < rowCount; i++) {
                    balance = helper.get(i, "enable_balance", 0.0);
                    if(balance != 0){
                        if(availableCashTV!=null) {
                            availableCashTV.setText(DataUtils.keepInteger(balance));
                        }
                        return;
                    }
                }

            }
        });
    }

    private void tradeStock(){
        if (decisionET.getText() == null) {
            return;
        }
        if (TextUtils.isEmpty(decisionET.getText().toString())) {
            return;
        }
        if (priceET.getText() == null) {
            return;
        }
        if (TextUtils.isEmpty(priceET.getText().toString())) {
            return;
        }
        final double quantity = Double.valueOf(decisionET.getText().toString());
        final double price = Double.valueOf(priceET.getText().toString());
        if (price <= 0) {
            THToast.show("股票价格错误");
            return;
        }
        if (quantity <= 0) {
            THToast.show("股票交易数量错误");
            return;
        }
        double totalAmount = price * quantity;
        if(totalAmount > balance) {
            THToast.show("买入股票价格超出可用本金");
            return;
        }
        if(isSHASHE()) {
            if(tradeManager!=null){
                if(getActivity()!=null) {
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(SecurityOptActivity.INTENT_START_TRADING));
                }
                tradeManager.sendData(TradeInterface.ID_ENTRUST, new IPackageProxy() {

                    @Override
                    public void onSend(TradeDataHelper helper) {
                        helper.set(TradeInterface.KEY_MARKET_CODE, SecurityUtils.getMarketCodeBySymbol(securitySymbol));
                        helper.set(TradeInterface.KEY_ENTRUST_TYPE, "1");
                        SecAccountInfo secAccountInfo = tradeManager.getSecAccounts().get(0);
                        helper.set(TradeInterface.KEY_SEC_ACCOUNT, secAccountInfo.getAccount());
                        helper.set(TradeInterface.KEY_SEC_CODE, securitySymbol);
                        helper.set(TradeInterface.KEY_ENTRUST_PRICE, String.valueOf(price));
                        helper.set(TradeInterface.KEY_ENTRUST_AMT, String.valueOf(quantity));
                        helper.set(TradeInterface.KEY_MARKET_ORDER_TYPE, "");
                    }

                    @Override
                    public void onReceive(TradeDataHelper helper) {
                        String resultMsg = helper.getResultMsg();
                        if(!TextUtils.isEmpty(resultMsg)) {
                            THToast.show(resultMsg);
                        }
                        queryBalance();
                        queryPositionsNoRepeat();
                        if(getActivity()!=null) {
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(SecurityOptActivity.INTENT_END_TRADING));
                        }
                        if(decisionET!=null){
                            decisionET.setText("");
                        }
                    }

                    @Override
                    public void onRequestFail(String msg) {
                        THToast.show(msg);
                        if(getActivity()!=null) {
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(SecurityOptActivity.INTENT_END_TRADING));
                        }
                        if(decisionET!=null){
                            decisionET.setText("");
                        }
                    }

                });
            }
        }
    }

    private void setExchange(String marketName){
        if(TextUtils.isEmpty(marketName)){
            return;
        }
        if(marketName.equals("沪A")){
            securityExchange = "SHA";
            return;
        }
        if(marketName.equals("深A")){
            securityExchange = "SHE";
            return;
        }
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
}
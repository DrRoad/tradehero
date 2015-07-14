package com.tradehero.chinabuild.fragment.security;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tradehero.th.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by palmer on 15/7/7.
 */
public class SecurityOptMockPositionAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private ArrayList<SecurityOptPositionDTO> securityOptPositionDTOs = new ArrayList();

    private int color_up;
    private int color_down;

    public SecurityOptMockPositionAdapter(Context context){
        inflater = LayoutInflater.from(context);
        color_up = context.getResources().getColor(R.color.number_up);
        color_down = context.getResources().getColor(R.color.number_down);
    }

    public void addData(ArrayList<SecurityOptPositionDTO> securityOptPositionDTOs){
        this.securityOptPositionDTOs.clear();
        if(securityOptPositionDTOs != null){
            this.securityOptPositionDTOs.addAll(securityOptPositionDTOs);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return securityOptPositionDTOs.size();
    }

    @Override
    public SecurityOptPositionDTO getItem(int i) {
        return securityOptPositionDTOs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.security_opt_position_item, null);
            holder = new Holder();
            holder.stockName = (TextView)convertView.findViewById(R.id.stock_name);
            holder.code = (TextView)convertView.findViewById(R.id.stock_code);
            holder.benefit = (TextView)convertView.findViewById(R.id.stock_benefit);
            holder.percentageBenefit = (TextView)convertView.findViewById(R.id.stock_benefit_percent);
            holder.base = (TextView)convertView.findViewById(R.id.stock_base_a);
            holder.basePrice = (TextView)convertView.findViewById(R.id.stock_base_b);
            holder.totalAccount = (TextView)convertView.findViewById(R.id.stock_number);
            holder.availableAccount = (TextView)convertView.findViewById(R.id.stock_available);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }
        SecurityOptPositionDTO securityOptPositionDTO = getItem(i);
        holder.stockName.setText(securityOptPositionDTO.name);
        holder.totalAccount.setText(String.valueOf(securityOptPositionDTO.shares));
        holder.availableAccount.setText(String.valueOf(securityOptPositionDTO.sellableShares));
        holder.code.setText(securityOptPositionDTO.symbol);
        int benefit = (int)(securityOptPositionDTO.unrealizedPLRefCcy);
        double percentage = securityOptPositionDTO.unrealizedPLRefCcy / (securityOptPositionDTO.averagePriceRefCcy * securityOptPositionDTO.shares);
        DecimalFormat df = new DecimalFormat("#.00");
        holder.benefit.setText(securityOptPositionDTO.currencyDisplay + String.valueOf(benefit));
        holder.percentageBenefit.setText(df.format(percentage * 100) + "%");
        if(securityOptPositionDTO.unrealizedPLRefCcy >= 0){
            holder.benefit.setTextColor(color_up);
            holder.percentageBenefit.setTextColor(color_up);
        } else {
            holder.benefit.setTextColor(color_down);
            holder.percentageBenefit.setTextColor(color_down);
        }
        int base = (int)(securityOptPositionDTO.averagePriceRefCcy * securityOptPositionDTO.shares);
        holder.base.setText(securityOptPositionDTO.currencyDisplay + String.valueOf(base));
        holder.basePrice.setText(securityOptPositionDTO.currencyDisplay + df.format(securityOptPositionDTO.averagePriceRefCcy));
        return convertView;
    }

    class Holder {
        public TextView stockName;
        public TextView code;
        public TextView benefit;
        public TextView percentageBenefit;
        public TextView base;
        public TextView basePrice;
        public TextView totalAccount;
        public TextView availableAccount;
    }

}

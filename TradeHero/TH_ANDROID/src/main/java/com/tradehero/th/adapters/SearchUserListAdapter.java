package com.tradehero.th.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tradehero.chinabuild.utils.UniversalImageLoader;
import com.tradehero.th.R;
import com.tradehero.th.api.users.UserSearchResultDTO;
import com.tradehero.th.models.number.THSignedNumber;
import com.tradehero.th.models.number.THSignedPercentage;
import com.tradehero.th.utils.DaggerUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchUserListAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private List<UserSearchResultDTO> userSearchResultDTOs;

    public SearchUserListAdapter(Context context)
    {
        DaggerUtils.inject(this);
        this.context = context;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setListData(List<UserSearchResultDTO> list)
    {
        this.userSearchResultDTOs = list;
        notifyDataSetChanged();
    }

    public void addListData(List<UserSearchResultDTO> list)
    {
        if (userSearchResultDTOs == null) userSearchResultDTOs = new ArrayList<>();
        userSearchResultDTOs.addAll(list);
        notifyDataSetChanged();
    }

    @Override public int getCount()
    {
        return userSearchResultDTOs == null ? 0 : userSearchResultDTOs.size();
    }

    @Override public UserSearchResultDTO getItem(int i)
    {
        return userSearchResultDTOs == null ? null : userSearchResultDTOs.get(i);
    }

    @Override public long getItemId(int i)
    {
        return i;
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        UserSearchResultDTO item = getItem(position);
        if (item != null)
        {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = inflater.inflate(R.layout.user_search_list_item, viewGroup, false);
                holder = new ViewHolder();
                holder.imgUserHead = (ImageView) convertView.findViewById(R.id.imgUserHead);
                holder.imgUserName = (TextView) convertView.findViewById(R.id.tvUserName);
                holder.tvUserExtraTitle = (TextView) convertView.findViewById(R.id.tvUserExtraTitle);
                holder.tvUserExtraValue = (TextView) convertView.findViewById(R.id.tvUserExtraValue);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageLoader.getInstance()
                    .displayImage(item.userPicture,
                            holder.imgUserHead,
                            UniversalImageLoader.getAvatarImageLoaderOptions(false));

            holder.imgUserName.setText(item.userthDisplayName);

            if (item.userRoiSinceInception != null)
            {
                double roi = item.userRoiSinceInception;
                THSignedNumber thRoiSinceInception = THSignedPercentage.builder(roi * 100).withSign().signTypeArrow()
                        .build();
                holder.tvUserExtraValue.setText(thRoiSinceInception.toString());
                holder.tvUserExtraValue.setTextColor(context.getResources().getColor(thRoiSinceInception.getColorResId()));
            }
            else if (holder.tvUserExtraValue != null) {
                holder.tvUserExtraValue.setText("--");
                holder.tvUserExtraValue.setTextColor(context.getResources().getColor(R.color.black));
            }
        }
        return convertView;
    }

    static class ViewHolder
    {
        public ImageView imgUserHead = null;
        public TextView imgUserName = null;
        public TextView tvUserExtraTitle = null;
        public TextView tvUserExtraValue = null;
    }
}

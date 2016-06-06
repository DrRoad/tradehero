package com.androidth.general.fragments.social.friend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidth.general.R;
import java.util.List;

public class SocialTypeListAdapter extends ArrayAdapter<SocialTypeItem>
{
    public SocialTypeListAdapter(Context context, int resource, List<SocialTypeItem> objects)
    {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewGroup viewGroup;
        if (convertView == null)
        {
            viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.invite_friends_item, (ViewGroup) convertView, false);
        }
        else
        {
            viewGroup = (ViewGroup) convertView;
        }
        displayItem(position, viewGroup);
        return viewGroup;
    }

    private void displayItem(int position, ViewGroup viewGroup)
    {
        SocialTypeItem item = getItem(position);

        ImageView logoView = (ImageView) viewGroup.findViewById(R.id.social_item_logo);
        TextView titleView = (TextView) viewGroup.findViewById(R.id.social_item_title);

        logoView.setImageResource(item.imageResource);
        titleView.setText(getContext().getString(item.titleResource));

        int pL = viewGroup.getPaddingLeft();
        int pR = viewGroup.getPaddingRight();
        int pT = viewGroup.getPaddingTop();
        int pB = viewGroup.getPaddingBottom();

        viewGroup.setBackgroundResource(item.backgroundResource);
        viewGroup.setPadding(pL, pT, pR, pB);
    }
}

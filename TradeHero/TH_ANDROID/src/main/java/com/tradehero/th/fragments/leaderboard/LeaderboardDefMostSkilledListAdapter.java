package com.tradehero.th.fragments.leaderboard;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.LeaderboardDefDTO;
import java.util.List;

/** Created with IntelliJ IDEA. User: tho Date: 10/16/13 Time: 6:03 PM Copyright (c) TradeHero */
public class LeaderboardDefMostSkilledListAdapter extends LeaderboardDefListAdapter
{
    public LeaderboardDefMostSkilledListAdapter(Context context, LayoutInflater inflater, List<LeaderboardDefDTO> items, int layoutResourceId)
    {
        super(context, inflater, items, layoutResourceId);

        // create friends leaderboard def item view
        createFriendLeaderboardDefItemView();
    }

    private LeaderboardDefDTO createFriendLeaderboardDefItemView()
    {
        LeaderboardDefDTO friendLeaderboardDefDTO = new LeaderboardDefDTO();
        friendLeaderboardDefDTO.name = context.getString(R.string.leaderboard_friends);
        return friendLeaderboardDefDTO;
    }

    @Override public void setItems(List<LeaderboardDefDTO> items)
    {
        items.add(createFriendLeaderboardDefItemView());
        super.setItems(items);
    }

    @Override protected View getView(int position, LeaderboardDefView convertView)
    {
        convertView.setBackgroundResource(R.drawable.leaderboard_button_border_full);

        if (getCount() >= 2)
        {
            if (position == 0)
            {
                convertView.setBackgroundResource(R.drawable.leaderboard_button_border_top);
            }
            else if (position == getCount() - 1)
            {
                convertView.setBackgroundResource(R.drawable.leaderboard_button_border_bottom);
            }
        }
        return convertView;
    }
}

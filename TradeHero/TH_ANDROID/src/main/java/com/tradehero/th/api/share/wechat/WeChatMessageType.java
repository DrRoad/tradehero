package com.tradehero.th.api.share.wechat;

import com.tradehero.th.R;

public enum WeChatMessageType
{
    News(1, R.string.share_to_wechat_timeline_news),
    CreateDiscussion(2, R.string.share_to_wechat_timeline_create_discussion),
    Discussion(3, R.string.share_to_wechat_timeline_discussion),
    Timeline(4, R.string.share_to_wechat_timeline_timeline),
    Trade(5, R.string.share_to_wechat_timeline_trade),
    Invite(6, R.string.share_to_wechat_invite_friends),
    ShareSell(7, R.string.share_to_wechat_invite_friends),
    ShareSellToTimeline(8, R.string.share_to_wechat_invite_friends);

    private final int value;
    private final int titleResId;

    WeChatMessageType(int value, int titleResId)
    {
        this.value = value;
        this.titleResId = titleResId;
    }

    public int getTitleResId()
    {
        return titleResId;
    }

    public int getValue()
    {
        return value;
    }

    public static WeChatMessageType fromValue(int value)
    {
        for (WeChatMessageType wxType: values())
        {
            if (wxType.value == value)
            {
                return wxType;
            }
        }
        return null;
    }
}

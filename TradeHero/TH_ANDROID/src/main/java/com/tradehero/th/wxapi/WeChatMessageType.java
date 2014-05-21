package com.tradehero.th.wxapi;

import com.tradehero.th.R;

public enum WeChatMessageType
{
    News(1, R.string.share_to_wechat_timeline_news),
    CreateDiscussion(2, R.string.share_to_wechat_timeline_create_discussion),
    Discussion(3, R.string.share_to_wechat_timeline_discussion),
    Timeline(4, R.string.share_to_wechat_timeline_timeline),
    Trade(5, R.string.share_to_wechat_timeline_trade);

    private final int type;
    private final int titleResId;

    WeChatMessageType(int type, int titleResId)
    {
        this.type = type;
        this.titleResId = titleResId;
    }

    public int getTitleResId()
    {
        return titleResId;
    }

    public int getType()
    {
        return type;
    }

    static WeChatMessageType fromType(int type)
    {
        for (WeChatMessageType wxType: values())
        {
            if (wxType.type == type)
            {
                return wxType;
            }
        }
        return null;
    }
}

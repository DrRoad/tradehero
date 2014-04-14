package com.tradehero.th.api.timeline.form;

import java.util.Map;

/**
 * Created by xavier2 on 2014/4/11.
 */
public class TradePublishableFormDTO extends PublishableFormDTO
{
    public String tradeComment;

    public TradePublishableFormDTO()
    {
        super();
    }

    public TradePublishableFormDTO(Boolean publishToFb, Boolean publishToTw, Boolean publishToLi, String geo_alt, String geo_lat, String geo_long,
            boolean aPublic,
            String tradeComment)
    {
        super(publishToFb, publishToTw, publishToLi, geo_alt, geo_lat, geo_long, aPublic);
        this.tradeComment = tradeComment;
    }

    @Override public Map<String, String> toStringMap()
    {
        Map<String, String> map = super.toStringMap();
        if (tradeComment != null)
        {
            map.put(POST_KEY_TRADE_COMMENT, tradeComment);
        }
        return map;
    }
}

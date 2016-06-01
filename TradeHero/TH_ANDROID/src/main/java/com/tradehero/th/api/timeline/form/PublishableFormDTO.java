package com.ayondo.academy.api.timeline.form;

import android.support.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

public class PublishableFormDTO
{
    public static final String POST_KEY_PUBLISH_TO_FACEBOOK = "publishToFb";
    public static final String POST_KEY_PUBLISH_TO_TWITTER = "publishToTw";
    public static final String POST_KEY_PUBLISH_TO_LINKEDIN = "publishToLi";
    public static final String POST_KEY_PUBLISH_TO_WEIBO = "publishToWb";
    public static final String POST_KEY_GEO_ALT = "geo_alt";
    public static final String POST_KEY_GEO_LAT = "geo_lat";
    public static final String POST_KEY_GEO_LONG = "geo_long";
    public static final String POST_KEY_IS_PUBLIC = "isPublic";
    public static final String POST_KEY_TRADE_COMMENT = "tradeComment";

    public Boolean publishToFb;
    public Boolean publishToTw;
    public Boolean publishToLi;
    public Boolean publishToWb;

    public String geo_alt;
    public String geo_lat;
    public String geo_long;

    public boolean isPublic;

    //<editor-fold desc="Constructors">
    public PublishableFormDTO()
    {
    }

    public PublishableFormDTO(Boolean publishToFb, Boolean publishToTw, Boolean publishToLi, Boolean publishToWb, String geo_alt, String geo_lat, String geo_long,
            boolean aPublic)
    {
        this.publishToFb = publishToFb;
        this.publishToTw = publishToTw;
        this.publishToLi = publishToLi;
        this.publishToWb = publishToWb;
        this.geo_alt = geo_alt;
        this.geo_lat = geo_lat;
        this.geo_long = geo_long;
        isPublic = aPublic;
    }
    //</editor-fold>

    @NonNull public Map<String, String> toStringMap()
    {
        Map<String, String> map = new HashMap<>();
        if (publishToFb != null)
        {
            map.put(POST_KEY_PUBLISH_TO_FACEBOOK, publishToFb ? "1" : "0");
        }
        if (publishToTw != null)
        {
            map.put(POST_KEY_PUBLISH_TO_TWITTER, publishToTw ? "1" : "0");
        }
        if (publishToLi != null)
        {
            map.put(POST_KEY_PUBLISH_TO_LINKEDIN, publishToLi ? "1" : "0");
        }
        if (publishToWb != null)
        {
            map.put(POST_KEY_PUBLISH_TO_WEIBO, publishToWb ? "1" : "0");
        }
        if (geo_alt != null)
        {
            map.put(POST_KEY_GEO_ALT, geo_alt);
        }
        if (geo_lat != null)
        {
            map.put(POST_KEY_GEO_LAT, geo_lat);
        }
        if (geo_long != null)
        {
            map.put(POST_KEY_GEO_LONG, geo_long);
        }
        map.put(POST_KEY_IS_PUBLIC, isPublic ? "1" : "0");

        return map;
    }

    @Override public String toString()
    {
        return "PublishableFormDTO{" +
                "geo_alt='" + geo_alt + '\'' +
                ", publishToFb=" + publishToFb +
                ", publishToTw=" + publishToTw +
                ", publishToLi=" + publishToLi +
                ", publishToWb=" + publishToWb +
                ", geo_lat='" + geo_lat + '\'' +
                ", geo_long='" + geo_long + '\'' +
                ", isPublic=" + isPublic +
                '}';
    }
}

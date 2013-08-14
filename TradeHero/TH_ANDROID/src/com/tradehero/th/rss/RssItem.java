
package com.tradehero.th.rss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class RssItem implements Comparable<RssItem>, Parcelable
{

    private RssFeed feed;
    private String title;
    private String link;
    private Date pubDate;
    private String description;
    private String content;
    private String thumbnail = "";

    public RssItem()
    {

    }

    public RssItem(Parcel source)
    {

        Bundle data = source.readBundle();
        title = data.getString("title").trim();
        link = data.getString("link").trim();
        pubDate = (Date) data.getSerializable("pubDate");
        description = data.getString("description").trim();
        content = data.getString("content").trim();
        thumbnail = data.getString("thumbnail").trim();
        feed = data.getParcelable("feed");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

        Bundle data = new Bundle();
        data.putString("title", title);
        data.putString("link", link);
        data.putSerializable("pubDate", pubDate);
        data.putString("description", description);
        data.putString("content", content);
        data.putString("thumbnail", thumbnail);
        data.putParcelable("feed", feed);
        dest.writeBundle(data);
    }

    public static final Parcelable.Creator<RssItem> CREATOR = new Parcelable.Creator<RssItem>()
    {
        public RssItem createFromParcel(Parcel data)
        {
            return new RssItem(data);
        }

        public RssItem[] newArray(int size)
        {
            return new RssItem[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    //	public RssFeed getFeed() {
    //		return feed;
    //	}
    //
    //	public void setFeed(RssFeed feed) {
    //		this.feed = feed;
    //	}

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public Date getPubDate()
    {
        return pubDate;
    }

    public void setPubDate(Date pubDate)
    {
        this.pubDate = pubDate;
    }

    public void setPubDate(String pubDate)
    {

        try
        {
            SimpleDateFormat dateFormat = RssHandler.getRssDateFormat();
            //new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            if (pubDate.length() > 0)
            {
                this.pubDate = dateFormat.parse(pubDate);
            }
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getContent()
    {
        return content;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public int compareTo(RssItem another)
    {
        if (getPubDate() != null && another.getPubDate() != null)
        {
            return getPubDate().compareTo(another.getPubDate());
        }
        else
        {
            return 0;
        }
    }
}

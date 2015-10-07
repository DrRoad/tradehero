package com.tradehero.th.api.news;

import android.support.annotation.Nullable;
import com.tradehero.th.api.ExtendedDTO;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.news.key.NewsItemDTOKey;
import com.tradehero.th.api.security.SecurityCompactDTO;

public class NewsItemCompactDTO extends AbstractDiscussionCompactDTO<NewsItemCompactDTO>
{
    public String title;
    public String caption;
    public String description;

    @Nullable public NewsItemSourceDTO source;
    public String url;

    public NewsItemCategoryDTO category;
    public SecurityCompactDTO topReferencedSecurity;

    public String imageUrl;

    //<editor-fold desc="Constructors">
    public NewsItemCompactDTO()
    {
    }

    public <ExtendedDTOType extends ExtendedDTO>
    NewsItemCompactDTO(ExtendedDTOType other,
            Class<? extends NewsItemCompactDTO> myClass)
    {
        super(other, myClass);
    }
    //</editor-fold>

    @Override public NewsItemDTOKey getDiscussionKey()
    {
        return new NewsItemDTOKey(id);
    }

    @Override public String toString()
    {
        return "NewsItemCompactDTO{" +
                "title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

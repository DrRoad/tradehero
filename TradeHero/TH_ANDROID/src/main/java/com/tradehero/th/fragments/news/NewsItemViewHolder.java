package com.tradehero.th.fragments.news;

import android.widget.TextView;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.fragments.discussion.AbstractDiscussionItemViewHolder;
import java.net.MalformedURLException;
import java.net.URL;

public class NewsItemViewHolder<DiscussionType extends NewsItemDTO> extends
        AbstractDiscussionItemViewHolder<DiscussionType>
{
    @InjectView(R.id.news_title_description) TextView newsDescription;
    @InjectView(R.id.news_title_title) TextView newsTitle;
    @InjectView(R.id.news_source) TextView newsSource;

    //<editor-fold desc="Constructors">
    public NewsItemViewHolder()
    {
        super();
    }
    //</editor-fold>

    @Override public void linkWith(DiscussionType discussionDTO, boolean andDisplay)
    {
        super.linkWith(discussionDTO, andDisplay);
        if (andDisplay)
        {
        }
    }

    @Override public boolean isAutoTranslate()
    {
        return false;
    }

    //<editor-fold desc="Display Methods">
    @Override public void display()
    {
        super.display();
        displaySource();
        displayTitle();
        displayDescription();
    }

    private void displaySource()
    {
        if (discussionDTO != null)
        {
            newsSource.setText(parseHost(discussionDTO.url));
        }
        else
        {
            newsSource.setText(R.string.na);
        }
    }

    private String parseHost(String url)
    {
        try
        {
            return new URL(url).getHost();
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    @Override public void displayTranslatableTexts()
    {
        displayTitle();
        displayDescription();
    }

    private void displayTitle()
    {
        newsTitle.setText(getTitleText());
    }

    private String getTitleText()
    {
        switch (currentTranslationStatus)
        {
            case ORIGINAL:
            case TRANSLATING:
                if (discussionDTO != null)
                {
                    return discussionDTO.title;
                }
                return null;

            case TRANSLATED:
                if (translatedDiscussionDTO != null)
                {
                    return translatedDiscussionDTO.title;
                }
                return null;
        }
        throw new IllegalStateException("Unhandled state " + currentTranslationStatus);
    }

    private void displayDescription()
    {
        newsDescription.setText(getDescriptionText());
    }

    private String getDescriptionText()
    {
        switch (currentTranslationStatus)
        {
            case ORIGINAL:
            case TRANSLATING:
                if (discussionDTO != null)
                {
                    return discussionDTO.description;
                }
                return null;

            case TRANSLATED:
                if (translatedDiscussionDTO != null)
                {
                    return translatedDiscussionDTO.description;
                }
                return null;
        }
        throw new IllegalStateException("Unhandled state " + currentTranslationStatus);
    }
    //</editor-fold>
}

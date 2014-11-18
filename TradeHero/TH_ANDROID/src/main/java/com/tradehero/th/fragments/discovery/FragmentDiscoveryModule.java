package com.tradehero.th.fragments.discovery;

import android.content.SharedPreferences;
import com.tradehero.common.annotation.ForUser;
import com.tradehero.th.api.news.CountryLanguagePairDTO;
import java.util.Locale;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                DiscoveryMainFragment.class,

                NewsHeadlineFragment.class,
                RegionalNewsHeadlineFragment.class,
                DiscoveryDiscussionFragment.class,
                LearningFragment.class,
                DiscoveryGamesFragment.class,
                GamesItemView.class,

                RegionalNewsSelectorView.class,
                RegionalNewsSearchableSelectorView.class
        },
        library = true,
        complete = false
)
public class FragmentDiscoveryModule
{
    private static final String PREF_KEY_REGIONAL_NEWS_COUNTRY_LANGUAGE = "PREF_KEY_REGIONAL_NEWS_COUNTRY_LANGUAGE";

    @Provides @Singleton @RegionalNews
    public CountryLanguagePreference provideRegionalNewsCountryLanguagePreference(@ForUser SharedPreferences sharedPreferences, Locale locale)
    {
        CountryLanguagePairDTO countryLanguagePairDTO = new CountryLanguagePairDTO(null, null, locale.getLanguage());
        return new CountryLanguagePreference(sharedPreferences, PREF_KEY_REGIONAL_NEWS_COUNTRY_LANGUAGE, countryLanguagePairDTO);
    }
}

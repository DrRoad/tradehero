package com.tradehero.th.api.translation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tradehero.common.persistence.DTO;
import com.tradehero.th.BuildConfig;
import com.tradehero.th.api.i18n.LanguageDTO;
import com.tradehero.th.api.translation.bing.BingUserTranslationSettingDTO;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = UserTranslationSettingDTO.class,
        property = "translatorType"
)
@JsonSubTypes(
        @JsonSubTypes.Type(value = BingUserTranslationSettingDTO.class, name = BingUserTranslationSettingDTO.SETTING_TYPE)
)
public class UserTranslationSettingDTO implements DTO
{
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final boolean DEFAULT_AUTO_TRANSLATE = !BuildConfig.DEBUG;

    @NonNull public final String languageCode;
    public final boolean autoTranslate;

    //<editor-fold desc="Constructors">
    protected UserTranslationSettingDTO()
    {
        this(DEFAULT_LANGUAGE_CODE);
    }

    public UserTranslationSettingDTO(
            @NonNull String languageCode)
    {
        this(languageCode, DEFAULT_AUTO_TRANSLATE);
    }

    public UserTranslationSettingDTO(
            @NonNull String languageCode,
            boolean autoTranslate)
    {
        this.languageCode = languageCode;
        this.autoTranslate = autoTranslate;
    }
    //</editor-fold>

    @NonNull public UserTranslationSettingDTO cloneForLanguage(@NonNull LanguageDTO languageDTO)
    {
        throw new IllegalArgumentException("Not implemented");
    }

    @NonNull public UserTranslationSettingDTO cloneForAuto(boolean newAutoValue)
    {
        throw new IllegalArgumentException("Not implemented");
    }

    public int getProviderStringResId()
    {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override public int hashCode()
    {
        return getClass().hashCode();
    }

    @Override public boolean equals(@Nullable Object other)
    {
        return other != null && other.getClass().equals(getClass());
    }
}

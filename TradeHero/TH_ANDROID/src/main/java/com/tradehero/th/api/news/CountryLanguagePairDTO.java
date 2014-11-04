package com.tradehero.th.api.news;

public class CountryLanguagePairDTO
{
    public String name;
    public String countryCode;
    public String languageCode;

    public CountryLanguagePairDTO(String name, String countryCode, String languageCode)
    {
        this.name = name;
        this.countryCode = countryCode;
        this.languageCode = languageCode;
    }

    /** Naked constructor for deserialization */
    public CountryLanguagePairDTO() { }

    @Override public String toString()
    {
        return this.name;
    }
}

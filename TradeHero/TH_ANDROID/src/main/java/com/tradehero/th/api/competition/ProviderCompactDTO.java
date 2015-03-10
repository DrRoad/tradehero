package com.tradehero.th.api.competition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradehero.common.persistence.DTO;
import com.tradehero.th.api.competition.specific.ProviderSpecificKnowledgeDTO;
import com.tradehero.th.api.competition.specific.ProviderSpecificResourcesDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.api.portfolio.PortfolioCompactDTO;
import com.tradehero.th.utils.SecurityUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ProviderCompactDTO implements DTO
{
    public int id;
    public String name;
    public String logoUrl;
    public boolean isUserEnrolled;
    public String hexColor;
    public String navigationLogoUrl;
    public List<AdDTO> advertisements;
    public String competitionScreenTitle;
    public String competitionScreenSubtitle;
    public String textHexColor;
    public String joinedLogoUrl;
    public String secondaryHexColor;
    public String providerSubtitle;
    public String joinedProviderSubtitle;
    public double singleRowHeightPoint;
    public double multiRowHeightPoint;
    public String singleImageUrl;
    public String singleSelectedImageUrl;
    public String multiImageUrl;
    public String multiSelectedImageUrl;
    public String singleJoinedImageUrl;
    public String singleJoinedSelectedImageUrl;
    public String multiJoinedImageUrl;
    public String multiJoinedSelectedImageUrl;
    public String tradeButtonImageUrl;
    public String tradeButtonSelectedImageUrl;
    public String tileImageUrl;
    public String tileJoinedImageUrl;
    public String timerImageUrl;
    public boolean volumeRestrictionEnabled;
    public Date tradeRestrictionStartHourUtc;
    public Date tradeRestrictionEndHourUtc;
    public String appiTunesUrl;
    public String iosAppSecurityPageUrlTemplate;
    public String helpVideoText;
    public String ruleText;
    public boolean hasHelpVideo;
    public String wizardUrl;
    public String wizardTitle;
    public String wizardImageUrl;
    public String ctaLocationTags;
    public String currencyDisplay;
    public String currencyISO;

    public PortfolioCompactDTO associatedPortfolio;

    // These 2 fields are populated exclusively on the client
    @JsonIgnore @Nullable public ProviderSpecificKnowledgeDTO specificKnowledge;
    @JsonIgnore @Nullable public ProviderSpecificResourcesDTO specificResources;

    /**
     * Creates the id that identifies this DTO.
     * @return
     */
    @JsonIgnore
    public ProviderId getProviderId()
    {
        return new ProviderId(id);
    }

    @JsonIgnore
    public boolean hasWizard()
    {
        if (specificKnowledge != null && specificKnowledge.hasWizard != null)
        {
            return specificKnowledge.hasWizard;
        }
        return wizardUrl != null && wizardUrl.length() > 0;
    }

    @JsonIgnore
    public String getStatusSingleImageUrl(boolean isSelected)
    {
        return isSelected ? getStatusSingleSelectedImageUrl() : getStatusSingleImageUrl();
    }

    @JsonIgnore
    public String getStatusSingleImageUrl()
    {
        return isUserEnrolled ? singleJoinedImageUrl : singleImageUrl;
    }

    @JsonIgnore
    public String getStatusSingleSelectedImageUrl()
    {
        return isUserEnrolled ? singleJoinedSelectedImageUrl : singleSelectedImageUrl;
    }

    @JsonIgnore
    public String getStatusMultiImageUrl(boolean isSelected)
    {
        return isSelected ? getStatusMultiSelectedImageUrl() : getStatusMultiImageUrl();
    }

    @JsonIgnore
    public String getStatusMultiImageUrl()
    {
        return isUserEnrolled ? multiJoinedImageUrl : multiImageUrl;
    }

    @JsonIgnore
    public String getStatusMultiSelectedImageUrl()
    {
        return isUserEnrolled ? multiJoinedSelectedImageUrl : multiSelectedImageUrl;
    }

    @JsonIgnore
    public String getStatusTileImageUrl()
    {
        return isUserEnrolled ? tileJoinedImageUrl : tileImageUrl;
    }

    @JsonIgnore
    public String getTradeButtonImageUrl(boolean isSelected)
    {
        return isSelected ? tradeButtonSelectedImageUrl : tradeButtonImageUrl;
    }

    @JsonIgnore
    public String getNiceCurrency()
    {
        if (hasValidCurrencyDisplay())
        {
            return currencyDisplay;
        }
        return SecurityUtils.DEFAULT_VIRTUAL_CASH_CURRENCY_DISPLAY;
    }

    @JsonIgnore
    public boolean hasValidCurrencyDisplay()
    {
        return currencyDisplay != null && !currencyDisplay.isEmpty();
    }

    public static List<ProviderId> getProviderIds(List<ProviderCompactDTO> providerDTOs)
    {
        if (providerDTOs == null)
        {
            return null;
        }

        List<ProviderId> providerIds = new ArrayList<>();

        for (ProviderCompactDTO providerDTO: providerDTOs)
        {
            providerIds.add(providerDTO.getProviderId());
        }

        return providerIds;
    }

    public boolean hasAdvertisement()
    {
        return advertisements != null && !advertisements.isEmpty();
    }

    @JsonIgnore @Nullable
    public OwnedPortfolioId getAssociatedOwnedPortfolioId()
    {
        if (associatedPortfolio == null)
        {
            return null;
        }
        return associatedPortfolio.getOwnedPortfolioId();
    }

    @Override public String toString()
    {
        return "ProviderCompactDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", isUserEnrolled=" + isUserEnrolled +
                ", hexColor='" + hexColor + '\'' +
                ", navigationLogoUrl='" + navigationLogoUrl + '\'' +
                ", advertisements=" + advertisements +
                ", competitionScreenTitle='" + competitionScreenTitle + '\'' +
                ", competitionScreenSubtitle='" + competitionScreenSubtitle + '\'' +
                ", joinedLogoUrl='" + joinedLogoUrl + '\'' +
                ", secondaryHexColor='" + secondaryHexColor + '\'' +
                ", providerSubtitle='" + providerSubtitle + '\'' +
                ", joinedProviderSubtitle='" + joinedProviderSubtitle + '\'' +
                ", singleRowHeightPoint=" + singleRowHeightPoint +
                ", multiRowHeightPoint=" + multiRowHeightPoint +
                ", singleImageUrl='" + singleImageUrl + '\'' +
                ", singleSelectedImageUrl='" + singleSelectedImageUrl + '\'' +
                ", multiImageUrl='" + multiImageUrl + '\'' +
                ", multiSelectedImageUrl='" + multiSelectedImageUrl + '\'' +
                ", singleJoinedImageUrl='" + singleJoinedImageUrl + '\'' +
                ", singleJoinedSelectedImageUrl='" + singleJoinedSelectedImageUrl + '\'' +
                ", multiJoinedImageUrl='" + multiJoinedImageUrl + '\'' +
                ", multiJoinedSelectedImageUrl='" + multiJoinedSelectedImageUrl + '\'' +
                ", tradeButtonImageUrl='" + tradeButtonImageUrl + '\'' +
                ", tradeButtonSelectedImageUrl='" + tradeButtonSelectedImageUrl + '\'' +
                ", tileImageUrl='" + tileImageUrl + '\'' +
                ", tileJoinedImageUrl='" + tileJoinedImageUrl + '\'' +
                ", timerImageUrl='" + timerImageUrl + '\'' +
                ", volumeRestrictionEnabled=" + volumeRestrictionEnabled +
                ", tradeRestrictionStartHourUtc=" + tradeRestrictionStartHourUtc +
                ", tradeRestrictionEndHourUtc=" + tradeRestrictionEndHourUtc +
                ", appiTunesUrl='" + appiTunesUrl + '\'' +
                ", iosAppSecurityPageUrlTemplate='" + iosAppSecurityPageUrlTemplate + '\'' +
                ", helpVideoText='" + helpVideoText + '\'' +
                ", ruleText='" + ruleText + '\'' +
                ", hasHelpVideo=" + hasHelpVideo +
                ", wizardUrl='" + wizardUrl + '\'' +
                ", ctaLocationTags=" + ctaLocationTags +
                ", currencyDisplay=" + currencyDisplay +
                ", currencyISO=" + currencyISO +
                ", associatedPortfolio=" + associatedPortfolio +
        '}';
    }
}
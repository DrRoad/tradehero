package com.tradehero.th.fragments.competition.zone.dto;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tradehero.th.api.users.UserProfileCompactDTO;

public class CompetitionZonePortfolioDTO extends CompetitionZoneDTO
{
    @NonNull public final UserProfileCompactDTO userProfileCompactDTO;

    //<editor-fold desc="Constructors">
    public CompetitionZonePortfolioDTO(
            @Nullable String title,
            @Nullable String description,
            @NonNull UserProfileCompactDTO userProfileCompactDTO)
    {
        super(title, description);
        this.userProfileCompactDTO = userProfileCompactDTO;
    }
    //</editor-fold>

    @Override public String toString()
    {
        return "CompetitionZonePortfolioDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", userProfileCompactDTO='" + userProfileCompactDTO + '\'' +
                '}';
    }
}

package com.tradehero.th.fragments.competition.zone.dto;

import com.tradehero.th.api.users.UserProfileCompactDTO;

public class CompetitionZonePortfolioDTO extends CompetitionZoneDTO
{
    public UserProfileCompactDTO userProfileCompactDTO;

    public CompetitionZonePortfolioDTO(String title, String description, UserProfileCompactDTO userProfileCompactDTO)
    {
        super(title, description);
        this.userProfileCompactDTO = userProfileCompactDTO;
    }

    @Override public String toString()
    {
        return "CompetitionZonePortfolioDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", userProfileCompactDTO='" + userProfileCompactDTO + '\'' +
                '}';
    }
}

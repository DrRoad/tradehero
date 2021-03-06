package com.tradehero.th.fragments.competition.zone;

import android.content.Context;
import android.util.AttributeSet;
import com.squareup.picasso.Transformation;
import com.tradehero.th.R;
import com.tradehero.th.api.users.UserProfileCompactDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZonePortfolioDTO;
import com.tradehero.th.models.graphics.ForUserPhoto;
import javax.inject.Inject;

public class CompetitionZonePortfolioView extends CompetitionZoneListItemView
{
    @Inject @ForUserPhoto protected Transformation zoneIconTransformation;

    //<editor-fold desc="Constructors">
    @SuppressWarnings("UnusedDeclaration")
    public CompetitionZonePortfolioView(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CompetitionZonePortfolioView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CompetitionZonePortfolioView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    //<editor-fold desc="Display Methods">
    @Override public void displayIcon()
    {
        super.displayIcon();

        if (zoneIcon != null)
        {
            boolean loaded = false;
            picasso.cancelRequest(zoneIcon);
            
            if (competitionZoneDTO instanceof CompetitionZonePortfolioDTO)
            {
                UserProfileCompactDTO profileDTO = ((CompetitionZonePortfolioDTO) competitionZoneDTO).userProfileCompactDTO;
                if (profileDTO != null && profileDTO.picture != null)
                {

                    picasso.load(profileDTO.picture)
                            .transform(zoneIconTransformation)
                            .centerInside()
                            .fit()
                            .into(zoneIcon);
                    loaded = true;
                }
            }

            if (!loaded)
            {
                picasso.load(R.drawable.superman_facebook)
                        .transform(zoneIconTransformation)
                        .centerInside()
                        .fit()
                        .into(zoneIcon);
            }
        }
    }
    //</editor-fold>
}

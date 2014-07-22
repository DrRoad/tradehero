package com.tradehero.th.fragments.competition.zone;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.LeaderboardUserDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLeaderboardDTO;
import com.tradehero.th.utils.THSignedNumber;

public class CompetitionZoneLeaderboardListItemView extends CompetitionZoneListItemView
{
    public static final int COLOR_ACTIVE = R.color.black;
    public static final int COLOR_INACTIVE = R.color.text_gray_normal;

    @InjectView(R.id.competition_roi) protected TextView roiView;

    //<editor-fold desc="Constructors">
    public CompetitionZoneLeaderboardListItemView(Context context)
    {
        super(context);
    }

    public CompetitionZoneLeaderboardListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CompetitionZoneLeaderboardListItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override protected void initViews()
    {
        super.initViews();
    }

    @Override public void linkWith(CompetitionZoneDTO competitionZoneDTO, boolean andDisplay)
    {
        super.linkWith(competitionZoneDTO, andDisplay);
        if (andDisplay)
        {
            displayROI();
        }
    }

    //<editor-fold desc="Display Methods">
    @Override public void display()
    {
        super.display();
        displayROI();
    }

    public void displayIcon()
    {
        if (zoneIcon != null)
        {
            if (competitionZoneDTO != null && competitionZoneDTO instanceof CompetitionZoneLeaderboardDTO)
            {
                CompetitionZoneLeaderboardDTO zoneLeaderboard = (CompetitionZoneLeaderboardDTO) competitionZoneDTO;
                if (zoneLeaderboard.competitionDTO != null)
                {
                    String iconUrl = zoneLeaderboard.competitionDTO.getIconUrl();
                    if (iconUrl != null)
                    {
                        picasso.load(iconUrl).into(zoneIcon);
                    }
                }
            }
        }
    }

    @Override public void displayTitle()
    {
        super.displayTitle();
        if (title != null)
        {
            title.setTextColor(getResources().getColor(getTitleColorResId()));
        }
    }

    public int getTitleColorResId()
    {
        Boolean isActive = isActive();
        return isActive == null || isActive ? COLOR_ACTIVE : COLOR_INACTIVE;
    }

    public Boolean isActive()
    {
        if (competitionZoneDTO == null)
        {
            return null;
        }
        return ((CompetitionZoneLeaderboardDTO) competitionZoneDTO).isActive();
    }

    public void displayROI()
    {
        if (roiView != null)
        {
            if (competitionZoneDTO != null && competitionZoneDTO instanceof CompetitionZoneLeaderboardDTO)
            {
                LeaderboardUserDTO leaderboardUserDTO = ((CompetitionZoneLeaderboardDTO) competitionZoneDTO).competitionDTO.leaderboardUser;
                if(leaderboardUserDTO != null)
                {
                    THSignedNumber thRoi = new THSignedNumber(
                            THSignedNumber.TYPE_PERCENTAGE,
                            leaderboardUserDTO.roiInPeriod * 100);

                    roiView.setText(thRoi.toString());
                    roiView.setTextColor(getResources().getColor(thRoi.getColor()));
                }
                else
                {
                    roiView.setTextColor(Color.BLACK);
                    roiView.setText(R.string.na);
                }
            }
        }
    }
    //</editor-fold>
}

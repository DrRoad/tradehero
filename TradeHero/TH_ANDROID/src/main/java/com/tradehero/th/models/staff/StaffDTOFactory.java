package com.tradehero.th.models.staff;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.tradehero.th.R;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class StaffDTOFactory
{
    public static final String SEPARATOR_NAME_TITLE = "\\|";

    @NonNull public static List<StaffDTO> getTradeHeroStaffers(@NonNull Resources resources)
    {
        String[] staffs = resources.getStringArray(R.array.staffs);
        List<StaffDTO> staffDTOs = new ArrayList<>(staffs.length);
        for (String staff: staffs)
        {
            String[] staffInfo = staff.split(SEPARATOR_NAME_TITLE);
            if (staffInfo.length < 2)
            {
                continue;
            }
            staffDTOs.add(new StaffDTO(staffInfo[0], staffInfo[1]));
        }
        return staffDTOs;
    }
}

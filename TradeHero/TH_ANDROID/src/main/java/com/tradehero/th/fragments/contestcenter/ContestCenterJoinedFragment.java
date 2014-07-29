package com.tradehero.th.fragments.contestcenter;

import com.tradehero.th.R;
import com.tradehero.th.api.competition.ProviderDTO;
import org.jetbrains.annotations.NotNull;

public class ContestCenterJoinedFragment extends ContestCenterBaseFragment
{
    @Override public void recreateAdapter()
    {
        setContestCenterScreen(android.R.id.list);
        contestListAdapter = createAdapter();
        if (providerDTOs != null)
        {
            for (@NotNull ProviderDTO providerDTO : providerDTOs)
            {
                if (providerDTO.isUserEnrolled)
                {
                    contestListAdapter.add(new ProviderContestPageDTO(providerDTO));
                    if (providerDTO.vip != null && providerDTO.vip)
                    {
                        contestListAdapter.add(new EmptyHeadLineDTO());
                    }
                }
            }
        }

        contestListView.setAdapter(contestListAdapter);

        if (isNojoinedContest())
        {
            setContestCenterScreen(R.id.contest_no_joined);
        }
    }

    @Override public ContestCenterTabType getCCTabType()
    {
        return ContestCenterTabType.JOINED;
    }

    public boolean isNojoinedContest()
    {
        if (providerDTOs != null)
        {
            for (@NotNull ProviderDTO providerDTO : providerDTOs)
            {
                if (providerDTO.isUserEnrolled)
                {
                    return false;
                }
            }
        }
        return true;
    }
}

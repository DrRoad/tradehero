package com.tradehero.th.api.education;

import com.tradehero.common.api.BaseArrayList;
import com.tradehero.common.persistence.DTO;
import java.util.Collection;

public class VideoCategoryDTOList extends BaseArrayList<VideoCategoryDTO> implements DTO
{
    public VideoCategoryDTOList()
    {
        super();
    }

    public VideoCategoryDTOList(Collection<? extends VideoCategoryDTO> c)
    {
        super(c);
    }
}

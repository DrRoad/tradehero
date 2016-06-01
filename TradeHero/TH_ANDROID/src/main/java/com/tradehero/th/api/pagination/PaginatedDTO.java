package com.ayondo.academy.api.pagination;

import com.tradehero.common.persistence.DTO;
import java.util.Collections;
import java.util.List;

public class PaginatedDTO<ListedType> implements DTO
{
    private List<ListedType> data;
    private PaginationInfoDTO pagination;

    public List<ListedType> getData()
    {
        return Collections.unmodifiableList(data);
    }

    public void setData(List<ListedType> data)
    {
        this.data = data;
    }

    public PaginationInfoDTO getPagination()
    {
        return pagination;
    }

    public void setPagination(PaginationInfoDTO pagination)
    {
        this.pagination = pagination;
    }

    public boolean hasNullItem()
    {
        List<ListedType> data = getData();
        if (data == null)
        {
            return true;
        }
        for (ListedType item: data)
        {
            if (item == null)
            {
                return true;
            }
        }
        return false;
    }
}

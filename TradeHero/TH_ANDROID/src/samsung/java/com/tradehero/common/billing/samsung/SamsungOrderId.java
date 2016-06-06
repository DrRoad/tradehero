package com.androidth.general.common.billing.samsung;

import com.androidth.general.common.billing.OrderId;

public class SamsungOrderId implements OrderId
{
    public final String purchaseId;

    public SamsungOrderId(String purchaseId)
    {
        this.purchaseId = purchaseId;
    }

    @Override public int hashCode()
    {
        return purchaseId == null ? 0 : purchaseId.hashCode();
    }

    @Override public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    public boolean equals(SamsungOrderId other)
    {
        if (other == null)
        {
            return false;
        }
        if (purchaseId == null)
        {
            return other.purchaseId == null;
        }
        return purchaseId.equals(other.purchaseId);
    }
}

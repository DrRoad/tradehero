package com.tradehero.th.models.sms.twilio;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tradehero.th.models.sms.SMSId;

public class TwilioSMSId implements SMSId
{
    @NonNull public final String id;

    @JsonCreator public TwilioSMSId(@NonNull String id)
    {
        this.id = id;
    }

    @Override public int hashCode()
    {
        return id.hashCode();
    }

    @Override public boolean equals(Object o)
    {
        return o instanceof TwilioSMSId && ((TwilioSMSId) o).id.equals(id);
    }

    @JsonValue @NonNull String getId()
    {
        return id;
    }
}

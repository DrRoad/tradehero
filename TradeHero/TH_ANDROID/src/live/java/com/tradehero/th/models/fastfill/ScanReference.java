package com.tradehero.th.models.fastfill;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tradehero.th.models.fastfill.jumio.NetverifyScanReference;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes(
        @JsonSubTypes.Type(value = NetverifyScanReference.class, name = NetverifyScanReference.TYPE)
)
public interface ScanReference
{
    @NonNull String getValue();
    int hashCode();
    boolean equals(@Nullable Object o);
}

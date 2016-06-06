package com.androidth.general.api.discussion.key;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.androidth.general.common.persistence.DTOKey;
import com.androidth.general.api.Querylizable;
import com.androidth.general.api.discussion.DiscussionType;
import java.util.HashMap;
import java.util.Map;

public class DiscussionListKey
        implements DTOKey, Querylizable<String>
{
    public static final String IN_REPLY_TO_TYPE_NAME_BUNDLE_KEY = DiscussionListKey.class.getName() + ".inReplyToType";
    public static final String IN_REPLY_TO_ID_BUNDLE_KEY = DiscussionListKey.class.getName() + ".inReplyToId";

    public final DiscussionType inReplyToType;
    public final int inReplyToId;

    //<editor-fold desc="Constructors">
    public DiscussionListKey(DiscussionType inReplyToType, int inReplyToId)
    {
        this.inReplyToType = inReplyToType;
        this.inReplyToId = inReplyToId;
    }

    public DiscussionListKey(Bundle args)
    {
        if (!args.containsKey(IN_REPLY_TO_TYPE_NAME_BUNDLE_KEY))
        {
            throw new IllegalArgumentException("Missing IN_REPLY_TO_TYPE_NAME_BUNDLE_KEY");
        }
        if (!args.containsKey(IN_REPLY_TO_ID_BUNDLE_KEY))
        {
            throw new IllegalArgumentException("Missing IN_REPLY_TO_ID_BUNDLE_KEY");
        }
        this.inReplyToType = DiscussionType.valueOf(args.getString(IN_REPLY_TO_TYPE_NAME_BUNDLE_KEY));
        this.inReplyToId = args.getInt(IN_REPLY_TO_ID_BUNDLE_KEY);
    }
    //</editor-fold>

    @Override public int hashCode()
    {
        return (inReplyToType == null ? 0 : inReplyToType.hashCode()) ^
                Integer.valueOf(inReplyToId).hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override public boolean equals(Object other)
    {
        return equalClass(other) && equalFields((DiscussionListKey) other);
    }

    public boolean equalClass(Object other)
    {
        return other != null && getClass().equals(other.getClass());
    }

    protected boolean equalFields(DiscussionListKey other)
    {
        return other != null &&
                (inReplyToType == null ? other.inReplyToType == null : inReplyToType.equals(other.inReplyToType)) &&
                inReplyToId == other.inReplyToId;
    }

    public boolean equivalentFields(@Nullable DiscussionKey other)
    {
        return (other != null) &&
                (inReplyToType == null ? other.getType() == null : inReplyToType.equals(other.getType())) &&
                Integer.valueOf(inReplyToId).equals(other.id);
    }

    public Bundle getArgs()
    {
        Bundle args = new Bundle();
        putParameters(args);
        return args;
    }

    protected void putParameters(Bundle args)
    {
        args.putString(IN_REPLY_TO_TYPE_NAME_BUNDLE_KEY, inReplyToType.name());
        args.putInt(IN_REPLY_TO_ID_BUNDLE_KEY, inReplyToId);
    }

    @Override public Map<String, Object> toMap()
    {
        return new HashMap<>();
    }

    @Override public String toString()
    {
        return "DiscussionListKey{" +
                "inReplyToType=" + inReplyToType +
                ", inReplyToId=" + inReplyToId +
                '}';
    }
}

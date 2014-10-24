package com.tradehero.th.fragments.discussion;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.form.DiscussionFormDTO;
import com.tradehero.th.api.discussion.form.SecurityReplyDiscussionFormDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class SecurityDiscussionEditPostFragment extends DiscussionEditPostFragment
{
    private static final String BUNDLE_KEY_SECURITY_ID = SecurityDiscussionEditPostFragment.class.getName() + ".securityId";

    @SuppressWarnings("UnusedDeclaration") @Inject Context doNotRemoveOrItFails;

    @Nullable Subscription securityCompactCacheSubscription;
    @Nullable SecurityCompactDTO securityCompactDTO;

    public static void putSecurityId(@NotNull Bundle args, @NotNull SecurityId securityId)
    {
        args.putBundle(BUNDLE_KEY_SECURITY_ID, securityId.getArgs());
    }

    @Nullable public static SecurityId getSecurityId(@Nullable Bundle args)
    {
        SecurityId extracted = null;
        if (args != null && args.containsKey(BUNDLE_KEY_SECURITY_ID))
        {
            extracted = new SecurityId(args.getBundle(BUNDLE_KEY_SECURITY_ID));
        }
        return extracted;
    }

    @Nullable private SecurityId securityId;

    private void linkWith(SecurityId securityId, boolean andDisplay)
    {
        this.securityId = securityId;

        if (andDisplay && securityId != null)
        {
            String securityName = String.format("%s:%s", securityId.getExchange(), securityId.getSecuritySymbol());
            discussionPostContent.setHint(getString(R.string.discussion_new_post_hint, securityName));
        }

        if (securityId != null)
        {
            detachSubscription(securityCompactCacheSubscription);
            securityCompactCacheSubscription = securityCompactCache.get(securityId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Pair<SecurityId, SecurityCompactDTO>>()
                    {
                        @Override public void onCompleted()
                        {
                        }

                        @Override public void onError(Throwable e)
                        {
                        }

                        @Override public void onNext(Pair<SecurityId, SecurityCompactDTO> pair)
                        {
                            securityCompactDTO = pair.second;
                            setActionBarSubtitle(getString(R.string.discussion_edit_post_subtitle, pair.second.name));
                            FragmentActivity activityCopy = getActivity();
                            if (activityCopy != null)
                            {
                                activityCopy.invalidateOptionsMenu();
                            }
                        }
                    });
        }
        if (andDisplay)
        {
        }
    }

    @Override protected DiscussionFormDTO buildDiscussionFormDTO()
    {
        SecurityReplyDiscussionFormDTO discussionFormDTO = (SecurityReplyDiscussionFormDTO) super.buildDiscussionFormDTO();
        if (discussionFormDTO != null && securityCompactDTO != null)
        {
            discussionFormDTO.inReplyToId = securityCompactDTO.id;
        }
        return discussionFormDTO;
    }

    @Override protected DiscussionType getDiscussionType()
    {
        return DiscussionType.SECURITY;
    }

    @Override public void onResume()
    {
        super.onResume();

        SecurityId fromArgs = getSecurityId(getArguments());
        if (fromArgs != null)
        {
            linkWith(fromArgs, true);
        }
    }

    @Override public void onDestroyView()
    {
        detachSubscription(securityCompactCacheSubscription);
        super.onDestroyView();
    }
}

package com.tradehero.th.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.AbstractDiscussionCompactDTO;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.VoteDirection;
import com.tradehero.th.api.discussion.key.DiscussionVoteKey;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.DiscussionServiceWrapper;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class VotePair extends LinearLayout
{
    @InjectView(R.id.timeline_action_button_vote_up) VoteView voteUp;
    @InjectView(R.id.timeline_action_button_vote_down) VoteView voteDown;

    @Inject Lazy<DiscussionServiceWrapper> discussionServiceWrapper;
    @Inject Provider<NetworkInfo> networkInfoProvider;

    @Nullable private Subscription voteSubscription;
    private AbstractDiscussionCompactDTO discussionDTO;
    private boolean downVote = false;

    public static interface OnVoteListener
    {
        void onVoteSuccess(DiscussionDTO discussionDTO);
    }

    private OnVoteListener onVoteListener;

    public void setOnVoteListener(OnVoteListener onVoteListener)
    {
        this.onVoteListener = onVoteListener;
    }

    @SuppressWarnings("UnusedDeclaration")
    public VotePair(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        HierarchyInjector.inject(context, this);
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VotePair);
            downVote = a.getBoolean(R.styleable.VotePair_downVote, false);
            a.recycle();
        }
    }

    private void updateDownVoteVisibility()
    {
        if (voteDown != null)
        {
            voteDown.setVisibility(downVote ? VISIBLE : GONE);
        }
    }

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ButterKnife.inject(this);
        updateDownVoteVisibility();
    }

    @Override protected void onDetachedFromWindow()
    {
        detachVoteMiddleCallback();

        ButterKnife.reset(this);

        super.onDetachedFromWindow();
    }

    protected void detachVoteMiddleCallback()
    {
        if (voteSubscription != null)
        {
            voteSubscription.unsubscribe();
        }
        voteSubscription = null;
    }

    @SuppressWarnings("UnusedDeclaration") @OnClick({
            R.id.timeline_action_button_vote_up,
            R.id.timeline_action_button_vote_down
    })
    public void onItemClicked(View view)
    {
        if (discussionDTO == null)
        {
            // TODO inform player about lack of information
            return;
        }
        switch (view.getId())
        {
            case R.id.timeline_action_button_vote_up:
                boolean targetVoteUp = voteUp.isChecked();
                fakeUpdateForVoteUp(targetVoteUp ? VoteDirection.UpVote : VoteDirection.UnVote);
                updateVoting(targetVoteUp ? VoteDirection.UpVote : VoteDirection.UnVote);
                break;
            case R.id.timeline_action_button_vote_down:
                if (voteDown.isChecked())
                {
                    //voteUp.setChecked(false);
                }
                updateVoting(voteDown.isChecked() ? VoteDirection.DownVote : VoteDirection.UnVote);
                break;
        }
    }

    private void fakeUpdateForVoteUp(VoteDirection targetVoteDirection)
    {
        if (targetVoteDirection == VoteDirection.UpVote)
        {
            voteUp.setValue(discussionDTO.upvoteCount + 1);
            voteUp.setChecked(true);

        }
        else if (targetVoteDirection == VoteDirection.UnVote)
        {
            int count = discussionDTO.upvoteCount - 1;
            voteUp.setValue(count < 0 ? 0:count);
            voteUp.setChecked(false);
        }
    }

    protected class VoteObserver implements Observer<DiscussionDTO>
    {
        private final AbstractDiscussionCompactDTO discussionDTO;
        private final VoteDirection targetVoteDirection;

        //<editor-fold desc="Constructors">
        public VoteObserver(VoteDirection voteDirection)
        {
            this.discussionDTO = VotePair.this.discussionDTO;
            this.targetVoteDirection = voteDirection;
        }
        //</editor-fold>

        @Override public void onNext(DiscussionDTO discussionDTO)
        {
            if (this.discussionDTO == null || VotePair.this.discussionDTO == null)
            {
                Timber.e("VoteCallback success but discussionDTO is null");
                return;
            }
            if (this.discussionDTO.id != discussionDTO.id)
            {
                Timber.e("VoteCallback success but id is not the same");
                return;
            }
            VoteDirection returnedVoteDirection = VoteDirection.fromValue(discussionDTO.voteDirection);
            if (this.discussionDTO.id == VotePair.this.discussionDTO.id)
            {
                //means the same item
                if (targetVoteDirection != returnedVoteDirection)
                {   //server may return the wrong voteDirection
                    discussionDTO.voteDirection = targetVoteDirection.value;
                    Timber.e("targetVoteDirection(%s) and returnedVoteDirection(%s) not the same",targetVoteDirection,returnedVoteDirection);
                }
                discussionDTO.populateVote(VotePair.this.discussionDTO);
                Timber.d("VoteCallback success and item is the same. voteDirection:%s",VotePair.this.discussionDTO.voteDirection);
                display(VotePair.this.discussionDTO);
                // TODO update cached timeline item
                Timber.d("Success");
                if (onVoteListener != null)
                {
                    onVoteListener.onVoteSuccess(discussionDTO);
                }
            }
            else
            {
                if (targetVoteDirection != returnedVoteDirection)
                {   //server ma
                    discussionDTO.voteDirection = targetVoteDirection.value;
                    Timber.e("targetVoteDirection(%s) and returnedVoteDirection(%s) not the same",targetVoteDirection,returnedVoteDirection);
                }
                discussionDTO.populateVote(this.discussionDTO);
                //do nothing
                Timber.e("VoteCallback success and item is not the same");
            }
        }

        @Override public void onCompleted()
        {
        }

        @Override public void onError(Throwable e)
        {
            Timber.e("VoteCallback Failure");
        }
    }

    private void updateVoting(VoteDirection voteDirection)
    {
        if (discussionDTO == null)
        {
            return;
        }
        DiscussionType discussionType = getDiscussionType();

        DiscussionVoteKey discussionVoteKey = new DiscussionVoteKey(
                discussionType,
                discussionDTO.id,
                voteDirection);
        detachVoteMiddleCallback();
        voteSubscription = discussionServiceWrapper.get().voteRx(discussionVoteKey)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VoteObserver(voteDirection));
    }

    public boolean hasDownVote()
    {
        return downVote;
    }

    public void setDownVote(boolean downVote)
    {
        this.downVote = downVote;

        updateDownVoteVisibility();
    }

    private DiscussionType getDiscussionType()
    {
        if (discussionDTO != null && discussionDTO.getDiscussionKey() != null)
        {
            return discussionDTO.getDiscussionKey().getType();
        }

        throw new IllegalStateException("Unknown discussion type");
    }

    public void display(AbstractDiscussionCompactDTO discussionDTO)
    {
        this.discussionDTO = discussionDTO;
        if (voteUp != null)
        {
            // We need to make these tests because view are detached from window in
            // disparate order
            // https://www.crashlytics.com/tradehero/android/apps/com.tradehero.th/issues/5360b347e3de5099ba24841d
            voteUp.display(discussionDTO);
        }
        if (voteDown != null)
        {
            voteDown.display(discussionDTO);
        }
    }
}

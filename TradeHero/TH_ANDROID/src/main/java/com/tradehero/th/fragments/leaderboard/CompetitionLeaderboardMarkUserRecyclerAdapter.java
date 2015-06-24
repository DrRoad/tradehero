package com.tradehero.th.fragments.leaderboard;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.R;
import com.tradehero.th.api.leaderboard.key.LeaderboardKey;

public class CompetitionLeaderboardMarkUserRecyclerAdapter extends LeaderboardMarkUserRecyclerAdapter<LeaderboardItemDisplayDTO>
{
    public CompetitionLeaderboardMarkUserRecyclerAdapter(Context context, @LayoutRes int itemLayoutRes, @LayoutRes int ownRankingRes,
            @NonNull LeaderboardKey leaderboardKey)
    {
        super(LeaderboardItemDisplayDTO.class, context, itemLayoutRes, ownRankingRes, leaderboardKey);
    }

    @NonNull @Override
    protected LbmuHeaderViewHolder<LeaderboardItemDisplayDTO> createOwnRankingLbmuViewHolder(ViewGroup parent)
    {
        return new CompetitionLbmuHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(ownRankingRes, parent, false), picasso,
                analytics);
    }

    @NonNull @Override
    protected LbmuItemViewHolder<LeaderboardItemDisplayDTO> createLbmuItemViewholder(ViewGroup parent)
    {
        return new CompetitionLbmuItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes, parent, false), picasso, analytics);
    }

    public static class CompetitionLbmuItemViewHolder extends LbmuItemViewHolder<LeaderboardItemDisplayDTO>
    {
        @InjectView(R.id.lbmu_item_prize) ImageView prizeIcon;

        public CompetitionLbmuItemViewHolder(View itemView, Picasso picasso, Analytics analytics)
        {
            super(itemView, picasso, analytics);
        }

        @Override public void display(LeaderboardItemDisplayDTO dto)
        {
            super.display(dto);
            if (dto instanceof CompetitionLeaderboardItemDisplayDTO)
            {
                prizeIcon.setVisibility(((CompetitionLeaderboardItemDisplayDTO) dto).prizeIconVisibility);
            }
            else
            {
                prizeIcon.setVisibility(View.GONE);
            }
        }
    }

    public static class CompetitionLbmuHeaderViewHolder extends LbmuHeaderViewHolder<LeaderboardItemDisplayDTO>
    {
        @InjectView(R.id.competition_own_ranking_info_text) TextView infoText;
        @InjectView(R.id.competition_own_ranking_info_container) ViewGroup container;
        @InjectView(R.id.lbmu_item_prize) ImageView prizeIcon;
        private ClickableSpan clickableSpan;

        public CompetitionLbmuHeaderViewHolder(View itemView, Picasso picasso, Analytics analytics)
        {
            super(itemView, picasso, analytics);
        }

        @Override public void display(LeaderboardItemDisplayDTO dto)
        {
            super.display(dto);
            if (dto instanceof CompetitionLeaderboardOwnRankingDisplayDTO)
            {
                CompetitionLeaderboardOwnRankingDisplayDTO displayDTO = (CompetitionLeaderboardOwnRankingDisplayDTO) dto;
                prizeIcon.setVisibility((displayDTO).prizeIconVisibility);
                container.setVisibility((displayDTO).infoButtonContainerVisibility);
                infoText.setText((displayDTO).infoText);
                if (displayDTO.rule != null && displayDTO.textColorSpan != null)
                {
                    this.lbmuRoi.setText(createSpan(displayDTO.rule, displayDTO.textColorSpan));
                    this.lbmuRoi.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            else
            {
                prizeIcon.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                infoText.setText("");
            }
        }

        @SuppressWarnings("UnusedDeclaration")
        @OnClick(R.id.competition_own_ranking_info)
        public void onInfoButtonClicked()
        {
            if (infoText.getVisibility() == View.GONE)
            {
                infoText.setVisibility(View.VISIBLE);
            }
            else
            {
                infoText.setVisibility(View.GONE);
            }
        }

        private CharSequence createSpan(String rule, ForegroundColorSpan textColorSpan)
        {
            if (clickableSpan == null)
            {
                clickableSpan = createClickableSpan();
            }

            Spannable span = new SpannableString(rule);
            span.setSpan(clickableSpan, 0, rule.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(textColorSpan, 0, rule.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return span;
        }

        private ClickableSpan createClickableSpan()
        {
            return new ClickableSpan()
            {
                @Override public void onClick(View widget)
                {
                    if (currentDto != null)
                    {
                        userActionSubject.onNext(new LeaderboardItemUserAction(currentDto, LeaderboardItemUserAction.UserActionType.RULES));
                    }
                }
            };
        }
    }
}

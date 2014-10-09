package com.tradehero.th.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.api.achievement.QuestBonusDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuestIndicatorView extends RelativeLayout implements DTOView<QuestBonusDTO>
{
    @InjectView(R.id.quest_logo_indicator) ImageView logo;
    @InjectView(R.id.quest_top_indicator) TextView topIndicator;
    @InjectView(R.id.quest_bottom_indicator) TextView botIndicator;
    private int mCurrentColor = Color.BLACK;
    private int mCurrentLevel;
    @Nullable private QuestBonusDTO mQuestBonusDTO;
    private Animator scaleUpAnimator;

    //<editor-fold desc="Constructors">
    public QuestIndicatorView(Context context)
    {
        super(context);
        init();
    }

    @SuppressWarnings("UnusedDeclaration")
    public QuestIndicatorView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("UnusedDeclaration")
    public QuestIndicatorView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }
    //</editor-fold>

    private void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.quest_indicator, this, true);
        ButterKnife.inject(this);
    }

    private void defaultStyle()
    {
        logo.clearAnimation();
        unBoldText();
    }

    private void boldText()
    {
        topIndicator.setTypeface(topIndicator.getTypeface(), Typeface.BOLD);
        botIndicator.setTypeface(botIndicator.getTypeface(), Typeface.BOLD);
    }

    private void unBoldText()
    {
        topIndicator.setTypeface(topIndicator.getTypeface(), Typeface.NORMAL);
        botIndicator.setTypeface(botIndicator.getTypeface(), Typeface.NORMAL);
    }

    private void on()
    {
        logo.setImageResource(R.drawable.ic_achievement_star_on);
        defaultStyle();
    }

    private void off()
    {
        logo.setImageResource(R.drawable.ic_achievement_star_off);
        updateTextColor(getResources().getColor(R.color.text_gray_normal));
        defaultStyle();
    }

    private void animateOn()
    {
        updateTextColor(mCurrentColor);

        logo.setImageResource(R.drawable.ic_achivement_star_animate);
        AnimationDrawable animationDrawable = (AnimationDrawable) logo.getDrawable();
        animationDrawable.start();

        if (scaleUpAnimator == null)
        {
            scaleUpAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.quest_indicator_scale);
            scaleUpAnimator.setTarget(this);
            scaleUpAnimator.start();
        }

        boldText();
    }

    private void updateTextColor(int col)
    {
        topIndicator.setTextColor(col);
        botIndicator.setTextColor(col);
    }

    private void setText(String top, String bot)
    {
        topIndicator.setText(top);
        botIndicator.setText(bot);
    }

    public void display(@NotNull QuestBonusDTO dto, int currentLevel)
    {
        this.mCurrentLevel = currentLevel;
        display(dto);
    }

    @Override public void display(@NotNull QuestBonusDTO dto)
    {
        this.mQuestBonusDTO = dto;
        display();
    }

    private void display()
    {
        QuestBonusDTO dto = mQuestBonusDTO;
        if (dto != null)
        {
            setText(dto.levelStr, dto.bonusStr);

            if (dto.level < mCurrentLevel)
            {
                on();
            }
            else if (dto.level == mCurrentLevel)
            {
                animateOn();
            }
            else
            {
                off();
            }
        }
    }

    public void shouldShowColor(int mCurrentColor)
    {
        this.mCurrentColor = mCurrentColor;
        if (this.mQuestBonusDTO != null && mCurrentLevel > 0)
        {
            display();
        }
    }
}

package com.tradehero.th.fragments.achievement;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import com.tradehero.th.R;
import com.tradehero.th.api.achievement.UserAchievementDTO;
import com.tradehero.th.api.achievement.UserAchievementId;
import com.tradehero.th.api.level.LevelDefDTO;
import com.tradehero.th.fragments.base.BaseDialogFragment;
import com.tradehero.th.utils.GraphicUtil;
import com.tradehero.th.utils.achievement.UserAchievementDTOUtil;
import com.tradehero.th.widget.UserLevelProgressBar;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AchievementDialogFragment extends BaseDialogFragment
{
    private static final String BUNDLE_KEY_USER_ACHIEVEMENT_ID = AchievementDialogFragment.class.getName() + ".UserAchievementDTOKey";

    @InjectView(R.id.achievement_content_container) ViewGroup contentContainer;

    @InjectView(R.id.achievement_header) TextView header;
    @InjectView(R.id.achievement_title) TextView title;
    @InjectView(R.id.achievement_description) TextView description;
    @InjectView(R.id.achievement_more_description) TextView moreDescription;

    @InjectView(R.id.achievement_badge) ImageView badge;
    @InjectView(R.id.achievement_pulse) ImageView pulseEffect;
    @InjectView(R.id.achievement_starburst) ImageView starBurst;

    @InjectView(R.id.user_level_progress_bar) UserLevelProgressBar userLevelProgressBar;

    @InjectView(R.id.btn_achievement_dismiss) Button btnDismiss;
    @InjectView(R.id.btn_achievement_share) Button btnShare;

    @InjectView(R.id.user_level_progress_level_up) TextView levelUp;
    @InjectView(R.id.user_level_progress_xp_earned) TextView xpEarned;
    @InjectView(R.id.user_level_progress_virtual_dollar_earned) TextView dollarEarned;

    @InjectView(R.id.achievement_xp_dollar_earned_container) ViewGroup xpDollarEarnedContainer;

    @Inject UserAchievementDTOUtil userAchievementDTOUtil;
    @Inject Picasso picasso;
    @Inject GraphicUtil graphicUtil;

    private UserAchievementId userAchievementId;
    private UserAchievementDTO userAchievementDTO;
    private float mXpEarnedOriginalY = -1;
    private long mMsLevelUpDelay;

    protected AchievementDialogFragment()
    {
        super();
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog d = super.onCreateDialog(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TH_Achievement_Dialog);
        d.getWindow().setWindowAnimations(R.style.TH_Achievement_Dialog_Animation);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return d;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.achievement_dialog_fragment, container, false);
        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init()
    {
        userAchievementId = new UserAchievementId(getArguments().getBundle(BUNDLE_KEY_USER_ACHIEVEMENT_ID));
        userAchievementDTO = userAchievementDTOUtil.pop(userAchievementId);
        mMsLevelUpDelay = getResources().getInteger(R.integer.achievement_level_up_end_start_offset) - getResources().getInteger(
                R.integer.achievement_level_up_start_duration);
        initView();
    }

    private void initView()
    {
        updateColor();
        displayStarburst();
        displayHeader();
        displayBadge();
        displayTitle();
        displayText();
        displaySubText();
        displayXPDollarsEarned();
        initProgressBar();
    }

    private void displayStarburst()
    {
        Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.achievement_starburst);
        starBurst.startAnimation(a);
    }

    private void updateColor()
    {
        int color = graphicUtil.parseColor(userAchievementDTO.achievementDef.hexColor, Color.BLACK);
        Drawable d = pulseEffect.getDrawable();

        d.setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        title.setTextColor(color);
    }

    private void displayHeader()
    {
        header.setText(userAchievementDTO.achievementDef.header);
    }

    private void displayBadge()
    {
        Animation pulse = AnimationUtils.loadAnimation(getActivity(), R.anim.achievement_pulse);
        pulseEffect.startAnimation(pulse);
        picasso.load(userAchievementDTO.achievementDef.visual)
                .placeholder(R.drawable.achievement_unlocked_placeholder)
                .fit()
                .centerInside()
                .into(badge);
    }

    private void displayTitle()
    {
        title.setText(userAchievementDTO.achievementDef.thName);
    }

    private void displayText()
    {
        description.setText(userAchievementDTO.achievementDef.text);
    }

    private void displaySubText()
    {
        if (userAchievementDTO.achievementDef.subText != null)
        {
            moreDescription.setText(userAchievementDTO.achievementDef.subText);
        }
        else
        {
            moreDescription.setVisibility(View.GONE);
        }
    }

    private void displayXPDollarsEarned()
    {
        xpEarned.setText(getString(R.string.achievement_xp_earned_format, userAchievementDTO.xpEarned));
        dollarEarned.setText(getString(R.string.achievement_virtual_dollars_earned_format, userAchievementDTO.achievementDef.virtualDollars));
    }

    private void initProgressBar()
    {
        userLevelProgressBar.startsWith(userAchievementDTO.getBaseExp());
        userLevelProgressBar.setStartDelayOnLevelUp(mMsLevelUpDelay);
        userLevelProgressBar.setUserLevelProgressBarListener(createUserLevelProgressBarListener());
    }

    @Override public void onResume()
    {
        super.onResume();
        contentContainer.setOnTouchListener(
                new AchievementDialogSwipeDismissTouchListener(contentContainer, null,
                        new AchievementDialogSwipeDismissTouchListener.DismissCallbacks()
                        {
                            @Override public boolean canDismiss(Object token)
                            {
                                return true;
                            }

                            @Override public void onDismiss(View view, Object token)
                            {
                                removeDialogAnimation();
                                getDialog().dismiss();
                            }
                        }));
    }

    private void removeDialogAnimation()
    {
        getDialog().getWindow().setWindowAnimations(R.style.TH_Achievement_Dialog_NoAnimation);
    }

    @Override public void onPause()
    {
        contentContainer.setOnTouchListener(null);
        super.onPause();
    }

    @Override public void onDestroyView()
    {
        userLevelProgressBar.setUserLevelProgressBarListener(null);
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @OnClick(R.id.btn_achievement_share)
    public void onShareClicked()
    {
        userLevelProgressBar.increment(400);
    }

    @OnClick(R.id.btn_achievement_dismiss)
    public void onDismissBtnClicked()
    {
        getDialog().dismiss();
    }

    @OnClick(R.id.achievement_dummy_container)
    public void onOutsideContentClicked()
    {
        getDialog().dismiss();
    }

    private void playLevelUpAnimation()
    {
        if (levelUp != null)
        {
            if(mXpEarnedOriginalY < 0)
            {
                mXpEarnedOriginalY = xpDollarEarnedContainer.getY();
            }

            levelUp.setVisibility(View.VISIBLE);

            Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.achievement_level_up);
            a.setAnimationListener(createLevelUpAnimationListener());

            ValueAnimator moveUp = ObjectAnimator.ofFloat(xpDollarEarnedContainer, "y", mXpEarnedOriginalY - xpEarned.getHeight());
            moveUp.setDuration(getResources().getInteger(R.integer.achievement_level_up_start_duration));
            moveUp.setInterpolator(a.getInterpolator());

            ValueAnimator moveDown = ObjectAnimator.ofFloat(xpDollarEarnedContainer, "y", mXpEarnedOriginalY);
            moveDown.setDuration(getResources().getInteger(R.integer.achievement_level_up_end_duration));
            moveDown.setInterpolator(a.getInterpolator());

            if(mMsLevelUpDelay >= 0)
            {
                moveDown.setStartDelay(mMsLevelUpDelay);
            }

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(moveUp, moveDown);
            animatorSet.start();

            levelUp.startAnimation(a);
        }
    }

    private LevelUpAnimationListener createLevelUpAnimationListener()
    {
        return new LevelUpAnimationListener();
    }

    private UserLevelProgressBar.UserLevelProgressBarListener createUserLevelProgressBarListener()
    {
        return new AchievementUserLevelProgressBarListener();
    }

    private class LevelUpAnimationListener implements Animation.AnimationListener
    {

        @Override public void onAnimationStart(Animation animation)
        {

        }

        @Override public void onAnimationEnd(Animation animation)
        {
            if (levelUp != null)
            {
                levelUp.setVisibility(View.GONE);
            }
        }

        @Override public void onAnimationRepeat(Animation animation)
        {

        }
    }

    protected class AchievementUserLevelProgressBarListener implements UserLevelProgressBar.UserLevelProgressBarListener
    {
        @Override public void onLevelUp(LevelDefDTO fromLevel, LevelDefDTO toLevel)
        {
            playLevelUpAnimation();
        }
    }

    public static class Creator
    {
        @Inject UserAchievementDTOUtil userAchievementDTOUtil;

        @Inject public Creator()
        {
            super();
        }

        @Nullable public AchievementDialogFragment newInstance(@NotNull UserAchievementId userAchievementId)
        {
            if (!userAchievementDTOUtil.shouldShow(userAchievementId))
            {
                return null;
            }

            Bundle args = new Bundle();
            args.putBundle(BUNDLE_KEY_USER_ACHIEVEMENT_ID, userAchievementId.getArgs());
            AchievementDialogFragment f = new AchievementDialogFragment();
            f.setArguments(args);
            return f;
        }
    }
}

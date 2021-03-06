package com.tradehero.th.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.utils.DaggerUtils;
import com.tradehero.th.utils.ShortcutUtil;
import com.tradehero.metrics.Analytics;
import com.tradehero.th.utils.metrics.AnalyticsConstants;
import com.tradehero.th.utils.metrics.events.MethodEvent;
import com.tradehero.th.utils.metrics.events.SimpleEvent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class GuideActivity extends Activity
        implements
        ViewPager.OnPageChangeListener,
        View.OnClickListener
{
    private static final int CLOSE_IMAGE_ID = 0x88888;
    @Inject Analytics analytics;
    @Inject CurrentUserId currentUserId;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DaggerUtils.inject(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide);
        ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager);
        List<Integer> list = new ArrayList<>();
        list.add(R.drawable.guide1);
        list.add(R.drawable.guide2);
        list.add(R.drawable.guide3);

        viewpager.setAdapter(new ListViewPagerAdapter(list));
        viewpager.setOnPageChangeListener(this);

        ShortcutUtil.recreateShortcut(this);

        analytics.openSession();
        analytics.tagScreen(AnalyticsConstants.Splash);
    }

    @Override protected void onPause()
    {
        analytics.closeSession();
        super.onPause();
    }

    @Override public void onClick(View v)
    {
        analytics.addEvent(new SimpleEvent(AnalyticsConstants.SplashScreenCancel));
        if (currentUserId.get() > 0)
        {
            ActivityHelper.launchDashboard(this);
        }
        else
        {
            ActivityHelper.launchAuthentication(this);
        }
        finish();
    }

    class ListViewPagerAdapter extends PagerAdapter
    {
        private List<Integer> drawableIdList = null;

        public ListViewPagerAdapter(List<Integer> drawableIdList)
        {
            this.drawableIdList = drawableIdList;
        }

        @Override public int getCount()
        {
            return drawableIdList.size();
        }

        private boolean isLast(int position)
        {
            return position == getCount() - 1;
        }

        private boolean isClickable(int position)
        {
            return isLast(position);
        }

        @Override public Object instantiateItem(ViewGroup container, int position)
        {
            View view;
            ImageView imageView = (ImageView) LayoutInflater.from(GuideActivity.this).inflate(R.layout.guide_layout, null);
            if (position == getCount() - 1) {
                RelativeLayout rl = new RelativeLayout(GuideActivity.this);
                rl.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                rl.addView(imageView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                ImageView closeIv = new ImageView(GuideActivity.this);
                closeIv.setId(CLOSE_IMAGE_ID);
                closeIv.setImageResource(R.drawable.cross_red);
                closeIv.setOnClickListener(GuideActivity.this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_TOP);
                lp.rightMargin = (int)GuideActivity.this.getResources().getDimension(R.dimen.guide_close_margin);
                lp.topMargin = lp.rightMargin;
                rl.addView(closeIv, lp);

                view = rl;
            }
            else
            {
                view = imageView;
            }
            try
            {
                imageView.setBackgroundResource(drawableIdList.get(position));
            }
            catch (OutOfMemoryError e)
            {
                Timber.e(e, "Expanding position %d", position);
            }
            if (isClickable(position))
            {
                imageView.setOnClickListener(GuideActivity.this);
            }
            else
            {
                imageView.setOnClickListener(null);
            }
            container.addView(view);
            analytics.addEvent(new MethodEvent(AnalyticsConstants.SplashScreen, AnalyticsConstants.Screen + String.valueOf(position)));
            return view;
        }

        @Override public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);
            if (object instanceof ImageView)
            {
                ((ImageView) object).setBackgroundResource(0);
            }
        }

        @Override public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }
    }

    @Override public void onPageScrolled(int i, float v, int i2)
    {
    }

    @Override public void onPageSelected(int i)
    {
    }

    @Override public void onPageScrollStateChanged(int i)
    {
    }
}

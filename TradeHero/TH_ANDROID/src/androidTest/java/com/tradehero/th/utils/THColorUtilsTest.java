package com.tradehero.th.utils;

import android.content.Context;
import android.graphics.Color;
import com.tradehero.RobolectricMavenTestRunner;
import com.tradehero.th.R;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricMavenTestRunner.class)
public class THColorUtilsTest
{
    @Inject Context context;

    @Test public void maxRedValue_shouldBeLargeEnough()
    {
        assertThat(THColorUtils.MAX_RED_VALUE).isGreaterThan(200);
    }

    @Test public void maxGreenValue_shouldBeLargeEnough()
    {
        assertTrue(THColorUtils.MAX_GREEN_VALUE >= 200);
    }

    @Test public void getColorForPercentage_onMinus1_shouldReturnDown()
    {
        int colorMinus1 = THColorUtils.getProperColorForNumber(-1);
        int colorDown = context.getResources().getColor(R.color.number_down);
        assertEquals(colorMinus1, colorDown);
    }

    @Test public void getColorForPercentage_onMinus05_shouldReturnDown()
    {
        int colorMinus1 = THColorUtils.getProperColorForNumber(-0.5f);
        int colorDown = context.getResources().getColor(R.color.number_down);
        assertEquals(colorMinus1, colorDown);
    }

    @Test public void getColorForPercentage_on0_shouldReturnBlack()
    {
        int color0 = THColorUtils.getProperColorForNumber(0);
        int colorBlack = Color.rgb(0, 0, 0);
        assertEquals(color0, colorBlack);
    }

    @Test public void getColorForPercentage_on05_shouldReturnUp()
    {
        int color1 = THColorUtils.getProperColorForNumber(0.5f);
        int colorUp = context.getResources().getColor(R.color.number_up);
        assertEquals(color1, colorUp);
    }

    @Test public void getColorForPercentage_on1_shouldReturnUp()
    {
        int color1 = THColorUtils.getProperColorForNumber(1);
        int colorUp = context.getResources().getColor(R.color.number_up);
        assertEquals(color1, colorUp);
    }
}

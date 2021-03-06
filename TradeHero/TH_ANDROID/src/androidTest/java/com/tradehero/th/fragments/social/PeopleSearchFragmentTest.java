package com.tradehero.th.fragments.social;

import android.content.Context;
import com.tradehero.THRobolectricTestRunner;
import com.tradehero.th.R;
import com.tradehero.th.activities.DashboardActivity;
import com.tradehero.th.fragments.DashboardNavigator;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(THRobolectricTestRunner.class)
public class PeopleSearchFragmentTest
{
    @Inject Context context;
    private PublicPeopleSearchFragment peopleSearchFragment;
    @Inject DashboardNavigator dashboardNavigator;

    @Before public void setUp()
    {
        DashboardActivity activity = Robolectric.setupActivity(DashboardActivity.class);
        activity.inject(this);
    }

    @After public void tearDown()
    {
        peopleSearchFragment = null;
    }

    //<editor-fold desc="Hint">
    @Ignore("This test will fail because the setup mocks the action bar, which then returns a null view")
    @Test public void testHintIsCorrect()
    {
        peopleSearchFragment = dashboardNavigator.pushFragment(PublicPeopleSearchFragment.class);

        assertThat(peopleSearchFragment.getSearchTextField().getHint())
                .isEqualTo(context.getString(R.string.search_social_friend_hint));
    }
    //</editor-fold>
}

package com.tradehero.th.api.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradehero.THRobolectricTestRunner;
import com.tradehero.common.annotation.ForApp;
import com.tradehero.th.BuildConfig;
import com.tradehero.th.api.BaseApiTestClass;
import com.tradehero.th.base.TestTHApp;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(THRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AlertCompactDTOTest extends BaseApiTestClass
{
    @Inject @ForApp ObjectMapper normalMapper;
    private InputStream alertCompactBody1Stream;

    @Before public void setUp()
    {
        TestTHApp.staticInject(this);
        alertCompactBody1Stream = getClass().getResourceAsStream(getPackagePath() + "/AlertCompactDTOBody1.json");
    }

    @Test public void testCanDeserialise() throws IOException
    {
        AlertCompactDTO alertCompact1 = normalMapper.readValue(alertCompactBody1Stream, AlertCompactDTO.class);

        assertThat(alertCompact1.id).isEqualTo(1511);
        assertThat(alertCompact1.active).isTrue();
    }
}

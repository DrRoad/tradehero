package com.tradehero.th.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradehero.THRobolectricTestRunner;
import com.tradehero.common.annotation.ForApp;
import com.tradehero.th.BuildConfig;
import com.tradehero.th.api.BaseApiTestClass;
import com.tradehero.th.api.security.compact.EquityCompactDTO;
import com.tradehero.th.api.security.compact.WarrantDTO;

import com.tradehero.th.base.TestTHApp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(THRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SecurityCompactDTODeserialiserTest extends BaseApiTestClass
{
    @Inject @ForApp ObjectMapper normalMapper;

    private InputStream securityCompactDTOBody1Stream;
    private InputStream warrantDTOBody1Stream;

    @Before
    public void setUp() throws IOException
    {
        TestTHApp.staticInject(this);
        securityCompactDTOBody1Stream = getClass().getResourceAsStream(getPackagePath() + "/SecurityCompactDTOGoogleBody1.json");
        warrantDTOBody1Stream = getClass().getResourceAsStream(getPackagePath() + "/WarrantDTOBody1.json");
    }

    @Test
    public void testNormalDeserialiseBody1() throws IOException
    {
        SecurityCompactDTO converted = normalMapper.readValue(securityCompactDTOBody1Stream, SecurityCompactDTO.class);
        assertEquals(EquityCompactDTO.class, converted.getClass());
        assertEquals("GOOG", converted.symbol);
    }

    @Test
    public void testNormalDeserialiseWarrantBody1() throws IOException
    {
        SecurityCompactDTO converted = normalMapper.readValue(warrantDTOBody1Stream, SecurityCompactDTO.class);
        assertEquals(WarrantDTO.class, converted.getClass());
        assertEquals(4.5d, ((WarrantDTO) converted).strikePrice, 0.0001d);
    }
}

package com.tradehero.th.api.translation;

import com.tradehero.THRobolectricTestRunner;
import com.tradehero.TestConstants;
import com.tradehero.th.api.translation.bing.BingTranslationToken;
import com.tradehero.th.api.translation.bing.BingUserTranslationSettingDTO;
import com.tradehero.th.base.TestTHApp;
import java.io.IOException;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(THRobolectricTestRunner.class)
public class UserTranslationSettingDTOFactoryTest
{
    @Inject UserTranslationSettingDTOFactory userTranslationSettingDTOFactory;

    @Before public void setUp()
    {
        TestTHApp.staticInject(this);
    }

    //<editor-fold desc="Passing Nulls">
    @Test(expected = IllegalArgumentException.class)
    public void ifIntelliJCreateFromNullThrowsIllegal() throws IOException
    {
        assumeTrue(TestConstants.IS_INTELLIJ);
        //noinspection ConstantConditions
        userTranslationSettingDTOFactory.create(null);
    }

    @Test(expected = NullPointerException.class)
    public void ifNotIntelliJCreateFromNullThrowsNPE() throws IOException
    {
        assumeTrue(!TestConstants.IS_INTELLIJ);
        //noinspection ConstantConditions
        userTranslationSettingDTOFactory.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifIntelliJSerialiseFromNullThrowsIllegal() throws IOException
    {
        assumeTrue(TestConstants.IS_INTELLIJ);
        //noinspection ConstantConditions
        userTranslationSettingDTOFactory.serialise(null);
    }

    @Test public void ifNotIntelliJSerialiseFromNullReturnsNull() throws IOException
    {
        assumeTrue(!TestConstants.IS_INTELLIJ);
        //noinspection ConstantConditions
        assertThat(userTranslationSettingDTOFactory.serialise(null))
            .isEqualTo("null");
    }
    //</editor-fold>

    //<editor-fold desc="Proper create and serialise">
    @Test public void canCreateSettingFromSerialised() throws IOException
    {
        String serialised = "{\"translatorType\":\"MicrosoftTranslator\",\"languageCode\":\"zh\",\"autoTranslate\":false}";
        UserTranslationSettingDTO settingDTO = userTranslationSettingDTOFactory.create(serialised);

        assertThat(settingDTO).isExactlyInstanceOf(BingUserTranslationSettingDTO.class);
        assertThat(settingDTO.languageCode).isEqualTo("zh");
        assertThat(settingDTO.autoTranslate).isFalse();
    }

    @Test public void canCreateStringFromObject() throws IOException
    {
        UserTranslationSettingDTO settingDTO = new BingUserTranslationSettingDTO("it", true);
        String expected = "{\"translatorType\":\"MicrosoftTranslator\",\"languageCode\":\"it\",\"autoTranslate\":true}";

        assertThat(userTranslationSettingDTOFactory.serialise(settingDTO))
                .isEqualTo(expected);
    }
    //</editor-fold>

    //<editor-fold desc="Create Default">
    @Test(expected = IllegalArgumentException.class)
    public void ifIntelliJCreateDefaultWithNullThrowsIllegal()
    {
        assumeTrue(TestConstants.IS_INTELLIJ);
        //noinspection ConstantConditions
        userTranslationSettingDTOFactory.createDefaultPerType(null);
    }

    @Test(expected = NullPointerException.class)
    public void ifNotIntelliJCreateDefaultWithNullThrowsNPE()
    {
        assumeTrue(!TestConstants.IS_INTELLIJ);
        //noinspection ConstantConditions
        userTranslationSettingDTOFactory.createDefaultPerType(null);
    }

    @Test public void createDefaultPerTypeUnknownReturnsNull()
    {
        assertThat(userTranslationSettingDTOFactory.createDefaultPerType(new TranslationToken()))
                .isNull();
    }

    @Test public void createDefaultPerTypeBing()
    {
        assertThat(userTranslationSettingDTOFactory.createDefaultPerType(new BingTranslationToken()))
                .isExactlyInstanceOf(BingUserTranslationSettingDTO.class);
    }
    //</editor-fold>
}

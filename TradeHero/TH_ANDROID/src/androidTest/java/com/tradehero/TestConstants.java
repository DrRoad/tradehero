package com.tradehero;

public class TestConstants
{
    public static final String TEST_COMMAND = System.getProperties().getProperty("sun.java.command");
    public static final boolean IS_INTELLIJ = TEST_COMMAND != null && TEST_COMMAND.toLowerCase().contains("intellij");

    public static final String BASE_APP_FOLDER = IS_INTELLIJ ? "./TradeHero/TH_ANDROID/" : "./";
    public static final String JSON_MIME_UTF8 = "application/json; charset=utf-8";

    public static final String[] LIBRARIES_POSSIBLE_GENERATED_FOLDER = IS_INTELLIJ ?
            new String[] { "gen-external-apklibs" } :
            new String[] { "target/unpacked-libs", "target/unpack/apklibs" } ;
    public static final String MANIFEST_PATH = BASE_APP_FOLDER + "AndroidManifest.xml";
    public static final String RES_PATH = BASE_APP_FOLDER + "res";
    public static final String ASSETS_PATH = BASE_APP_FOLDER + "assets";
}

package com.tradehero;

import com.tradehero.th.base.TestTHApp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * This class do a nice trick to search for generated libraries, for each library, it will pick up corresponding resource folder
 * and add it into resource pool, they are to be used for testing with reobolectric
 */
@Deprecated // probably
public class MavenAndroidManifest extends AndroidManifest
{
    public MavenAndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory)
    {
        super(androidManifestFile, resDirectory, assetsDirectory);
    }

    public MavenAndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory, String packageName)
    {
        super(androidManifestFile, resDirectory, assetsDirectory, packageName);
    }

    @Override public String getApplicationName()
    {
        return TestTHApp.class.getName();
    }

    /**
     * Note that robolectric does not support sdk ver 19 at this time
     * @return minSdkTarget support
     */
    @Override public int getTargetSdkVersion()
    {
        return 16;
    }

    @Override protected List<FsFile> findLibraries()
    {
        // Try unpack folder from Maven/IntelliJ, when one folder is found with unpacked libraries, use it and exit
        for (String generatedPath: TestConstants.LIBRARIES_POSSIBLE_GENERATED_FOLDER)
        {
            FsFile unpack = getBaseDir().join(generatedPath);
            if (unpack.exists())
            {
                FsFile[] libs = unpack.listFiles(new FsFile.Filter()
                {
                    @Override public boolean accept(FsFile fsFile)
                    {
                        return (fsFile != null) && (fsFile.isDirectory());
                    }
                });

                if (libs != null)
                {
                    return asList(libs);
                }
            }
        }
        return emptyList();
    }

    List<FsFile> getGitSubmoduleLibraries()
    {
        List<FsFile> libraries = new ArrayList<>();
        List<String> modules = getModulePath();
        if (modules != null)
        {
            for (String module: modules)
            {
                FsFile currentFs = Fs.newFile(new File(module));
                if (!TestConstants.BASE_APP_FOLDER.contains(module) && currentFs.join("AndroidManifest.xml").exists())
                {
                    libraries.add(currentFs);
                }
            }
        }
        return libraries;
    }

    private List<String> getModulePath()
    {
        File file = new File("pom.xml");
        if (file.exists())
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
                byte[] data = new byte[(int)file.length()];
                fis.read(data);
                String pomFileContent = new String(data, "UTF-8");

                Pattern pattern = Pattern.compile("<module>([^<]*)</module>");
                Matcher matcher = pattern.matcher(pomFileContent);

                List<String> modules = new ArrayList<>();
                while (matcher.find())
                {
                    modules.add(matcher.group(1));
                }
                return unmodifiableList(modules);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (fis != null)
                {
                    try
                    {
                        fis.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    //@Override protected AndroidManifest createLibraryAndroidManifest(FsFile libraryBaseDir)
    //{
    //    return new MavenAndroidManifest(libraryBaseDir);
    //}
}

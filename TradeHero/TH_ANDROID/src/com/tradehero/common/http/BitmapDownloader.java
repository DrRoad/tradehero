package com.tradehero.common.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Created with IntelliJ IDEA. User: xavier Date: 9/11/13 Time: 11:35 AM To change this template use File | Settings | File Templates. */
/*
public class BitmapDownloader implements Runnable
{
    private static final int IO_BUFFER_SIZE = 4 * 1024;
    private static final String TAG = BitmapDownloader.class.getSimpleName();

    private String mUrl;
    private Bitmap mDownloaded;

    public BitmapDownloader()
    {
        super();
    }

    public BitmapDownloader(String url)
    {
        super();
        mUrl = url;
    }

    public void run()
    {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;

        try
        {
            in = new BufferedInputStream(new URL(mUrl).openStream(), IO_BUFFER_SIZE);

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);

            copy(in, out);
            out.flush();

            final byte[] data = dataStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 1;

            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not load Bitmap from: " + mUrl);
        }
        finally
        {
            closeStream(in);
            closeStream(out);
        }
    }
}
*/
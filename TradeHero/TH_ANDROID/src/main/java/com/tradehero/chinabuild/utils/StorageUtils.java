package com.tradehero.chinabuild.utils;

import android.content.Context;
import android.os.Environment;

/**
 * Created by palmer on 15/3/27.
 */
public class StorageUtils {

    public static String getImageStoragePath(Context context){
        String dir = "/th";
        String path = getDefaultStoragePath(context) + dir;
        return path;
    }

    public static String getDefaultStoragePath(Context context){
        if(Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) && Environment.getExternalStorageDirectory().canWrite()){
            return Environment.getExternalStorageDirectory().getPath();
        }else{
            return context.getFilesDir().getPath();
        }
    }
}

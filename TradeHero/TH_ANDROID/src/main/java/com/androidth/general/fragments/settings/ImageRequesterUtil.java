package com.androidth.general.fragments.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.androidth.general.common.activities.ActivityResultRequester;
import com.androidth.general.common.utils.THToast;
import com.androidth.general.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class ImageRequesterUtil implements ActivityResultRequester
{
    public static final int REQUEST_GALLERY = 1309;
    public static final int REQUEST_CAMERA = 1310;
    public final static int REQUEST_PHOTO_ZOOM = 1311;

    @Nullable private final Integer cropAspectX;
    @Nullable private final Integer cropAspectY;
    @Nullable private final Integer cropSizeX;
    @Nullable private final Integer cropSizeY;
    private final BehaviorSubject<Bitmap> bitmapSubject;
    private File mCurrentPhotoFile;
    private File croppedPhotoFile;
    private int currentRequest = -1;

    public ImageRequesterUtil(
            @Nullable Integer cropAspectX,
            @Nullable Integer cropAspectY,
            @Nullable Integer cropSizeX,
            @Nullable Integer cropSizeY)
    {
        this.cropAspectX = cropAspectX;
        this.cropAspectY = cropAspectY;
        this.cropSizeX = cropSizeX;
        this.cropSizeY = cropSizeY;
        bitmapSubject = BehaviorSubject.create();
    }

    @NonNull public Observable<Bitmap> getBitmapObservable()
    {
        return bitmapSubject.asObservable();
    }

    public File getCroppedPhotoFile()
    {
        return croppedPhotoFile;
    }

    public void onImageFromCameraRequested(@NonNull Activity activity, @NonNull int requestCode)
    {
        PackageManager pm = activity.getPackageManager();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        List<ResolveInfo> handlerActivities = pm.queryIntentActivities(cameraIntent, 0);
        if (handlerActivities.size() > 0)
        {
            mCurrentPhotoFile = createImageFile(activity);
            if (mCurrentPhotoFile == null)
            {
                THToast.show(R.string.error_save_image_in_external_storage);
                return;
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(mCurrentPhotoFile));
            activity.startActivityForResult(cameraIntent, requestCode);
        }
        else
        {
            THToast.show(R.string.device_no_camera);
        }
    }

    public void onImageFromLibraryRequested(@NonNull Activity activity, @NonNull int requestCode)
    {
        Intent libraryIntent = new Intent(Intent.ACTION_PICK);
        libraryIntent.setType("image/jpeg");
        try
        {
            activity.startActivityForResult(libraryIntent, requestCode);
        } catch (ActivityNotFoundException e)
        {
            Timber.e(e, "Could not request gallery");
            THToast.show(R.string.error_launch_photo_library);
        }
    }

    @Override public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK
                && data != null)
        {
            currentRequest = REQUEST_GALLERY;
//            startPhotoZoom(activity, data.getData());

            Bitmap tempBitmap = null;

            Uri selectedImageUri = data.getData();
            if(selectedImageUri!=null) {
                FileOutputStream out = null;

                try {
                    InputStream imageStream = activity.getContentResolver().openInputStream(selectedImageUri);

                    tempBitmap = BitmapFactory.decodeStream(imageStream);

                    saveBitmapToFile(activity, tempBitmap, true);

                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK)
        {
            currentRequest = REQUEST_CAMERA;
            startPhotoZoom(activity, Uri.fromFile(mCurrentPhotoFile));
        }
        else if (requestCode == REQUEST_PHOTO_ZOOM && data != null)
        {

            Uri selectedImageUri = data.getData();
            if(selectedImageUri!=null){
                try{
                    InputStream imageStream = activity.getContentResolver().openInputStream(selectedImageUri);

                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    if (bitmap == null || saveBitmapToFile(activity, bitmap, false))
                    {
                        return;
                    }

                    if (currentRequest == REQUEST_CAMERA)
                    {
                        currentRequest = -1;
                        bitmapSubject.onNext(bitmap);
                    }
                    else if (currentRequest == REQUEST_GALLERY)
                    {
                        currentRequest = -1;
                        bitmapSubject.onNext(bitmap);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void startPhotoZoom(@NonNull Activity activity, Uri data)
    {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", "true");
        if (cropAspectX != null)
        {
            intent.putExtra("aspectX", cropAspectX);
        }
        if (cropAspectY != null)
        {
            intent.putExtra("aspectY", cropAspectY);
        }
        if (cropSizeX != null)
        {
            intent.putExtra("outputX", cropSizeX);
        }
        if (cropSizeY != null)
        {
            intent.putExtra("outputY", cropSizeY);
        }
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, REQUEST_PHOTO_ZOOM);
    }

    //TODO Maybe make this static, such that Bitmap from netVerify can be stored in the same file
    //And return the fileName
    private boolean saveBitmapToFile(@NonNull ContextWrapper contextWrapper, @NonNull Bitmap bitmap, boolean mustStartEditing)
    {
        croppedPhotoFile = createImageFile(contextWrapper);
        if (croppedPhotoFile == null)
        {
            THToast.show(R.string.error_save_image_in_external_storage);
            return true;
        }
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(croppedPhotoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            outputStream.flush();
        } catch (Exception e)
        {
            THToast.show(R.string.error_save_image_in_external_storage);
            return true;
        } finally
        {
            if (outputStream != null)
            {
                try
                {
                    outputStream.close();

                } catch (IOException e)
                {
                    Timber.e(e, "Close");
                }
            }
        }
        return false;
    }

    @Nullable private File createImageFile(@NonNull ContextWrapper contextWrapper)
    {
        String imageFileName = "JPEG_" + System.currentTimeMillis();
        File storageDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image;
        try
        {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

        } catch (IOException e)
        {
            Timber.e(e, "createImageFile");
            return null;
        }
        return image;
    }

}

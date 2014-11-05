package com.tradehero.common.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import com.squareup.picasso.Transformation;
import javax.inject.Inject;
import android.support.annotation.NonNull;
import timber.log.Timber;

public class RoundedShapeTransformation implements Transformation
{
    @NonNull private final CenterCropTransformation centerCropTransformation;

    //<editor-fold desc="Constructors">
    @Inject public RoundedShapeTransformation()
    {
        super();
        this.centerCropTransformation = new CenterCropTransformation();
    }
    //</editor-fold>

    @NonNull
    @Override public Bitmap transform(@NonNull Bitmap scaleBitmapImage)
    {
        scaleBitmapImage = centerCropTransformation.transform(scaleBitmapImage);
        int desiredEdge = Math.min(scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight());

        Bitmap targetBitmap;
        try
        {
            targetBitmap = Bitmap.createBitmap(desiredEdge, desiredEdge, Bitmap.Config.ARGB_8888);
        }
        catch (OutOfMemoryError e)
        {
            Timber.e(e, null);
            return scaleBitmapImage;
        }

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) desiredEdge) / 2,
                ((float) desiredEdge) / 2,
                (Math.min(((float) desiredEdge), ((float) desiredEdge)) / 2),
                Path.Direction.CW);
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        //paint.setStyle(Paint.Style.STROKE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        canvas.clipPath(path);

        //TODO need check scaleBitmapImage before use it by alex
        canvas.drawBitmap(scaleBitmapImage, new Rect(0, 0, scaleBitmapImage.getWidth(),
                scaleBitmapImage.getHeight()), new RectF(0, 0, desiredEdge,
                desiredEdge), paint);

        if (targetBitmap != scaleBitmapImage)
        {
            scaleBitmapImage.recycle();
        }
        return targetBitmap;
    }

    @Override public String key()
    {
        return "rounded";
    }
}

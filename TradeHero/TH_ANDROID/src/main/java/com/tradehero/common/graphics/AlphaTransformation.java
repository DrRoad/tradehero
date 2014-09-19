package com.tradehero.common.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class AlphaTransformation implements com.squareup.picasso.Transformation
{
    private float alpha = 1f;

    //<editor-fold desc="Constructors">
    public AlphaTransformation(float alpha)
    {
        super();
        this.alpha = alpha;
        if (alpha > 1)
        {
            this.alpha = 1;
        }
        else if (alpha < 0)
        {
            throw new RuntimeException("Alpha cannot be less than 0!");
        }
    }
    //</editor-fold>

    @Override public Bitmap transform(Bitmap imgIn)
    {
        //    	int w = imgIn.getWidth();
        //        int h = imgIn.getHeight();
        //        int[] pixels = new int[w * h];
        //        imgIn.getPixels(pixels, 0, w, 0, 0, w, h);
        //
        //        for (int i = 0; i < pixels.length; i++) {
        //        	int color = pixels[i];
        //        	int alpha = (int) (Color.alpha(color) * 0.1);
        //        	 pixels[i] = Color.argb(alpha, Color.red(color),  Color.green(color),  Color.blue(color));
        //		}
        //        imgIn.setPixels(pixels, 0, w, 0, 0, w, h);
        //
        //        if(true){
        //        	return imgIn;
        //        }

        Bitmap result = Bitmap.createBitmap(imgIn.getWidth(), imgIn.getHeight(), imgIn.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAlpha((int) (255 * this.alpha));
        canvas.drawBitmap(imgIn, 0, 0, paint);
        imgIn.recycle();
        //Log.i("AlphaTransparentTransformation", "AlphaTransparentTransformation");
        return result;
    }

    @Override public String key()
    {
        return "AlphaTransformation";
    }
}

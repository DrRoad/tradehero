package com.squareup.widgets;

import android.support.annotation.NonNull;
import com.squareup.picasso.Callback;

public class AspectRatioImageViewCallback implements Callback
{
    @NonNull private final AspectRatioImageView loading;

    public AspectRatioImageViewCallback(@NonNull AspectRatioImageView loading)
    {
        this.loading = loading;
    }

    @Override public void onSuccess()
    {
        int imgHeight = loading.getDrawable().getIntrinsicHeight();
        int imgWidth = loading.getDrawable().getIntrinsicWidth();
        loading.setAspectRatioEnabled(true);
        int dominantMeasurement = loading.getDominantMeasurement();
        if (dominantMeasurement == AspectRatioImageView.MEASUREMENT_WIDTH)
        {
            loading.setAspectRatio((float) imgHeight / (float) imgWidth);
        }
        else if (dominantMeasurement == AspectRatioImageView.MEASUREMENT_HEIGHT)
        {
            loading.setAspectRatio((float) imgWidth / (float) imgHeight);
        }
        else
        {
            throw new IllegalArgumentException("Unhandled dominant measurement " + dominantMeasurement);
        }
    }

    @Override public void onError()
    {

    }
}

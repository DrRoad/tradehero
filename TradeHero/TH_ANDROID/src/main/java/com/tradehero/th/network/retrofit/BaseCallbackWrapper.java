package com.tradehero.th.network.retrofit;

import android.support.annotation.Nullable;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BaseCallbackWrapper<ValueType>
    implements CallbackWrapper<ValueType>
{
    @Nullable protected Callback<ValueType> primaryCallback;

    //<editor-fold desc="Constructors">
    public BaseCallbackWrapper(@Nullable Callback<ValueType> primaryCallback)
    {
        this.primaryCallback = primaryCallback;
    }
    //</editor-fold>

    @Override public void setPrimaryCallback(@Nullable Callback<ValueType> primaryCallback)
    {
        this.primaryCallback = primaryCallback;
    }

    protected void notifySuccess(ValueType value, Response response)
    {
        Callback<ValueType> primaryCallbackCopy = primaryCallback;
        if (primaryCallbackCopy != null)
        {
            primaryCallbackCopy.success(value, response);
        }
    }

    protected void notifyFailure(RetrofitError error)
    {
        Callback<ValueType> primaryCallbackCopy = primaryCallback;
        if (primaryCallbackCopy != null)
        {
            primaryCallbackCopy.failure(error);
        }
    }
}

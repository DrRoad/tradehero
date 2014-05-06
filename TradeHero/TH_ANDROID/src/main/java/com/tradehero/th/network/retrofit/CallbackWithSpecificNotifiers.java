package com.tradehero.th.network.retrofit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public abstract class CallbackWithSpecificNotifiers<T> implements Callback<T>
{
    @Override public void success(T returned, Response response)
    {
        notifyIsQuerying(false);
    }

    @Override public void failure(RetrofitError retrofitError)
    {
        notifyIsQuerying(false);
        BasicRetrofitErrorHandler.handle(retrofitError);
    }

    public abstract void notifyIsQuerying(boolean isQuerying);
}

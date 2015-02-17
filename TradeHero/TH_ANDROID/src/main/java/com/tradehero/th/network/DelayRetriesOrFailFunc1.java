package com.tradehero.th.network;

import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.base.THApp;
import com.tradehero.th.misc.exception.THException;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Func1;

public class DelayRetriesOrFailFunc1 implements Func1<Observable<? extends Throwable>, Observable<?>>
{
    private int countDown;
    private long delayMillis;

    //<editor-fold desc="Constructors">
    public DelayRetriesOrFailFunc1(int retries, long delayMillis)
    {
        this.countDown = retries;
        this.delayMillis = delayMillis;
    }
    //</editor-fold>

    @Override public Observable<?> call(Observable<? extends Throwable> attempts)
    {
        return attempts.flatMap(new Func1<Throwable, Observable<? extends Integer>>()
        {
            @Override public Observable<? extends Integer> call(Throwable error)
            {
                if (countDown <= 0)
                {
                    return Observable.error(error);
                }
                THToast.show(String.format(
                        THApp.getResourceString(R.string.service_retry),
                        countDown,
                        new THException(error).getMessage()));
                countDown--;
                return Observable.just(1)
                        .delay(delayMillis, TimeUnit.MILLISECONDS);
            }
        });
    }
}

package com.tradehero.th.auth.tencent_qq;

import com.tencent.tauth.UiError;

public class QQUiErrorException extends RuntimeException
{
    public final UiError error;

    public QQUiErrorException(UiError error)
    {
        super();
        this.error = error;
    }
}

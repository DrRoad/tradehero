package com.tradehero.th.utils;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class NetworkUtils
{
    public static SSLSocketFactory createBadSslSocketFactory()
    {
        try
        {
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager permissive = new X509TrustManager()
            {
                @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException
                {
                }

                @Override public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException
                {
                }

                @Override public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
            };
            context.init(null, new TrustManager[] {permissive}, new SecureRandom());
            return context.getSocketFactory();
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }
}

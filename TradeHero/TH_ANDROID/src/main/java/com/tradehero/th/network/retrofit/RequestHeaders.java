package com.tradehero.th.network.retrofit;

import android.content.Context;
import com.tradehero.th.models.push.DeviceTokenHelper;
import com.tradehero.th.models.user.auth.CredentialsDTO;
import com.tradehero.th.models.user.auth.MainCredentialsPreference;
import com.tradehero.th.persistence.prefs.LanguageCode;
import com.tradehero.th.utils.Constants;
import com.tradehero.th.utils.VersionUtils;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import retrofit.RequestInterceptor;

public class RequestHeaders implements RequestInterceptor
{
    private final MainCredentialsPreference mainCredentialsPreference;
    private final DeviceTokenHelper deviceTokenHelper;
    private final String version;
    private final String languageCode;

    //<editor-fold desc="Constructors">
    @Inject public RequestHeaders(
            Context context,
            MainCredentialsPreference mainCredentialsPreference,
            DeviceTokenHelper deviceTokenHelper,
            @LanguageCode String languageCode)
    {
        this.mainCredentialsPreference = mainCredentialsPreference;
        this.deviceTokenHelper = deviceTokenHelper;
        this.version = VersionUtils.getVersionId(context);
        this.languageCode = languageCode;
    }
    //</editor-fold>

    @Override
    public void intercept(RequestFacade request)
    {
        if (mainCredentialsPreference.getCredentials() != null)
        {
            buildAuthorizationHeader(request);
        }
        request.addHeader(Constants.TH_CLIENT_VERSION, version);
        request.addHeader(Constants.TH_LANGUAGE_CODE, "zh-CN");
        // OkHttp will apparently add "Accept-Encoding: gzip" itself
        request.addHeader(Constants.TH_CLIENT_TYPE, String.valueOf(deviceTokenHelper.getDeviceType().getServerValue()));
    }

    private void buildAuthorizationHeader(RequestFacade request)
    {
        request.addHeader(Constants.AUTHORIZATION, createTypedAuthParameters(mainCredentialsPreference.getCredentials()));
    }

    public String createTypedAuthParameters(@NotNull CredentialsDTO credentialsDTO)
    {
        return String.format("%1$s %2$s", credentialsDTO.getAuthType(), credentialsDTO.getAuthHeaderParameter());
    }
}
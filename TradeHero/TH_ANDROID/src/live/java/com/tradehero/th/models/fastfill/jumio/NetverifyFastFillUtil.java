package com.tradehero.th.models.fastfill.jumio;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.jumio.mobile.sdk.PlatformNotSupportedException;
import com.jumio.mobile.sdk.ResourceNotFoundException;
import com.jumio.mobile.sdk.enums.JumioDataCenter;
import com.jumio.netverify.sdk.NetverifyDocumentData;
import com.jumio.netverify.sdk.NetverifySDK;
import com.jumio.netverify.sdk.enums.NVDocumentType;
import com.tradehero.th.R;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.models.fastfill.FastFillUtil;
import com.tradehero.th.models.fastfill.ScannedDocument;
import com.tradehero.th.models.fastfill.ScannedDocumentType;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * https://www.jumio.com/downloads/pdf/fastfill_netverify_mobile_implementation_guide_for_android_v1_5_0_jumio_sdk.pdf
 */
public class NetverifyFastFillUtil implements FastFillUtil
{
    public static final JumioDataCenter DATA_CENTER = JumioDataCenter.US;
    public static final int NET_VERIFY_REQUEST_CODE = R.string.net_verify_request_code & 0xFF;

    @NonNull private final CurrentUserId currentUserId;
    @NonNull private final NetverifyServiceWrapper netverifyServiceWrapper;
    @NonNull private final BehaviorSubject<ScannedDocument> scannedDocumentSubject;
    private NetverifySDK netverifySDK;
    private Map<ScannedDocumentType, NVDocumentType> documentTypeMap;

    @Inject public NetverifyFastFillUtil(@NonNull CurrentUserId currentUserId,
            @NonNull NetverifyServiceWrapper netverifyServiceWrapper)
    {
        this.currentUserId = currentUserId;
        this.netverifyServiceWrapper = netverifyServiceWrapper;
        this.scannedDocumentSubject = BehaviorSubject.create();
        this.documentTypeMap = new HashMap<>();
        documentTypeMap.put(ScannedDocumentType.DRIVER_LICENSE, NVDocumentType.DRIVER_LICENSE);
        documentTypeMap.put(ScannedDocumentType.IDENTITY_CARD, NVDocumentType.IDENTITY_CARD);
        documentTypeMap.put(ScannedDocumentType.PASSPORT, NVDocumentType.PASSPORT);
    }

    @NonNull public NetverifySDK getNetverifySDK(@NonNull Activity activity)
    {
        NetverifySDK netverifySDK;
        if (isSupported(activity))
        {
            try
            {
                netverifySDK = NetverifySDK.create(activity, NetverifyConstants.NET_VERIFY_MERCHANT_API_TOKEN,
                        NetverifyConstants.NET_VERIFY_ACTIVE_API_SECRET, DATA_CENTER);
                netverifySDK.setCustomerId(currentUserId.get().toString());
            } catch (ResourceNotFoundException | PlatformNotSupportedException e)
            {
                Timber.e(e, "Failed to initialise NetverifySDK");
                throw new IllegalArgumentException("Failed to initialise NetverifySDK");
            }
        }
        else
        {
            throw new IllegalArgumentException("Netverify is not supported");
        }
        return netverifySDK;
    }

    public static boolean isSupported(@NonNull Activity activity)
    {
        boolean supported = NetverifySDK.isSupportedPlatform(activity);
        if (!supported)
        {
            Timber.e(new Exception(), "Netverify %s is not supported on this device", NetverifySDK.getSDKVersion());
        }
        return supported;
    }

    @NonNull @Override public Observable<Boolean> isAvailable(@NonNull Activity activity)
    {
        return Observable.just(isSupported(activity));
    }

    @Override public void fastFill(@NonNull Activity activity)
    {
        fastFill(activity, null);
    }

    @Override public void fastFill(@NonNull Activity activity, @Nullable ScannedDocumentType documentType)
    {
        fastFill(activity, getNetverifySDK(activity), documentType);
    }

    public void fastFill(@NonNull Activity activity, @NonNull NetverifySDK netverifySDK, @Nullable ScannedDocumentType documentType)
    {
        if (documentType != null)
        {
            netverifySDK.setPreselectedDocumentType(documentTypeMap.get(documentType));
        }
        this.netverifySDK = netverifySDK;
        activity.startActivityForResult(netverifySDK.getIntent(), NET_VERIFY_REQUEST_CODE);
    }

    @Override public void fastFill(@NonNull Fragment fragment)
    {
        fastFill(fragment, getNetverifySDK(fragment.getActivity()));
    }

    public void fastFill(@NonNull Fragment fragment, @NonNull NetverifySDK netverifySDK)
    {
        this.netverifySDK = netverifySDK;
        fragment.startActivityForResult(netverifySDK.getIntent(), NET_VERIFY_REQUEST_CODE);
    }

    @Override public void onActivityResult(@NonNull Activity activity, int requestCode, final int resultCode, Intent data)
    {
        if (requestCode == NET_VERIFY_REQUEST_CODE)
        {
            if (resultCode == NetverifySDK.RESULT_CODE_SUCCESS || resultCode ==
                    NetverifySDK.RESULT_CODE_BACK_WITH_SUCCESS)
            {
                NetverifyScanReference scanReference = new NetverifyScanReference(data.getStringExtra(NetverifySDK.RESULT_DATA_SCAN_REFERENCE));
                scannedDocumentSubject.onNext(new NetverifyScannedDocument(
                        scanReference,
                        data.<NetverifyDocumentData>getParcelableExtra(NetverifySDK.RESULT_DATA_SCAN_DATA)));
            }
            else if (resultCode == NetverifySDK.RESULT_CODE_CANCEL)
            {
                scannedDocumentSubject.onError(new FastFillNetverifyError(
                        data.getStringExtra(NetverifySDK.RESULT_DATA_ERROR_MESSAGE),
                        data.getStringExtra(NetverifySDK.RESULT_DATA_SCAN_REFERENCE),
                        data.getIntExtra(NetverifySDK.RESULT_DATA_ERROR_CODE, 0)));
            }
            this.netverifySDK.destroy();
        }
    }

    @NonNull @Override public Observable<ScannedDocument> getScannedDocumentObservable()
    {
        return scannedDocumentSubject.asObservable();
    }
}

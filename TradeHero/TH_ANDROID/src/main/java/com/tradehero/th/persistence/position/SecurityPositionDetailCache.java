package com.tradehero.th.persistence.position;

import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import com.tradehero.common.utils.THLog;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.portfolio.PortfolioDTO;
import com.tradehero.th.api.position.PositionDTOCompact;
import com.tradehero.th.api.position.SecurityPositionDetailDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.network.BasicRetrofitErrorHandler;
import com.tradehero.th.network.service.SecurityService;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.th.persistence.security.SecurityCompactCache;
import dagger.Lazy;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit.RetrofitError;

/** Created with IntelliJ IDEA. User: xavier Date: 10/3/13 Time: 4:47 PM To change this template use File | Settings | File Templates. */
@Singleton public class SecurityPositionDetailCache implements DTOCache<String, SecurityId, SecurityPositionDetailDTO>
{
    public static final String TAG = SecurityPositionDetailCache.class.getSimpleName();
    public static final int DEFAULT_MAX_SIZE = 1000;

    // We need to compose here, instead of inheritance, otherwise we get a compile error regarding erasure on put and put.
    private LruCache<String, SecurityPositionDetailCache.SecurityPositionDetailCutDTO> lruCache;
    @Inject protected Lazy<SecurityService> securityService;
    @Inject protected Lazy<SecurityCompactCache> securityCompactCache;

    //<editor-fold desc="Constructors">
    @Inject public SecurityPositionDetailCache()
    {
        this(DEFAULT_MAX_SIZE);
    }

    public SecurityPositionDetailCache(int maxSize)
    {
        lruCache = new LruCache<>(maxSize);
    }
    //</editor-fold>

    protected SecurityPositionDetailDTO fetch(SecurityId key)
    {
        SecurityPositionDetailDTO securityPositionDetailDTO = null;
        try
        {
            securityPositionDetailDTO = securityService.get().getSecurity(key.exchange, key.securitySymbol);
        }
        catch (RetrofitError retrofitError)
        {
            BasicRetrofitErrorHandler.handle(retrofitError);
            THLog.e(TAG, "Error requesting key " + key.toString(), retrofitError);
        }
        return securityPositionDetailDTO;
    }

    @Override public SecurityPositionDetailDTO getOrFetch(SecurityId key, boolean force)
    {
        SecurityPositionDetailCutDTO securityPositionDetailCutDTO = lruCache.get(key.makeKey());
        SecurityCompactDTO securityCompactDTO = securityCompactCache.get().get(key);
        SecurityPositionDetailDTO securityPositionDetailDTO = null;

        if (force || securityPositionDetailCutDTO == null || securityCompactDTO == null)
        {
            securityPositionDetailDTO = fetch(key);
            put(key, securityPositionDetailDTO);
        }
        else
        {
            securityPositionDetailDTO = securityPositionDetailCutDTO.create(securityCompactDTO);
        }
        return securityPositionDetailDTO;
    }

    public AsyncTask<Void, Void, SecurityPositionDetailDTO> getOrFetch(final SecurityId key, final boolean force, final Listener<SecurityId, SecurityPositionDetailDTO> callback)
    {
        final WeakReference<Listener<SecurityId, SecurityPositionDetailDTO>> weakCallback = new WeakReference<Listener<SecurityId, SecurityPositionDetailDTO>>(callback);

        return new AsyncTask<Void, Void, SecurityPositionDetailDTO>()
        {
            @Override protected SecurityPositionDetailDTO doInBackground(Void... voids)
            {
                return getOrFetch(key, force);
            }

            @Override protected void onPostExecute(SecurityPositionDetailDTO value)
            {
                super.onPostExecute(value);
                Listener<SecurityId, SecurityPositionDetailDTO> retrievedCallback = weakCallback.get();
                // We retrieve the callback right away to avoid having it vanish between the 2 get() calls.
                if (!isCancelled() && retrievedCallback != null)
                {
                    retrievedCallback.onDTOReceived(key, value);
                }
            }
        };
    }
    
    @Override public SecurityPositionDetailDTO get(SecurityId key)
    {
        SecurityPositionDetailCutDTO securityPositionDetailCutDTO = this.lruCache.get(key.makeKey());
        SecurityCompactDTO securityCompactDTO = securityCompactCache.get().get(key);
        if (securityPositionDetailCutDTO == null || securityCompactDTO == null)
        {
            return null;
        }
        return securityPositionDetailCutDTO.create(securityCompactDTO);
    }

    @Override public SecurityPositionDetailDTO put(SecurityId key, SecurityPositionDetailDTO value)
    {
        SecurityPositionDetailDTO previous = null;

        securityCompactCache.get().put(key, value.security);
        SecurityPositionDetailCutDTO previousCut = lruCache.put(key.makeKey(), new SecurityPositionDetailCutDTO(value));

        if (previousCut != null)
        {
            previous = previousCut.create(value.security);
        }

        return previous;
    }

    // The purpose of this class is to save on memory usage by cutting out the SecurityCompactDTO that already enjoys its own cache.
    // It is static so as not to keep a link back to the cache instance.
    private static class SecurityPositionDetailCutDTO
    {
        public List<PositionDTOCompact> positions;
        public PositionDTOCompact position;
        public PortfolioDTO portfolio;
        public List<ProviderDTO> providers;
        public int firstTradeAllTime;

        public SecurityPositionDetailCutDTO(SecurityPositionDetailDTO securityPositionDetailDTO)
        {
            this.positions = securityPositionDetailDTO.positions;
            this.portfolio = securityPositionDetailDTO.portfolio;
            this.providers = securityPositionDetailDTO.providers;
            this.firstTradeAllTime = securityPositionDetailDTO.firstTradeAllTime;
        }

        public SecurityPositionDetailDTO create(SecurityCompactDTO securityCompactDTO)
        {
            return new SecurityPositionDetailDTO(
                    securityCompactDTO,
                    positions,
                    portfolio,
                    providers,
                    firstTradeAllTime
            );
        }
    }
}

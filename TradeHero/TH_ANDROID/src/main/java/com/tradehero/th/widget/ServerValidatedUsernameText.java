package com.tradehero.th.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.th.R;
import com.tradehero.th.api.users.DisplayNameDTO;
import com.tradehero.th.api.users.UserAvailabilityDTO;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.persistence.user.UserAvailabilityCache;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit.RetrofitError;

public class ServerValidatedUsernameText extends ServerValidatedText
{
    @Inject UserAvailabilityCache userAvailabilityCache;
    @Nullable private DTOCacheNew.Listener<DisplayNameDTO, UserAvailabilityDTO> userAvailabilityListener;
    private boolean isValidInServer = true;
    private String originalUsernameValue;

    //<editor-fold desc="Constructors">
    public ServerValidatedUsernameText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        HierarchyInjector.inject(this);
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        userAvailabilityListener = createValidatedUserNameListener();
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (userAvailabilityListener == null)
        {
            userAvailabilityListener = createValidatedUserNameListener();
        }
    }

    @Override protected void onDetachedFromWindow()
    {
        detachUserAvailabilityCache();
        userAvailabilityListener = null;
        super.onDetachedFromWindow();
    }

    private void detachUserAvailabilityCache()
    {
        userAvailabilityCache.unregister(userAvailabilityListener);
    }

    public void setOriginalUsernameValue(String originalUsernameValue)
    {
        this.originalUsernameValue = originalUsernameValue;
    }

    @Override protected boolean validate()
    {
        boolean superValidate = super.validate();

        if (!superValidate)
        {
            // We need to reset the value as otherwise it will prompt that the username is taken even when the field is empty
            isValidInServer = true;
            return false;
        }

        String displayName = getText().toString();
        boolean sameDisplayName =
                (this.originalUsernameValue != null && this.originalUsernameValue.equalsIgnoreCase(
                        displayName));
        if (sameDisplayName)
        {
            isValidInServer = true;
            return true;
        }

        if (displayName != null)
        {
            UserAvailabilityDTO cachedAvailability =
                    userAvailabilityCache.get(new DisplayNameDTO(displayName));
            if (cachedAvailability != null)
            {
                isValidInServer = cachedAvailability.available;
            }
            else
            {
                queryCache(displayName);
            }
        }
        return isValidInServer;
    }

    @Override public ValidationMessage getCurrentValidationMessage()
    {
        if (!isValidInServer)
        {
            return new ValidationMessage(this, false,
                    getContext().getString(R.string.validation_server_username_not_available));
        }
        return super.getCurrentValidationMessage();
    }

    protected void queryCache(@Nullable String displayName)
    {
        if (displayName != null)
        {
            handleServerRequest(true);
            DisplayNameDTO key = new DisplayNameDTO(displayName);
            detachUserAvailabilityCache();
            userAvailabilityCache.register(key, userAvailabilityListener);
            userAvailabilityCache.getOrFetchAsync(key, true);
        }
    }

    private void handleReturnFromServer(boolean newIsValidFromServer)
    {
        boolean hasChanged = isValidInServer != newIsValidFromServer;
        isValidInServer = newIsValidFromServer;

        if (hasChanged)
        {
            setValid(validate());
        }
    }

    public void handleNetworkError(RetrofitError retrofitError)
    {
        hintDefaultStatus();
    }

    @NotNull protected DTOCacheNew.Listener<DisplayNameDTO, UserAvailabilityDTO> createValidatedUserNameListener()
    {
        return new ValidatedUserNameAvailabilityListener();
    }

    protected class ValidatedUserNameAvailabilityListener implements DTOCacheNew.HurriedListener<DisplayNameDTO, UserAvailabilityDTO>
    {
        @Override public void onPreCachedDTOReceived(
                @NotNull DisplayNameDTO key,
                @NotNull UserAvailabilityDTO value)
        {
            if (key.isSameName(getText().toString()))
            {
                handleReturnFromServer(value.available);
            }
        }

        @Override public void onDTOReceived(
                @NotNull DisplayNameDTO key,
                @NotNull UserAvailabilityDTO value)
        {
            if (key.isSameName(getText().toString()))
            {
                handleServerRequest(false);
                handleReturnFromServer(value.available);
            }
        }

        @Override public void onErrorThrown(
                @NotNull DisplayNameDTO key,
                @NotNull Throwable error)
        {
            if (key.isSameName(getText().toString()))
            {
                handleServerRequest(false);
                if (error instanceof RetrofitError && ((RetrofitError) error).isNetworkError())
                {
                    handleNetworkError((RetrofitError) error);
                }
            }
        }
    }
}

package com.tradehero.th.models.graphics;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this is to be used for a user photo
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface ForUserPhotoBackground
{
}

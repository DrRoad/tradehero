package com.tradehero.th;

import com.tradehero.th.api.GooglePlayApiTestModule;
import dagger.Module;

@Module(
        includes = {
                GooglePlayApiTestModule.class,
        },
        complete = false,
        library = true
)
public class GooglePlayAppTestModule
{
}

package com.tradehero.th;

import com.tradehero.th.api.AmazonApiTestModule;
import dagger.Module;

@Module(
        includes = {
                AmazonApiTestModule.class,
        },
        complete = false,
        library = true
)
public class AmazonAppTestModule
{
}

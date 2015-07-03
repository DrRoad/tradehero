package com.tradehero.th.models.fastfill;

import com.tradehero.th.models.fastfill.jumio.DebugJumioFastFillUtil;
import com.tradehero.th.models.fastfill.jumio.JumioFastFillUtil;
import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class FastFillBuildTypeModule
{
    @Provides JumioFastFillUtil providesFastFillUtil(DebugJumioFastFillUtil fastFillUtil)
    {
        return fastFillUtil;
    }
}
